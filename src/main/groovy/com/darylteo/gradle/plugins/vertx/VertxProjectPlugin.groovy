package com.darylteo.gradle.plugins.vertx

import groovy.json.*

import org.gradle.api.*
import org.gradle.api.artifacts.ProjectDependency
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.bundling.Zip
import org.gradle.plugins.ide.idea.IdeaPlugin
import org.vertx.java.core.Handler
import org.vertx.java.platform.PlatformLocator
import org.vertx.java.platform.impl.ModuleClassLoader

/**
 * Plugin responsible for configuring a vertx enabled project
 *
 * Required Properties
 *  * vertxVersion - version of Vertx to use
 * @author Daryl Teo
 */
class VertxProjectPlugin implements Plugin<Project> {
  @Override
  public void apply(Project project) {
    project.convention.plugins.projectPlugin = new ProjectPluginConvention(project)

    configureProject project
    addModuleTasks project
  }

  private void configureProject(Project project) {
    project.beforeEvaluate {
      project.with {
        println "Configuring $it"

        // configure language plugins
        if(vertx.language in ['java', 'groovy', 'scala']){
          apply plugin: vertx.language
        } else {
          apply plugin: 'java'
        }

        repositories {
          mavenCentral()
          maven { url 'https://oss.sonatype.org/content/repositories/snapshots' }
        }

        configurations {
          vertxcore

          compile.extendsFrom vertxcore
        }

        /* Module Configuration */
        dependencies {
          vertxcore "io.vertx:vertx-core:${vertx.version}"
          vertxcore "io.vertx:vertx-platform:${vertx.version}"

          vertxcore "io.vertx:testtools:${vertx.version}"
        }

        // Configuring Classpath
        sourceSets {
          all { compileClasspath += configurations.vertxcore }
        }

        // Map the 'provided' dependency configuration to the appropriate IDEA visibility scopes.
        plugins.withType(IdeaPlugin) {
          idea {
            module {
              scopes.PROVIDED.plus += configurations.vertxcore
              scopes.COMPILE.minus += configurations.vertxcore
              scopes.TEST.minus += configurations.vertxcore
              scopes.RUNTIME.minus += configurations.vertxcore
            }
          }
        }
      }
    }
  }

  private void addModuleTasks(Project project){
    project.afterEvaluate {
      project.with {
        task('copyMod', dependsOn: 'classes', type: Copy, description: 'Assemble the module into the local mods directory') {
          into rootProject.file("mods/${project.moduleName}")
          sourceSets.all { from it.output }

          // and then into module library directory
          into ('lib') {
            from configurations.compile
            exclude { it.file in configurations.vertxcore.files }
          }

        }
      }
    }
    //      test { dependsOn copyMod }
    //
    //      // Zipping up the module
    //      task('modZip', type: Zip, dependsOn: 'pullInDeps', description: 'Package the module .zip file') {
    //        group = 'vert.x'
    //        description = "Assembles a vert.x module"
    //        destinationDir = file("$buildDir/libs")
    //        archiveName = "${artifact}-${version}.zip"
    //
    //        from copyMod
    //      }
    //
    //      // Adding Tasks
    //      task('pullInDeps', dependsOn: 'copyMod', description: 'Pull in all the module dependencies for the module into the nested mods directory') << {
    //        if (pullInDeps == 'true') {
    //          def pm = PlatformLocator.factory.createPlatformManager()
    //          System.out.println("Pulling in dependencies for module $moduleName. Please wait...")
    //          pm.pullInDependencies(moduleName)
    //          System.out.println("Dependencies pulled into mods directory of module")
    //        }
    //      }
    //
    //      // run task
    //      if (isRunnable) {
    //        task("run-${artifact}", dependsOn: 'copyMod', description: 'Run the module using all the build dependencies (not using installed vertx)') << {
    //          def mutex = new Object()
    //
    //          ModuleClassLoader.reverseLoadOrder = false
    //          def pm = PlatformLocator.factory.createPlatformManager()
    //          pm.deployModule(moduleName, null, 1, new Handler<String>() {
    //              public void handle(String deploymentID) {
    //                if (!deploymentID){
    //                  println.error 'Verticle failed to deploy.'
    //
    //                  // Wake the main thread
    //                  synchronized(mutex){
    //                    mutex.notify()
    //                  }
    //                  return
    //                }
    //
    //                println "Verticle deployed! Deployment ID is: $deploymentID"
    //                println 'CTRL-C to stop server'
    //              }
    //            });
    //
    //          // Waiting thread so that Verticle will continue running
    //          synchronized (mutex){
    //            mutex.wait()
    //          }
    //        }
    //      }
    //
    //      jar { archiveName = "$artifact-${version}.jar" }
    //
    //      task('javadocJar', type: Jar, dependsOn: javadoc) {
    //        classifier = 'javadoc'
    //        from "$buildDir/docs/javadoc"
    //      }
    //
    //      task('sourcesJar', type: Jar) {
    //        from sourceSets.main.allSource
    //        classifier = 'sources'
    //      }
    //
    //      artifacts { archives modZip }
    //      if (produceJar) {
    //        artifacts {
    //          archives javadocJar
    //          archives sourcesJar
    //        }
    //      } else {
    //        configurations.archives.artifacts.removeAll configurations.archives.artifacts.findAll { artifact ->
    //          jar.outputs.files.contains(artifact.file)
    //        }
    //      }

  }

  private void applyLanguagePlugins(Project project) {
    def applied = false

    project.with {
      // Apply language plugins
      ['java', 'scala', 'groovy'].each { def lang ->
        if(it.file("src/main/$lang").isDirectory() || it.file("src/test/$lang").isDirectory()){
          println "$it: $lang detected. Applying $lang plugin."
          it.apply plugin: lang
          applied = true
        }
      }

      if (!applied) {
        apply plugin: 'java' // required for test task and copying of resources dir
      }

      sourceCompatibility = '1.7'
      targetCompatibility = '1.7'

      defaultTasks = ['assemble']
    }

  }

  private class ProjectPluginConvention {
    private Project project
    private VertxPropertiesHandler properties

    ProjectPluginConvention(Project project){
      this.project = project
      this.properties = new VertxPropertiesHandler()
    }

    String getModuleName() {
      return "${project.group}~${project.name}~${project.version}"
    }

    def vertx(Closure closure) {
      closure.setDelegate(properties)
      closure.resolveStrategy = Closure.DELEGATE_FIRST
      closure(properties)
    }

    def getVertx() {
      return properties
    }
  }

}
