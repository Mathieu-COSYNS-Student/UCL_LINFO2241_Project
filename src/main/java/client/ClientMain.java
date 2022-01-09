package client;

import common.Request;
import common.Response;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.io.IOUtils;

public class ClientMain {

  private static final Random RANDOM = new Random(3);
  private static List<String[]> dataLines = new ArrayList<>();
  private static CSV csvMaker = new CSV();


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

    for (int turn = 1; turn <= 5; turn++) {
      // Create temporary encrypted files used for the tests
      ArrayList<Future<Request>> futuresRequests = new ArrayList<>();
      ArrayList<File> inputsFiles = new ArrayList<>();
      ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime()
              .availableProcessors());

      for (String password : getPasswordList()) {
        //File inputFile = getRandomFileToBeEncrypted(args[0]);
        File inputFile = new File("filesToBeEncrypted/file-4.bin");
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
          Thread.sleep(getPoissonRandomNumber(0.2)); // 1 request every 5 seconds aka 1/5
          Socket socket = new Socket("linfo2241.ddns.net", 3333);
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
          int port  = sender.getSocket().getLocalPort();
          System.out.println("------------------------------------------");
          System.out.println("The request below used port "+port);
          sender.join();
          Response response = sender.getResponse();
          if (response != null) {
            getFormattedTimeMeasurements(sender.getElapsedTime(), i);
            //String[] data = new String[]{"request-" + i, "" + sender.getElapsedTime() + ""};
            String[] data = new String[]{"xyxy-100KB", ""+turn+"", "" + sender.getElapsedTime() + ""};

            dataLines.add(data);
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
          System.out.println("------------------------------------------");

        }
      } catch (InterruptedException | IOException e) {
        e.printStackTrace();
      }


      try {
        csvMaker.givenDataArray_whenConvertToCSV_thenOutputCreated(dataLines);
      } catch (IOException e) {
        e.printStackTrace();
      }
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
    return new String[]{"x"}; // x, xy, xyx, xyxy, xyxyz, xyxyzz
  };

//    return new String[]{
//            "vrmeh", "oiwfm", "hhfsp", "alley", "zvgeo","anne",
//            "zurich", "jnggw", "ymmpa", "dfkyd", "uihup", "mbabw", "kate",
//            "wgqyl", "pgkbh", "pqdqg", "forum", "pjhek", "pgkiy", "call",
//            "sentra", "smokie", "trinh", "xgvgd", "ghihv", "jeqnb", "rico",
//            "nbkij", "diane", "xqtfg", "tcluo", "ewddr", "aefqd","yaya",
//            "kinky", "sbhba", "zjpof", "lrfft", "inyqc", "kngfg","berry",
//            "mesdr", "lpsok", "gvaox", "fmzsh", "blnlo", "lillie","ultra",
//            "kkbnb", "umgtf", "lksty", "dogboy", "qasiz", "manga", "hugo",
//            "fnlbi", "nsene", "wgpwd", "ixumt", "perry", "pmrho", "farm",
//            "amasz", "pszbw", "miffs", "qahfr", "malone", "bppmn",
//            "safety", "saqfs",  "ybuxx", "qckwd", "sparky","volvo","paco"
//    };
//  }




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
