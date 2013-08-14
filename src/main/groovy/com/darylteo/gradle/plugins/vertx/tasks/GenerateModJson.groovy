package com.darylteo.gradle.plugins.vertx.tasks;

import groovy.json.JsonOutput

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

public class GenerateModJson extends DefaultTask {
  def confdir
  def modjson

  public GenerateModJson() {
    super()
    this.description = 'Generates mod.json for this project.'

    this.project.with {
      this.confdir = file("$buildDir/conf")
      this.modjson = file("$confdir/mod.json")
    }

    outputs.file this.modjson
    outputs.dir this.confdir

    outputs.upToDateWhen { false }
  }

  @TaskAction
  void run() {
    confdir.mkdirs()
    modjson.delete()
    modjson.createNewFile()

    modjson << JsonOutput.toJson(project.vertx?.config)
  }
}
