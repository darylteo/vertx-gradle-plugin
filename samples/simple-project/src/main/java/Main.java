import org.vertx.java.platform.Verticle;

public class Main extends Verticle {
  @Override
  public void start() {
    System.out.println("Received Config: " + container.config().toString());
  }
}
