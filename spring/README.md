# Camunda Cloud - Get Started - Spring Boot

This guide explains how to setup a Spring Boot project to automate a process using
[Camunda Cloud](https://camunda.com/products/cloud/).

# Install dependencies

The open source library [spring-zeebe](https://github.com/camunda-community-hub/spring-zeebe)
provides a Zeebe client.

```
<dependency>
  <groupId>io.camunda</groupId>
  <artifactId>spring-zeebe-starter</artifactId>
  <version>1.1.0</version>
</dependency>
```

# Create Client

If we want to connect to a Camunda Cloud SaaS cluster we need the `clusterId`
from the [Clusters details
page](https://docs.camunda.io/docs/product-manuals/cloud-console/manage-clusters/create-cluster),
a `clientId` and `clientSecret` from a [client credentials
pair](https://docs.camunda.io/docs/product-manuals/cloud-console/manage-clusters/manage-api-clients). 

The credentails can be added to the application.properties.

```properties
zeebe.client.cloud.clusterId=365eed98-16c1-4096-bb57-eb8828ed131e
zeebe.client.cloud.clientId=GZVO3ALYy~qCcD3MYq~sf0GIszNzLE_z
zeebe.client.cloud.clientSecret=.RPbZc6q0d6uzRbB4LW.B8lCpsxbBEpmBX0AHQGzINf3.KK9RkzZW1aDaZ-7WYNJ
```

If you are using a self managed Camunda Cloud cluster, you create the client
using the following application config, see
[application.localhost.yaml](src/main/resources/application.localhost.properties).

```properties
zeebe.client.broker.gatewayAddress=127.0.0.1:26500
zeebe.client.security.plaintext=true
```

To enable the Zeebe client integration annotate your application class with
`@EnableZeebeClient`, see
[ProcessApplication.java](src/main/java/io/camunda/getstarted/ProcessApplication.java).

```java
@SpringBootApplication
@EnableZeebeClient
public class ProcessApplication {

  public static void main(String[] args) {
    SpringApplication.run(ProcessApplication.class, args);
  }

}
```

# Deploy Process and Start Instance

To deploy a process you can use the annotation `@ZeebeDeployment`, which allows
to specify a list of classpath resources `classPathResources` to be deployed on
start up.

```java
@SpringBootApplication
@EnableZeebeClient
@ZeebeDeployment(resources = "classpath:send-email.bpmn")
public class ProcessApplication {

  public static void main(String[] args) {
    SpringApplication.run(ProcessApplication.class, args);
  }

}
```

To start a new instance you can specify the `bpmnProcessId`, i.e.
`send-email` and **optionally** process variables.

```java
final ProcessInstanceEvent event =
  client
    .newCreateInstanceCommand()
    .bpmnProcessId("send-email")
    .latestVersion()
    .variables(Map.of("message_content", "Hello from the Spring Boot get started"))
    .send()
    .join();

LOG.info("Started instance for processDefinitionKey='{}', bpmnProcessId='{}', version='{}' with processInstanceKey='{}'",
	event.getProcessDefinitionKey(), event.getBpmnProcessId(), event.getVersion(), event.getProcessInstanceKey());
```

For the complete code see the
[`ProcessApplication.java`](src/main/java/io/camunda/getstarted/ProcessApplication.java) file. You can
run it using the following command.

```bash
mvn spring-boot:run
```

# Job Worker

To complete a [service
task](https://docs.camunda.io/docs/reference/bpmn-workflows/service-tasks/service-tasks/),
a [job
worker](https://docs.camunda.io/docs/product-manuals/concepts/job-workers) has
to be subscribed the to task type defined in the process, i.e. `email`. For this
the `@ZeebeWorker` annotation can be used and the `type` has to be specified.

```
@ZeebeWorker(type = "email")
public void sendEmail(final JobClient client, final ActivatedJob job) {
  final String message_content = (String) job.getVariablesAsMap().get("message_content");

  LOG.info("Sending email with message content: {}", message_content);

  client.newCompleteCommand(job.getKey()).send()
    .whenComplete((result, exception) -> {
      if (exception == null) {
        LOG.info("Completed job successful with result:" + result);
      } else {
        LOG.error("Failed to complete job", exception);
      }
    });
}
```

For the complete code see the
[EmailWorker.java](src/main/java/io/camunda/getstarted/EmailWorker.java) file. You can
run it using the following command.

```bash
mvn spring-boot:run
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

This is discussed in more detail in [this blog post about writing good workers for Camunda Cloud](https://blog.bernd-ruecker.com/writing-good-workers-for-camunda-cloud-61d322cad862).
