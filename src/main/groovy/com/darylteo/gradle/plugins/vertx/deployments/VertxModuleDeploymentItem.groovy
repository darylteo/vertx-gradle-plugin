package com.darylteo.gradle.plugins.vertx.deployments;

import org.gradle.api.Project

public class VertxModuleDeploymentItem extends VertxDeploymentItem {
  private String notation
  
  public VertxModuleDeploymentItem(String notation, int instances = 1) {
    super(instances)
    this.notation = notation
  }
  
  public String getNotation() {
    return this.notation
  }
}
