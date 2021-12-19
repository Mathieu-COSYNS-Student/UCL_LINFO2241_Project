package server.receivers;

import common.Hash;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import server.StringsIterator;

public class BrutForceReceiver extends Receiver {

  BrutForceReceiver(Socket socket) {
    super(socket);
  }

  @Override
  protected String crackPassword(byte[] passwordHash, int passwordLength) {
    StringsIterator stringsIterator = new StringsIterator(passwordLength);

    while (stringsIterator.hasNext()) {
      String string = stringsIterator.next();

      try {
        byte[] hash = Hash.sha1(string);
        if (Arrays.equals(passwordHash, hash)) {
          return string;
        }
      } catch (NoSuchAlgorithmException e) {
        e.printStackTrace();
      }
    }

    return null;
  }
}
