package com.darylteo.gradle.plugins.vertx.properties

import groovy.json.JsonBuilder
import org.gradle.api.Project
import com.darylteo.gradle.plugins.vertx.deployments.VertxDeploymentsContainer

public class VertxPropertiesHandler {
  /* public properties */
  public final Project project

  public String version = '+'
  public String modsDir;

  private final VertxDeploymentsContainer _deployments
  private JsonBuilder _config = new JsonBuilder()

  public VertxPropertiesHandler(Project project) {
    this.project = project

    this.modsDir = "${this.project.rootProject.buildDir}/mods"
    this._deployments  = new VertxDeploymentsContainer(project)
  }

  /* Configuration Methods */
  public void config(Closure closure) {
    this._config.call(closure)
  }

  public def getConfig(){
    return this._config.content
  }

  public void groovy(String version) {
    def langModule = "io.vertx:lang-groovy:${version}"
    project.dependencies {
      vertxcore langModule
      vertxzips "$langModule:mod@zip"
    }
  }
}