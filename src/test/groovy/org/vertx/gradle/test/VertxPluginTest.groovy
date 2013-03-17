import org.gradle.api.*
import org.gradle.testfixtures.*;

import org.junit.*
import static org.junit.Assert.*

class VertxPluginTest {
  def builder

  def root, runnable, nonrunnable, library

  @Before
  public void before(){
    /* Simulating build lifecycle */
    this.builder = ProjectBuilder.builder()

    // creating project hierarchy
    root = createProject('src/test/resources/rootproject', 'root', null)
    runnable = createProject('src/test/resources/rootproject/runnable', 'runnable', root)
    nonrunnable = createProject('src/test/resources/rootproject/nonrunnable', 'nonrunnable', root)
    library = createProject('src/test/resources/rootproject/library', 'library', root)

    // Clean up some stuff that are creating during tests
    root.delete 'mods'
    root.delete 'userHome'
    runnable.delete 'build'
    runnable.delete 'userHome'
    nonrunnable.delete 'build'
    runnable.delete 'userHome'
    library.delete 'build'
    library.delete 'userHome'

    // apply plugin from root build.gradle
    applyScript(root)

    // apply subproject scripts
    applyScript(runnable)
    applyScript(nonrunnable)
    applyScript(library)
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
  public void testModuleAssembly() {
    runnable.tasks.copyMod.execute()

    assertTrue('mods directory not created', root.file('mods').isDirectory())
    assertTrue('module directory not copied into mods directory', root.file("mods/${runnable.moduleName}").isDirectory())

    runnable.tasks.modZip.execute()
    assertTrue('zip not created', runnable.file("${runnable.buildDir}/libs/${runnable.artifact}-${runnable.version}.zip").exists())
  }

  def createProject(String path, String name, Project parent) {
    def projectDir = new File(path)
    def project = builder.withProjectDir(projectDir).withParent(parent).withName(name).build()
    loadProperties(project)

    return project
  }

  def loadProperties(Project project) {
    def file = project.file('gradle.properties')

    if (!file.canRead()) {
      return
    }

    file.withReader { def reader ->
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

  def applyScript(Project project) {
    def file = project.file('build.gradle')

    if (!file.canRead()) {
      return
    }

    project.apply from: file
  }
}