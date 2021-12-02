package common;

import java.io.File;

public class Request {

  private final byte[] passwordHash;
  private final int passwordLength;
  private final long fileLength;
  private final File file;

  public Request(byte[] passwordHash, int passwordLength, long fileLength, File file) {
    this.passwordHash = passwordHash;
    this.passwordLength = passwordLength;
    this.fileLength = fileLength;
    this.file = file;
  }

  public byte[] getPasswordHash() {
    return passwordHash;
  }

  public int getPasswordLength() {
    return passwordLength;
  }

  public long getFileLength() {
    return fileLength;
  }

  public File getFile() {
    return file;
  }

  public static class Builder {
    private byte[] passwordHash;
    private int passwordLength;
    private long fileLength;
    private File file;

    public Builder() {}

    public byte[] getPasswordHash() {
      return passwordHash;
    }

    public void setPasswordHash(byte[] passwordHash) {
      this.passwordHash = passwordHash;
    }

    public int getPasswordLength() {
      return passwordLength;
    }

    public void setPasswordLength(int passwordLength) {
      this.passwordLength = passwordLength;
    }

    public long getFileLength() {
      return fileLength;
    }

    public void setFileLength(long fileLength) {
      this.fileLength = fileLength;
    }

    public File getFile() {
      return file;
    }

    public void setFile(File file) {
      this.file = file;
    }

    public Request build() {
      return new Request(this.passwordHash, this.passwordLength, this.fileLength, this.file);
    }
  }
}
