package io.camunda.getstarted;

import io.camunda.zeebe.spring.client.EnableZeebeClient;
import io.camunda.zeebe.spring.client.annotation.ZeebeWorker;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@EnableZeebeClient
public class EmailWorker {

  private final static Logger LOG = LoggerFactory.getLogger(EmailWorker.class);

  @ZeebeWorker(type = "email")
  public void sendEmail(final JobClient client, final ActivatedJob job) {
    final String message_content = (String) job.getVariablesAsMap().get("message_content");

    LOG.info("Sending email with message content: {}", message_content);

    client.newCompleteCommand(job.getKey()).send()
      // join(); <-- This would block for the result. While this is easier-to-read code, it has limitations for parallel work.
      // Hence, the following code leverages reactive programming. This is discssed in https://blog.bernd-ruecker.com/writing-good-workers-for-camunda-cloud-61d322cad862.
      .whenComplete((result, exception) -> {
        if (exception == null) {
          LOG.info("Completed job successful with result:" + result);
        } else {
          LOG.error("Failed to complete job", exception);
        }
      });    
  }

}
