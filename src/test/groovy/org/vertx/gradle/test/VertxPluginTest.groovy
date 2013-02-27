import org.gradle.api.*
import org.gradle.testfixtures.*;

import org.junit.*
import static org.junit.Assert.*

import org.vertx.gradle.*

class VertxPluginTest {

  @Test
  public void testVertxPluginApplied() {
    Project project = ProjectBuilder.builder().build()
    project.apply plugin: VertxPlugin

    assertTrue('VertxPlugin not applied', project.vertx)
  }
}