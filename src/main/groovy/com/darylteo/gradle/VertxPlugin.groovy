/*
 * Copyright 2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.darylteo.gradle

import org.gradle.api.*
import org.gradle.api.artifacts.*;
import org.gradle.api.logging.*;
import org.gradle.api.tasks.*
import org.gradle.api.tasks.bundling.Zip

import org.gradle.plugins.ide.idea.IdeaPlugin;

import groovy.json.*

import java.nio.file.Files

import org.vertx.java.core.Handler
import org.vertx.java.platform.PlatformLocator
import org.vertx.java.platform.impl.ModuleClassLoader

class VertxPlugin implements Plugin<Project> {
  def logger = Logging.getLogger(VertxPlugin.class)

  void apply(Project project) {
    project.with {
      ext.vertx = true

      apply plugin: PropertiesLoader

      println "Configuring Module: $it"

      apply plugin: 'eclipse'
      apply plugin: 'idea'

      defaultTasks = ['assemble']

      configurations {
        provided
        testCompile.extendsFrom provided
      }

      repositories {
        if (System.getenv("JENKINS_HOME") == null) {
          // We don't want to use mavenLocal when running on CI - mavenLocal is only useful in Gradle for
          // publishing artifacts locally for development purposes - maven local is also not threadsafe when there
          // are concurrent builds
          mavenLocal()
        }
        maven { url 'https://oss.sonatype.org/content/repositories/snapshots' }
        mavenCentral()
      }

      // Language Plugins
      apply plugin: 'java'
      apply plugin: 'scala'
      apply plugin: 'groovy'

      dependencies {
        provided "io.vertx:vertx-core:${vertxVersion}"
        provided "io.vertx:vertx-platform:${vertxVersion}"
        testCompile "junit:junit:${junitVersion}"
        testCompile "io.vertx:testtools:${toolsVersion}"
      }

      sourceCompatibility = '1.7'
      targetCompatibility = '1.7'

      sourceSets {
        main {
          compileClasspath = compileClasspath + configurations.provided
        }
      }

      // Project configuration
      loadDefaults(it)
      loadModuleProperties(it)
      loadModuleConfig(it)
      loadBuildScript(it)

      ext.moduleName = "${repotype}:${group}:${artifact}:${version}"
      ext.isRunnable = config.main != null

      defaultTasks = ['assemble']

      task('copyMod', dependsOn: 'classes', description: 'Assemble the module into the local mods directory') << {
        // Copy into module directory
        copy {

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
      }

      task('modZip', type: Zip, dependsOn: 'pullInDeps', description: 'Package the module .zip file') {
        group = 'vert.x'
        description = "Assembles a vert.x module"
        destinationDir = file("$buildDir/libs")
        archiveName = "${artifact}-${version}.zip"
        from tasks.copyMod
      }

      task('pullInDeps', dependsOn: 'copyMod', description: 'Pull in all the module dependencies for the module into the nested mods directory') << {
        if (pullInDeps == 'true') {
          def pm = PlatformLocator.factory.createPlatformManager()
          System.out.println("Pulling in dependencies for module $moduleName. Please wait...")
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

      // Map the 'provided' dependency configuration to the appropriate IDEA visibility scopes.
      plugins.withType(IdeaPlugin) {
        idea {
          module {
            scopes.PROVIDED.plus += configurations.provided
            scopes.COMPILE.minus += configurations.provided
            scopes.TEST.minus += configurations.provided
            scopes.RUNTIME.minus += configurations.provided
          }
        }
      }

    }
  }

  def loadDefaults(Project project){
    (
      [
        group: 'my-company',
        artifact: project.name,
        version: '1.0.0-SNAPSHOT',
        repotype: 'local',

        isModule: false,
        isLibrary: false
      ]
    ).each { def k,v ->
      if (!project.hasProperty(k) && !project.ext.hasProperty(k)){
        println("$k:$v")
        project.ext[k] = v
      }
    }

    if (project.file('module.gradle').exists()){
      project.isModule = true
    } else {
      project.isLibrary = true
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
          println("$k:$v")
          project[k] = v
        } else {
          project.ext[k] = v
        }
      }
    }
  }

  def loadBuildScript(Project project) {
    def f = project.file('module.gradle')
    if(!f.canRead()){
      return [:]
    }

    project.apply from: project.file('module.gradle')
  }

}