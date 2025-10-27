package edu.ntnu.bidata.smg.group8.broker.infra.network;

import edu.ntnu.bidata.smg.group8.common.protocol.Protocol;
import edu.ntnu.bidata.smg.group8.common.util.AppLogger;
import edu.ntnu.bidata.smg.group8.common.util.JsonBuilder;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;


/**
 * Handler for a connected TCP client.
 *
 * @see TcpServer
 */
final class ClientHandler implements Runnable {

  private static final Logger log = AppLogger.get(ClientHandler.class);

  // Heartbeat/idle settings
  private static final int READ_TIMEOUT_MS = 30_000; // 30 seconds
  private static final int MAX_IDLE_MISSES = 2;

  private final Socket socket;
  private final ConnectionRegistry registry;
  private final AtomicBoolean running = new AtomicBoolean(true);

  private volatile boolean handshaken = false;
  private String role = null;
  private String nodeId = null;
  private OutputStream registeredPanelOut;

  private long lastSeen = System.currentTimeMillis();
  private int idleMisses = 0;

  /**
   * Create a handler for the given socket and registry.
   *
   * @param socket the connected client socket
   * @param registry the connection registry
   */
  ClientHandler(Socket socket, ConnectionRegistry registry) {
    this.socket = socket;
    this.registry = registry;
    try {
      this.socket.setTcpNoDelay(true);
      this.socket.setKeepAlive(true);
      this.socket.setSoTimeout(READ_TIMEOUT_MS);
    } catch (IOException e) {
      log.warn("Failed to configure socket {}: {}", remote(), e.getMessage());
    }
  }

  /**
   * Run the client handler loop.
   */
  @Override
  public void run() {
    final String who = remote();
    log.info("Handler started for {}", who);

    try (InputStream in = socket.getInputStream(); OutputStream out = socket.getOutputStream()) {

      while (running.get()) {
        try {
          final byte[] frame = FrameCodec.readFrame(in); // may throw
          // SocketTimeoutException
          final String msg = FrameCodec.utf8(frame).trim();

          lastSeen = System.currentTimeMillis();
          idleMisses = 0;

          if (!handshaken) {
            if (!handleHandshake(msg, out, who)) {
              break;
            }
            continue;
          }

          processMessage(msg, out, who);

        } catch (SocketTimeoutException ste) {
          if (!onIdleTimeout(out, who)) {
            break;
          }
        }
      }

    } catch (EOFException eof) {
      log.info("Client {} closed the connection (EOF).", who);
    } catch (IOException ex) {
      if (running.get()) {
        log.warn("I/O error for {}: {}", who, ex.getMessage());
      }
    } finally {
      closeAndUnregister(who);
    }
  }

  // ---------- Helpers ----------
  /**
   * Returns false if handshake failed.
   *
   * @param msg the received message
   * @param out the output stream to reply on
   * @param who the client identifier
   * @return true if handshake succeeded
   * @throws IOException on I/O errors
   */
  private boolean handleHandshake(String msg, OutputStream out, String who) throws IOException {
    final String firstType = getType(msg);
    if (!(Protocol.TYPE_REGISTER_NODE.equals(firstType)
          || Protocol.TYPE_REGISTER_CONTROL_PANEL.equals(firstType))) {
      log.warn("First message from {} was not a REGISTER_* message: {}", who, msg);
      return false;
    }

    role = getField(msg, "role");
    nodeId = getField(msg, "nodeId");
    if (role == null || nodeId == null) {
      log.warn("REGISTER_* missing role/nodeId from {}: {}", who, msg);
      return false;
    }

    handshaken = true;

    final String ack = JsonBuilder.build("type", Protocol.TYPE_REGISTER_ACK, "protocolVersion",
            Protocol.PROTOCOL_VERSION, "role", role, "nodeId", nodeId, "message",
            "Registration successful");
    FrameCodec.writeFrame(out, ack.getBytes(StandardCharsets.UTF_8));
    log.info("Registration OK for {} role={} nodeId={}", who, role, nodeId);

    if (Protocol.ROLE_CONTROL_PANEL.equalsIgnoreCase(role)) {
      registry.registerPanel(out, who);
      registeredPanelOut = out;
      log.info("Panels connected: {}", registry.controlPanelCount());
    }
    return true;
  }

