package com.darylteo.gradle.plugins.vertx.deployments.impl;

import com.darylteo.gradle.plugins.vertx.deployments.VertxDeployment

public interface DeploymentRunner {
  void run(VertxDeployment deployment);
  void dryRun(VertxDeployment deployment);
}
