import org.vertx.java.platform.Verticle;
import com.mycompany.module2.*;

public class Module1 extends Verticle {

  public void start() {
    System.out.println("MyVerticle started");

    // This should output some text
    MyClass myclass = new MyClass();
  }

  public void stop() {
  }
}
