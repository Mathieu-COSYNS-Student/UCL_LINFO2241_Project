package server;

import common.Hash;
import common.PasswordHash;
import java.security.NoSuchAlgorithmException;

public class BrutForceThread extends Thread {

  private final String[] passwords;
  private final PasswordHash[] results;

  public BrutForceThread(String[] passwords) {
    this.passwords = passwords;
    this.results = new PasswordHash[passwords.length];
  }

  public PasswordHash[] getResults() {
    return results;
  }

  @Override
  public void run() {
    for (int i = 0; i < passwords.length; i++) {
      try {
        results[i] = new PasswordHash(passwords[i], Hash.sha1(passwords[i]));
      } catch (NoSuchAlgorithmException e) {
        e.printStackTrace();
      }
    }
  }
}
