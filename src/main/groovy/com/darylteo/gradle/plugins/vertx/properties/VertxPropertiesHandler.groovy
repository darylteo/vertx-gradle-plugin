package com.darylteo.gradle.plugins.vertx.properties

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

  public VertxPropertiesHandler(Project project) {
    this.project = project
    this._deployments  = new VertxDeploymentsContainer(project)
  }
}