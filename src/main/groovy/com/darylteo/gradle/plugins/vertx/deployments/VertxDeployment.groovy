package com.darylteo.gradle.plugins.vertx.deployments;

import org.gradle.api.Project

class VertxDeployment implements Iterable<VertxDeploymentItem> {
  public final String name
  public final Project project

  private List<VertxDeploymentItem> modules = []

  public VertxDeployment(Project project, String name) {
    this.name = name
    this.project = project
  }

  void deploy(Project project, int instances, Closure closure) {
    def item = new VertxProjectDeploymentItem(project, instances)
    closure.delegate = item
    closure.resolveStrategy = Closure.DELEGATE_FIRST
    closure.call(item)

    modules += item
  }

  void deploy(String notation, int instances, Closure closure) {
    def item = new VertxDeploymentItem(notation, instances)
    closure.delegate = item
    closure.resolveStrategy = Closure.DELEGATE_FIRST
    closure.call(item)

    modules += item
  }

  public Iterator<VertxDeploymentItem> iterator() {
    return this.modules.iterator()
  }
}
