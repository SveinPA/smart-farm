package edu.ntnu.bidata.smg.group8.broker.infra.network;

import edu.ntnu.bidata.smg.group8.common.protocol.Protocol;
import edu.ntnu.bidata.smg.group8.common.util.AppLogger;
import edu.ntnu.bidata.smg.group8.common.util.JsonBuilder;
import edu.ntnu.bidata.smg.group8.common.protocol.FrameCodec;
import edu.ntnu.bidata.smg.group8.common.protocol.MessageParser;
import edu.ntnu.bidata.smg.group8.common.protocol.dto.RegisterMessage;
import edu.ntnu.bidata.smg.group8.common.protocol.dto.ActuatorCommandMessage;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;


/**
 * Handles a single TCP client connection in a dedicated thread.
 *
 * <p>This handler manages the complete lifecycle of a client connection:
 * <ol>
 *   <li><strong>Handshake:</strong> Requires REGISTER_NODE or REGISTER_CONTROL_PANEL as first message</li>
 *   <li><strong>Registration:</strong> Registers client with ConnectionRegistry based on role</li>
 *   <li><strong>Message Processing:</strong> Routes messages according to protocol and client role</li>
 *   <li><strong>Cleanup:</strong> Unregisters client and closes socket on disconnect</li>
 * </ol>
 *
 * <p><strong>Message Routing:</strong>
 * <ul>
 *   <li><strong>SENSOR_DATA:</strong> Broadcast from sensor nodes to all control panels</li>
 *   <li><strong>ACTUATOR_COMMAND:</strong> Route from control panels to specific sensor nodes</li>
 *   <li><strong>HEARTBEAT:</strong> Keep-alive messages (logged only, resets timeout)</li>
 *   <li><strong>NODE_CONNECTED/DISCONNECTED:</strong> Lifecycle events broadcast to control panels</li>
 * </ul>
 *
 * <p><strong>Role-Based Validation:</strong>
 * <ul>
 *   <li>Only sensor nodes can send SENSOR_DATA messages</li>
 *   <li>Only control panels can send ACTUATOR_COMMAND messages</li>
 *   <li>Invalid role/message combinations are rejected and logged</li>
 * </ul>
 *
 * <p><strong>Heartbeat and Timeout Mechanism:</strong>
 * <ul>
 *   <li>Socket read timeout: 30 seconds (READ_TIMEOUT_MS)</li>
 *   <li>Any received message resets timeout counter and lastSeen timestamp</li>
 *   <li>On idle timeout (no message for 30s), broker sends HEARTBEAT to client</li>
 *   <li>After 2 consecutive idle timeouts (MAX_IDLE_MISSES), connection is closed</li>
 *   <li>Total idle time before disconnect: 60 seconds (2 × 30s)</li>
 * </ul>
 *
 * <p><strong>Thread Safety:</strong> Each client runs in its own thread (via Runnable).
 * The handler can be stopped externally via {@link #stop()}, which sets the running flag
 * and closes the socket, causing the message loop to exit gracefully.
 *
 * <p><strong>Error Handling:</strong>
 * <ul>
 *   <li>EOFException: Client closed connection (logged as info)</li>
 *   <li>SocketTimeoutException: Triggers heartbeat mechanism</li>
 *   <li>IOException: Logged as warning, connection closed</li>
 *   <li>Unknown message types: Logged as warning, connection continues</li>
 * </ul>
 *
 * @see TcpServer
 * @see ConnectionRegistry
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
  private OutputStream registeredSensorOut;

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
    final RegisterMessage register = MessageParser.parseRegister(msg);
    final String firstType = register.getType();

    if (!(Protocol.TYPE_REGISTER_NODE.equals(firstType)
            || Protocol.TYPE_REGISTER_CONTROL_PANEL.equals(firstType))) {
      log.warn("First message from {} was not a REGISTER_* message: {}", who, msg);
      return false;
    }

    role = register.getRole();
    nodeId = register.getNodeId();
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
    if (Protocol.ROLE_SENSOR_NODE.equalsIgnoreCase(role)) {
      registry.registerSensorNode(nodeId, out, who);
      registeredSensorOut = out;
      log.info("Sensor nodes connected: {}", registry.sensorNodeCount());
      // Broadcast node connected event to all control panels
      broadcastNodeEvent(Protocol.TYPE_NODE_CONNECTED, nodeId);
    }
    return true;
  }

  /**
   * Broadcast a node lifecycle event (connected/disconnected) to all control panels.
   * 
   * @param eventType the event type constant (TYPE_NODE_CONNECTED or TYPE_NODE_DISCONNECTED)
   * @param nodeId the node ID that triggered the event
   */
  private void broadcastNodeEvent(String eventType, String nodeId) {
    final String event = JsonBuilder.build(
      "type", eventType,
      "nodeId", nodeId,
      "timestamp", String.valueOf(System.currentTimeMillis())
    );
    registry.broadcastToPanels(event.getBytes(StandardCharsets.UTF_8));
    log.info("Broadcasted {} for node {}", eventType, nodeId);
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
    final String type = MessageParser.getType(msg);

    if (Protocol.TYPE_HEARTBEAT.equals(type)) {
      handleHeartbeat(who);
      return;
    }

    log.info("From {} ({}/{}): {}", who, role, nodeId, msg);

    if (Protocol.TYPE_SENSOR_DATA.equals(type)) {
      handleSensorData(msg, who);
      return;
    }

    if (Protocol.TYPE_ACTUATOR_COMMAND.equals(type)) {
      handleActuatorCommand(msg, who);
      return;
    }

    if (Protocol.TYPE_ACTUATOR_STATUS.equals(type)) {
      handleActuatorStatus(msg, who);
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
   * Handle an actuator command message.
   * Routes the command from a control panel to the target sensor node.
   * 
   * @param msg the received message
   * @param who the client identifier
   */
  private void handleActuatorCommand(String msg, String who) {
    // Only control panels should send actuator commands
    if (!Protocol.ROLE_CONTROL_PANEL.equalsIgnoreCase(role)) {
      log.warn("Rejected ACTUATOR_COMMAND from non-panel {} ({})", who, role);
      return;
    }

    // Parse the command to extract the target node
    ActuatorCommandMessage cmd = MessageParser.parseActuatorCommand(msg);
    String targetNode = cmd.getTargetNode();

    if (targetNode == null || targetNode.trim().isEmpty()) {
      log.warn("ACTUATOR_COMMAND from {} missing targetNode: {}", who, msg);
      return;
    }

    // Look up the sensor node's output stream
    OutputStream targetOut = registry.getSensorNodeStream(targetNode);
    if (targetOut == null) {
      log.warn("ACTUATOR_COMMAND from {} to unknown/disconnected node {}", who, targetNode);
      // TODO: Send ERROR response to control panel
      return;
    }

    // Forward the command to the sensor node
    try {
      FrameCodec.writeFrame(targetOut, msg.getBytes(StandardCharsets.UTF_8));
      log.info("Routed ACTUATOR_COMMAND from {} to sensor node {}", who, targetNode);
    } catch (IOException e) {
      log.error("Failed to forward ACTUATOR_COMMAND to {}: {}", targetNode, e.getMessage());
      // TODO: Send ERROR response to control panel
    }
  }

  /**
   * Handle an actuator status message.
   * Broadcasts actuator state from sensor nodes to control panel.
   *
   * @param msg the received message
   * @param who the client identifier
   */
  private void handleActuatorStatus(String msg, String who) {
    // Only sensor nodes should send actuator status
    if (!Protocol.ROLE_SENSOR_NODE.equalsIgnoreCase(role)) {
      log.warn("Rejected ACTUATOR_STATUS from non-sensor {} ({})", who, role);
      return;
    }
    
    // Broadcast to all control panels (just like sensor data)
    registry.broadcastToPanels(msg.getBytes(StandardCharsets.UTF_8));
    log.debug("Broadcasted ACTUATOR_STATUS from {} to all panels", who);
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

      if (Protocol.ROLE_SENSOR_NODE.equalsIgnoreCase(role) && registeredSensorOut != null) {
        registry.unregisterSensorNode(nodeId, who);
        registeredSensorOut = null;
        log.info("Sensor nodes connected: {}", registry.sensorNodeCount());
        // Broadcast node disconnected event to all control panels
        broadcastNodeEvent(Protocol.TYPE_NODE_DISCONNECTED, nodeId);
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
}