  /**
   * Process a received message.
   *
   * @param msg the received message
   * @param out the output stream to reply on
   * @param who the client identifier
   * @throws IOException on I/O errors
   */
  private void processMessage(String msg, OutputStream out, String who) throws IOException {
    final String type = getType(msg);

    if (Protocol.TYPE_HEARTBEAT.equals(type)) {
      handleHeartbeat(who);
      return;
    }

    log.info("From {} ({}/{}): {}", who, role, nodeId, msg);

    if (Protocol.TYPE_SENSOR_DATA.equals(type)) {
      handleSensorData(msg, who);
      return;
    }
    // unknown type -> ERROR handling could be added here
    log.warn("Unknown message type from {}: {}", who, type);
  }

  /**
   * Handle a received heartbeat.
   *
   * @param who the client identifier
   */
  private void handleHeartbeat(String who) {
    log.debug("Heartbeat received from {}", who);
  }

  /**
   * Handle a received sensor data message.
   *
   * @param msg the received message
   * @param who the client identifier
   */
  private void handleSensorData(String msg, String who) {
    if (!Protocol.ROLE_SENSOR_NODE.equalsIgnoreCase(role)) {
      log.warn("Rejected SENSOR_DATA from non-sensor {} ({})", who, role);
      return;
    }
    registry.broadcastToPanels(msg.getBytes(StandardCharsets.UTF_8));
  }

  /**
   * Handle an idle timeout.
   *
   * @param out the output stream to reply on
   * @param who the client identifier
   * @return true to continue, false to close the connection
   */
  private boolean onIdleTimeout(OutputStream out, String who) {
    idleMisses++;
    if (handshaken) {
      try {
        sendHeartbeat(out, who);
      } catch (IOException ioe) {
        log.warn("Failed to send HEARTBEAT to {}: {}", who, ioe.getMessage());
        return false;
      }
    }
    if (idleMisses > MAX_IDLE_MISSES) {
      log.info("Closing idle connection {} (role={}, nodeId={}) after {} misses", who, role,
                    nodeId, idleMisses);
      return false;
    }
    return true;
  }

  /**
   * Send a heartbeat message.
   *
   * @param out the output stream to send on
   * @param who the client identifier
   * @throws IOException on I/O errors
   */
  private void sendHeartbeat(OutputStream out, String who) throws IOException {
    final String hb = JsonBuilder.build("type", Protocol.TYPE_HEARTBEAT, "direction",
                Protocol.HB_SERVER_TO_CLIENT, "protocolVersion", Protocol.PROTOCOL_VERSION);
    FrameCodec.writeFrame(out, hb.getBytes(StandardCharsets.UTF_8));
    log.debug("HEARTBEAT → {} (idleMisses={})", who, idleMisses);
  }

  /**
   * Close the connection and unregister from the registry.
   *
   * @param who the client identifier
   * @throws IOException on I/O errors
   */
  private void closeAndUnregister(String who) {
    try {
      if (Protocol.ROLE_CONTROL_PANEL.equalsIgnoreCase(role) && registeredPanelOut != null) {
        registry.unregisterPanel(registeredPanelOut, who); // <-- use the same stream
        registeredPanelOut = null;
        log.info("Panels connected: {}", registry.controlPanelCount());
      }
    } catch (Exception ignored) {
      // ignore
    } finally {
      closeQuietly(); // close socket last
      log.info("Handler stopped for {}", who);
    }
  }

  /**
   * Stop the client handler.
   */
  void stop() {
    running.set(false);
    closeQuietly();
  }

  /**
   * Close the socket quietly.
   *
   * @throws IOException on I/O errors
   */
  private void closeQuietly() {
    try {
      socket.close();
    } catch (IOException ignored) {
      // ignore
    }
  }

  /**
   * Get the remote address of the socket.
   *
   * @return the remote address as string
   */
  private String remote() {
    return socket.getRemoteSocketAddress() != null ? socket.getRemoteSocketAddress().toString()
                : "unknown";
  }

  // --------- JSON helpers (temporary until real parser) ---------
  private static String getType(String json) {
    return getField(json, "type");
  }

  private static String getField(String json, String key) {
    Pattern p = Pattern.compile("\"" + key + "\"\\s*:\\s*\"([^\"]+)\"");
    Matcher m = p.matcher(json);
    return m.find() ? m.group(1) : null;
  }
}
