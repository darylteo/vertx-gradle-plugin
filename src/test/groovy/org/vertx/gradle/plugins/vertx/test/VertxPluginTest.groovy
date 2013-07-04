package org.vertx.gradle.plugins.vertx.test;
import static org.junit.Assert.*

import org.gradle.api.*
import org.gradle.testfixtures.*
import org.junit.*

import com.darylteo.gradle.vertx.VertxProjectPlugin

import groovyx.net.http.*

class VertxPluginTest {
  static builder
  static root, runnable, nonrunnable, library

  @BeforeClass
  public static void beforeClass() {
    /* Simulating build lifecycle */
    this.builder = ProjectBuilder.builder()

    // creating project hierarchy
    root = createProject('src/test/resources/rootproject', 'root', null)
    runnable = createProject('src/test/resources/rootproject/runnable', 'runnable', root)
    nonrunnable = createProject('src/test/resources/rootproject/nonrunnable', 'nonrunnable', root)
    library = createProject('src/test/resources/rootproject/library', 'library', root)

    // Clean up some stuff that are creating during tests
    root.delete 'mods'
    runnable.delete 'build'
    nonrunnable.delete 'build'
    library.delete 'build'

    root.delete "${System.getProperty('user.home')}/.m2/repository/com/darylteo/plugin-module1"
    root.delete "${System.getProperty('user.home')}/.m2/repository/com/darylteo/plugin-module2"
    root.delete "${System.getProperty('user.home')}/.m2/repository/com/darylteo/library"

    def http = new HTTPBuilder('https://oss.sonatype.org/content/repositories/snapshots/com/darylteo/plugin-module1')
    http.auth.basic(root.sonatypeUsername, root.sonatypePassword)

    http.request(Method.DELETE, ContentType.ANY) { req ->
      response.success = { resp, reader ->
        println 'Deleted plugin snapshot'
      }
      response.failure = {
        // no op
      }
    }

    applyBuildScript(root)
    evaluateProject(root)
  }

  @Test
  public void testRunnable() {
    runnable.with {
      assertNotNull('VertxProjectPlugin not applied', plugins.getPlugin(VertxProjectPlugin))

      assertTrue('Project should be module', isModule)
      assertTrue('Module should be runnable', isRunnable)

      assertNotNull('Runnable should have copy task', tasks.findByPath('copyMod'))
      assertNotNull('Runnable should have zip task', tasks.findByPath('modZip'))
      assertNotNull('Runnable should have run task', tasks.findByPath('run-plugin-module1'))
    }
  }

  @Test
  public void testNonRunnable() {
    nonrunnable.with {
      assertNotNull('VertxProjectPlugin not applied', plugins.getPlugin(VertxProjectPlugin))

      assertTrue('Project should be module', isModule)
      assertFalse('Module should not be runnable', isRunnable)

      assertNotNull('Nonrunnable should have copy task', tasks.findByPath('copyMod'))
      assertNotNull('Nonrunnable should have zip task', tasks.findByPath('modZip'))
      assertNull('Nonrunnable should not have run task for runnable module', tasks.findByPath('run-module1'))
      assertNull('Nonrunnable should not have run task for itself', tasks.findByPath('run-plugin-module2'))
    }
  }

  @Test
  public void testLibrary() {
    library.with {
      assertNotNull('VertxProjectPlugin not applied', plugins.getPlugin(VertxProjectPlugin))

      assertFalse('Project should not be module', isModule)
      assertFalse('Since not module should definitely not be runnable', isRunnable)

      assertNull('Library should not have copy task', tasks.findByPath('copyMod'))
      assertNull('Library should not have zip task', tasks.findByPath('modZip'))
      assertNull('Library should not have run task for runnable module', tasks.findByPath('run-module1'))
    }
  }

  @Test
  public void testModuleAssembly() {
    executeTask(runnable.modZip)

    assertTrue('mods directory not created', root.file('mods').isDirectory())
    assertTrue('module directory not copied into mods directory', root.file("mods/${runnable.moduleName}").isDirectory())

    assertTrue('lib directory not created', root.file("mods/${runnable.moduleName}/lib").isDirectory())
    assertFalse('nonrunnable should not be copied into lib', root.file("mods/${runnable.moduleName}/lib/nonrunnable-1.0.0-SNAPSHOT.jar").isFile())
    assertTrue('library jar not copied into lib', root.file("mods/${runnable.moduleName}/lib/library-1.0.0-SNAPSHOT.jar").isFile())

    assertTrue('zip not created', runnable.file("${runnable.buildDir}/libs/${runnable.artifact}-${runnable.version}.zip").exists())
  }

