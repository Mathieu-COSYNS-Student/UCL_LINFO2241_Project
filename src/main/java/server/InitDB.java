package server;

import common.StringUtils;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class InitDB {

  private static final int BUFFER_SIZE = 1024;
  private static final int MAX_TREADS_CREATED = 8;
  private static final BrutForceThread[] brutForceThreads = new BrutForceThread[MAX_TREADS_CREATED];
  private static int treadsCount = 0;
  private static Database database;

  public static void main(String[] args) {
    long startTime = System.currentTimeMillis();

    database = new Database();
    // database.initDb();

    System.out.println("Starting to hash passwords...");

    final int[] nbInBuffer = {0};
    String[] passwords = new String[BUFFER_SIZE];

    Consumer<Integer> fn = size -> {
      addBrutForceThead(Arrays.copyOf(passwords, size));
      if (treadsCount >= MAX_TREADS_CREATED) {
        runThreads();
        treadsCount = 0;
      }
    };

    for (int i = 0; i < 7; i++) {
      StringUtils.loopAll(new StringBuilder(), i, s -> {
        if (nbInBuffer[0] == BUFFER_SIZE) {
          fn.accept(nbInBuffer[0]);
          nbInBuffer[0] = 0;
        }
        passwords[nbInBuffer[0]] = s;
        nbInBuffer[0]++;
        return null;
      });
    }

    fn.accept(nbInBuffer[0]);

    double delta = (System.currentTimeMillis() - startTime) / 1000.0;
    System.out.println("InitDb finished after " + delta + " seconds");
  }

  private static void addBrutForceThead(String[] passwords) {
    brutForceThreads[treadsCount] = new BrutForceThread(passwords);
    treadsCount++;
  }

  private static void runThreads() {
    ExecutorService pool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    for (int i = 0; i < treadsCount; i++) {
      pool.execute(brutForceThreads[i]);
    }

    pool.shutdown();

  }

}
