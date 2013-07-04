package com.darylteo.gradle.plugins.vertx

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
      explodedModules

      provided.extendsFrom explodedModules
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
        doFirst { println "Installing Modules for $project" }
      }

      // Setting up the classpath for compilation
      configurations.modules.dependencies.each { dep ->
        def vertxName = "${dep.group}~${dep.name}~${dep.version}"

        configurations.modules.files(dep)
          // ignore non zips
          .findAll { file ->
            return file.name.endsWith('.zip')
          }.each { file ->
            // contents of zip
            def modZip = rootProject.zipTree(file)

            // destination of exploded zip
            def modDir = "mods/$vertxName"

            // destination of library jars. installModules will put them there
            def modLibraries = rootProject.files(modZip
              .matching { include 'lib/*.jar' }
              .collect { f -> return "$modDir/lib/$f.name" }
            ) { builtBy installModules }

            // Configure install modules to install this zip
            installModules {
              from modZip
              into rootProject.file(modDir)
            }

            dependencies.explodedModules rootProject.files(modDir)
            dependencies.explodedModules modLibraries
          } // end artifacts .each

      } // end dependencies .each

      sourceSets {
        all {
          dependentProjects.each { dependentProject ->
            compileClasspath += dependentProject.configurations.explodedModules
          }

          compileClasspath -= configurations.modules
          compileClasspath += configurations.explodedModules
        }
      } // end sourceSets

    } // end .with
  }

  private class VertxModulesPluginConvention {
    private Project project

    def dependentProjects = []

    VertxModulesPluginConvention(Project project) {
      this.project = project
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
      // Force zip artifact, in case jar exists
      return "$group:$name:$version@zip"
    }
  }

}
