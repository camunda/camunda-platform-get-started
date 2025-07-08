package io.camunda.getstarted;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.ProcessInstanceEvent;

import java.util.HashMap;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.camunda.bpm.model.xml.Model;
import org.camunda.bpm.model.xml.impl.ModelImpl;

public class DeployAndStartInstance {

  private static final Logger LOG = LogManager.getLogger(DeployAndStartInstance.class);

  public static void main(String[] args) {
    try (ZeebeClient client = ZeebeClientFactory.getZeebeClient()) {
      client.newDeployResourceCommand()
          .addResourceFromClasspath("send-email.bpmn")
          .send()
          .join();


      Map<String, Object> variables = new HashMap<>();
      variables.put("message_content", "Hello from the Java get started");
      final ProcessInstanceEvent event = client.newCreateInstanceCommand()
          .bpmnProcessId("send-email")
          .latestVersion()
          .variables(variables)
          .send()
          .join();

      // just here to provoke a compile error if using jdk 8 but byte code incompatible model lib
      Model model = new ModelImpl("");

      LOG.info("Started instance");
    }
  }
}