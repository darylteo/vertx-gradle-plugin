package com.darylteo.vertx.gradle.tests.integration;

import org.gradle.tooling.BuildLauncher;
import org.gradle.tooling.GradleConnector;
import org.gradle.tooling.ProjectConnection;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.io.File;

/**
 * Created by dteo on 5/09/2014.
 */
public class Tests {

  GradleConnector connector;
  ProjectConnection connection;
  BuildLauncher launcher;

  @BeforeTest
  public void setup() {
    this.connector = GradleConnector.newConnector().forProjectDirectory(new File("testprojects"));
    this.connection = this.connector.connect();
    this.launcher = this.connection.newBuild();

    System.out.println(this.connector);
  }

  @Test
  public void test() {
    this.launcher.forTasks(":test1:tasks").run();
  }
}
