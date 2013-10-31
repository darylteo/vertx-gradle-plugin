package com.darylteo.vertx.gradle.configuration

import java.nio.file.Path

import org.gradle.api.Project

import com.darylteo.nio.DirectoryChangedSubscriber
import com.darylteo.nio.DirectoryWatcher
import com.darylteo.nio.ThreadPoolDirectoryWatchService

class ProjectConfiguration {
  // Module Information
  final Node info = new Node(null, "info")

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
      def list = info.get(name)

      if(list[0]){
        section.children().each { def element ->
          if(element instanceof Node) {
            list[0].append element
          } else {
            list[0].setValue(element.toString())
          }
        }
      } else {
        info.append section
      }
    }
  }

  public String getVertxName() {
    def group = info.groupId[0].value()
    def name = info.artifactId[0].value()
    def version = info.version[0].value()

    return "$group~$name~$version"
  }

  public String getMavenName() {
    def group = info.groupId[0].value()
    def name = info.artifactId[0].value()
    def version = info.version[0].value()

    return "$group:$name:$version"
  }
}
