

package com.darylteo.gradle.plugins.vertx.deployments.impl;

import org.gradle.api.Project

class DefaultDeploymentRunner extends AbstractDeploymentRunner {
  private def jsonClazz
  private def handlerClazz

  private def platform

  public DefaultDeploymentRunner(Project project, String version) {
    super(project, version)
  }

  protected void beforeRun() {
    project.configurations { deploymentrunner_core }

    project.dependencies {
      deploymentrunner_core "io.vertx:vertx-platform:${version}"
      deploymentrunner_core "io.vertx:vertx-core:${version}"
    }

    // set vertx.mods here, before loading class!
    System.setProperty('vertx.mods', 'build/mods')

    def urls = project.configurations.deploymentrunner_core.collect({ file ->
      return file.toURL()
    }).toArray(new URL[0])

    // required for the platform manager to locate vertx classes
    ClassLoader cl = new URLClassLoader(urls, this.class.classLoader)
    Thread.currentThread().contextClassLoader = cl

    // build the vertx platform
    def factoryClazz = cl.loadClass("org.vertx.java.platform.PlatformManagerFactory")
    this.jsonClazz = cl.loadClass("org.vertx.java.core.json.JsonObject")
    this.handlerClazz = cl.loadClass("org.vertx.java.core.Handler")

    def factory = ServiceLoader.load(factoryClazz, cl).iterator().next()
    this.platform = factory.createPlatformManager()
  }

  protected void deploy(String module, int instances, String config, Closure callback) {
    def json = jsonClazz.getConstructor(String.class).newInstance(config)

    println "Deploying Module: $module"
    this.platform.deployModule(module, json, instances, { result ->
      if(result.succeeded()){
        callback.call(success: true, exception: null)
      }else{
        callback.call(success: false, exception: result.cause())
      }
    }.asType(handlerClazz) );
  }
}
