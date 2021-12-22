package client;

import common.Request;
import common.Response;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.apache.commons.io.IOUtils;

public class ClientMain {

  private static final Random RANDOM = new Random(3);

  public static void main(String[] args) {

    if (args.length != 1) {
      System.err.println(
          "Set the first argument to a directory that contains the files that will be encrypted and sent to the server.");
      System.exit(1);
    }

    if (getRandomFileToBeEncrypted(args[0]) == null) {
      System.out.println("No files in " + args[0]
          + ". Are you providing a directory ? Does this directory contains files ?");
      System.exit(1);
    }

    // Create temporary encrypted files used for the tests
    ArrayList<Future<Request>> futuresRequests = new ArrayList<>();
    ArrayList<File> inputsFiles = new ArrayList<>();
    ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime()
        .availableProcessors());

    for (String password : getPasswordList()) {
      File inputFile = getRandomFileToBeEncrypted(args[0]);
      RequestPrepareCallable requestPrepareCallable = new RequestPrepareCallable(inputFile,
          password);
      inputsFiles.add(inputFile);
      futuresRequests.add(executorService.submit(requestPrepareCallable));
    }
    executorService.shutdown();

    ArrayList<Request> requests = new ArrayList<>();
    try {
      for (Future<Request> futureRequest : futuresRequests) {
        requests.add(futureRequest.get());
      }
    } catch (InterruptedException | ExecutionException e) {
      e.printStackTrace();
      requests.clear();
    }

    // Send requests
    ArrayList<Sender> senders = new ArrayList<>();
    try {
      for (Request request : requests) {
        Thread.sleep(getPoissonRandomNumber(0.06)); // 1 request every 15 seconds aka 1/15

        Socket socket = new Socket("localhost", 3333);
        Sender sender = new Sender(request, socket);
        sender.start();
        senders.add(sender);
      }
    } catch (InterruptedException | IOException e) {
      e.printStackTrace();
    }

    // Await for all requests to finish
    try {
      for (int i = 0; i < senders.size(); i++) {
        Sender sender = senders.get(i);
        sender.join();
        Response response = sender.getResponse();
        if (response != null) {
          getFormattedTimeMeasurements(sender.getElapsedTime(), i);
          if (!filesCompareByByte(inputsFiles.get(i), response.getFile())) {
            System.out.println(
                "!!! the file received form the server is not the same as the original one");
            System.out.println(
                "!!! " + inputsFiles.get(i).getAbsolutePath() + " >< " + response.getFile()
                    .getAbsolutePath());
          }
        } else {
          System.out.println("No response from the server");
        }
      }
    } catch (InterruptedException | IOException e) {
      e.printStackTrace();
    }
  }

  public static void getFormattedTimeMeasurements(long timeInMilliseconds, int senderID) {
    long minutes = (timeInMilliseconds / 1000) / 60;
    long seconds = (timeInMilliseconds / 1000) % 60;
    System.out.println(
        "Request n°" + senderID + " ---> Request/Response Time : " + minutes + " minutes and "
            + seconds + " seconds");
  }

  private static int getPoissonRandomNumber(double rate) {
    Random r = new Random();
    double L = Math.exp(-rate);
    int k = 0;
    double p = 1.0;
    do {
      p = p * r.nextDouble();
      k++;
    } while (p > L);
    return k - 1;
  }

  private static String[] getPasswordList() {
    // return new String[]{"test"};
    return new String[]{"vrmeh", "oiwfm", "hhfsp", "alley", "redskin",
        "billybob", "zvgeo", "qckwd", "sparky", "punkrock", "ghihv", "jeqnb", "volvo",
        "zurich", "jnggw", "ymmpa", "dfkyd", "uihup", "mbabw", "trinh", "xgvgd",
        "wgqyl", "pgkbh", "pqdqg", "forum", "pjhek", "pgkiy", "sentra", "smokie",
        "nbkij", "diane", "xqtfg", "schmidt", "tcluo", "mailman", "ewddr", "aefqd",
        "kinky", "sbhba", "zjpof", "lrfft", "inyqc", "kngfg", "ybuxx", "amasz",
        "fastball", "ccccccc", "pszbw", "miffs", "qahfr", "malone", "bppmn", "anne",
        "mesdr", "lpsok", "gvaox", "classics", "fmzsh", "blnlo", "lillie", "candyass",
        "kkbnb", "umgtf", "lksty", "dogboy", "qasiz", "manga", "safety", "saqfs",
        "fnlbi", "nsene", "wgpwd", "ixumt", "perry", "pmrho"
    };
  }

  private static File getRandomFileToBeEncrypted(String dir) {
    File directory = new File(dir);
    File[] files = directory.listFiles();
    if (files != null) {
      int randomIndex = RANDOM.nextInt(files.length);
      return files[randomIndex];
    }
    return null;
  }

  public static boolean filesCompareByByte(File file1, File file2) throws IOException {
    try (InputStream in1 = new FileInputStream(file1);
        InputStream in2 = new FileInputStream(file2)) {
      return IOUtils.contentEquals(in1, in2);
    }
  }
}
