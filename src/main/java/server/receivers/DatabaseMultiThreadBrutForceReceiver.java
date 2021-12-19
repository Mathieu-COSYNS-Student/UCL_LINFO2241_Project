package server.receivers;

import java.net.Socket;
import server.Database;

public class DatabaseMultiThreadBrutForceReceiver extends MultiThreadBrutForceReceiver {

  DatabaseMultiThreadBrutForceReceiver(Socket socket) {
    super(socket);
  }

  @Override
  protected String crackPassword(byte[] passwordHash, int passwordLength) {
    Database database = new Database();

    String password = database.getPassword(passwordHash);

    if (password != null) {
      System.out.println("Password in DB");
      return password;
    }

    return super.crackPassword(passwordHash, passwordLength);
  }
}
