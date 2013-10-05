package com.darylteo.vertx.gradle.configuration


class ProjectConfiguration {
  private def _info = [] 

  def getInfo() {
    return _info
  }
  def info(Closure closure) {
    _info += closure
  }
}
