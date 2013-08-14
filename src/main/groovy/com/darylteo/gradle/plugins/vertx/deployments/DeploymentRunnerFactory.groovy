package com.darylteo.gradle.plugins.vertx.deployments;

import org.gradle.api.Project

import com.darylteo.gradle.plugins.vertx.deployments.impl.DefaultDeploymentRunner;

public class DeploymentRunnerFactory {

  private final String version
  private final Project project

  public DeploymentRunnerFactory(Project project) {
    this.version = project.vertx?.version
    this.project = project
  }


  public DeploymentRunnerFactory(Project project, String version) {
    this.version = version
    this.project = project
  }

  public DeploymentRunner getRunner() {
    // Future Work: if platform api changes, we'll need to detect which version we're trying to deploy on
    // and return the appropriate deployment runner
    return new DefaultDeploymentRunner(this.project, this.version)
  }
}
