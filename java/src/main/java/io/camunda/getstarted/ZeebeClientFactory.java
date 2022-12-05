package io.camunda.getstarted;

import io.camunda.zeebe.client.ZeebeClient;

public class ZeebeClientFactory {

  public static ZeebeClient getZeebeClient() {
    return ZeebeClient.newCloudClientBuilder()
        .withClusterId("YOUR_CLUSTER_ID")
        .withClientId("YOUR_CLIENT_ID")
        .withClientSecret("YOUR_CLIENT_SECRET")
        .withRegion("bru-2")
        .build();
  }

}
