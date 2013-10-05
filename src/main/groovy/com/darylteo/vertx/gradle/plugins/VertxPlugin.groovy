package com.darylteo.vertx.gradle.plugins

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.bundling.Zip

import com.darylteo.vertx.gradle.configuration.ModuleConfiguration
import com.darylteo.vertx.gradle.configuration.PlatformConfiguration
import com.darylteo.vertx.gradle.configuration.ProjectConfiguration
import com.darylteo.vertx.gradle.tasks.GenerateModJson

public class VertxPlugin implements Plugin<Project> {
  public void apply(Project project) {
    applyExtensions(project)
    configure(project)
  }

  private void configure(Project project) {
    project.with {
      apply plugin: 'java'

      configurations {
        provided

        vertxcore

        provided.extendsFrom vertxcore

        compile.extendsFrom provided
      }

      task('generateModJson', type: GenerateModJson) {}
      task('modZip', type: Zip) {
        classifier = 'mod'
      }

      afterEvaluate {

        project.ext.archivesBaseName = "${vertx.module.group}:${vertx.module.name}:${vertx.module.version}"

        modZip {
          if(project.hasProperty('sourceSets')) {
            project.sourceSets.all { from it.output }
          }

          into('lib') {
            from project.configurations.compile - project.configurations.provided
          }
        }
      }
    }
  }

  private void applyExtensions(Project project) {
    project.extensions.create 'vertx', ProjectConfiguration

    project.vertx.extensions.create 'platform', PlatformConfiguration
    project.vertx.extensions.create 'module', ModuleConfiguration
  }
}