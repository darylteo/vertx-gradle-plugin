package com.darylteo.vertx.gradle.plugins

import com.darylteo.gradle.watcher.tasks.WatcherTask
import com.darylteo.vertx.gradle.configuration.ModuleConfiguration
import com.darylteo.vertx.gradle.configuration.PlatformConfiguration
import com.darylteo.vertx.gradle.configuration.ProjectConfiguration
import com.darylteo.vertx.gradle.deployments.Deployment
import com.darylteo.vertx.gradle.tasks.GenerateDeploymentConfig
import com.darylteo.vertx.gradle.tasks.GenerateModJson
import com.darylteo.vertx.gradle.tasks.RunVertx
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.ProjectDependency
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.Sync
import org.gradle.api.tasks.bundling.Zip

public class VertxPlugin implements Plugin<Project> {
  public void apply(Project project) {
    applyPlugins project
    applyExtensions project
    addDependencies project
    addTasks project
  }

  private void applyPlugins(Project project) {
    project.with {
      apply plugin: 'java'
      apply plugin: 'watcher'
    }
  }

  private void addDependencies(Project project) {
    project.with {
      repositories { mavenCentral() }

      configurations {
        vertxAll

        vertxCore
        vertxLang
        vertxTest
        vertxIncludes

        vertxAll.extendsFrom vertxCore
        vertxAll.extendsFrom vertxLang
        vertxAll.extendsFrom vertxTest
        vertxAll.extendsFrom vertxIncludes

        provided.extendsFrom vertxAll
        compile.extendsFrom provided
      }

      afterEvaluate {
        // validations
        if (vertx?.platform?.version == null) {
          println('Vert.x Platform Version not set. e.g. "vertx.platform.version = \'2.1\'".')
        } else {
          def vertxGroup = 'io.vertx'

          dependencies {
            // core and lang modules
            vertxCore("${vertxGroup}:vertx-platform:${vertx.platform.version}")

            if (vertx.platform.lang != null) {
              def module = getModuleForLang(project, vertx.platform.lang)
              if (!module) {
                println("Unsupported Language: ${vertx.platform.lang}")
              } else {
                vertxLang(module)
              }
            }

            if (vertx.platform.toolsVersion) {
              vertxTest("${vertxGroup}:testtools:${vertx.platform.toolsVersion}")
            }

            // includes
            vertx.config?.map?.includes?.collect { String dep ->
              dep.replace('~', ':')
            }.each { dep -> vertxIncludes dep }
          }
        }
      }
    }
  }

  private String getModuleForLang(Project project, String lang) {
    // load langs.properties and get the correct version if a version was not specified
    def cp = (project.configurations.vertxCore.files + project.file('conf'))
      .collect({ file ->
      // File.toURL() is bugged. Use toURI().toURL(). See Javadoc
      file.toURI().toURL()
    }).toArray(new URL[0])

    def cl = new URLClassLoader(cp)

    def props = new Properties()

    [
      'default-langs.properties',
      'langs.properties'
    ].each { file ->
      cl.getResourceAsStream(file)?.withReader { r ->
        props.load(r)
      }
    }

    // vertx modules are defined in a different format.
    def module = props.getProperty(lang)?.split(":", -1)?.getAt(0)?.replace('~', ':')
    return module
  }

  private void applyExtensions(Project project) {
    project.extensions.create 'vertx', ProjectConfiguration, project

    project.vertx.extensions.create 'platform', PlatformConfiguration, project
    project.vertx.extensions.create 'config', ModuleConfiguration, project
    project.vertx.extensions.deployments = project.container Deployment.class
  }

  private void addTasks(Project project) {
    addArchiveTasks project
    addRunTasks project
  }

