package com.darylteo.gradle.plugins.vertx.deployments.impl;

public class DeploymentRunnerFactory {
  public DeploymentRunner getRunner() {
    return new DefaultDeploymentRunner()
  }
}
