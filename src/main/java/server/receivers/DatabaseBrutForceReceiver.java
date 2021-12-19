package server.receivers;

import java.net.Socket;
import server.Database;

public class DatabaseBrutForceReceiver extends BrutForceReceiver {

  DatabaseBrutForceReceiver(Socket socket) {
    super(socket);
  }

  @Override
  protected String crackPassword(byte[] passwordHash, int passwordLength) {
    Database database = new Database();

    String password = database.getPassword(passwordHash);

    if (password != null) {
      return password;
    }

    return super.crackPassword(passwordHash, passwordLength);
  }
}
