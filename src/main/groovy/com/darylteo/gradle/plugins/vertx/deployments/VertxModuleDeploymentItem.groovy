package com.darylteo.gradle.plugins.vertx.deployments;

import org.gradle.api.Project

public class VertxModuleDeploymentItem extends VertxDeploymentItem {
  private Project project

  public VertxModuleDeploymentItem(Project project, int instances = 1) {
    super(instances)

    this.project = project
  }
}
