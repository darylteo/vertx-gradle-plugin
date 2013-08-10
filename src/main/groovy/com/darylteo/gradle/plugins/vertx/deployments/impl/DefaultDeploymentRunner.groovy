package com.darylteo.gradle.plugins.vertx.deployments.impl;

import com.darylteo.gradle.plugins.vertx.deployments.VertxDeployment

class DefaultDeploymentRunner extends AbstractDeploymentRunner {
  public DefaultDeploymentRunner(String version) {
    super(version)
  }

  public void doRun(VertxDeployment deployment) {
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
    def jsonClazz = cl.loadClass("org.vertx.java.core.json.JsonObject")
    def handlerClazz = cl.loadClass("org.vertx.java.core.Handler")

    def factory = ServiceLoader.load(factoryClazz, cl).iterator().next()
    def platform = factory.createPlatformManager()

    deployment.each { item ->
      def moduleName = item.notation

      if(moduleName.startsWith(':')){
        def module = project.rootProject.project(moduleName)
        moduleName = module.moduleName
      }

      def config = jsonClazz.getConstructor(String.class).newInstance(item.config.toString())

      platform.deployModule(moduleName, config, item.instances, { result ->
        if(!result.succeeded()){
          println 'Failed to deploy module'
          result.cause().printStackTrace()

          abort()
        }
      }.asType(handlerClazz) );
    }

  }
}
