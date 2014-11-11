package com.darylteo.vertx.gradle.deployments;

import com.darylteo.vertx.gradle.configuration.ClusterConfiguration;
import groovy.lang.Closure;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class PlatformConfiguration {
  private List<String> args = new ArrayList<String>();
  private List<String> classpath = new ArrayList<String>();
  private File conf;
  private int instances;
  private ClusterConfiguration cluster;
  private String version;

  public List<String> getArgs() {
    return args;
  }

  public void setArgs(List<String> args) {
    this.args = args;
  }

  public List<String> getClasspath() {
    return classpath;
  }

  public void setClasspath(List<String> classpath) {
    this.classpath = classpath;
  }

  public File getConf() {
    return conf;
  }

  public void setConf(File conf) {
    this.conf = conf;
  }

  public int getInstances() {
    return instances;
  }

  public void setInstances(int instances) {
    this.instances = instances;
  }

  public ClusterConfiguration getCluster() {
    return cluster;
  }

  public void setCluster(ClusterConfiguration cluster) {
    this.cluster = cluster;
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public void args(Collection<?> values) {
    for (Object obj : values) {
      this.args.add(obj.toString());
    }
  }

  public void args(Object... values) {
    for (Object obj : values) {
      this.args.add(obj.toString());
    }

  }

  public void version(String version) {
    this.version = version;
  }

  public void conf(File file) {
    this.conf = file;
  }

  public void instances(int instances) {
    this.args("-instances", instances);
  }

  public void cluster(Closure closure) {
    closure.setDelegate(cluster);
    closure.setResolveStrategy(Closure.DELEGATE_FIRST);
    closure.call(cluster);
  }

  public void cluster(ClusterConfiguration clusterConfig) {
    cluster = clusterConfig;
  }

  public void cluster(String hostname, Integer port) {
    this.args("-cluster");
    if (hostname != null) {
      this.args("-cluster-host", hostname);
    }

    if (port != null) {
      this.args("-cluster-port", String.valueOf(port));
    }

  }

  public void cluster(String hostname) {
    cluster(hostname, null);
  }

  public void cluster() {
    cluster(null, null);
  }

  public List<String> getEffectiveArgs() {
    List<String> result = new ArrayList<>(this.args);

    if (conf != null) {
      result.addAll(Arrays.asList("-conf", conf.toString()));
    }

    return result;
  }

}
