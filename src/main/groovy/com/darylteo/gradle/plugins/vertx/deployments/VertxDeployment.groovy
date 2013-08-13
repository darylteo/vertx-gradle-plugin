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

  public void deploy(Project project, int instances, Closure closure) {
    this._deploy(new VertxProjectDeploymentItem(project, instances), instances, closure)
  }

  public void deploy(String notation, int instances, Closure closure) {
    this._deploy(new VertxModuleDeploymentItem(notation, instances), instances, closure)
  }

  public Iterator<VertxDeploymentItem> iterator() {
    return this.modules.iterator()
  }

  private void _deploy(VertxDeploymentItem item, int instances, Closure closure) {
    closure.delegate = item
    closure.resolveStrategy = Closure.DELEGATE_FIRST
    closure.call(item)

    modules += item
  }
}
