package com.darylteo.gradle.plugins.vertx.deployments;

class VertxDeployment {
  private List<VertxDeploymentItem> modules = []

  def deploy(String notation, int instances, Closure closure) {
    def item = new VertxDeploymentItem(notation, instances)
    closure.delegate = item
    closure.resolveStrategy = Closure.DELEGATE_FIRST
    closure.call(item)

    modules += item
  }
}
