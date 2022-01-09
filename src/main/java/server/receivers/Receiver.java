package server.receivers;

import common.CryptoUtils;
import common.FileManagement;
import common.Request;
import common.Request.Builder;
import common.Response;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

public abstract class Receiver extends Thread {

  private final Socket socket;

  public Receiver(Socket socket) {
    this.socket = socket;
  }

  @Override
  public void run() {
    System.out.println(socket + " Processing request.");
    try (
        DataOutputStream out = new DataOutputStream(socket.getOutputStream());
        DataInputStream in = new DataInputStream(socket.getInputStream())) {

      Request.Builder builder = new Builder();
      readRequestHeader(in, builder);
      System.out.println(socket + " Start reading file.");
      readRequestFile(in, builder);
      System.out.println(socket + " File read.");

      Response response = handleRequest(builder.build());
      if (response != null) {
        writeResponseHeader(out, response);
        System.out.println(socket + " Start writing file (" + response.getFile().length() + ").");
        writeResponseFile(out, response);
        System.out.println(socket + " File wrote. (" + out.size() + " bytes)");
      }

      System.out.println(socket + " Closing socket...");
      socket.close();
      System.out.println(socket + " Closed");
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void readRequestHeader(DataInputStream in, Request.Builder builder) throws IOException {
    byte[] passwordHash;
    passwordHash = new byte[20];
    int count = in.read(passwordHash, 0, 20);
    if (count < 0) {
      throw new IOException("Server could not read from the stream");
    }
    builder.setPasswordHash(passwordHash);
    builder.setPasswordLength(in.readInt());
    builder.setFileLength(in.readLong());
  }

  private void readRequestFile(InputStream in, Request.Builder builder) throws IOException {
    File requestFile = File.createTempFile("server-request-from-client", null);
    FileManagement.receiveFile(in, requestFile, builder.getFileLength());

    builder.setFile(requestFile);
  }

  private Response handleRequest(Request request) throws IOException {

    File decryptedFile = File.createTempFile("server-decrypted", null);
    long fileLength = request.getFileLength();

    System.out.println(socket + " Encrypted file length from the request: " + fileLength);
    System.out.println(socket + " Encrypted file length: " + request.getFile().length());

    String password = crackPassword(request.getPasswordHash(), request.getPasswordLength());
    if (password != null) {
      SecretKey serverKey;
      try {
        serverKey = CryptoUtils.getKeyFromPassword(password);
        CryptoUtils.decryptFile(serverKey, request.getFile(), decryptedFile);

        return new Response(decryptedFile.length(), decryptedFile);
      } catch (NoSuchPaddingException | IllegalBlockSizeException | IOException |
          NoSuchAlgorithmException | BadPaddingException | InvalidKeyException |
          InvalidKeySpecException e) {
        e.printStackTrace();
      }
    }

    return Response.EMPTY;
  }

  protected abstract String crackPassword(byte[] passwordHash, int passwordLength);

  private void writeResponseHeader(DataOutputStream out, Response response) throws IOException {
    out.writeLong(response.getFileLength());
    out.flush();
  }

  private void writeResponseFile(OutputStream out, Response response) throws IOException {
    FileManagement.sendFile(response.getFile(), out);
  }


}
