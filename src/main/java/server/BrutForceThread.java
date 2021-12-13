package server;

import common.Hash;
import java.security.NoSuchAlgorithmException;

public class BrutForceThread extends Thread {

  private final String[] passwords;
  private final byte[][] hashes;

  public BrutForceThread(String[] passwords) {
    this.passwords = passwords;
    this.hashes = new byte[passwords.length][];
  }

  public String[] getPasswords() {
    return passwords;
  }

  public byte[][] getHashes() {
    return hashes;
  }

  @Override
  public void run() {
    for (int i = 0; i < passwords.length; i++) {
      try {
        hashes[i] = Hash.sha1(passwords[i]);
      } catch (NoSuchAlgorithmException e) {
        e.printStackTrace();
      }
    }
  }
}
