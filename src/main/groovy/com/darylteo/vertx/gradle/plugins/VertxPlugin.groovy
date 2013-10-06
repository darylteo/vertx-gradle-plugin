package com.darylteo.vertx.gradle.plugins

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.bundling.Zip

import com.darylteo.vertx.gradle.configuration.ModuleConfiguration
import com.darylteo.vertx.gradle.configuration.PlatformConfiguration
import com.darylteo.vertx.gradle.configuration.ProjectConfiguration
import com.darylteo.vertx.gradle.deployments.Deployment
import com.darylteo.vertx.gradle.tasks.GenerateModJson
import com.darylteo.vertx.gradle.tasks.RunVertx

public class VertxPlugin implements Plugin<Project> {
  public void apply(Project project) {
    applyExtensions project
    addDependencies project
    addTasks project
  }

  private void addDependencies(Project project) {
    project.apply plugin: 'java'

    project.configurations {
      vertxcore
      vertx

      provided {
        extendsFrom vertxcore
        extendsFrom vertx
      }

      compile { extendsFrom provided }
    }

    project.with {
      afterEvaluate {
        dependencies {
          def platform = vertx.platform

          if(!platform.language || !platform.version) {
            println "WARN: No vert.x language set for $project"
          } else if(platform.language == 'java') {
            vertxcore("io.vertx:vertx-platform:${platform.version}") {
              exclude group:'log4j', module:'log4j'
            }
          } else {
            apply plugin: platform.language
            vertxcore("io.vertx:lang-${platform.language}:${platform.version}"){
              exclude group:'log4j', module:'log4j'
            }
          }
        }
      }
    }
  }

  private void applyExtensions(Project project) {
    project.extensions.create 'vertx', ProjectConfiguration

    project.vertx.extensions.create 'platform', PlatformConfiguration
    project.vertx.extensions.create 'module', ModuleConfiguration

    project.vertx.extensions.deployments = project.container Deployment
  }

  private void addTasks(Project project) {
    project.with {
      // archive tasks
      task('generateModJson', type: GenerateModJson) {}
      task('modZip', type: Zip) { classifier = 'mod' }

      afterEvaluate {
        // archives
        ext.archivesBaseName = "${vertx.module.group}:${vertx.module.name}:${vertx.module.version}"
        modZip {
          sourceSets.all { from it.output }

          from generateModJson

          into('lib') {
            from project.configurations.compile - project.configurations.provided
          }
        }
      }

      // run tasks
      vertx.deployments {
        mod {  deploy project  }
      }

      afterEvaluate {
        vertx.deployments.each { dep ->
          task("run${dep.name.capitalize()}", type: RunVertx, group: 'Run') { /* */ deployment dep     }

          task("debug${dep.name.capitalize()}", type: RunVertx, group: 'Run') {
            //
            //
            deployment dep
            //
            //
          }
        }
      }
    }
  }
}