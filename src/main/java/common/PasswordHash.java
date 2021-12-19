package common;

public class PasswordHash {

  private final String password;
  private final byte[] hash;

  public PasswordHash(String password, byte[] hash) {
    this.password = password;
    this.hash = hash;
  }

  public String getPassword() {
    return password;
  }

  public byte[] getHash() {
    return hash;
  }
}
