package com.darylteo.vertx.gradle.configuration

import org.gradle.api.Project

class PlatformConfiguration {
  Project project
  String version
  String toolsVersion
  def lang

  PlatformConfiguration(Project project) {
    this.project = project
  }

  def version(String version) {
    this.setVersion(version)
  }

  def setVersion(String version) {
    this.version = version
  }

  def tools(String version) {
    this.toolsVersion = version
  }

  def lang(String language) {
    this.lang = language
  }
}
