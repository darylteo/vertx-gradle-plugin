package com.darylteo.gradle.plugins.vertx.deployments;

import org.gradle.api.Project

public class VertxProjectDeploymentItem extends VertxDeploymentItem {
  private Project project
  public VertxProjectDeploymentItem(Project project, int instances = 1) {
    super(instances)
    this.project = project
  }

  public String getNotation() {
    return this.project.moduleName
  }
}
