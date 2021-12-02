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
  private final Socket socket;
  private Response response;

  public Sender(Request request, Socket socket) {
    this.request = request;
    this.response = null;
    this.socket = socket;
  }

  public Response getResponse() {
    return response;
  }

  @Override
  public void run() {
    try (
        DataOutputStream out = new DataOutputStream(socket.getOutputStream());
        DataInputStream in = new DataInputStream(socket.getInputStream())) {

      writeRequestHeader(out);
      writeRequestFile(out);

      Response.Builder builder = new Response.Builder();
      readResponseHeader(in, builder);
      readResponseFile(in, builder);

      this.response = builder.build();

      socket.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
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
    File responseFile = new File("response-" + System.currentTimeMillis() + ".pdf");
    FileManagement.receiveFile(in, responseFile, builder.getFileLength());

    builder.setFile(responseFile);
  }
}
