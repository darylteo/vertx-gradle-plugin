import org.gradle.api.*
import org.gradle.testfixtures.*;

import org.junit.*
import static org.junit.Assert.*

import org.vertx.gradle.*

class VertxPluginTest {
  def builder

  def root, runnable, nonrunnable

  @Before
  public void before(){
    this.builder = ProjectBuilder.builder()

    File projectDir = new File('src/test/resources/rootproject')
    root = builder.withProjectDir(projectDir).withName('root').build()

    projectDir = new File('src/test/resources/rootproject/runnable')
    runnable = builder.withProjectDir(projectDir).withParent(root).withName('runnable').build()

    projectDir = new File('src/test/resources/rootproject/nonrunnable')
    nonrunnable = builder.withProjectDir(projectDir).withParent(root).withName('nonrunnable').build()

    loadProperties(root)
    assertNotNull('Gradle Properties not loaded', root.vertxVersion)
    assertNotNull('Gradle Properties not loaded', runnable.vertxVersion)
    assertNotNull('Gradle Properties not loaded', nonrunnable.vertxVersion)

    root.apply plugin: VertxPlugin
  }

  @Test
  public void testVertxPluginApplied() {
    assertTrue('VertxPlugin not applied', root.vertx)
  }

  @Test
  public void testModulePluginRunnable() {
    assertTrue('VertxModulePlugin not applied', runnable.vertx)

    assertNotNull('VertxModulePlugin did not set props', runnable.props)
    assertTrue('VertxModulePlugin did not set props of right type', runnable.props instanceof VertxModuleProperties)

    assertEquals('VertxModulePlugin did not set props main properly', runnable.props.main, 'app.js')
    assertTrue('Module should be runnable', runnable.props.runnable)

    assertNotNull('Run Task was not created', runnable.tasks.getByPath('run-module1'))
  }

  @Test
  public void testModulePluginNonRunnable() {
    assertTrue('VertxModulePlugin not applied', nonrunnable.vertx)

    assertNotNull('VertxModulePlugin did not set props', nonrunnable.props)
    assertTrue('VertxModulePlugin did not set props of right type', nonrunnable.props instanceof VertxModuleProperties)

    assertNull('VertxModulePlugin did not set props main properly', nonrunnable.props.main)
    assertFalse('Module should not be runnable', nonrunnable.props.runnable)

    try {
      assertNull(nonrunnable.tasks.getByPath('run-module2'))
      fail('Run Task was created!')
    } catch(UnknownTaskException e) {
    }
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