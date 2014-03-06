package com.darylteo.vertx.gradle.configuration

import org.gradle.api.Project

class ProjectConfiguration {
  final Project project
  public ProjectConfiguration(Project project) {
    this.project = project
  }

  // Module Information
  final Node _info = new Node(null, "info")

  public NodeList getInfo() {
    return _info.children()
  }

  public void info(Closure closure) {
    // hack for appending closure to child nodes
    Node root = new Node(null, "temp")
    Node empty = new Node(root,"empty")

    closure.resolveStrategy = Closure.DELEGATE_FIRST

    // all top level children must be unique
    empty + closure // append in front
    root.remove(empty)

    // merge top level nodes into _info
    root.children().each { Node section ->
      def name = section.name()
      def list = _info.get(name)

      if(list[0]){
        section.children().each { def element ->
          if(element instanceof Node) {
            list[0].append element
          } else {
            list[0].setValue(element.toString())
          }
        }
      } else {
        _info.append section
      }
    }
  }

  public String getVertxName() {
    def group = project.group ?: 'group'
    def name = project.name
    def version = project.version

    return "$group~$name~$version"
  }

  public String getMavenName() {
    def group = project.group
    def name = project.name
    def version = project.version

    return "$group:$name:$version"
  }
}
