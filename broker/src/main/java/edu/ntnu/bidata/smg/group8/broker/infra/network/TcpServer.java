package edu.ntnu.bidata.smg.group8.broker.infra.network;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;

import edu.ntnu.bidata.smg.group8.common.util.AppLogger;

public class TcpServer {
  private static final Logger log = AppLogger.get(TcpServer.class);

  private final int port;
  private final AtomicBoolean running = new AtomicBoolean(false);
  private final ConnectionRegistry registry = new ConnectionRegistry();

  private volatile ServerSocket serverSocket;
  private ExecutorService acceptExecutor;
  private ExecutorService clientExecutor;

  public TcpServer(int port) {
    if (port < 1 || port > 65535) {
      throw new IllegalArgumentException("Ivalid port: " + port);
    }
    this.port = port;
  }

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
      ss.bind(new InetSocketAddress(port), 50);
      this.serverSocket = ss;

      // Single-threaded accept loop
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
      log.error("Failed to start TcpServver on port {}: {}", port, e.getMessage());
      throw new RuntimeException("Failed to start TcpServer", e);
    }
  }

  public void stop() {
    if(!running.compareAndSet(true, false)) {
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

  private void acceptLoop() {
    Objects.requireNonNull(serverSocket, "serverSocket");
    while (running.get()) {
      try {
        Socket client = serverSocket.accept();

        String remote = client.getRemoteSocketAddress() != null
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
        // if serverSocket was cloes due to stop(), break loop
        if (!running.get()) break;
      }
    }
  }

  private static void closeQuietly(ServerSocket ss) {
    if (ss != null) {
      try {
        ss.close();
      } catch (IOException ignored) {
        // ignore
      }
    }
  }

  private static void closeQuietly(Socket s) {
    if (s != null) {
      try {
        s.close();
      } catch (IOException ignored) {
        // ignore
      }
    }
  }
}
