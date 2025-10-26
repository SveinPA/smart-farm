package edu.ntnu.bidata.smg.group8.broker.app;

import java.util.concurrent.CountDownLatch;

import org.slf4j.Logger;

import edu.ntnu.bidata.smg.group8.broker.infra.network.TcpServer;
import edu.ntnu.bidata.smg.group8.common.util.AppLogger;

public class BrokerMain {
  private static final Logger log = AppLogger.get(BrokerMain.class);
  
  public static void main(String[] args) {
    final int port = readPortFromEnvOrDefault("BROKER_PORT", 23048);
    final TcpServer server = new TcpServer(port);
    final CountDownLatch keepAlive = new CountDownLatch(1);

    /**
     * Shutdown hook to stop the server on JVM termination (Ctrl+C).
     * This ensures that resources are released properly.
     * 
     */
    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
      log.info("Shutdown signal received, stopping server...");
      server.stop();
      keepAlive.countDown();
    }, "Broker-Shutdown-hook"));

    log.info("Broker starting on port {}", port);
    server.start(); // Start the TCP server (Still skeleton implementation)
    log.info("Broker started. Press Ctrl+C to exit.");

    try {
      keepAlive.await(); // block main thread
    } catch (InterruptedException ie) {
      Thread.currentThread().interrupt();
    }
  }

  private static int readPortFromEnvOrDefault(String key, int def) {
    try {
      final String v = System.getenv(key);
      return (v == null || v.isBlank()) ? def : Integer.parseInt(v);
    } catch (NumberFormatException nfe) {
      return def;
    }
  }
}
