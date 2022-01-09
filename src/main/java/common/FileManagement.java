package common;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class FileManagement {

  public static void receiveFile(InputStream inputStream, File file, long fileLength)
      throws IOException {
    if (file == null) {
      return;
    }

    try (OutputStream out = new FileOutputStream(file)) {
      int readFromFile = 0;
      int bytesRead;
      byte[] readBuffer = new byte[64];
      while ((readFromFile < fileLength)) {
        bytesRead = inputStream.read(readBuffer);
        readFromFile += bytesRead;
        out.write(readBuffer, 0, bytesRead);
      }
    }

  }

  public static void sendFile(File file, OutputStream outputStream)
      throws IOException {
    if (file == null) {
      return;
    }

    try (InputStream in = new FileInputStream(file)) {
      int readCount;
      byte[] buffer = new byte[64];
      //read from the file and send it in the socket
      while ((readCount = in.read(buffer)) > 0) {
        outputStream.write(buffer, 0, readCount);
      }
    }
    outputStream.flush();

  }
}
