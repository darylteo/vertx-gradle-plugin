package org.vertx.gradle

import org.gradle.api.*
import org.gradle.api.tasks.*
import org.gradle.api.tasks.bundling.Zip

import groovy.json.*

import java.nio.file.Files

import org.vertx.java.core.Handler
import org.vertx.java.platform.PlatformLocator
import org.vertx.java.platform.impl.ModuleClassLoader

class VertxModulePlugin implements Plugin<Project> {
  void apply(Project project) {
    project.ext.vertx = true

    project.extensions.create('props', VertxModuleProperties)

    loadVertxProperties(project).each { k,v ->
      project.ext[k] = v
    }

    loadModuleProperties(project).each { k,v ->
      project.props[k] = v
    }

    def config = loadModuleConfig(project)
    project.props.main = config.main

    setupTasks(project)
  }

  private void setupTasks(Project project){
    project.defaultTasks = ['assemble']

    project.task('copyMod', type:Copy, dependsOn: 'classes', description: 'Assemble the module into the local mods directory') {
      // Copy into module directory
      def moduleName = project.props.moduleName

      into project.rootProject.file("mods/$moduleName")
      from project.compileJava
      from project.file('src/main/resources')

      // and then into module library directory
      into( 'lib' ) {
        from project.configurations.compile.copy {
          return it.dependencyProject.library
        }
      }
    }

    project.task('modZip', type: Zip, dependsOn: 'pullInDeps', description: 'Package the module .zip file') {
      group = 'vert.x'
      description = "Assembles a vert.x module"
      destinationDir = project.file('build/libs')
      archiveName = "${project.props.moduleName}-${project.props.version}" + ".zip"
      from project.tasks.copyMod
    }

    project.task('pullInDeps', dependsOn: project.tasks.copyMod, description: 'Pull in all the module dependencies for the module into the nested mods directory') << {
      if (pullInDeps == 'true') {
        def pm = PlatformLocator.factory.createPlatformManager()
        System.out.println("Pulling in dependencies for module ${properites.moduleName}. Please wait...")
        pm.pullInDependencies(project.props.moduleName)
        System.out.println("Dependencies pulled into mods directory of module")
      }
    }

    // run task
    if (project.props.runnable) {
      project.task("run-${project.props.moduleName}", dependsOn: project.tasks.copyMod, description: 'Run the module using all the build dependencies (not using installed vertx)') << {
        def mutex = new Object()

        ModuleClassLoader.reverseLoadOrder = false
        def pm = PlatformLocator.factory.createPlatformManager()
        pm.deployModule(project.props.moduleName, null, 1, new Handler<String>() {
          public void handle(String deploymentID) {
            if (!deploymentID){
              logger.error 'Verticle failed to deploy.'

              // Wake the main thread
              synchronized(mutex){
                mutex.notify()
              }
              return
            }

            logger.info "Verticle deployed! Deployment ID is: $deploymentID"
            logger.info 'CTRL-C to stop server'
          }
        });

        // Waiting thread so that Verticle will continue running
        synchronized (mutex){
          mutex.wait()
        }
      }

    }
  }

  def loadModuleConfig(Project project){
    project.file('src/main/resources/mod.json').withReader { def reader ->
      return new JsonSlurper().parse(reader)
    }
  }

  def loadModuleProperties(Project project){
    project.file('module.properties').withReader { def reader ->
      def props = new Properties()
      props.load(reader)

      return props
    }
  }

  def loadVertxProperties(Project project){
    def f = project.file('gradle.properties')
    if(!f.canRead()){
      return
    }

    props.withReader { def reader ->
      def props = new Properties()
      props.load(reader)

      return props
    }
  }

}

class VertxModuleProperties {
  String repotype = 'maven'
  String groupname = 'my-company'
  String artifact = 'my-module'
  String version = '1.0.0-SNAPSHOT'

  String vertVersion = 'LATEST'
  String toolsVersion = 'LATEST'
  String junitVersion = 'LATEST'

  String main = null
  String testtimeout = '300'

  boolean runnable = false

  String getModuleName() {
    if (repotype != 'local') {
      return "$repotype:groupname:artifact:version"
    }else {
      return "$groupname-$artifact-$version"
    }
  }

  boolean getRunnable() {
    return this.main != null
  }
}