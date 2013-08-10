package com.darylteo.gradle.plugins.vertx.handlers

import groovy.json.JsonBuilder
import org.gradle.api.Project
import com.darylteo.gradle.plugins.vertx.deployments.VertxDeploymentsContainer

public class VertxPropertiesHandler {
  /* public properties */
  String version = '+'
  String language = 'java'

  final Project project
  private final VertxDeploymentsContainer _deployments
  private JsonBuilder _config = new JsonBuilder()

  public void config(Closure closure) {
    this._config.call(closure)
  }

  public def getConfig(){
    return this._config.content
  }

  public void deployments(Closure closure) {
    closure.setDelegate(_deployments);
    closure.setResolveStrategy(Closure.DELEGATE_FIRST);
    closure.call(_deployments);
  }

  public VertxDeploymentsContainer getDeployments(){
    return this._deployments
  }

  public VertxPropertiesHandler(Project project) {
    this.project = project
    this._deployments  = new VertxDeploymentsContainer(project)
  }
}