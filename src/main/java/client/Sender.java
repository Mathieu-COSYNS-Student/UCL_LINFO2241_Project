package client;

import common.FileManagement;
import common.Request;
import common.Response;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class Sender extends Thread {

  private final Request request;

  public Socket getSocket() {
    return socket;
  }

  private final Socket socket;
  private long elapsedTime;
  private Response response;

  public Sender(Request request, Socket socket) {
    this.request = request;
    this.response = null;
    this.socket = socket;
  }

  public long getElapsedTime() {
    return elapsedTime;
  }

  public Response getResponse() {
    return response;
  }

  @Override
  public void run() {
    long start = System.currentTimeMillis();
    try (
        DataOutputStream out = new DataOutputStream(socket.getOutputStream());
        DataInputStream in = new DataInputStream(socket.getInputStream())) {

      writeRequestHeader(out);
      writeRequestFile(out);
      System.out.println();
      Response.Builder builder = new Response.Builder();

      System.out.println("Reading the response header from the server - Port : "+socket.getLocalPort());
      readResponseHeader(in, builder);
      System.out.println("Finished reading the response header from the server - Port : "+socket.getLocalPort());
      System.out.println("Reading the response file from the server - Port : "+socket.getLocalPort());
      readResponseFile(in, builder);
      System.out.println("Finished reading the response file from the server - Port : "+socket.getLocalPort());
      this.response = builder.build();

      System.out.println("Building response COMPLETE - Port : "+socket.getLocalPort());

      socket.close();
    } catch (IOException e) {
      e.printStackTrace();
    }

    this.elapsedTime = System.currentTimeMillis() - start;
  }

  private void writeRequestHeader(DataOutputStream out) throws IOException {
    out.write(this.request.getPasswordHash(), 0, 20);
    out.writeInt(this.request.getPasswordLength());
    out.writeLong(this.request.getFileLength());
    out.flush();
  }

  private void writeRequestFile(OutputStream out) throws IOException {
    FileManagement.sendFile(this.request.getFile(), out);
  }

  private void readResponseHeader(DataInputStream in, Response.Builder builder) throws IOException {
    builder.setFileLength(in.readLong());
  }

  private void readResponseFile(InputStream in, Response.Builder builder) throws IOException {
    File responseFile = File.createTempFile("client-response-from-server-", null);
    FileManagement.receiveFile(in, responseFile, builder.getFileLength());

    builder.setFile(responseFile);
  }
}
