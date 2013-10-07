package com.darylteo.vertx.gradle.tasks

import groovy.json.JsonOutput

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

class GenerateModJson extends DefaultTask {
  def destinationDir = { "${project.buildDir}/conf" }

  public GenerateModJson() {
    project.afterEvaluate {
      def dir = project.file(this.destinationDir)
      outputs.file "$dir/mod.json"
    }
  }

  @TaskAction
  def run() {
    project.with {
      def modjson = file("${this.destinationDir}/mod.json")
      modjson.mkdirs()
      modjson.delete()

      def module = project.vertx.module

      def data = [main : module.main]
      modjson << JsonOutput.toJson(data)
    }
  }

  public File getDestinationDir() {
    return project.file(destinationDir)
  }
}
