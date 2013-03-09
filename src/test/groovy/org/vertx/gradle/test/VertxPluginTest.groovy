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
    root.delete 'mods'

    runnable.apply plugin: VertxPlugin
    nonrunnable.apply plugin: VertxPlugin
    library.apply plugin: VertxPlugin
  }

  @Test
  public void testModulePluginRunnable() {
    assertTrue('VertxPlugin not applied', runnable.vertx)

    assertTrue('isModule should be true', runnable.isModule)
    assertFalse('isLibrary should be false', runnable.isLibrary)

    assertEquals('VertxPlugin did not set props main properly', runnable.config.main, 'app.js')
    assertTrue('Module should be runnable', runnable.isRunnable)

    assertNotNull('Run Task was not created', runnable.tasks.getByPath('run-module1'))
  }

  @Test
  public void testModulePluginNonRunnable() {
    assertTrue('VertxPlugin not applied', nonrunnable.vertx)

    assertTrue('isModule should be true', nonrunnable.isModule)
    assertFalse('isLibrary should be false', nonrunnable.isLibrary)

    assertNull('VertxPlugin did not set props main properly', nonrunnable.config.main)
    assertFalse('Module should not be runnable', nonrunnable.isRunnable)

    try {
      assertNull(nonrunnable.tasks.getByPath('run-module2'))
      fail('Run Task was created!')
    } catch(UnknownTaskException e) {
    }
  }

  @Test
  public void testClassLibrary() {
    assertFalse('isModule should be false', library.isModule)
    assertTrue('isLibrary should be true', library.isLibrary)
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

    println runnable.file("${runnable.buildDir}/libs/${runnable.artifact}-${runnable.version}.zip").exists()
    // assertTrue('zip not created', runnable.file("${runnable.buildDir}/libs/${runnable.artifact}-${runnable.version}.zip").exists())
  }

  @Test
  public void testBuildGradleApplied() {
    assertTrue('module.gradle was not applied to runnable task', runnable.applied)
    assertTrue('module.gradle was not applied to nonrunnable task', nonrunnable.applied)
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