package io.camunda.getstarted;

import io.camunda.spring.client.EnableZeebeClient;
import io.camunda.spring.client.ZeebeClientLifecycle;
import io.camunda.spring.client.annotation.ZeebeDeployment;
import io.camunda.zeebe.client.api.response.ProcessInstanceEvent;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableZeebeClient
@ZeebeDeployment(classPathResources = "send-email.bpmn")
public class ProcessApplication implements CommandLineRunner {

  private final static Logger LOG = LoggerFactory.getLogger(ProcessApplication.class);

  public static void main(String[] args) {
    SpringApplication.run(ProcessApplication.class, args);
  }

  @Autowired
  private ZeebeClientLifecycle client;

  @Override
  public void run(final String... args) throws Exception {
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
  }
}
