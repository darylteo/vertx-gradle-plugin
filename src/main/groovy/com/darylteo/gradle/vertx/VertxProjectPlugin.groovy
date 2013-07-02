package com.darylteo.gradle.vertx

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
    project.convention.plugins.jsonProperties = new ModuleJsonConvention(project)

    //    loadModuleProperties(project)
    //    configureProject(project)

    project.beforeEvaluate {
      println "Before Evaluate"
      println project.vertx
      println project.vertx.version
    }
    //    project.afterEvaluate { addModuleTasks project }
  }

  private void configureProject(Project project) {
    println "Configuring $project"
    applyLanguagePlugins(project)

    project.with {
      // Adds IDE Tasks
      apply plugin: 'eclipse'
      apply plugin: 'idea'

      repositories {
        if (System.getenv('JENKINS_HOME') == null) {
          // We don't want to use mavenLocal when running on CI - mavenLocal is only useful in Gradle for
          // publishing artifacts locally for development purposes - maven local is also not threadsafe when there
          // are concurrent builds
          mavenLocal()
        }
        maven { url 'https://oss.sonatype.org/content/repositories/snapshots' }
        mavenCentral()
      }

      configurations {
        provided
        testCompile.extendsFrom provided
      }

      /* Module Configuration */
      dependencies {
        provided "io.vertx:vertx-core:${vertxVersion}"
        provided "io.vertx:vertx-platform:${vertxVersion}"

        testCompile "io.vertx:testtools:${toolsVersion}"
        testCompile "junit:junit:${junitVersion}"
      }

      // Configures test task if exists
      test {
        // Make sure tests are always run!
        outputs.upToDateWhen { false }
      }

      if (repotype == 'maven') {
        apply plugin: MavenProjectPlugin
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

  private void addModuleTasks(Project project){
    project.with {
      if (!isModule) { return }

      task('copyMod', dependsOn: 'classes', type: Copy, description: 'Assemble the module into the local mods directory') {
        def targetDir = rootProject.file("mods/$moduleName")

        into targetDir
        from compileJava
        from file('src/main/resources')

        // and then into module library directory
        into ('lib') {
          from configurations.compile.copy { def dependency ->
            // remove any project dependencies that are configured as modules
            if (dependency instanceof ProjectDependency) {
              return !dependency.dependencyProject.isModule
            } else {
              return true
            }
          }
        }
      }

      test { dependsOn copyMod }

      // Zipping up the module
      task('modZip', type: Zip, dependsOn: 'pullInDeps', description: 'Package the module .zip file') {
        group = 'vert.x'
        description = "Assembles a vert.x module"
        destinationDir = file("$buildDir/libs")
        archiveName = "${artifact}-${version}.zip"

        from copyMod
      }

      // Adding Tasks
      task('pullInDeps', dependsOn: 'copyMod', description: 'Pull in all the module dependencies for the module into the nested mods directory') << {
        if (pullInDeps == 'true') {
          def pm = PlatformLocator.factory.createPlatformManager()
          System.out.println("Pulling in dependencies for module $moduleName. Please wait...")
          pm.pullInDependencies(moduleName)
          System.out.println("Dependencies pulled into mods directory of module")
        }
      }

      // run task
      if (isRunnable) {
        task("run-${artifact}", dependsOn: 'copyMod', description: 'Run the module using all the build dependencies (not using installed vertx)') << {
          def mutex = new Object()

          ModuleClassLoader.reverseLoadOrder = false
          def pm = PlatformLocator.factory.createPlatformManager()
          pm.deployModule(moduleName, null, 1, new Handler<String>() {
              public void handle(String deploymentID) {
                if (!deploymentID){
                  println.error 'Verticle failed to deploy.'

                  // Wake the main thread
                  synchronized(mutex){
                    mutex.notify()
                  }
                  return
                }

                println "Verticle deployed! Deployment ID is: $deploymentID"
                println 'CTRL-C to stop server'
              }
            });

          // Waiting thread so that Verticle will continue running
          synchronized (mutex){
            mutex.wait()
          }
        }
      }

      jar { archiveName = "$artifact-${version}.jar" }

      task('javadocJar', type: Jar, dependsOn: javadoc) {
        classifier = 'javadoc'
        from "$buildDir/docs/javadoc"
      }

      task('sourcesJar', type: Jar) {
        from sourceSets.main.allSource
        classifier = 'sources'
      }

      artifacts { archives modZip }
      if (produceJar) {
        artifacts {
          archives javadocJar
          archives sourcesJar
        }
      } else {
        configurations.archives.artifacts.removeAll configurations.archives.artifacts.findAll { artifact ->
          jar.outputs.files.contains(artifact.file)
        }
      }

    } // end .with
  }

  private void loadGlobalProperties(Project project){
    project.props(new File("${System.getProperty('user.home')}/.gradle/gradle.properties"));
  }

  private void loadModuleProperties(Project project){
    // Load default properties
    project.defaults (
      group: 'my-company',
      artifact: project.name,
      version: '1.0.0',
      repotype: 'local',
      produceJar: false,

      isModule: false,

      release: false
      )

    if(!project.release) {
      project.version = "${project.version}-SNAPSHOT"
    }
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
    private VertxProperties properties

    ProjectPluginConvention(Project project){
      this.project = project
      this.properties = new VertxProperties()
    }

    String getModuleName() {
      return "${project.group}~${project.artifact}~${project.version}"
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

  private class ModuleJsonConvention {
    private Project project = null
    private Map properties = null

    ModuleJsonConvention(Project project) {
      this.project = project

      def file = project.file('src/main/resources/mod.json')

      if(file.isFile()) {
        file.withReader { def reader ->
          this.properties = new JsonSlurper().parse(reader)
        }
      }
    }

    boolean getIsModule() {
      return this.properties != null
    }

    boolean getIsRunnable() {
      return getIsModule() ? properties.main : false
    }
  }
}
