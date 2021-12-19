package server;

import common.PasswordHash;
import java.util.Arrays;
import java.util.Iterator;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Function;

public class MultiThreadBrutForce {

  private static final int BUFFER_SIZE = 1024;
  private static final int MAX_TREADS = 512;
  private final BrutForceThread[] threads;
  private final Future<?>[] threadsFutures;
  private final Function<PasswordHash[], Boolean> resultProcessor;
  private ExecutorService pool;
  private int threadCount;

  public MultiThreadBrutForce(Function<PasswordHash[], Boolean> resultProcessor) {
    this.resultProcessor = resultProcessor;
    this.threadCount = 0;
    this.threads = new BrutForceThread[MAX_TREADS];
    this.threadsFutures = new Future[MAX_TREADS];
  }

  public void crack(int passwordLength) {
    this.crack(passwordLength, passwordLength);
  }

  public void crack(int minPasswordLength, int maxPasswordLength) {
    this.crack(new StringsIterator(minPasswordLength, maxPasswordLength));
  }

  public void crack(Iterator<String> stringIterator) {
    pool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    int nbInBuffer = 0;
    String[] passwords = new String[BUFFER_SIZE];

    try {
      while (stringIterator.hasNext()) {
        String string = stringIterator.next();
        if (nbInBuffer == BUFFER_SIZE) {
          if (addTreadAndRun(Arrays.copyOf(passwords, nbInBuffer), false)) {
            break;
          }

          nbInBuffer = 0;
        }
        passwords[nbInBuffer++] = string;
      }
      addTreadAndRun(Arrays.copyOf(passwords, nbInBuffer), true);
      pool.shutdown();
    } catch (InterruptedException | ExecutionException e) {
      e.printStackTrace();
    }
  }

  private boolean addTreadAndRun(String[] passwords, boolean forceRun)
      throws InterruptedException, ExecutionException {
    threads[threadCount] = new BrutForceThread(passwords);
    threadsFutures[threadCount] = pool.submit(threads[threadCount]);
    if (forceRun || threadCount + 1 >= MAX_TREADS) {
      for (int i = 0; i < threadCount; i++) {
        threadsFutures[threadCount].get();
        if (resultProcessor.apply(threads[i].getResults())) {
          return true;
        }
      }

      threadCount = 0;
      return false;
    }
    threadCount++;
    return false;
  }
}
