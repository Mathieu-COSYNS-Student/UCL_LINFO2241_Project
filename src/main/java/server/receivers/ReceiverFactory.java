package server.receivers;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.Socket;

public class ReceiverFactory {

  private final Class<? extends Receiver> receiverClass;

  public ReceiverFactory(String className) {
    try {
      //noinspection unchecked
      this.receiverClass = (Class<? extends Receiver>) Class.forName(className);
    } catch (ClassNotFoundException e) {
      throw new IllegalArgumentException();
    }

    if (!Receiver.class.isAssignableFrom(this.receiverClass)) {
      throw new IllegalArgumentException();
    }
  }

  public Receiver newInstance(Socket socket) {
    @SuppressWarnings("unchecked")
    Constructor<? extends Receiver>[] constructors = (Constructor<? extends Receiver>[]) receiverClass.getDeclaredConstructors();
    try {
      return constructors[0].newInstance(socket);
    } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
      e.printStackTrace();
    }
    return null;
  }

}
