package com.darylteo.vertx.gradle.tests.integration;

import org.gradle.tooling.BuildLauncher;
import org.gradle.tooling.GradleConnector;
import org.gradle.tooling.ProjectConnection;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

/**
 * Created by dteo on 5/09/2014.
 */
public class PluginTests {
  GradleConnector connector;
  ProjectConnection connection;
  BuildLauncher launcher;

  @Before
  public void setup() {
    this.connector = GradleConnector.newConnector().forProjectDirectory(new File("testprojects"));
    this.connection = this.connector.connect();
    this.launcher = this.connection.newBuild();

//    String version = String.format("%s:%s:%s", "com.darylteo.vertx", "vertx-gradle-plugin", "0.2.0");

    this.launcher.withArguments("--no-daemon", "-PpluginClasspath=../build/classes/main");
  }

  @Test
  public void testTasks() {
    this.launcher.forTasks(":test1:tasks").run();
  }
}
