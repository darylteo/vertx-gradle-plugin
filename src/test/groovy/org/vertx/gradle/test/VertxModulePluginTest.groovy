import org.gradle.api.*
import org.gradle.testfixtures.*;

import org.junit.*
import static org.junit.Assert.*

import org.vertx.gradle.*

class VertxModulePluginTest {
  @Test
  public void testModulePluginApplied() {
    File testDir = new File('src/test/resources/rootproject/runnable')
    Project project = ProjectBuilder.builder().withProjectDir(testDir).build()
    project.apply plugin: VertxModulePlugin

    assertTrue('VertxModulePlugin not applied', project.vertx)

    assertNotNull('VertxModulePlugin did not set props', project.props)
    assertTrue('VertxModulePlugin did not set props', project.props instanceof VertxModuleProperties)
  }

  @Test
  public void testModulePluginRunnable() {
    File testDir = new File('src/test/resources/rootproject/runnable')
    Project project = ProjectBuilder.builder().withProjectDir(testDir).build()
    project.apply plugin: VertxModulePlugin

    assertEquals('VertxModulePlugin did not set props main properly', project.props.main, 'app.js')
    assertTrue('Module should be runnable', project.props.runnable)
  }

  @Test
  public void testModulePluginNonRunnable() {
    File testDir = new File('src/test/resources/rootproject/nonrunnable')
    Project project = ProjectBuilder.builder().withProjectDir(testDir).build()
    project.apply plugin: VertxModulePlugin

    assertNull('VertxModulePlugin did not set props main properly', project.props.main)
    assertFalse('Module should not be runnable', project.props.runnable)
  }
}