  @Test
  public void testMavenInstall() {
    executeTask(library.install)
    executeTask(nonrunnable.install)
    executeTask(runnable.install)

    def repo = "${System.getProperty('user.home')}/.m2/repository/com/darylteo/${runnable.artifact}"

    assertTrue('Create runnable repo directory', root.file("$repo").isDirectory())
    assertTrue('Create runnable repo version', root.file("$repo/${runnable.version}").isDirectory())
    assertTrue('Create runnable zip', root.file("$repo/${runnable.version}/${runnable.artifact}-${runnable.version}.zip").isFile())
    assertFalse('Runnable Module should not have jar', root.file("$repo/${runnable.version}/${runnable.artifact}-${runnable.version}.jar").isFile())

    repo = "${System.getProperty('user.home')}/.m2/repository/com/darylteo/${nonrunnable.artifact}"

    assertTrue('Create nonrunnable repo directory', root.file("$repo").isDirectory())
    assertTrue('Create nonrunnable repo version', root.file("$repo/${nonrunnable.version}").isDirectory())
    assertTrue('Create nonrunnable zip', root.file("$repo/${nonrunnable.version}/${nonrunnable.artifact}-${nonrunnable.version}.zip").isFile())
    assertTrue('Nonrunnable Module with produceJar should have jar', root.file("$repo/${nonrunnable.version}/${nonrunnable.artifact}-${nonrunnable.version}.jar").isFile())

    repo = "${System.getProperty('user.home')}/.m2/repository/com/darylteo/${library.artifact}"

    assertTrue('Create library repo directory', root.file("$repo").isDirectory())
    assertTrue('Create library repo version', root.file("$repo/${library.version}").isDirectory())
    assertFalse('Library should not have zip', root.file("$repo/${library.version}/${library.artifact}-${library.version}.zip").isFile())
    assertTrue('Library should always have jar', root.file("$repo/${library.version}/${library.artifact}-${library.version}.jar").isFile())
  }

  @Test
  public void testMavenUpload() {
    executeTask(runnable.uploadArchives)

    def http = new HTTPBuilder("https://oss.sonatype.org/content/repositories/snapshots/com/darylteo/plugin-module1/${runnable.version}/${runnable.artifact}-${runnable.version}.zip")

    http.request(Method.GET, ContentType.ANY) { req ->
      response.success = { resp, reader ->
      }
      response.failure = {
        fail('Zip was not uploaded to maven')
      }
    }
  }

  static createProject(String path, String name, Project parent) {
    def projectDir = new File(path)
    def project = builder.withProjectDir(projectDir).withParent(parent).withName(name).build()
    loadProperties(project, "${System.getProperty('user.home')}/.gradle/gradle.properties")
    loadProperties(project)

    return project
  }

  static loadProperties(Project project) {
    loadProperties(project, 'gradle.properties')
  }

  static loadProperties(Project project, String filename) {
    def file = project.file(filename)

    loadProperties(project, file)
  }

  static loadProperties(Project project, File file) {
    if (!file.canRead()) {
      return
    }

    file.withReader { def reader ->
      def props = new Properties()
      props.load(reader)

      props.each { k,v ->
        if (project.hasProperty(k)){
          project[k] = v
        } else {
          project.ext[k] = v
        }
      }
    }
  }

  static applyBuildScript(Project project) {
    def file = project.file('build.gradle')

    if (!file.canRead()) {
      return
    }

    project.apply from: file

    project.childProjects.each { def name, child ->
      applyBuildScript(child)
    }
  }

  static evaluateProject(Project project) {
    project.evaluate()

    project.childProjects.each { def name, child ->
      evaluateProject(child)
    }
  }

  static executeTask(Project project, String taskName) {
    Task task = project.tasks.findByPath(taskName)

    return task ? executeTask(task) : null
  }

  static executeTask(Task task) {
    for (t in task.taskDependencies.getDependencies(task)) {
      executeTask(t)
    }

    task.execute()
  }
}