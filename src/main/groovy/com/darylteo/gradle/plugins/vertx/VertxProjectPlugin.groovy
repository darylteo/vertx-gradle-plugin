package com.darylteo.gradle.plugins.vertx

import groovy.json.*

import org.gradle.api.*
import org.gradle.api.tasks.Sync
import org.gradle.plugins.ide.idea.IdeaPlugin

import com.darylteo.gradle.plugins.vertx.deployments.VertxDeployment
import com.darylteo.gradle.plugins.vertx.deployments.VertxDeploymentItem
import com.darylteo.gradle.plugins.vertx.handlers.VertxPropertiesHandler
import com.darylteo.gradle.plugins.vertx.tasks.VertxRunTask

/**
 * Plugin responsible for configuring a vertx enabled project
 *
 * @author Daryl Teo
 */
class VertxProjectPlugin implements Plugin<Project> {
  @Override
  public void apply(Project project) {
    project.convention.plugins.projectPlugin = new ProjectPluginConvention(project)

    // classloader hack : dependency cannot be loaded after buildscript is evaluated.
    //    PlatformLocator.factory.createPlatformManager()

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
      task('generateModJson') {
        def confdir = file("$buildDir/conf")
        def modjson = file("$confdir/mod.json")
        outputs.file modjson
        outputs.upToDateWhen { false }

        doLast {
          confdir.mkdirs()
          modjson.delete()
          modjson.createNewFile()

          modjson << JsonOutput.toJson(vertx.config)
        }
      }

      task('copyMod', dependsOn: [
        classes,
        generateModJson
      ], type: Sync) {
        group = 'vert.x'
        description = 'Assemble the module into the local mods directory'

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

      task('pullIncludes') << {
        println "Pulling in dependencies for module $moduleName. Please wait"
        new ProjectModuleInstaller(project).install()
      }

      // Required for test tasks
      test {
        dependsOn copyMod
        systemProperty 'vertx.modulename', moduleName
        systemProperty 'vertx.mods', copyMod.modsDir

        workingDir rootProject.projectDir
      }

      compileJava.dependsOn pullIncludes

      // Adding deployment tasks
      afterEvaluate {
        project.vertx?.deployments?.each { VertxDeployment dep ->
          task("run-${dep.name}", type: VertxRunTask) {
            deployment = dep
            dependsOn {
              dep.findAll({ VertxDeploymentItem module ->
                module.notation.startsWith(':')
              }).collect({ VertxDeploymentItem module ->
                project.project(module.notation).copyMod
              })
            }
          }
        }
      }

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

  private class ProjectModuleInstaller {
    private Project project

    ProjectModuleInstaller(Project project){
      this.project = project
    }

    def void install() {
      installModules(this.project.includes)
    }

    private def void installModules(def modules){
      if(modules instanceof String) {
        modules = modules.split("\\s*,\\s*")
      } else {
        modules = modules ?: []
      }

      modules.each { module ->
        println "Installing $module"
        try {
          installModule(module)
          println "$module pulled in successfully"

          this.project.rootProject.file("mods/$module/mod.json").withReader { reader->
            def json = new JsonSlurper().parse(reader)

            installModules(json.includes)
          }
        }catch(Exception e) {
          println "$module did not install successfully"
          e.printStackTrace()
        }
      }
    }

    private void installModule(String module) {
      //      def latch = new CountDownLatch(1)
      //      def result;
      //
      //      this.pm.installModule(module, new AsyncResultHandler<Void>() {
      //          public void handle(AsyncResult<Void> asyncResult) {
      //            result = asyncResult;
      //            latch.countDown();
      //          }
      //        })
      //
      //      latch.await(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
      //      if (!result.succeeded()) {
      //        if(!result.cause().message.contains("already installed")) {
      //          throw result.cause()
      //        }
      //      }
    }
  }
}
