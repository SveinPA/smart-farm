package edu.ntnu.bidata.smg.group8.control.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
* <h3>UI Executors - Shared Thread Pool for UI Operations</h3>
*
* <p>Provides centralized thread pool management for asynchronous UI operations.
* All threads are daemon threads to ensure they don't prevent JVM shutdown.</p>
*
* <p><strong>AI Usage:</strong> Daemon thread factory pattern (preventing JVM shutdown
* blocking) discussed with AI guidance. All implementation by Andrea Sandnes.
*
* @author Andrea Sandnes
* @version 1.0
* @since 11.11.2025
*/
public class UiExecutors {
  private static final int THREAD_POOL_SIZE = 10;
  private static final ExecutorService INSTANCE = createExecutor();
  private static final ScheduledExecutorService SCHEDULED_INSTANCE =
          createScheduledExecutor();

  private UiExecutors() {}

  /**
  * Creates the shared fixed thread pool used for UI command execution.
  *
  * @return a daemon based fixed thread pool executor
  */
  private static ExecutorService createExecutor() {
    return Executors.newFixedThreadPool(THREAD_POOL_SIZE, new ThreadFactory() {

      private final AtomicInteger counter = new AtomicInteger(0);
      @Override
      public Thread newThread(Runnable r) {
        Thread t = new Thread(r, "ui-command-" + counter.incrementAndGet());
        t.setDaemon(true);
        return t;
      }
    });
  }

  /**
  * Submit a task for asynchronous execution using the shared UI executor.
  *
  * @param task the runnable task to execute
  */
  public static void execute(Runnable task) {
    INSTANCE.execute(task);
  }

  /**
  * Shutdown the all executor services managed by this class.
  */
  public static void shutDown() {
    INSTANCE.shutdown();
    SCHEDULED_INSTANCE.shutdown();
    try {
      if (!INSTANCE.awaitTermination(5, TimeUnit.SECONDS)) {
        INSTANCE.shutdownNow();
      }
      if (!SCHEDULED_INSTANCE.awaitTermination(5, TimeUnit.SECONDS)) {
        SCHEDULED_INSTANCE.shutdownNow();
      }
    } catch (InterruptedException e) {
      INSTANCE.shutdownNow();
      SCHEDULED_INSTANCE.shutdownNow();
      Thread.currentThread().interrupt();
    }
  }

  /**
  * Creates the shared scheduled executor used for delayed task execution.
  *
  * @return a daemon-based scheduled executor service
  */
  private static ScheduledExecutorService createScheduledExecutor() {
    return Executors.newScheduledThreadPool(2, new ThreadFactory() {
      private final AtomicInteger counter = new AtomicInteger(0);

      @Override
      public Thread newThread(Runnable r) {
        Thread t = new Thread(r, "ui-scheduled-" + counter.incrementAndGet());
        t.setDaemon(true);
        return t;
      }
    });
  }

  /**
  * Schedule a task to be executed after a specified delay.
  *
  * @param task the runnable task to schedule
  * @param delay the delay before execution begins
  * @param unit the time unit for the delay value
  */
  public static void schedule(Runnable task, long delay, TimeUnit unit) {
    SCHEDULED_INSTANCE.schedule(task, delay, unit);
  }

  /**
  * Schedule a task to be executed repeatedly with a fixed rate.
  *
  * @param task the runnable task to schedule
  * @param initialDelay the initial delay before first execution
  * @param period the period between successive executions
  * @param unit the time unit for delay and period values
  * @return a ScheduledFuture representing pending completion of the task
  */
  public static ScheduledFuture<?> scheduleAtFixedRate(Runnable task, long initialDelay,
      long period, TimeUnit unit) {
    return SCHEDULED_INSTANCE.scheduleAtFixedRate(task, initialDelay, period, unit);
  }
}