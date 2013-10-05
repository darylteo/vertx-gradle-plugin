package com.darylteo.vertx.gradle.configuration

import org.gradle.api.Project

class ModuleInformation {
  private NodeList _developers
  private NodeList _licenses

  ModuleInformation(Project project) {
    _developers = new NodeList()
    _licenses = new NodeList()
  }

  def developers(Closure closure) {
    _developers + closure
  }

  def getDevelopers() {
    return _developers
  }

  def licenses(Closure closure) {
    _licenses + closure
  }

  def getLicenses() {
    return _licenses
  }
}

