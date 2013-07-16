package org.vertx.gradle.plugins.vertx.test;
import static org.junit.Assert.*

import org.gradle.api.*
import org.gradle.testfixtures.*
import org.junit.*

import com.darylteo.gradle.plugins.vertx.*

import groovyx.net.http.*

public class VertxPluginTest {
  static builder
  static root, runnable, nonrunnable, library

  @BeforeClass
  public static void beforeClass() {
    /* Simulating build lifecycle */
    this.builder = ProjectBuilder.builder()

    // creating project hierarchy
    root = createProject('testroot/simple', 'root', null)
  
    applyBuildScript(root)
    evaluateProject(root)
  }

  @Test
  public void test(){
    assertNotNull(root.vertx)
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