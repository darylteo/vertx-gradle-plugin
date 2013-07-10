package com.darylteo.gradle.plugins.vertx.resolvers;

import org.gradle.api.artifacts.ArtifactRepositoryContainer
import org.vertx.java.core.AsyncResult
import org.vertx.java.core.AsyncResultHandler
import org.vertx.java.platform.PlatformLocator

public class VertxModuleResolver {
  private String repo = 'http://repo1.maven.org/'

  public String getIdentifier(ArtifactRepositoryContainer repos, String notation) {
    def (group,name,version) = notation.split("~")
    return getIdentifier(repos, group,name,version)
  }

  public String getIdentifier(ArtifactRepositoryContainer repos, String group, String name, String version) {
    //TODO: Cache result on file

    def platform = PlatformLocator.factory.createPlatformManager()
    def handler = { AsyncResult result ->
      println result.succeeded()
    } as AsyncResultHandler<Void>

    platform.installModule("${group}:${name}:${version}", handler)

    //
    //    for(repo in repos) {
    //      def result = getIdentifier(repo.url.toString(), group, name, version)
    //      println repo.url
    //
    //      if(result) {
    //        return result
    //      }
    //    }

    return "$group:$name:$version@zip"
  }

  public String getIdentifier(String repo, String group, String name, String version) {
    String result = checkCache(group,name,version)

    if(result) {
      return result
    }

    def group_url = group.replace('.','/');
    def nomod = "${group_url}/${name}-${version}.zip"
    def withmod = "${group_url}/${name}-${version}-mod.zip"

    if(checkURL(repo, withmod)) {
      return "$group:$name:$version:mod@zip"
    }else if(checkURL(repo, nomod)) {
      return "$group:$name:$version@zip"
    } else{
      return null;
    }
  }

  public String checkCache(String group, String name, String version) {
    null
  }


}
