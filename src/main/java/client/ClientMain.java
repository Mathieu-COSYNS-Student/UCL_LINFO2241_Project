package client;

import common.CryptoUtils;
import common.Hash;
import common.Request;
import common.Response;
import server.receivers.Receiver;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

public class ClientMain {

  private static ArrayList<Sender> connections = new ArrayList<>();


  public static void main(String[] args) {
    try {
      String password = "test";
      SecretKey keyGenerated = CryptoUtils.getKeyFromPassword(password);

      File inputFile = new File("test_file.pdf");
      File encryptedFile = new File("test_file-encrypted-client.pdf");

      // This is an example to help you create your request
      CryptoUtils.encryptFile(keyGenerated, inputFile, encryptedFile);
      System.out.println("Input file length: " + inputFile.length());
      System.out.println("Encrypted file length: " + encryptedFile.length());

      // SEND THE PROCESSING INFORMATION AND FILE
      byte[] hashPwd = Hash.sha1(password);
      int pwdLength = password.length();
      long fileLength = encryptedFile.length();
      // Creating socket to connect to server (in this example it runs on the localhost on port 3333)
      Sender sender = null;
      for (int i = 0; i < 100; i++) {
        Socket socket = new Socket("localhost", 3333);
        Request request = new Request(hashPwd, pwdLength, fileLength, encryptedFile);
        sender = new Sender(request, socket);
        connections.add(sender);
        sender.start();
      }
      for (Sender connection:connections) {
        connection.join();
        Response response = connection.getResponse();
        if (response != null) {
          System.out.println("Decrypted file length from the server response: " + response.getFileLength());
          System.out.println("Decrypted file length: " + response.getFile().length());
        } else {
          System.out.println("No response from the server");
        }
      }

    } catch (NoSuchAlgorithmException | InvalidKeySpecException | InvalidAlgorithmParameterException
        | NoSuchPaddingException | IllegalBlockSizeException | IOException | BadPaddingException
        | InvalidKeyException | InterruptedException e) {
      e.printStackTrace();
    }

  }


}

