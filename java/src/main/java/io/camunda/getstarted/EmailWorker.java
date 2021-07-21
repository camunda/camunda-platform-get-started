package io.camunda.getstarted;

import io.camunda.zeebe.client.ZeebeClient;
import java.util.Scanner;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class EmailWorker {

  private static final Logger LOG = LogManager.getLogger(EmailWorker.class);

  public static void main(String[] args) {
    try (ZeebeClient client = ZeebeClientFactory.getZeebeClient()) {
      client.newWorker().jobType("email").handler((jobClient, job) -> {
        final String message_content = (String) job.getVariablesAsMap().get("message_content");

        LOG.info("Sending email with message content: {}", message_content);

        jobClient.newCompleteCommand(job.getKey()).send()
          // join(); <-- This would block for the result. While this is easier-to-read code, it has limitations for parallel work.
          // Hence, the following code leverages reactive programming. This is discssed in https://blog.bernd-ruecker.com/writing-good-workers-for-camunda-cloud-61d322cad862.
          .whenComplete((result, exception) -> {
            if (exception == null) {
              LOG.info("Completed job successful with result:" + result);
            } else {
              LOG.error("Failed to complete job", exception);
            }
          });
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