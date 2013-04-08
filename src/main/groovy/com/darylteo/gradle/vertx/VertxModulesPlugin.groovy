package com.darylteo.gradle.vertx

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.ProjectDependency
import org.gradle.api.tasks.Sync

/**
 * Plugin for handling support of external vertx modules
 * Responsible for handling dependencies, setting up classpaths, tasks etc.
 * @author Daryl Teo
 */
class VertxModulesPlugin implements Plugin<Project>{
  @Override
  public void apply(Project project) {

    project.convention.plugins.modulesPlugin = new VertxModulesPluginConvention(project)

    project.configurations {
      modules
    }

    project.afterEvaluate {
      // Establishing the dependent projects
      project.dependentProjects = project.configurations
        .compile
        .dependencies
        .withType(ProjectDependency)
        .collect { dep -> dep.dependencyProject }

      afterEvaluation(project)
    } // end afterEvaluate

  }

  def afterEvaluation(Project project) {
    project.with {
      // TODO: probably inefficient, but unlikely to be an issue
      // improve with a map or something later if it does
      for (def dep in dependentProjects) {
        if (!dep.state.executed) {
          println "Deferring final configuration of $project for $dep"

          dep.afterEvaluate {
            afterEvaluation(project)
          }

          return
        }
      }

      // this task is responsible for extracting all the zip files
      task('installModules', type:Sync, dependsOn: configurations.modules) {
        // resolve and extract module zips
        allVertxModules
          // extract each
          .each { file ->
            def modName = file.name - '.zip'
            def modDir = rootProject.file("mods/$modName")

            into modDir
            from zipTree(file)
          } // end .each

        doFirst { println "Installing Modules for $project" }
      }

      // Setting up the classpath for compilation
      allVertxModules
        .each { file ->
          def modName = file.name - '.zip'
          def modDir = "mods/$modName"
          def modLibraries = rootProject.files(
              rootProject.zipTree(file)
              .matching { include 'lib/*.jar' }
              .collect { f -> return "$modDir/lib/$f.name" }
            ) { builtBy installModules }

          // This is required to compile locally
          sourceSets {
            all {
              compileClasspath -= rootProject.files(file)
              compileClasspath += rootProject.files(modDir)
              compileClasspath += modLibraries
            }
          }

          if (vertxModules.contains(file)) {
            dependencies.provided rootProject.files(modDir)
            dependencies.provided modLibraries
          }
        } // end .each

    } // end .with
  }

  private class VertxModulesPluginConvention {
    private Project project

    private _vertxModules
    private _allModules

    def dependentProjects = []

    VertxModulesPluginConvention(Project project) {
      this.project = project
    }

    def getVertxModules() {
      if (_vertxModules) {
        return _vertxModules
      }

      // return empty filecollection if unresolved yet
      if (!this.project.state.executed) {
        return this.project.files()
      }

      _vertxModules = this.project.configurations.modules.incoming.files.filter({ file -> file.name.endsWith('.zip')})
      return _vertxModules
    }

    def getAllVertxModules() {
      if (_allModules) {
        return _allModules
      }

      // return empty filecollection if unresolved yet
      if (!this.project.state.executed) {
        return this.project.files()
      }

      def result = getVertxModules()
      dependentProjects
        .collect({ dep -> dep.allVertxModules })
        .each({ files ->
          result = result + files
        })

      _allModules = result
      return _allModules
    }

    // Convenience Methods for converting vertx notation dependencies
    // into regular ones
    def vertxModule(String notation) {
      def (group, name, version) = notation.split('~')

      return vertxModule(group, name, version)
    }

    def vertxModule(Map module) {
      return vertxModule(module.group, module.name, module.version)
    }

    String vertxModule(String group, String name, String version) {
      return "$group:$name:$version@zip"
    }
  }

}
