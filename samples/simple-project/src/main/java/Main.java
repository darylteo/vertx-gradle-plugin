import org.vertx.java.core.Handler;
import org.vertx.java.core.http.HttpServer;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.platform.Verticle;

public class Main extends Verticle {
  @Override
  public void start() {
    HttpServer server = vertx.createHttpServer();
   
    server.requestHandler(new Handler<HttpServerRequest>() {
      @Override
      public void handle(HttpServerRequest event) {
        // set a breakpoint here
        event.response().end("Hello World!");
      }
    });

    server.listen(12345);
  }
}
