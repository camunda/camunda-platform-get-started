package io.camunda.getstarted;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.ProcessInstanceEvent;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DeployAndStartInstance {

  private static final Logger LOG = LogManager.getLogger(DeployAndStartInstance.class);

  public static void main(String[] args) {
    try (ZeebeClient client = ZeebeClient.newCloudClientBuilder()
        .withClusterId("365eed98-16c1-4096-bb57-eb8828ed131e")
        .withClientId("GZVO3ALYy~qCcD3MYq~sf0GIszNzLE_z")
        .withClientSecret(".RPbZc6q0d6uzRbB4LW.B8lCpsxbBEpmBX0AHQGzINf3.KK9RkzZW1aDaZ-7WYNJ")
        .build()) {
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