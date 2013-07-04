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

        sourceCompatibility = '1.7'
        targetCompatibility = '1.7'

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
        task('generateModJson') {
          def confdir = file("$buildDir/conf")
          def modjson = file("$confdir/mod.json")
          outputs.dir modjson

          doLast{
            confdir.mkdirs()
            modjson.createNewFile()

            modjson << JsonOutput.toJson(vertx.config)
          }
        }

        task('copyMod', dependsOn: [classes, generateModJson], type: Copy) {
          group = 'vert.x'
          description = 'Assemble the module into the local mods directory'

          into rootProject.file("mods/${project.moduleName}")

          sourceSets.all {
            if (it.name != 'test'){
              println it.name
              from it.output
            }
          }
          from generateModJson

          // and then into module library directory
          into ('lib') {
            from configurations.compile
            exclude { it.file in configurations.vertxcore.files }
          }

        }

        test { dependsOn copyMod }
      }
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
