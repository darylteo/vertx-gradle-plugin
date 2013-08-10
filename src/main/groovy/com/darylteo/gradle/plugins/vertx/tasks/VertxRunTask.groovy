package com.darylteo.gradle.plugins.vertx.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import com.darylteo.gradle.plugins.vertx.deployments.VertxDeployment


class VertxRunTask extends DefaultTask {
  public VertxDeployment deployment = null
  public String version = '2.0.0-final'

  @TaskAction
  def run(){
    if(!deployment) {
      return
    }

    project.configurations { vertx_deploy_core }

    project.dependencies {
      vertx_deploy_core "io.vertx:vertx-platform:${this.version}"
      vertx_deploy_core "io.vertx:vertx-core:${this.version}"
    }

    def urls = project.configurations.vertx_deploy_core.collect({ file ->
      println file
      return file.toURL()
    }).toArray(new URL[0])

    ClassLoader cl = new URLClassLoader(urls, this.class.classLoader)

    // getting the platform factory
    def factoryClazz = cl.loadClass("org.vertx.java.platform.PlatformManagerFactory")
    def jsonClazz = cl.loadClass("org.vertx.java.core.json.JsonObject")
    def handlerClazz = cl.loadClass("org.vertx.java.core.Handler")

    // required for the platform manager to locate vertx classes
    Thread.currentThread().setContextClassLoader(cl);

    def factory = ServiceLoader.load(factoryClazz, cl).iterator().next()
    def platform = factory.createPlatformManager()
    def mutex = new Object()

    System.setProperty('vertx.mods', 'build/mods')
    println "Starting deployment: ${deployment.name}"

    deployment.each { item ->
      def moduleName = item.notation

      if(moduleName.startsWith(':')){
        def module = project.rootProject.project(moduleName)
        moduleName = module.moduleName
      }

      def config = jsonClazz.getConstructor(String.class).newInstance(item.config.toString())

      platform.deployModule(moduleName, config, item.instances, { result ->
        if(result.succeeded()){
          println 'Module Deployed. Press Ctrl + C to stop.'
        } else {
          println 'Failed to deploy module'
          result.cause().printStackTrace()

          synchronized(mutex){
            mutex.notifyAll();
          }
        }
      }.asType(handlerClazz) );
    }

    synchronized(mutex) {
      mutex.wait();
    }
  }
}
