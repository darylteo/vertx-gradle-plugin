package com.darylteo.gradle.plugins.vertx.deployments.impl;

public class DeploymentRunnerFactory {

  public String version

  public DeploymentRunnerFactory(String version) {
    this.version = version
  }

  public DeploymentRunner getRunner() {
    // Future Work: if platform api changes, we'll need to detect which version we're trying to deploy on 
    // and return the appropriate deployment runner
    return new DefaultDeploymentRunner(this.version)
  }
}
