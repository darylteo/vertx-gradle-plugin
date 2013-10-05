package com.darylteo.vertx.gradle.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction;

class RunVertx extends DefaultTask {
  def version

  def version(String version) {
    this.version = version
  }

  @TaskAction
  def run() {
  }
}
