import org.vertx.scala.core._
import org.vertx.scala.core.http.HttpServerRequest
import org.vertx.scala.platform.Verticle

class Main extends Verticle {

  override def start():Unit = {
  	var port:Int = 12345

    vertx.createHttpServer.requestHandler { request: HttpServerRequest =>
      request.response.end("Hello World!")
    }.listen(12345)
  }
}