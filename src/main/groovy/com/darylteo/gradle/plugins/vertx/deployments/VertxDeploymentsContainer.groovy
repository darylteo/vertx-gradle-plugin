package com.darylteo.gradle.plugins.vertx.deployments;

import org.gradle.api.Project

class VertxDeploymentsContainer implements Iterable<VertxDeployment>{
  private List<VertxDeployment> deployments = []
  public final Project project

  public VertxDeploymentsContainer(Project project) {
    this.project = project
  }

  def deployment(String name, Closure closure) {
    VertxDeployment deployment = new VertxDeployment(project, name);
    closure.delegate = deployment
    closure.resolveStrategy = Closure.DELEGATE_FIRST;
    closure.call(deployment)

    deployments += deployment
  }

  public Iterator<VertxDeployment> iterator() {
    return this.deployments.iterator()
  }
}
