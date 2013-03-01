import org.gradle.api.*
import org.gradle.testfixtures.*;

import org.junit.*
import static org.junit.Assert.*

import org.vertx.gradle.*

class VertxModulePluginTest {

  def builder

  @Before
  public void before(){
    this.builder = ProjectBuilder.builder()
  }

  @Test
  public void testModulePluginApplied() {
    File rootDir = new File('src/test/resources/rootproject')
    Project root = builder.withProjectDir(rootDir).withName('root').build()
    loadProperties(root)

    File childDir = new File('src/test/resources/rootproject/runnable')
    Project child = builder.withProjectDir(childDir).withParent(root).withName('runnable').build()

    root.apply plugin: VertxPlugin
    child.apply plugin: VertxModulePlugin

    assertTrue('VertxModulePlugin not applied', child.vertx)

    assertNotNull('VertxModulePlugin did not set props', child.props)
    assertTrue('VertxModulePlugin did not set props of right type', child.props instanceof VertxModuleProperties)
  }

  @Test
  public void testModulePluginRunnable() {
    File rootDir = new File('src/test/resources/rootproject')
    Project root = builder.withProjectDir(rootDir).withName('root').build()
    loadProperties(root)

    File childDir = new File('src/test/resources/rootproject/runnable')
    Project child = builder.withProjectDir(childDir).withParent(root).withName('runnable').build()

    root.apply plugin: VertxPlugin
    child.apply plugin: VertxModulePlugin

    assertEquals('VertxModulePlugin did not set props main properly', child.props.main, 'app.js')
    assertTrue('Module should be runnable', child.props.runnable)
  }

  @Test
  public void testModulePluginNonRunnable() {
    File rootDir = new File('src/test/resources/rootproject')
    Project root = builder.withProjectDir(rootDir).withName('root').build()
    loadProperties(root)

    File childDir = new File('src/test/resources/rootproject/nonrunnable')
    Project child = builder.withProjectDir(childDir).withParent(root).withName('runnable').build()

    root.apply plugin: VertxPlugin
    child.apply plugin: VertxModulePlugin

    assertNull('VertxModulePlugin did not set props main properly', child.props.main)
    assertFalse('Module should not be runnable', child.props.runnable)
  }

  def loadProperties(Project project){
  project.file('gradle.properties').withReader { def reader ->
    def props = new Properties()
    props.load(reader)

    props.each { k,v ->
      project.ext[k] = v
    }
  }
}
}