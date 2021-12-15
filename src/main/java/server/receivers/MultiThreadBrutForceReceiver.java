package server.receivers;

import common.PasswordHash;
import java.net.Socket;
import java.util.Arrays;
import server.MultiThreadBrutForce;

public class MultiThreadBrutForceReceiver extends Receiver {

  private String password;

  MultiThreadBrutForceReceiver(Socket socket) {
    super(socket);

    this.password = null;
  }

  @Override
  protected String crackPassword(byte[] passwordHash, int passwordLength) {
    MultiThreadBrutForce multiThreadBrutForce = new MultiThreadBrutForce(results -> {
      for (PasswordHash result : results) {
        if (Arrays.equals(result.getHash(), passwordHash)) {
          this.password = result.getPassword();
          return true;
        }
      }
      return false;
    });

    multiThreadBrutForce.crack(passwordLength);

    return this.password;
  }
}
