import org.gradle.api.*
import org.gradle.testfixtures.*;

import org.junit.*
import static org.junit.Assert.*

import org.vertx.gradle.*

class VertxPluginTest {

  @Test
  public void testVertxPluginApplied() {
    File testDir = new File('src/test/resources/rootproject')
    Project project = ProjectBuilder.builder().withProjectDir(testDir).build()
    project.apply plugin: VertxPlugin

    assertTrue('VertxPlugin not applied', project.vertx)
  }
}