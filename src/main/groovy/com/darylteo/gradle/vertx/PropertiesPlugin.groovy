import java.io.File;
import java.util.Map;

import org.gradle.api.*

class PropertiesPlugin implements Plugin<Project> {

  @Override
  public void apply(Project project) {
    project.convention.plugins.properties = new PropertiesPluginConvention(project)
  }

  private class PropertiesPluginConvention {
    def Project project

    PropertiesPluginConvention(Project project) {
      this.project = project
    }

    def defaults(Map map){
      map.each { def k,v ->
        if (!project.hasProperty(k) && !project.ext.hasProperty(k)){
          project.ext[k] = v
        } else if(project[k] == 'unspecified') {
          project[k] = v
        }
      }
    }

    def props(Map map) {
      map.each { k,v ->
        if (project.hasProperty(k)){
          project[k] = v
        } else {
          project.ext[k] = v
        }
      }
    }

    def props(File file) {
      if(file == null || !file.canRead()){
        return;
      }

      file.withReader { def reader ->
        def props = new Properties()
        props.load(reader)

        this.props(props)
      }
    }
  }
}