package com.darylteo.gradle.plugins.vertx.tasks

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

class VertxRunTask extends DefaultTask {
  private List<String> modules = [];

  void deploy(String notation) {
    modules += notation;
  }

  @TaskAction
  def run(){
    modules.each { it -> println it }
  }
}
