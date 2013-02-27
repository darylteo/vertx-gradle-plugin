package org.vertx.gradle

import org.gradle.api.*

class VertxPlugin implements Plugin<Project> {
  void apply(Project project) {
    project.ext.vertx = true
  }
}