package com.darylteo.vertx.gradle.deployments

class PlatformConfiguration {
  List<?> args = []
  String version

  def args(Iterable<?> values) {
    args.addAll(values)
  }

  def args(Object ... values) {
    args.addAll(values)
  }

  def version(String version) {
    this.version = version
  }

  // auxiliary parameters
  def conf(String file) {
    this.args('-conf', file)
  }

  def instances(int instances) {
    this.args('-instances', "$instances")
  }

  def cluster(String hostname = null, int port = null) {
    this.args('-cluster')
    if(hostname) {
      this.args('-cluster-host', hostname)
    }
    if(port) {
      this.args('-cluster-port', "$port")
    }
  }
}
