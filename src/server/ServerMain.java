package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerMain {

  public static void main(String[] args) {

    int portNumber = 3333;

    try(ServerSocket ss = new ServerSocket(portNumber)) {
      System.out.println("Listening on port " + portNumber);
      //noinspection InfiniteLoopStatement
      while(true) {
        Socket socket = ss.accept();
        Receiver receiver = new BrutForceReceiver(socket);
        receiver.start();
      }
    } catch (IOException e) {
      System.err.println("Could not listen on port " + portNumber);
      System.exit(-1);
    }
  }
}
