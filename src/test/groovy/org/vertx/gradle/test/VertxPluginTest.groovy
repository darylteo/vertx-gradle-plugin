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

    loadProperties(project)
    assertNotNull('Gradle Properties not loaded', project.vertxVersion)

    project.apply plugin: VertxPlugin
    assertTrue('VertxPlugin not applied', project.vertx)
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