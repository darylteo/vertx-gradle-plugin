package org.vertx.gradle

import org.gradle.api.*

import groovy.json.*

import java.nio.file.Files

class VertxModulePlugin implements Plugin<Project> {
  void apply(Project project) {
    project.ext.vertx = true

    project.extensions.create('props', VertxModuleProperties)
    def props = loadModuleProperties(project)
    project.props.main = props.main

    setupTasks(project)
  }

  void setupTasks(Project project){
    project.task("run-${project.name}") << {
      println project.name
    }
  }

  def loadModuleProperties(Project project){
    project.file('src/main/resources/mod.json').withReader { def reader ->
      return new JsonSlurper().parse(reader)
    }
  }

}

class VertxModuleProperties {
  String main

  boolean isRunnable() {
    return this.main != null
  }
}