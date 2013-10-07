package com.darylteo.vertx.gradle.plugins

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.Sync
import org.gradle.api.tasks.bundling.Zip

import com.darylteo.vertx.gradle.configuration.ModuleConfiguration
import com.darylteo.vertx.gradle.configuration.PlatformConfiguration
import com.darylteo.vertx.gradle.configuration.ProjectConfiguration
import com.darylteo.vertx.gradle.deployments.Deployment
import com.darylteo.vertx.gradle.tasks.GenerateModJson
import com.darylteo.vertx.gradle.tasks.RunVertx

public class VertxPlugin implements Plugin<Project> {
  public void apply(Project project) {
    applyPlugins project
    applyExtensions project
    addDependencies project
    addTasks project
  }

  private void applyPlugins (Project project) {
    project.with { apply plugin: 'java' }
  }

  private void addDependencies(Project project) {
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

    project.vertx.extensions.deployments = project.container Deployment.class
  }

  private void addTasks(Project project) {
    addArchiveTasks project
    addRunTasks project
  }

  private void addArchiveTasks(Project project) {
    project.with {
      // archive tasks
      task('generateModJson', type: GenerateModJson) {}
      task('copyMod', type: Sync) {
        into "$buildDir/mod"
        from sourceSets*.output
        from generateModJson

        into('lib') {
          from configurations.compile - configurations.provided
        }
      }
      task('installMod', type: Sync) {
        into rootProject.file("${rootProject.buildDir}/mods")
        from copyMod
      }
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
    }
  }

  private void addRunTasks(Project project) {
    project.with {
      vertx.deployments.whenObjectAdded { Deployment dep ->
        // add tasks for deployment
        def name = dep.name.capitalize()
        task("run$name", type: RunVertx) { deployment = dep }
        task("debug$name", type: RunVertx) {
          deployment = dep
          debug = true
        }
      }

      vertx.deployments.whenObjectRemoved { Deployment dep ->
        def name = dep.name.capitalize()
        tasks.removeAll tasks."run$name",tasks."debug$name"
      }

      vertx.deployments { mod { deploy project } }
    }
  }
}