import org.vertx.groovy.platform.Verticle

class Main extends Verticle {
  def start() {
    int port = 12345
    println "Starting Server on port $port"

    def server = vertx.createHttpServer()

    server.requestHandler { request ->
      // set a breakpoint here
      request.response.end("Hello World!")
    }

    server.listen(port)
  }
}