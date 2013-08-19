package com.darylteo.gradle.plugins.vertx

import groovy.json.*

import org.gradle.api.*
import org.gradle.api.tasks.Sync
import org.gradle.plugins.ide.idea.IdeaPlugin

import com.darylteo.gradle.plugins.vertx.properties.VertxPropertiesHandler
import com.darylteo.gradle.plugins.vertx.tasks.GenerateModJson
import com.darylteo.gradle.plugins.vertx.tasks.PullIncludes

/**
 * Plugin responsible for configuring a vertx enabled project
 *
 * @author Daryl Teo
 */
class VertxProjectPlugin implements Plugin<Project> {
  @Override
  public void apply(Project project) {
    project.convention.plugins.vertxProjectPlugin = new ProjectPluginConvention(project)

    configureProject project
    addModuleTasks project
  }

  private void configureProject(Project project) {
    project.with {
      apply plugin: 'java'

      sourceCompatibility = '1.7'
      targetCompatibility = '1.7'

      repositories {
        mavenCentral()
        maven { url 'https://oss.sonatype.org/content/repositories/snapshots' }
      }

      configurations {
        provided      // compile time dependencies that should not be packed in

        vertxcore     // holds all core vertx jars
        vertxincludes // holds all included modules
        vertxlibs     // holds all libs
        vertxzips     // holds all vertx mod zips for core languages

        provided.extendsFrom vertxcore
        provided.extendsFrom vertxincludes
        provided.extendsFrom vertxlibs

        compile.extendsFrom provided
      }

      /* Module Configuration */
      afterEvaluate {
        // configure other language environments
        if(vertx.language != 'java'){
          project.apply plugin: vertx.language
          dependencies {
            def langModule = "io.vertx:lang-${vertx.language}:${vertx.version}"
            vertxcore langModule
            vertxzips "$langModule:mod@zip"
          }

          configurations.vertxzips.each { zip ->
            dependencies {
              vertxincludes zipTree(zip).matching { include 'lib/*.jar' }
            }
          }
        }

        dependencies {
          vertxcore("io.vertx:vertx-core:${vertx.version}")
          vertxcore("io.vertx:vertx-platform:${vertx.version}")
          vertxcore("io.vertx:testtools:${vertx.version}")

          project.includes.each { module ->
            vertxincludes rootProject.files("mods/$module")
            vertxincludes rootProject.fileTree("mods/$module") {
              builtBy pullIncludes
              include 'lib/*.jar'
            }
          }
        }

        // Configuring Classpath
        sourceSets {
          all { compileClasspath += configurations.provided }
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
  }

  private void addModuleTasks(Project project){
    project.with {
      task('generateModJson', type:GenerateModJson) { group = 'vert.x' }

      task('copyMod', dependsOn: [
        generateModJson
      ], type: Sync) {
        group = 'vert.x'
        description = 'Installs the module into the mods directory (default ${rootProject.buildDir}/mods)'

        ext.modsDir = "${rootProject.buildDir}/mods"
        ext.destDir = rootProject.file("${modsDir}/${project.moduleName}")

        afterEvaluate {
          into destDir

          sourceSets.all {
            if (it.name != 'test'){
              from it.output
            }
          }
          from generateModJson

          // and then into module library directory
          into ('lib') { from configurations.vertxlibs }
        }
      }

      task('pullIncludes', type: PullIncludes) {
        group = 'vert.x'
        description "Pulling in dependencies for $project"
      }

      // Required for test tasks
      test {
        dependsOn copyMod
        systemProperty 'vertx.modulename', moduleName
        systemProperty 'vertx.mods', copyMod.modsDir

        workingDir rootProject.projectDir
      }

      compileJava.dependsOn pullIncludes

    }
  }

  private class ProjectPluginConvention {
    private Project project
    private VertxPropertiesHandler properties

    ProjectPluginConvention(Project project){
      this.project = project
      this.properties = new VertxPropertiesHandler(project)
    }

    String getModuleName() {
      return "${project.group}~${project.name}~${project.version}"
    }

    def getIncludes() {
      def includes = project.vertx.config?.includes ?: null

      if(includes instanceof GString) {
        includes = includes.toString()
      }

      if(includes instanceof String) {
        includes = includes.split("\\s*,\\s*")
      } else {
        includes = includes ?: []
      }

      return includes;
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
