# Camunda Platform 8 - Get Started - Java

This guide explains how to setup a Java project to automate a process using
[Camunda Platform 8](https://camunda.com/products/cloud/).

# Install dependencies

The open source library
[zeebe-client-java](https://docs.camunda.io/docs/next/apis-tools/java-client/)
provides a Zeebe client.

```
<dependency>
	<groupId>io.camunda</groupId>
	<artifactId>zeebe-client-java</artifactId>
	<version>8.7.6</version>
</dependency>
```

# Create Client

If we want to connect to a Camunda Platform 8 SaaS cluster we need the `clusterId`
from the [Clusters details
page](https://docs.camunda.io/docs/components/console/manage-clusters/create-cluster/),
a `clientId` and `clientSecret` from a [client credentials
pair](https://docs.camunda.io/docs/components/console/manage-clusters/manage-api-clients/).

The credentails can be specified in the client builder.

```java
ZeebeClient client = ZeebeClient.newCloudClientBuilder()
        .withClusterId("365eed98-16c1-4096-bb57-eb8828ed131e")
        .withClientId("GZVO3ALYy~qCcD3MYq~sf0GIszNzLE_z")
        .withClientSecret(".RPbZc6q0d6uzRbB4LW.B8lCpsxbBEpmBX0AHQGzINf3.KK9RkzZW1aDaZ-7WYNJ")
        .withRegion("bru-2")
        .build()
```

If you are using a self managed Camunda Platform 8 cluster, you create the default
client and have to disable security.

```java
ZeebeClient client = ZeebeClient.newClientBuilder().usePlaintext().build();
```

# Deploy Process and Start Instance

To deploy a process you can use the `newDeployResourceCommand` method, which allows
to specify a list of classpath resources to be deployed.

```java
client.newDeployResourceCommand()
          .addResourceFromClasspath("send-email.bpmn")
          .send()
          .join();
```

To start a new instance you can specify the `bpmnProcessId`, i.e.
`send-email` and **optionally** process variables.

```java
final ProcessInstanceEvent event = client.newCreateInstanceCommand()
          .bpmnProcessId("send-email")
          .latestVersion()
          .variables(Map.of("message_content", "Hello from the Java get started"))
          .send()
          .join();

LOG.info("Started instance for processDefinitionKey='{}', bpmnProcessId='{}', version='{}' with processInstanceKey='{}'",
	event.getProcessDefinitionKey(), event.getBpmnProcessId(), event.getVersion(), event.getProcessInstanceKey());
```

For the complete code see the
[`DeployAndStartInstance.java`](src/main/java/io/camunda/getstarted/DeployAndStartInstance.java) file. You can
run it using the following command.

```bash
mvn exec:java
```

# Job Worker

To complete a [service
task](https://docs.camunda.io/docs/reference/bpmn-workflows/service-tasks/service-tasks/),
a [job
worker](https://docs.camunda.io/docs/product-manuals/concepts/job-workers) has
to be subscribed to the task type defined in the process, i.e. `email`. For this
the `newWorker` method can be used.

```java
client.newWorker().jobType("email").handler((jobClient, job) -> {
	final String message_content = (String) job.getVariablesAsMap().get("message_content");

	LOG.info("Sending email with message content: {}", message_content);

	jobClient.newCompleteCommand(job.getKey()).send()
          .whenComplete((result, exception) -> {
                if (exception == null) {
                  LOG.info("Completed job successful with result:" + result);
                } else {
                  LOG.error("Failed to complete job", exception);
                }
          });
}).open();
```

For the complete code see the
[EmailWorker.java](src/main/java/io/camunda/getstarted/EmailWorker.java) file. You can
run it using the following command.

```bash
mvn exec:java -P worker
```

To make an job available, a user task has to be completed, follow the
instructions in [the guide](../README.md#complete-the-user-task).

# Blocking vs. Non-Blocking Code

Some of the code examples above (deploy, start process instance) used
```
send().join()
```
which is a blocking call to wait for the issues command to be executed on the workflow engine. While this is very straightforward to use and produces easy-to-read code, blocking code is limited in terms of scalability.

That's why the worker showed a different pattern:
```
send().whenComplete((result, exception) -> {})
```
This registers a callback to be executed if the command on the workflow engine was executed or resulted in an exception. This allows for parallelism, which is especially interesting in workers.

This is discussed in more detail in [this blog post about writing good workers for Camunda Platform 8](https://blog.bernd-ruecker.com/writing-good-workers-for-camunda-cloud-61d322cad862).
