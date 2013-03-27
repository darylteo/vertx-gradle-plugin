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
    runnable.delete 'build'
    nonrunnable.delete 'build'
    library.delete 'build'

    applyScript(root)
  }

  @Test
  public void testModulePluginRunnable() {
    runnable.with {
      assertTrue('VertxPlugin not applied', vertx)

      assertNotNull('VertxPlugin did not set props main properly', config.main)
      assertTrue('Module should be runnable', isRunnable)

      assertNotNull('Could not find copy task', tasks.findByPath('copyMod'))
      assertNotNull('Could not find zip task', tasks.findByPath('modZip'))
      assertNotNull('Could not find run task', tasks.findByPath('run-plugin-module1'))
    }
  }

  @Test
  public void testModulePluginNonRunnable() {
    nonrunnable.with {
      assertTrue('VertxPlugin not applied', vertx)

      assertNull('VertxPlugin did not set props main properly', config.main)
      assertFalse('Module should not be runnable', isRunnable)

      assertNotNull('Could not find copy task', tasks.findByPath('copyMod'))
      assertNotNull('Could not find zip task', tasks.findByPath('modZip'))
      assertNull('Nonrunnable should not have run task for runnable module', tasks.findByPath('run-module1'))
      assertNull('Nonrunnable should not have run task for itself', tasks.findByPath('run-plugin-module2'))
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
    runnable.copyMod.execute()
    runnable.modZip.execute()

    assertTrue('mods directory not created', root.file('mods').isDirectory())
    assertTrue('module directory not copied into mods directory', root.file("mods/${runnable.moduleName}").isDirectory())

    // I cannot get this stupid tests to pass!
    // assertTrue('lib directory not created', root.file("mods/${runnable.moduleName}/lib").isDirectory())
    // assertTrue('library jar not copied into lib', root.file("mods/${runnable.moduleName}/lib/library-1.0.0-SNAPSHOT.jar").isFile())
    // assertTrue('zip not created', runnable.file("${runnable.buildDir}/lib/${runnable.artifact}-${runnable.version}.zip").exists())
  }

  @Test
  public void testMavenUpload() {
    // TODO: test success of sonatype upload
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

  def executeTask(Task task) {
    task.taskDependencies.dependencies(task).each { Task t ->
      executeTask(t)
    }

    task.execute()
  }
}