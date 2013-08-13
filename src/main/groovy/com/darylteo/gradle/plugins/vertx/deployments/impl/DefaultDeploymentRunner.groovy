package com.darylteo.gradle.plugins.vertx.deployments.impl;

import com.darylteo.gradle.plugins.vertx.deployments.VertxDeployment
import com.darylteo.gradle.plugins.vertx.deployments.VertxDeploymentItem

class DefaultDeploymentRunner extends AbstractDeploymentRunner {
  private def jsonClazz
  private def handlerClazz

  private def platform

  public DefaultDeploymentRunner(String version) {
    super(version)
  }

  public void beforeRun(VertxDeployment deployment) {
    def project = deployment.project

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

    println "Deploying using vert.x-${this.version}"
  }

  protected void doRun(VertxDeployment deployment) {
    def incomplete = ([] as Set)

    deployment.each { dep ->
      incomplete.add(dep)
    }
    
    deployment.each { dep ->
      def moduleName = dep.notation
      def config = jsonClazz.getConstructor(String.class).newInstance(dep.config.toString())

      println "Deploying Module: $moduleName"
      this.platform.deployModule(moduleName, config, dep.instances, { result ->
        if(result.succeeded()){
          println "Module Deployed: $moduleName"

          synchronized(dep) {
            incomplete.remove(dep)
            if(incomplete.empty) {
              complete()
            }
          }
        }else{
          println 'Failed to deploy module'
          result.cause().printStackTrace()

          abort()
        }
      }.asType(handlerClazz) );
    }
  }

}
