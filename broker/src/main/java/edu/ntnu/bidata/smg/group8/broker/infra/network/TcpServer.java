package edu.ntnu.bidata.smg.group8.broker.infra.network;

import edu.ntnu.bidata.smg.group8.common.util.AppLogger;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;


/**
 * TCP listener for the broker.
 *
 * <p>Implements a multi-threaded TCP server using a dual executor architecture:
 * <ul>
 *   <li>Single-thread executor for the accept loop (isolates blocking {@code accept()} calls)</li>
 *   <li>Cached thread pool for client handlers (thread-per-connection)</li>
 * </ul>
 * 
 * <p>Thread-safe start/stop lifecycle management using {@link AtomicBoolean} with 
 * compareAndSet for idempotency. Server can be safely started and stopped multiple times.
 * 
 * <p>Port validation enforces IANA registered port range
 * {@value #MIN_APP_PORT}..{@value #MAX_APP_PORT} to avoid conflicts with system ports
 * and ephemeral ports. Default port (used by BrokerMain): 23048
 * 
 * <p><strong>AI Usage:</strong> Developed with AI assistance (Claude Code) for designing
 * the dual executor architecture (accept loop isolation + client thread pool) and
 * implementing thread-safe lifecycle management with AtomicBoolean compareAndSet patterns.
 * Accept loop shutdown coordination (graceful error handling when socket closes during 
 * stop) and ServerSocket configuration (SO_REUSEADDR, backlog) discussed with AI guidance.
 * All implementation and testing by Svein Antonsen.
 * 
 * @author Svein Antonsen
 * @since 1.0
 * @see ClientHandler
 * @see ConnectionRegistry
 */
public final class TcpServer {
  private static final Logger log = AppLogger.get(TcpServer.class);

  // Lowest allowed app port, inclusive.
  public static final int MIN_APP_PORT = 1024;
  // Highest allowed app port, inclusive.
  public static final int MAX_APP_PORT = 49151;

  private final int port;
  private final AtomicBoolean running = new AtomicBoolean(false);
  private final ConnectionRegistry registry = new ConnectionRegistry();

  private volatile ServerSocket serverSocket;
  private ExecutorService acceptExecutor;
  private ExecutorService clientExecutor;

  /**
   * Create a server bound to {@code port}.
   *
   * @throws IllegalArgumentException if port is outside 1024..49151 (inclusive)
   */
  public TcpServer(int port) {
    validateAppPort(port);
    this.port = port;
  }

  /**
   * Validate that the given port is in the allowed application port range.
   */
  public static void validateAppPort(int port) {
    if (port < MIN_APP_PORT || port > MAX_APP_PORT) {
      throw new IllegalArgumentException(
                "Invalid port " + port + " (allowed " + MIN_APP_PORT + ".." + MAX_APP_PORT + ")"
      );
    }
  }

  /**
   * Start the listener.
   *
   * @throws RuntimeException if the server socket could not be opened
   */
  public void start() {
    if (!running.compareAndSet(false, true)) {
      log.warn("TcpServer is already running.");
      return;
    }
    log.info("TcpServer starting on port {}", port);

    try {
      // Bind server socket
      ServerSocket ss = new ServerSocket();
      ss.setReuseAddress(true);
      ss.bind(new InetSocketAddress(port), /*backlog*/50);
      this.serverSocket = ss;

      // Executors
      acceptExecutor = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "tcp-accept-loop");
        t.setDaemon(true);
        return t;
      });
      clientExecutor = Executors.newCachedThreadPool(r -> {
        Thread t = new Thread(r, "tcp-client-" + System.nanoTime());
        t.setDaemon(true);
        return t;
      });

      acceptExecutor.execute(this::acceptLoop);
      log.info("TcpServer started; listening on port {}", port);

    } catch (IOException e) {
      running.set(false);
      closeQuietly(serverSocket);
      log.error("Failed to start TcpServer on port {}: {}", port, e.getMessage());
      throw new RuntimeException("Failed to start TcpServer", e);
    }
  }

  /**
   * Stop the listener.
   */
  public void stop() {
    if (!running.compareAndSet(true, false)) {
      return; // already stopped
    }
    log.info("TcpServer stopping...");
    closeQuietly(serverSocket);

    if (acceptExecutor != null) {
      acceptExecutor.shutdownNow();
    }
    if (clientExecutor != null) {
      clientExecutor.shutdownNow();
    }

    log.info("TcpServer stopped.");
  }

  // -------------------- internals --------------------

  /**
   * Main accept loop.
   */
  private void acceptLoop() {
    Objects.requireNonNull(serverSocket, "serverSocket");
    while (running.get()) {
      try {
        Socket client = serverSocket.accept();

        String remote = (client.getRemoteSocketAddress() != null)
                        ? client.getRemoteSocketAddress().toString()
                        : "unknown";
        log.debug("Accepted connection from {}", remote);

        clientExecutor.submit(new ClientHandler(client, registry));

      } catch (IOException e) {
        if (running.get()) {
          log.warn("Accept loop error: {}", e.getMessage());
        } else {
          // expected when stopping (serverSocket.close() unblocks accept)
          log.debug("Accept loop exiting due to stop.");
        }
        if (!running.get()) {
          break;
        } 
      }
    }
  }

  /**
   * Close server socket quietly.
   * 
   * @param ss the server socket to close
   */
  private static void closeQuietly(ServerSocket ss) {
    if (ss != null) {
      try { 
        ss.close(); 
      } catch (IOException ignored) {
        // ignore
      }
    }
  }

  // --------- Exposed for tests/diagnostics (package-private) ---------

  int getPort() { return port; }
  boolean isRunning() { return running.get(); }
  ConnectionRegistry registry() { return registry; }
}
