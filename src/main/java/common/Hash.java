package common;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Hash {

  /**
   * This function hashes a string with the SHA-1 algorithm
   *
   * @param data The string to hash
   * @return An array of 20 bytes which is the hash of the string
   */
  public static byte[] sha1(String data) throws NoSuchAlgorithmException {
    MessageDigest md = MessageDigest.getInstance("SHA-1");
    return md.digest(data.getBytes());
  }
}
