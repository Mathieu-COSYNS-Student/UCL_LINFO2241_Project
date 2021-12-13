package server;

import common.Hash;
import common.StringUtils;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class BrutForceReceiver extends Receiver {

  public BrutForceReceiver(Socket socket) {
    super(socket);
  }

  @Override
  protected String crackPassword(byte[] passwordHash, int passwordLength) {
    return StringUtils.loopAll(new StringBuilder(), passwordLength, s -> {
      try {
        byte[] hash = Hash.sha1(s);
        if (Arrays.equals(passwordHash, hash))
          return s;
      } catch (NoSuchAlgorithmException e) {
        e.printStackTrace();
      }

      return null;
    });
  }
}
