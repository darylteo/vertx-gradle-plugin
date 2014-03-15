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

  def lang(String language, String version) {
    this.lang = [
      'name': language,
      'version': version
    ]
  }

  def methodMissing(String name, args) {
    if (args.size() == 1) {
      lang(name, args[0])
    } else {
      throw new MissingMethodException(name, this.class, args)
    }
  }
}
