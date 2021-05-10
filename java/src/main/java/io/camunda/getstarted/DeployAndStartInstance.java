package io.camunda.getstarted;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.ProcessInstanceEvent;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DeployAndStartInstance {

  private static final Logger LOG = LogManager.getLogger(DeployAndStartInstance.class);

  public static void main(String[] args) {
    try (ZeebeClient client = ZeebeClientFactory.getZeebeClient()) {
      client.newDeployCommand()
          .addResourceFromClasspath("send-email.bpmn")
          .send()
          .join();

      final ProcessInstanceEvent event = client.newCreateInstanceCommand()
          .bpmnProcessId("send-email")
          .latestVersion()
          .variables(Map.of("message_content", "Hello from the Java get started"))
          .send()
          .join();

      LOG.info(
          "Started instance for processDefinitionKey='{}', bpmnProcessId='{}', version='{}' with processInstanceKey='{}'",
          event.getProcessDefinitionKey(), event.getBpmnProcessId(), event.getVersion(),
          event.getProcessInstanceKey());
    }
  }
}