import org.vertx.java.core.*;

public class Module1 extends Verticle{
  public void start(){
    System.out.println("Verticle Started");
  }

  public void close(){
    System.out.println("Verticle Stopped");
  }
}