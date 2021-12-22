package common;

import java.io.File;

public class Response {

  public static Response EMPTY = new Response(0, null);

  private final long fileLength;
  private final File file;

  public Response(long fileLength, File file) {
    this.fileLength = fileLength;
    this.file = file;
  }

  public long getFileLength() {
    return fileLength;
  }

  public File getFile() {
    return file;
  }

  public static class Builder {

    private long fileLength;
    private File file;

    public Builder() {
    }

    public long getFileLength() {
      return fileLength;
    }

    public Builder setFileLength(long fileLength) {
      this.fileLength = fileLength;
      return this;
    }

    public File getFile() {
      return file;
    }

    public Builder setFile(File file) {
      this.file = file;
      return this;
    }

    public Response build() {
      return new Response(this.fileLength, this.file);
    }

  }
}
