// Copyright 2022 Camunda Services GmbH
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package main

import (
	"context"
	"embed"
	"errors"
	"fmt"
	"github.com/camunda/zeebe/clients/go/v8/pkg/entities"
	"github.com/camunda/zeebe/clients/go/v8/pkg/pb"
	"github.com/camunda/zeebe/clients/go/v8/pkg/worker"
	"github.com/camunda/zeebe/clients/go/v8/pkg/zbc"
	"log"
	"os"
	"os/signal"
	"syscall"
	"time"
)

//go:embed resources
var res embed.FS

const (
	kMessageVariable = "message_content"
	kJobType         = "email"
	kWorkerName      = "go-get-started"
)

func main() {
	shutdownBarrier := make(chan bool, 1)
	SetupShutdownBarrier(shutdownBarrier)

	client := MustCreateClient()
	defer MustCloseClient(client)
	MustDeployProcessDefinition(client)
	w := MustStartWorker(client)
	defer w.Close()
	MustStartProcessInstance(client, "Hello from the Go get started")

	log.Printf("you can now start sending emails. log into tasklist to start doing so")
	log.Printf("use ctrl+c to exit or interrupt the application")

	<-shutdownBarrier
}

func SetupShutdownBarrier(done chan bool) {
	sigs := make(chan os.Signal, 1)
	signal.Notify(sigs, syscall.SIGINT, syscall.SIGTERM)
	go func() {
		<-sigs
		done <- true
	}()
}

func MustReadFile(resourceFile string) []byte {
	contents, err := res.ReadFile("resources/" + resourceFile)
	if err != nil {
		panic(err)
	}

	return contents
}

func MustCreateClient() zbc.Client {
	credentials, err := zbc.NewOAuthCredentialsProvider(&zbc.OAuthProviderConfig{
		ClientID:               "YOUR_CLIENT_ID",
		ClientSecret:           "YOUR_CLIENT_SECRET",
		AuthorizationServerURL: "https://login.cloud.camunda.io/oauth/token",
		Audience:               "zeebe.camunda.io",
	})
	if err != nil {
		panic(err)
	}

	config := zbc.ClientConfig{
		GatewayAddress:      "YOUR_CLUSTER_ID.bru-2.zeebe.camunda.io:26500",
		CredentialsProvider: credentials,
	}

	client, err := zbc.NewClient(&config)
	if err != nil {
		panic(err)
	}

	return client
}

func MustCloseClient(client zbc.Client) {
	log.Println("closing client")
	_ = client.Close()
}

func MustDeployProcessDefinition(client zbc.Client) *pb.ProcessMetadata {
	definition := MustReadFile("send-email.bpmn")
	command := client.NewDeployResourceCommand().AddResource(definition, "send-email.bpmn")

	ctx, cancelFn := context.WithTimeout(context.Background(), 10*time.Second)
	defer cancelFn()

	resource, err := command.Send(ctx)
	if err != nil {
		panic(err)
	}

	if len(resource.GetDeployments()) < 0 {
		panic(errors.New("failed to deploy send-email model; nothing was deployed"))
	}

	deployment := resource.GetDeployments()[0]
	process := deployment.GetProcess()
	if process == nil {
		panic(errors.New("failed to deploy send-email process; the deployment was successful, but no process was returned"))
	}

	log.Printf("deployed BPMN process [%s] with key [%d]", process.GetBpmnProcessId(), process.GetProcessDefinitionKey())
	return process
}

func MustStartProcessInstance(client zbc.Client, message string) *pb.CreateProcessInstanceResponse {
	command, err := client.NewCreateInstanceCommand().
		BPMNProcessId("send-email").
		LatestVersion().
		VariablesFromMap(map[string]interface{}{kMessageVariable: message})
	if err != nil {
		panic(fmt.Errorf("failed to create process instance command for message [%s]", message))
	}

	ctx, cancelFn := context.WithTimeout(context.Background(), 10*time.Second)
	defer cancelFn()

	process, err := command.Send(ctx)
	if err != nil {
		panic(err)
	}

	log.Printf("started process instance [%d] with {\"%s\": \"%s\"}", process.GetProcessInstanceKey(), kMessageVariable, message)
	return process
}

func MustStartWorker(client zbc.Client) worker.JobWorker {
	w := client.NewJobWorker().
		JobType(kJobType).
		Handler(HandleJob).
		Concurrency(1).
		MaxJobsActive(10).
		RequestTimeout(1 * time.Second).
		PollInterval(1 * time.Second).
		Name(kWorkerName).
		Open()

	log.Printf("started worker [%s] for jobs of type [%s]", kWorkerName, kJobType)
	return w
}

func HandleJob(client worker.JobClient, job entities.Job) {
	vars, err := job.GetVariablesAsMap()
	if err != nil {
		log.Printf("failed to get variables for job %d: [%s]", job.Key, err)
		return
	}

	log.Printf("Sending email with message content: %s", vars[kMessageVariable])

	ctx, cancelFn := context.WithTimeout(context.Background(), 5*time.Second)
	defer cancelFn()

	_, err = client.NewCompleteJobCommand().JobKey(job.Key).Send(ctx)
	if err != nil {
		log.Printf("failed to complete job with key %d: [%s]", job.Key, err)
	}

	log.Printf("completed job %d successfully", job.Key)
}
