package edu.ntnu.bidata.smg.group8.broker.app;

import edu.ntnu.bidata.smg.group8.broker.infra.network.TcpServer;
import edu.ntnu.bidata.smg.group8.common.util.AppLogger;
import java.util.concurrent.CountDownLatch;
import org.slf4j.Logger;


/**
 * Main class for the Broker application.
 * 
 * <p>Entry point for the Smart Farm broker server. Configures and starts the TCP server
 * on port 23048 (default) or custom port via {@code BROKER_PORT} environment variable.
 * 
 * <p>Implements graceful shutdown handling using a JVM shutdown hook that cleanly stops 
 * the server when Ctrl+C is pressed. Uses {@link CountDownLatch} to keep the main thread
 * alive while the server runs in background threads.
 * 
 * <p><strong>AI usage:</strong> Developed with AI assistance (Claude Code) for designing
 * the graceful shutdown mechanism (shutdown hook + CountDownLatch coordination) to ensure
 * clean server termination on exit signals. Port validation and environment configuration
 * pattern discussed with AI guidance. All implementation and testing by Svein Antonsen.
 * 
 * @author Svein Antonsen
 * @since 1.0
 * @see TcpServer
 */
public class BrokerMain {
  private static final Logger log = AppLogger.get(BrokerMain.class);

  /**
   * Main entry point for the Broker application.
   */
  public static void main(String[] args) {
    final int port = readPortFromEnvOrDefault("BROKER_PORT", 23048);
    TcpServer.validateAppPort(port);
    final TcpServer server = new TcpServer(port);
    final CountDownLatch keepAlive = new CountDownLatch(1);

    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
      log.info("Shutdown signal received, stopping server...");
      server.stop();
      keepAlive.countDown();
    }, "Broker-Shutdown-hook"));

    log.info("Broker starting on port {}", port);
    server.start();
    log.info("Broker started. Press Ctrl+C to exit.");

    try {
      keepAlive.await(); // block main thread
    } catch (InterruptedException ie) {
      Thread.currentThread().interrupt();
    }
  }

  /**
   * Read an integer port from environment variable or return default.
   *
   * @param key the environment variable key
   * @param def the default port if env var is not set or invalid
   */
  private static int readPortFromEnvOrDefault(String key, int def) {
    try {
      final String v = System.getenv(key);
      return (v == null || v.isBlank()) ? def : Integer.parseInt(v);
    } catch (NumberFormatException nfe) {
      return def;
    }
  }
}
