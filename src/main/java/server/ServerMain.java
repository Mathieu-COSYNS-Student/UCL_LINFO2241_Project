package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import server.receivers.BrutForceReceiver;
import server.receivers.DatabaseBrutForceReceiver;
import server.receivers.DatabaseMultiThreadBrutForceReceiver;
import server.receivers.MultiThreadBrutForceReceiver;
import server.receivers.Receiver;
import server.receivers.ReceiverFactory;

public class ServerMain {

  @SuppressWarnings("InfiniteLoopStatement")
  public static void main(String[] args) {

    if (args.length != 1) {
      printUsage();
    }

    int portNumber = 3333;

    ReceiverFactory receiverFactory = null;
    try {
      receiverFactory = new ReceiverFactory(args[0]);
      System.out.println("Receiver: " + args[0]);
    } catch (IllegalArgumentException ex) {
      printUsage();
    }

    assert receiverFactory != null;

    try (ServerSocket ss = new ServerSocket(portNumber)) {
      System.out.println("Listening on port " + portNumber);

      ExecutorService requestQueue = Executors.newFixedThreadPool(2);

      while (true) {
        Socket socket = ss.accept();
        Receiver receiver = receiverFactory.newInstance(socket);
        requestQueue.execute(receiver);
      }
    } catch (IOException e) {
      System.err.println("Could not listen on port " + portNumber);
      System.exit(-1);
    }
  }

  public static void printUsage() {
    System.err.println("Add an argument to the command");
    System.err.println("\t- " + BrutForceReceiver.class.getName() + " for pure brut force");
    System.err.println("\t- " + MultiThreadBrutForceReceiver.class.getName()
        + " for pure brut force on multiple thread");
    System.err.println("\t- " + DatabaseBrutForceReceiver.class.getName()
        + " for database then pure brut force");
    System.err.println("\t- " + DatabaseMultiThreadBrutForceReceiver.class.getName()
        + " for database then pure brut force on multiple thread");
    System.exit(1);
  }
}
