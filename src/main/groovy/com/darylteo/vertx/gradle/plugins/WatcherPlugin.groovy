package com.darylteo.vertx.gradle.plugins

import org.gradle.api.Plugin
import org.gradle.api.Project

import com.darylteo.gradle.watcher.WatcherConfiguration

class WatcherPlugin implements Plugin<Project>{
  public void apply(Project project) {
    project.extensions.create 'watcher', WatcherConfiguration, project
  }
}
