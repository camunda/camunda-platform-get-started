# Camunda Platform 8 - Get Started - Go

This guide explains how to set up a Go project to automate a process using 
[Camunda Platform 8](https://camunda.com/products/cloud/).

# Install dependencies

The client requires you to install Go with a version greater than or equal to 1.17. See [here](https://go.dev/doc/install) on how to do this for your platform.

The open source library
[github.com/camunda/zeebe/clients/go/v8](https://docs.camunda.io/docs/apis-clients/go-client/) provides a Zeebe client 
for Go.

To install using Go modules, simply run:

```sh
go get github.com/camunda/zeebe/clients/go/v8@v8.0.5
```

# Create Client

If we want to connect to a Camunda Platform 8 SaaS cluster we need the `clusterId`
from the [Clusters details
page](https://docs.camunda.io/docs/components/modeler/bpmn/service-tasks/),
a `clientId` and `clientSecret` from a [client credentials
pair](https://docs.camunda.io/docs/components/modeler/bpmn/service-tasks/).

The credentials can be specified by implementing your own 
[zbc.CredentialsProvider](https://pkg.go.dev/github.com/camunda/zeebe/clients/go/pkg/zbc#CredentialsProvider) or
using the existing 
[OAuthCredentialsProvider](https://pkg.go.dev/github.com/camunda/zeebe/clients/go/pkg/zbc#OAuthCredentialsProvider)
which is compatible with Camunda Platform 8 SaaS clusters out of the box.

```golang
credentials := zbc.NewOAuthCredentialsProvider(&zbc.OAuthProviderConfig{
    ClientID: "[Client ID]"
    ClientSecret: "[Client Secret]"
    AuthorizationServerURL: "[Authorization URL]"
})
config := zbc.ClientConfig{
	GatewayAddress: "[Cluster Address]"
	CredentialsProvider: credentials
}
client, err := zbc.NewClient(&config)
if err != nil {
    panic(err)
}
```

You can also use environment variables to setup the client:

```shell
export ZEEBE_ADDRESS='[Zeebe API]'
export ZEEBE_CLIENT_ID='[Client ID]'
export ZEEBE_CLIENT_SECRET='[Client Secret]'
export ZEEBE_AUTHORIZATION_SERVER_URL='[OAuth API]'
```

If you are using a self managed Camunda Platform 8 cluster, you create the default
client and have to disable security.

```golang
config := zbc.ClientConfig{UsePlaintextConnection: true, GatewayAddress: "localhost:26500"}
client, err := zbc.NewClient(&config)
if err != nil {
	panic(err)
}
```

# Deploy Process and Start Instance

To deploy a process you can use the `NewDeployResourceCommand` method, which allows
to specify a list of resources to be deployed.

```golang
var model []byte
// read the file with the model
deployment, err := client.NewDeployResourceCommand().AddResource(model, "send-email.bpmn").Send(context.Background())
if err != nil {
	panic(err)
}
```

To start a new instance you can specify the `bpmnProcessId`, i.e.
`send-email` and **optionally** process variables.

```golang
process, err := client.NewCreateInstanceCommand().
    BPMNProcessId("send-email").
    LatestVersion().
    VariablesFromMap(map[string]interface{}{"message_content": "Hello World from Go get started guide"})
    .Send(context.Background())
if err != nil {
    panic(err)
}

log.Printf("started instance for processDefinitionKey=[%d], bpmnProcessId=[%s] with processInstanceKey=[%d]",
    process.GetProcessDefinitionKey(), process.GetBpmnProcessId(), process.GetProcessInstanceKey())
```

For the complete code see the [`main.go`](main.go) file.

# Job Worker

To complete a [service
task](https://docs.camunda.io/docs/reference/bpmn-workflows/service-tasks/service-tasks/),
a [job
worker](https://docs.camunda.io/docs/product-manuals/concepts/job-workers) has
to be subscribed the to task type defined in the process, i.e. `email`. For this
the `newWorker` method can be used.

```golang
w := client.NewJobWorker().
    JobType("email").
    Handler(func(c worker.JobClient, job entities.Job) {
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
    }).
    MaxJobsActive(10).
    RequestTimeout(1 * time.Second).
    PollInterval(1 * time.Second).
    Name(kWorkerName).
    Open()
defer w.Close()

log.Printf("started worker [%s] for jobs of type [%s]", kWorkerName, kJobType)
```

For the complete code see the [main.go](main.go) file.

To make an job available, a user task has to be completed, follow the instructions in 
[the guide](../README.md#complete-the-user-task).

# Sample application

First, setup the client credentials to connect to an existing Zeebe cluster, either local or in Camunda Cloud. The easiest way to do this is via environment variables. See [here](https://docs.camunda.io/docs/apis-clients/go-client/get-started/) for more on how to do this.

```bash
go build -o example github.com/camunda/camunda-platform-get-started/go
./example
```

Once done, navigate to your Tasklist instance.

From there, claim and complete the user task. You should see corresponding log statements from the sample application
indicating that it has completed the job, e.g.

```
2022/04/07 22:11:40 Sending email with message content: Hello from the Go get started
2022/04/07 22:11:40 completed job 2251799813685394 successfully
```