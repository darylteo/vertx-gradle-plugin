import org.vertx.java.core.Handler;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.platform.Verticle;

public class Main extends Verticle {
  @Override
  public void start() {
    int port = 12345;
    System.out.println("Starting Server on port " + port);

    vertx.createHttpServer().requestHandler(new Handler<HttpServerRequest>() {
      @Override
      public void handle(HttpServerRequest event) {
        // set a breakpoint here
        event.response().end("Hello World!");
      }
    }).listen(port);
  }
}
