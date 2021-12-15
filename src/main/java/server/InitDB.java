package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class InitDB {

  public static void main(String[] args) {
    long startTime = System.currentTimeMillis();

    Database database = new Database();
    database.initDb();

    System.out.println("Starting to hash passwords...");

    ExecutorService executorService = Executors.newSingleThreadExecutor();

    MultiThreadBrutForce multiThreadBrutForce = new MultiThreadBrutForce(results -> {
      executorService.execute(() -> database.insertList(results));
      return false;
    });

    try (InputStream in = InitDB.class.getClassLoader()
        .getResourceAsStream("10k-most-common_filered.txt")) {
      if (in != null) {
        try (
            InputStreamReader streamReader =
                new InputStreamReader(in, StandardCharsets.UTF_8);
            BufferedReader reader = new BufferedReader(streamReader)) {

          ArrayList<String> dictionary = new ArrayList<>();

          String word;
          while ((word = reader.readLine()) != null) {
            dictionary.add(word);
          }

          multiThreadBrutForce.crack(dictionary.iterator());
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    }

    try {
      executorService.shutdown();
      //noinspection ResultOfMethodCallIgnored
      executorService.awaitTermination(1, TimeUnit.DAYS);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    double delta = (System.currentTimeMillis() - startTime) / 1000.0;
    System.out.println("InitDb finished after " + delta + " seconds");
  }
}
