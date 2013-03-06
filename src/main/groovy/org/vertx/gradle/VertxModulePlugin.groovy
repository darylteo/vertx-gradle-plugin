package org.vertx.gradle

import org.gradle.api.*
import org.gradle.api.artifacts.*;
import org.gradle.api.logging.*;
import org.gradle.api.tasks.*
import org.gradle.api.tasks.bundling.Zip

import groovy.json.*

import java.nio.file.Files

import org.vertx.java.core.Handler
import org.vertx.java.platform.PlatformLocator
import org.vertx.java.platform.impl.ModuleClassLoader

class VertxModulePlugin implements Plugin<Project> {
  def logger = Logging.getLogger(VertxModulePlugin.class)

  void apply(Project project) {
    project.with {
      ext.vertx = true

      loadModuleProperties(it)
      loadModuleConfig(it)
      loadBuildScript(it)

      ext.moduleName = "${repotype}:${group}:${artifact}:${version}"
      ext.isRunnable = config.main != null

      defaultTasks = ['assemble']

      task('copyMod', type:Copy, dependsOn: 'classes', description: 'Assemble the module into the local mods directory') {
        // Copy into module directory
        into rootProject.file("mods/$moduleName")
        from compileJava
        from file('src/main/resources')

        // and then into module library directory
        into( 'lib' ) {
          from configurations.compile.copy {
            if (it instanceof ProjectDependency) {
              return it.dependencyProject.isLibrary
            } else {
              return true
            }
          }
        }
      }

      task('modZip', type: Zip, dependsOn: 'pullInDeps', description: 'Package the module .zip file') {
        group = 'vert.x'
        description = "Assembles a vert.x module"
        destinationDir = project.file('build/libs')
        archiveName = "${moduleName}-${version}" + ".zip"
        from tasks.copyMod
      }

      task('pullInDeps', dependsOn: 'copyMod', description: 'Pull in all the module dependencies for the module into the nested mods directory') << {
        if (pullInDeps == 'true') {
          def pm = PlatformLocator.factory.createPlatformManager()
          System.out.println("Pulling in dependencies for module ${properites.moduleName}. Please wait...")
          pm.pullInDependencies(moduleName)
          System.out.println("Dependencies pulled into mods directory of module")
        }
      }

      // run task
      if (isRunnable == true) {
        task("run-${artifact}", dependsOn: 'copyMod', description: 'Run the module using all the build dependencies (not using installed vertx)') << {
          def mutex = new Object()

          ModuleClassLoader.reverseLoadOrder = false
          def pm = PlatformLocator.factory.createPlatformManager()
          pm.deployModule(moduleName, null, 1, new Handler<String>() {
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

      if(repotype == 'maven'){
        apply plugin: MavenSettings
      }

    }
  }

  def loadModuleConfig(Project project){
    def f = project.file('src/main/resources/mod.json')
    if(!f.canRead()){
      project.ext.config = [:]
      return
    }

    f.withReader { def reader ->
      project.ext.config = new JsonSlurper().parse(reader)
    }
  }

  def loadModuleProperties(Project project){
    def f = project.file('module.properties')
    if(!f.canRead()){
      return [:]
    }

    f.withReader { def reader ->
      def props = new Properties()
      props.load(reader)

      props.each { k,v ->
        if (project.hasProperty(k)){
          project[k] = v
        } else {
          project.ext[k] = v
        }
      }
    }
  }

  def loadBuildScript(Project project) {
    project.apply from: project.file('module.gradle')
  }

}