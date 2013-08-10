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

  def deploy(Project project, int instances, Closure closure) {
    String moduleName = this.project?.moduleName ?: null

    if(!moduleName) {
      throw new Exception('Cannot deploy $project as it is not vert.x enabled')
    }

    return this.deploy(moduleName, instances, closure)
  }

  def deploy(String notation, int instances, Closure closure) {
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
