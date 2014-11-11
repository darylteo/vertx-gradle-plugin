package com.darylteo.vertx.gradle.configuration;

import org.gradle.api.Project;

public class VertxPlatformConfiguration {
  private Project project;
  private String version;
  private String toolsVersion;
  private String lang;

  public VertxPlatformConfiguration(Project project) {
    this.project = project;
  }

  public Project getProject() {
    return project;
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public String getToolsVersion() {
    return toolsVersion;
  }

  public void setToolsVersion(String toolsVersion) {
    this.toolsVersion = toolsVersion;
  }

  public String getLang() {
    return lang;
  }

  public void setLang(String lang) {
    this.lang = lang;
  }

  public void version(String version) {
    this.setVersion(version);
  }

  public void tools(String version) {
    this.toolsVersion = version;
  }

  public void lang(String language) {
    this.lang = language;
  }

}
