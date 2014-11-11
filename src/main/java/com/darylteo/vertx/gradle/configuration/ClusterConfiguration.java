package com.darylteo.vertx.gradle.configuration;

/**
 * Created by dteo on 5/04/2014.
 */
public class ClusterConfiguration {
  public String getProvider() {
    return provider;
  }

  public void setProvider(String provider) {
    this.provider = provider;
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  private String provider;
  private String version;
}
