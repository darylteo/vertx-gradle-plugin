package com.darylteo.vertx.gradle.tasks

import groovy.json.JsonOutput

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

class GenerateModJson extends DefaultTask {
  def destinationDir = { "${project.buildDir}/conf" }

  public GenerateModJson() {
    project.afterEvaluate {
      inputs.property 'config', project.vertx.config
      inputs.property 'info', project.vertx.info

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

      // base configuration
      def data = [:]
      data << vertx.config

      // other info      
      data.developers = vertx.info.developers[0].developer.collect { it.name[0].value() }
      data.licenses = vertx.info.licenses[0].license.collect { it.name[0].value() }

      modjson << JsonOutput.toJson(data)
    }
  }

  public File getDestinationDir() {
    return project.file(destinationDir)
  }
}
