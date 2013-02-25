import org.vertx.java.platform.Verticle;
import com.mycompany.module2.*;
import com.mycompany.module4.*;

public class Module1 extends Verticle {

  public void start() {
    System.out.println("MyVerticle started");

    // This should output some text
    Module2 mod2 = new Module2();
    Module4 mod4 = new Module4();
  }

  public void stop() {
  }
}
