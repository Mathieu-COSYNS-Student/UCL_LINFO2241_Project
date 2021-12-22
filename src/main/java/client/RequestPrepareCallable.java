package client;

import common.CryptoUtils;
import common.Hash;
import common.Request;
import java.io.File;
import java.util.concurrent.Callable;
import javax.crypto.SecretKey;

public class RequestPrepareCallable implements Callable<Request> {

  private final File file;
  private final String password;

  public RequestPrepareCallable(File file, String password) {
    this.file = file;
    this.password = password;
  }

  @Override
  public Request call() throws Exception {
    SecretKey keyGenerated = CryptoUtils.getKeyFromPassword(password);
    File encryptedFile = File.createTempFile("client-encrypted-", null);
    CryptoUtils.encryptFile(keyGenerated, file, encryptedFile);

    Request.Builder builder = new Request.Builder();
    builder.setPasswordHash(Hash.sha1(password))
        .setPasswordLength(password.length())
        .setFile(encryptedFile)
        .setFileLength(encryptedFile.length());

    return builder.build();
  }
}
