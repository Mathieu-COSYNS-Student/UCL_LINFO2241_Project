package server;

import common.Hash;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class BrutForceReceiver extends Receiver {

  public BrutForceReceiver(Socket socket) {
    super(socket);
  }

  public static String crackPassword(byte[] passwordHash, int passwordLength, StringBuilder str)
      throws NoSuchAlgorithmException {
    // Stop condition, password and trial have same length
    if (passwordLength == str.length()) {
      byte[] hash = Hash.sha1(str.toString());
      if (Arrays.equals(passwordHash, hash)) {
        return str.toString();
      } else {
        return null;
      }
    }

    String s;

    for (char c = 'a'; c <= 'z'; c++) {
      // Add a new character to the given prefix
      str.append(c);
      // Try to find a password for the new prefix
      s = crackPassword(passwordHash, passwordLength, str);
      if (s != null) {
        return s;
      }
      // Didn't work out, remove the character
      str.deleteCharAt(str.length() - 1);
    }
    // All chars have been tried without success, go up one level
    return null;
  }

  @Override
  protected String crackPassword(byte[] passwordHash, int passwordLength) {
    try {
      return crackPassword(passwordHash, passwordLength, new StringBuilder());
    } catch (NoSuchAlgorithmException e) {
      e.printStackTrace();
    }

    return "";
  }
}
