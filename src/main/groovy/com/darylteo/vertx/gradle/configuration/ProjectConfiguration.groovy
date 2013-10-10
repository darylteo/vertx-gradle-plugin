package com.darylteo.vertx.gradle.configuration

import groovy.xml.XmlUtil

class ProjectConfiguration {
  // all top level children must be unique
  final Node info = new Node(null, "info")

  public void info(Closure closure) {
    // hack for appending closure to child nodes
    Node root = new Node(null, "temp")
    Node empty = new Node(root,"empty")

    closure.resolveStrategy = Closure.DELEGATE_FIRST

    empty + closure // append in front
    root.remove(empty)

    println info

    // merge top level nodes into _info
    root.children().each { Node section ->
      def name = section.name()
      println name
      def list = info.get(name)

      println "Children of $name"
      println "Value: ${section.value()}"
      println list.isEmpty()
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

    println XmlUtil.serialize(info)
  }
}
