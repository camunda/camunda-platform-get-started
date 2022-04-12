# Camunda Platform 8 - Get Started - C# Client

This guide explains how to setup a csharp project to automate a process using
[Camunda Platform 8](https://camunda.com/products/cloud/).

For the complete code see the [`Program.cs`](Program.cs) file. You can run it using the following command.

```bash
dotnet run
```

# Install dependencies

The open source library
[zeebe-client-csharp](https://docs.camunda.io/docs/apis-clients/community-clients/c-sharp/)
provides a Zeebe client. The nuget package is called `zb-client` and can be found [here](https://www.nuget.org/packages/zb-client).

# Create Client

If we want to connect to a Camunda Platform 8 SaaS cluster we need the contact point, also know as `ZEEBE_ADDRESS`.
Furthermore we need the `clientId` and `clientSecret` all these information we get from the [client credentials](https://docs.camunda.io/docs/components/console/manage-clusters/manage-api-clients/).

The credentails can be specified in the client builder.

```csharp
IZeebeClient zeebeClient = CamundaCloudClientBuilder.Builder()
        .UseClientId("kYbf5pwF3WP_ZuOU_13fde~uG7M_IaGZ")
        .UseClientSecret("1nl8HNRd8INmL4kUcMcZOPCbjk2ZNUdacsGeOOotkknHUeqV9L~1eJFztsSZ8kpq")
        .UseContactPoint("6ce770b8-cd6d-4977-854c-bff92d0d3d98.zeebe.camunda.io:443")
        .Build();
```

# Deploy Process and Start Instance

To deploy a process you can use the `NewDeployCommand` method, which allows
to specify a list of resources, which should be deployed.

```csharp
await zeebeClient.NewDeployCommand()
        .AddResourceFile("send-email.bpmn")
        .Send();
```

To start a new instance you can specify the `bpmnProcessId`, i.e.
`send-email` and **optionally** process variables.

```csharp
var processInstanceResponse = await zeebeClient
          .NewCreateProcessInstanceCommand()
          .BpmnProcessId("send-email")
          .LatestVersion()
          .Variables(variables)
          .Send();
```

# Job Worker

To complete a [service task](https://docs.camunda.io/docs/components/modeler/bpmn/service-tasks/),
a [job worker](https://docs.camunda.io/docs/components/modeler/bpmn/service-tasks/) has
to be subscribed to the task type defined in the process, i.e. `email`. For this
the `NewWorker` method can be used.

```csharp
zeebeClient.NewWorker()
           .JobType("email")
           .Handler(JobHandler)
           .MaxJobsActive(3)
           .Timeout(TimeSpan.FromSeconds(10))
           .PollInterval(TimeSpan.FromMinutes(1))
           .PollingTimeout(TimeSpan.FromSeconds(30))
           .Name("CsharpGetStartedWorker")
           .Open()
```

The job worker will call for each activated job the specified job handler, which can look like this:

```csharp
void JobHandler(IJobClient jobClient, IJob activatedJob)
{
    var variables = JsonConvert.DeserializeObject<Dictionary<string, string>>(activatedJob.Variables);

    Log.LogInformation($"Sending email with message content: {variables[VariablesKey]}");

    jobClient.NewCompleteJobCommand(activatedJob).Send();
}
```

To make an job available for that job worker, a user task has to be completed, follow the
instructions in [the guide](../README.md#complete-the-user-task).
