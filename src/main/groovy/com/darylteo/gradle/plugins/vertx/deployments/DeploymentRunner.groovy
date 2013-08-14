package com.darylteo.gradle.plugins.vertx.deployments;


public interface DeploymentRunner {
  void run(VertxDeployment deployment);
  void run(String module);
}
