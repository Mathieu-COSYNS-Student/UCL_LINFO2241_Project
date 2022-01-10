package client;

import common.Request;
import common.Response;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import org.apache.commons.io.IOUtils;

public class ClientMain {

  private static final Random RANDOM = new Random(3);
  private static final List<String[]> dataLines = new ArrayList<>();
  private static final CSV csvMaker = new CSV();
  private static String[] passwords;

  private static String FILE_PATH;

  private static final int TEST_PASSWORD = 1;
  private static final int TEST_FILES = 2;
  private static final int TEST_REALISTIC = 3;
  private static final int TEST_TYPE = TEST_REALISTIC;
  private static final boolean CONCURRENT_REQUESTS = true;

  public static void main(String[] args) throws InterruptedException {

    if (args.length != 1) {
      System.err.println(
          "Set the first argument to a directory that contains the files that will be encrypted and sent to the server.");
      System.exit(1);
    }

    FILE_PATH = args[0];

    if (getFileToEncrypt() == null) {
      System.out.println("No files in " + args[0]
          + ". Are you providing a directory ? Does this directory contains files ?");
      System.exit(1);
    }

    int TURNS = TEST_TYPE == TEST_REALISTIC ? 1 : 5;

    for (int turn = 1; turn <= TURNS; turn++) {
      // Create temporary encrypted files used for the tests
      ArrayList<Future<Request>> futuresRequests = new ArrayList<>();
      ArrayList<File> inputsFiles = new ArrayList<>();
      ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime()
          .availableProcessors());

      for (String password : getPasswords()) {
        File inputFile = getFileToEncrypt();
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
          if (CONCURRENT_REQUESTS) {
            Thread.sleep(
                getPoissonSleepTimeForXRequestPerSeconds(
                    0.07)); // 1 request every 5 seconds aka 1/5
          }
          Socket socket = new Socket("192.168.1.7", 3333);
          Sender sender = new Sender(request, socket);
          sender.start();
          if (!CONCURRENT_REQUESTS) {
            sender.join();
          }
          senders.add(sender);
        }
      } catch (InterruptedException | IOException e) {
        e.printStackTrace();
      }

      // Await for all requests to finish
      try {
        for (int i = 0; i < senders.size(); i++) {
          Sender sender = senders.get(i);
          int port = sender.getSocket().getLocalPort();
          System.out.println("------------------------------------------");
          System.out.println("The request below used port " + port);
          sender.join();
          Response response = sender.getResponse();
          if (response != null) {
            getFormattedTimeMeasurements(sender.getElapsedTime(), i);
            String[] data = new String[]{getRequestLabel(i), "" + turn,
                "" + sender.getElapsedTime() / 1000.0};

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

  private static long getPoissonSleepTimeForXRequestPerSeconds(double x) {
    double u = 1 - RANDOM.nextDouble();
    double next = -Math.log(u) / x;
    return (long) (next * 1000);
  }

  private static String[] getPasswords() {
    if (TEST_TYPE == TEST_PASSWORD) {
      return new String[]{"x", "xy", "xyx", "xyxy", "xyxyz", "xyxyzz"};
    }
    if (TEST_TYPE == TEST_FILES) {
      return new String[]{"xyxy"};
    }
    if (passwords == null) {
      loadPasswords();
    }
    return passwords;
  }

  public static void loadPasswords() {
    try (InputStream in = ClientMain.class.getClassLoader().getResourceAsStream("passwords.txt")) {
      assert in != null;
      passwords =
          new BufferedReader(new InputStreamReader(in,
              StandardCharsets.UTF_8)).lines().collect(Collectors.toList()).toArray(String[]::new);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private static String getRequestLabel(int i) {
    if (TEST_TYPE == TEST_PASSWORD) {
      return getPasswords()[i];
    }
    if (TEST_TYPE == TEST_FILES) {
      return getFileToEncrypt().getName();
    }
    return "request-" + i;
  }

  private static File getFileToEncrypt() {
    if (TEST_TYPE != TEST_REALISTIC) {
      return new File(FILE_PATH);
    }
    File directory = new File(FILE_PATH);
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