  private void addArchiveTasks(Project project) {
    project.with {
      // archive tasks
      task('assembleVertx', type: Sync) {
      }

      task("dummyMod") {
        // only work this if the target dir does not exist
        onlyIf { !compileJava.didWork && !project.vertx.moduleDir.exists() }
        mustRunAfter compileJava

        doLast {
          def modDir = project.vertx.moduleDir
          modDir.mkdirs()

          project.file("$modDir/mod.json").withWriter { writer ->
            writer << '{"main":"Main.java","auto-redeploy":true}'
          }

          project.file("$modDir/Main.java").withWriter { writer ->
            writer << '''\
import org.vertx.java.platform.Verticle;
public class Main extends Verticle {}\
            '''
          }
        }
      }

      task('copyMod', type: Sync) {
        into { project.vertx.moduleDir }
        from assembleVertx
      }

      task('generateModJson', type: GenerateModJson) {}

      // create the watcher task
      task('__watch', type: WatcherTask) {
        // flags
        block = false
        runImmediately = true

        includes = ['src/**']
        tasks = ['dummyMod', 'copyMod']
      }

      task('modZip', type: Zip) {
        group = 'Vertx'
        classifier = 'mod'
        from assembleVertx
      }

      afterEvaluate {
        // configure the test task with system variables
        test {
          systemProperty 'vertx.modulename', project.vertx.vertxName
          systemProperty 'vertx.mods', rootProject.file('build/mods');

          dependsOn copyMod
        }

        assembleVertx {
          def sourceSets = sourceSets.matching({ it.name != SourceSet.TEST_SOURCE_SET_NAME })

          into "$buildDir/mod"
          from sourceSets*.output
          from generateModJson

          into('lib') {
            from configurations.compile - configurations.provided
          }

          dependsOn generateModJson
          dependsOn sourceSets*.classesTaskName
        }

        // runtime configuration extends from compile configuration
        // so let's grab all vertx projects and link their copyMod tasks
        def deployedProjects = configurations.runtime.dependencies
          .withType(ProjectDependency.class)
          .collect { dep -> dep.dependencyProject }

        deployedProjects.each { module ->
          evaluationDependsOn(module.path)

          if (module.plugins.hasPlugin(VertxPlugin.class)) {
            tasks.copyMod.dependsOn module.tasks.copyMod
            tasks.__watch.dependsOn module.tasks.__watch
          }
        }
      }
    }
  }

  private void addRunTasks(Project project) {
    project.with {
      // configure the run/debug tasks
      vertx.deployments.whenObjectAdded { Deployment dep ->
        // add tasks for deployment
        def name = dep.name.capitalize()

        def configTask = task("generate${name}Config", type: GenerateDeploymentConfig) {
          deployment = dep
        }

        def runTask = task("run$name", type: RunVertx) {
          debug = false
        }
        def debugTask = task("debug$name", type: RunVertx) {
          debug = true
        }

        [runTask, debugTask]*.configure {
          deployment dep
          configFile { configTask.outputFile }

          dependsOn configTask
        }

        afterEvaluate {
          // make this project the default module target if it was not specified
          def module = dep.deploy?.module ?: project

          if (module instanceof Project) {
            if (module.vertx.config.map.'auto-redeploy') {
              module.tasks.compileJava.options.failOnError = false

              runTask.dependsOn module.dummyMod, module.tasks.__watch
              debugTask.dependsOn module.dummyMod, module.tasks.__watch
            } else {
              runTask.dependsOn module.tasks.copyMod
              debugTask.dependsOn module.tasks.copyMod
            }
          } else {
            // since this is an external module I don't see a use case where you would want to
            // debug the module
            debugTask.enabled = false
          }

          if (!dep.platform.version) {
            dep.platform.version = vertx.platform.version
          }
        }
      }

      vertx.deployments.whenObjectRemoved { Deployment dep ->
        def name = dep.name.capitalize()
        tasks.removeAll tasks."run$name", tasks."debug$name"
      }

      vertx.deployments {
        mod {
          deploy project
        }
      }
    }
  }

}