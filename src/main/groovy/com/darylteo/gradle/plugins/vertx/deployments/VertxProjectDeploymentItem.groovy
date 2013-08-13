package com.darylteo.gradle.plugins.vertx.deployments;

import org.gradle.api.Project

public class VertxProjectDeploymentItem extends VertxDeploymentItem {
  public VertxProjectDeploymentItem(Project project) {
    this(project, 1)
  }

  public VertxProjectDeploymentItem(Project project, int instances) {
    // project must have moduleName from vertx plugin
    super(project.moduleName, instances)
  }
}
