package com.darylteo.vertx.gradle.deployments

import com.darylteo.vertx.gradle.configuration.ClusterConfiguration

class PlatformConfiguration {
  def args = []
  def classpath = []

  def conf
  def instances
  def cluster
  def version

  def args(Iterable<?> values) {
    this.args.addAll(values)
  }

  def args(Object... values) {
    this.args.addAll(values)
  }

  def version(String version) {
    this.version = version
  }

  // auxiliary parameters
  def conf(def file) {
    this.conf = file.toString()
  }

  def instances(int instances) {
    this.args('-instances', "$instances")
  }

  def cluster(Closure closure) {
    if (cluster == null) {
      cluster = new ClusterConfiguration()
    }

    closure.delegate = cluster
    closure.resolveStrategy = Closure.DELEGATE_FIRST
    closure.call(cluster)
  }

  def cluster(ClusterConfiguration clusterConfig) {
    cluster = clusterConfig;
  }

  def cluster(String hostname = null, Integer port = null) {
    this.args('-cluster')
    if (hostname) {
      this.args('-cluster-host', hostname)
    }
    if (port) {
      this.args('-cluster-port', "$port")
    }
  }

  def classpath(def paths) {
    classpath += paths
  }

  def getEffectiveArgs() {
    def result = args
    if (conf != null) {
      result += ['-conf', conf]
    }
  }
}
