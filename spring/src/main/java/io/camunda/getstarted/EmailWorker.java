package io.camunda.getstarted;

import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.spring.client.annotation.JobWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class EmailWorker {

  private final static Logger LOG = LoggerFactory.getLogger(EmailWorker.class);

  @JobWorker(type = "email")
  public void sendEmail(final ActivatedJob job) {
    final String message_content = (String) job.getVariablesAsMap().get("message_content");
    LOG.info("Sending email with message content: {}", message_content);
  }

}
