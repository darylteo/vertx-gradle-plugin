package com.darylteo.gradle.watcher

import org.gradle.api.Project

class WatcherConfiguration {
  private ThreadPoolDirectoryWatchService
  
  final Project project
  WatcherConfiguration(Project project) {
    this.project = project
  }
}
