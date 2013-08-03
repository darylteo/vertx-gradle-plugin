package com.darylteo.gradle.plugins.vertx.deployments;

class VertxDeploymentsContainer {
  private List<VertxDeployment> deployments = []

  def deployment(String name, Closure closure) {
    VertxDeployment deployment = new VertxDeployment();
    closure.delegate = deployment
    closure.resolveStrategy = Closure.DELEGATE_FIRST;
    closure.call(deployment)

    deployments += deployment
  }
}
