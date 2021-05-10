package io.camunda.getstarted;

import io.camunda.zeebe.client.ZeebeClient;
import java.util.Scanner;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class EmailWorker {

  private static final Logger LOG = LogManager.getLogger(EmailWorker.class);

  public static void main(String[] args) {
    try (ZeebeClient client =
        ZeebeClient.newCloudClientBuilder()
            .withClusterId("365eed98-16c1-4096-bb57-eb8828ed131e")
            .withClientId("GZVO3ALYy~qCcD3MYq~sf0GIszNzLE_z")
            .withClientSecret(".RPbZc6q0d6uzRbB4LW.B8lCpsxbBEpmBX0AHQGzINf3.KK9RkzZW1aDaZ-7WYNJ")
            .build()) {
      client.newWorker().jobType("email").handler((jobClient, job) -> {
        final String message_content = (String) job.getVariablesAsMap().get("message_content");

        LOG.info("Sending email with message content: {}", message_content);

        jobClient.newCompleteCommand(job.getKey()).send().join();
      }).open();

      // run until System.in receives exit command
      waitUntilSystemInput("exit");
    }
  }

  private static void waitUntilSystemInput(final String exitCode) {
    try (final Scanner scanner = new Scanner(System.in)) {
      while (scanner.hasNextLine()) {
        final String nextLine = scanner.nextLine();
        if (nextLine.contains(exitCode)) {
          return;
        }
      }
    }
  }
}