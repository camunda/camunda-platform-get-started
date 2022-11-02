using System;
using System.Collections.Generic;
using System.Threading.Tasks;
using Microsoft.Extensions.Logging;
using Newtonsoft.Json;
using NLog.Extensions.Logging;
using Zeebe.Client;
using Zeebe.Client.Api.Responses;
using Zeebe.Client.Api.Worker;
using Zeebe.Client.Impl.Builder;

namespace csharp
{
    class Program
    {
        private static readonly string ClientID = "YOUR_CLIENT_ID";
        private static readonly string ClientSecret = "YOUR_CLIENT_SECRET";
        private static readonly string ClusterAddress = "YOUR_CLUSTER_ID.bru-2.zeebe.camunda.io:443";
        private static readonly ILoggerFactory LoggerFactory = new NLogLoggerFactory();
        private static readonly ILogger<Program> Log = LoggerFactory.CreateLogger<Program>();
        private const string LogMessage = "Started instance for" +
                                          " processDefinitionKey='{processDefinitionKey}'," +
                                          " bpmnProcessId='{bpmnProcessId}'," +
                                          " version='{version}'" +
                                          " with processInstanceKey='{processInstanceKey}'";

        private const string VariablesKey = "message_content";
        private const string VariablesValue = "Hello from the C# get started";

        static async Task Main(string[] _)
        {
            using (var zeebeClient = CreateZeebeClient())
            {
                var topology = await zeebeClient.TopologyRequest().Send();

                Console.WriteLine("Topology: " + topology);

                await zeebeClient.NewDeployCommand().AddResourceFile("send-email.bpmn").Send();

                var variables = $"{{\"{VariablesKey}\":\"{VariablesValue}\"}}";
                var processInstanceResponse = await zeebeClient
                    .NewCreateProcessInstanceCommand()
                    .BpmnProcessId("send-email")
                    .LatestVersion()
                    .Variables(variables).Send();

                Log.LogInformation(LogMessage,
                    processInstanceResponse.ProcessDefinitionKey,
                    processInstanceResponse.BpmnProcessId,
                    processInstanceResponse.Version,
                    processInstanceResponse.ProcessInstanceKey);

                using (zeebeClient.NewWorker()
                    .JobType("email")
                    .Handler(JobHandler)
                    .MaxJobsActive(3)
                    .Timeout(TimeSpan.FromSeconds(10))
                    .PollInterval(TimeSpan.FromMinutes(1))
                    .PollingTimeout(TimeSpan.FromSeconds(30))
                    .Name("CsharpGetStartedWorker")
                    .Open())
                {
                    AwaitExitUserCmd();
                }
            }
        }


        private static void JobHandler(IJobClient jobClient, IJob activatedJob)
        {
            var variables = JsonConvert.DeserializeObject<Dictionary<string, string>>(activatedJob.Variables);

            Log.LogInformation($"Sending email with message content: {variables[VariablesKey]}");

            jobClient.NewCompleteJobCommand(activatedJob).Send();
        }

        private static IZeebeClient CreateZeebeClient()
        {
            return CamundaCloudClientBuilder
                .Builder()
                .UseClientId(ClientID)
                .UseClientSecret(ClientSecret)
                .UseContactPoint(ClusterAddress)
                .UseLoggerFactory(LoggerFactory) // optional
                .Build();
        }

        private static void AwaitExitUserCmd()
        {
            var command = "";
            do
            {
                Console.Write("Type 'exit' to stop: ");
                command = Console.ReadLine();
            } while (!command.ToLower().Equals("exit"));
        }
    }
}
