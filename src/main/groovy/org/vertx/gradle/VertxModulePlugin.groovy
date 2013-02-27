package org.vertx.gradle

import org.gradle.api.*

import groovy.json.*

import java.nio.file.Files

class VertxModulePlugin implements Plugin<Project> {
  void apply(Project project) {
    project.ext.vertx = true

    project.file('src/main/resources/mod.json').withReader { def reader ->
      def modjson = new JsonSlurper().parse(reader)

      project.extensions.create('props', VertxModuleProperties)
      project.props.main = modjson.main
    }
  }

  void setupTasks(Project project){
    project.task("run-${project.name}") << {
      println project.name
    }
  }

}

class VertxModuleProperties {
  String main

  boolean isRunnable() {
    return this.main != null
  }
}