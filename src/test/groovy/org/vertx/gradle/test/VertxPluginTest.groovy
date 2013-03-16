import org.gradle.api.*
import org.gradle.testfixtures.*;

import org.junit.*
import static org.junit.Assert.*

import com.darylteo.gradle.*

class VertxPluginTest {
  def builder

  def root, runnable, nonrunnable, library

  @Before
  public void before(){
    this.builder = ProjectBuilder.builder()

    File projectDir = new File('src/test/resources/rootproject')
    root = builder.withProjectDir(projectDir).withName('root').build()

    projectDir = new File('src/test/resources/rootproject/runnable')
    runnable = builder.withProjectDir(projectDir).withParent(root).withName('runnable').build()

    projectDir = new File('src/test/resources/rootproject/nonrunnable')
    nonrunnable = builder.withProjectDir(projectDir).withParent(root).withName('nonrunnable').build()

    projectDir = new File('src/test/resources/rootproject/library')
    library = builder.withProjectDir(projectDir).withParent(root).withName('library').build()

    loadProperties(root)

    root.with {
      root.delete 'mods'
      root.delete "$buildDir/libs"
    }

    runnable.apply plugin: VertxPlugin
    nonrunnable.apply plugin: VertxPlugin
    library.apply plugin: VertxPlugin
  }

  @Test
  public void testModulePluginRunnable() {
    runnable.with {
      assertTrue('VertxPlugin not applied', vertx)

      assertNotNull('VertxPlugin did not set props main properly', config.main)
      assertTrue('Module should be runnable', isRunnable)

      assertNotNull(tasks.findByPath('copyMod'))
      assertNotNull(tasks.findByPath('modZip'))
      assertNotNull(tasks.findByPath('run-module1'))
    }
  }

  @Test
  public void testModulePluginNonRunnable() {
    nonrunnable.with {
      assertTrue('VertxPlugin not applied', vertx)

      assertNull('VertxPlugin did not set props main properly', config.main)
      assertFalse('Module should not be runnable', isRunnable)

      assertNotNull(tasks.findByPath('copyMod'))
      assertNotNull(tasks.findByPath('modZip'))
      assertNull(tasks.findByPath('run-module1'))
      assertNull(tasks.findByPath('run-module2'))
    }
  }

  @Test
  public void testClassLibrary() {
    // test make sure tasks not applied to this
    library.with {
      assertNull(tasks.findByPath('copyMod'))
      assertNull(tasks.findByPath('modZip'))
      assertNull(tasks.findByPath('run-module1'))
      assertNull(tasks.findByPath('run-module2'))
    }
  }

  @Test
  public void testModuleCopy() {
    runnable.tasks.copyMod.execute()

    assertTrue('mods directory not created', root.file('mods').isDirectory())
    assertTrue('module directory not copied into mods directory', root.file("mods/${runnable.moduleName}").isDirectory())
  }

  @Test
  public void testModuleZip() {
    runnable.tasks.modZip.execute()

    Thread.sleep(1000)

    assertTrue('zip not created', runnable.file("${runnable.buildDir}/libs/${runnable.artifact}-${runnable.version}.zip").exists())
  }

  @Test
  public void testBuildGradleApplied() {
    // If either of these fails, then that means the module.gradle script was not applied to the project
    assertTrue('module.gradle was not applied to runnable project', runnable.applied)
    assertTrue('module.gradle was not applied to nonrunnable project', nonrunnable.applied)
  }

  def loadProperties(Project project){
    project.file('gradle.properties').withReader { def reader ->
      def props = new Properties()
      props.load(reader)

      props.each { k,v ->
        if (project.hasProperty(k)){
          project[k] = v
        } else {
          project.ext[k] = v
        }
      }
    }
  }
}