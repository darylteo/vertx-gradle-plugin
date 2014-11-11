package com.darylteo.vertx.gradle.tests.integration;

import com.darylteo.vertx.gradle.tests.utils.GradleBuildRunner;
import com.darylteo.vertx.gradle.tests.utils.RootProject;
import org.gradle.api.GradleException;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Created by dteo on 24/09/2014.
 */
@RunWith(GradleBuildRunner.class)
@RootProject("testprojects")
public class GradleTests {

  @Test
  public void testDeployments() {
  }

  @Test
  public void testLatestVersion() {
  }

  @Test
  public void testSpecificVersion() {
  }

  @Test
  public void testGenerateModConfig() {
  }

  @Test
  public void testGenerateModJson() {
  }

  @Test
  public void testGroovyModule() {
  }

  @Test(expected = GradleException.class)
  public void testUnknownLanguage() {
  }
}
