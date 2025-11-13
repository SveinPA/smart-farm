package edu.ntnu.bidata.smg.group8.control.infra.network;

import edu.ntnu.bidata.smg.group8.common.protocol.Protocol;
import edu.ntnu.bidata.smg.group8.common.util.AppLogger;
import edu.ntnu.bidata.smg.group8.common.util.JsonBuilder;
import edu.ntnu.bidata.smg.group8.control.logic.state.StateStore;
import edu.ntnu.bidata.smg.group8.control.util.FlatJson;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;

/**
* Manages network communication between a control panel and a broker server.
* This agent handles registration, sensor data reception, and actuator command
* transmission for a specific control panel in the smart greenhouse system.
*
* <p>The agent establishes a socket connection to the broker, registers
* the control panel, and maintains a dedicated reader thread to process
* incoming messages. It supports automatic resource cleanup through the
* AutoCloseable interface</p>
*
* <p>Thread-safety: The sendActuatorsCommand method is synchronized to ensure
* thread-safe writes to the output stream</p>
*
* @author Andrea Sandnes
* @version 30.10.25
*/
public class PanelAgent implements AutoCloseable {
  private static final Logger log = AppLogger.get(PanelAgent.class);

  private final String host;
  private final int port;
  private final String nodeId;
  private final StateStore state;

  private final AtomicBoolean running = new AtomicBoolean(false);

  private Socket socket;
  private InputStream in;
  private OutputStream out;
  private Thread reader;

  /**
  * Constructs a new PanelAgent for managing communication with a broker.
  *
  * @param host the hostname or IP address of the broker server
  * @param port the port number on which the broker is listening
  * @param nodeId the unique identifier for this control panel
  * @param state the state store for managing sensor data and system state
  * @throws NullPointerException if host, nodeId, or state is null
  */
  public PanelAgent(String host, int port, String nodeId, StateStore state) {
    this.host = Objects.requireNonNull(host);
    this.port = port;
    this.nodeId = Objects.requireNonNull(nodeId);
    this.state = Objects.requireNonNull(state);
  }

  /**
  * Starts the panel agent by establishing a connection to the broker and
  * registering this control panel. Initiates a daemon reader thread to
  * process incoming messages.
  *
  * @throws IOException if an I/O error occurs when creating the socket
  * or sending the registration message
  */
  public void start() throws IOException {
    if (running.getAndSet(true)) {
      return;
    }
    try {
      socket = new Socket(host, port);
      in = socket.getInputStream();
      out = socket.getOutputStream();

      String reg = JsonBuilder.build(
              "type", Protocol.TYPE_REGISTER_CONTROL_PANEL,
              "protocolVersion", Protocol.PROTOCOL_VERSION,
              "role", Protocol.ROLE_CONTROL_PANEL,
              "nodeId", nodeId);

      ClientFrameCodec.writeFrame(out, ClientFrameCodec.utf8(reg));
      log.info("REGISTER_CONTROL_PANEL sent for {}", nodeId);

      reader = new Thread(this::readLoop, "panel-agent-reader");
      reader.setDaemon(true);
      reader.start();
    } catch (IOException e) {
      running.set(false);
      try {
        if (socket != null) {
          socket.close();
        }
      } catch (IOException closeEx) {
        log.debug("Failed to close socket during cleanup: {}", closeEx.getMessage());
      }
      throw e;
    }
  }

  /**
  * Continuously reads frames from the input stream and processes
  * incoming messages. This method runs in a separate daemon thread
  * and continues until the agent is stopped or an I/O error occurs.
  */
  private void readLoop() {
    try {
      while (running.get()) {
        byte[] frame = ClientFrameCodec.readFrame(in);
        String json = ClientFrameCodec.utf8(frame);
        handleIncoming(json);
      }
    } catch (IOException e) {
      if (running.get()) {
        log.error("Read loop error: {}", e.getMessage());
      } else {
        log.debug("Read loop closed.");
      }
    }
  }

  /**
  * Processes an incoming JSON message from the broker and takes
  * appropriate action based on the message type.
  *
  * <p>Supported message types:</p>
  * <ul>
  *     <li>SENSOR_DATA - updates the state store with new sensor readings</li>
  *     <li>HEARTBEAT - logs the heartbeat for connection monitoring</li>
  *     <li>ERROR - Logs error messages from the broker</li>
  *     <li>REGISTER_ACK - logs successful registration acknowledgment</li>
  * </ul>
  *
  * @param json the JSON-formatted message string to process
  */
  private void handleIncoming(String json) {
    Map<String, String> msg = FlatJson.parse(json);
    if (msg.isEmpty()) {
      log.warn("Invalid/unsupported JSON frame, ignoring: {}", json);
      return;
    }
    String type = msg.get("type");
    if (type == null) {
      log.debug("Missing 'type' in message, ignoring: {}", json);
      return;
    }

    switch (type) {
      case Protocol.TYPE_SENSOR_DATA ->  {
        String srcNodeId = msg.get("nodeId");
        String sensorKey = msg.get("sensorKey");
        String value = msg.get("value");
        String unit = msg.get("unit");
        long ts = parseLong(msg.get("timestamp"), System.currentTimeMillis());

        if (srcNodeId == null || sensorKey == null || value == null) {
          log.warn("Missing required fields in SENSOR_DATA: {}", json);
          return;
        }
        state.touchNodeSeen(srcNodeId);
        state.applySensor(srcNodeId, sensorKey, value, unit, Instant.ofEpochMilli(ts));
      }

      case Protocol.TYPE_ACTUATOR_STATE -> {
        String srcNodeId = msg.get("nodeId");
        String actuatorKey = msg.get("actuator");
        String current = msg.get("state");
        long ts = parseLong(msg.get("timestamp"), System.currentTimeMillis());

        if (srcNodeId == null || actuatorKey == null || current == null) {
          log.warn("Missing required fields in ACTUATOR_STATE: {}", json);
          return;
        }
        state.touchNodeSeen(srcNodeId);
        state.applyActuator(srcNodeId, actuatorKey, current, Instant.ofEpochMilli(ts));
        log.debug("ACTUATOR_STATE applied nodeId={} actuator={} state={}", srcNodeId, actuatorKey, current);
      }

//      case Protocol.TYPE_NODE_LIST -> {
//        String csv = msg.get("nodes");
//        state.replaceAllNodesFromCsv(csv);
//        log.info("NODE_LIST received: {}", csv);
//      }

      case Protocol.TYPE_NODE_CONNECTED -> {
        String nid = msg.get("nodeId");
        if (nid != null) {
          state.setNodeOnline(nid, true);
          log.info("Node connected: {}", nid);
        }
      }

      case Protocol.TYPE_NODE_DISCONNECTED -> {
        String nid = msg.get("nodeId");
        if (nid != null) {
          state.setNodeOnline(nid, false);
          log.warn("Node disconnected: {}", nid);
        }
      }

   //   case Protocol.TYPE_COMMAND_ACK -> {
   //     String cmdId = msg.get("commandId");
   //     String target = msg.get("nodeId");
   //     String accepted = msg.get("accepted");
   //     String reason = msg.get("reason");
   //     if (cmdId != null) {
   //       boolean ok = "true".equalsIgnoreCase(accepted);
   //       state.markCommandAccepted(cmdId, target, ok, reason);
   //       if (ok) {
   //         log.info("COMMAND_ACK OK [{}] node={}", cmdId, target);
   //       } else {
   //         log.warn("COMMAND_ACK REJECTED [{}] node={} reason={}", cmdId, target, reason);
   //       }
   //     } else {
   //         log.info("COMMAND_ACK (no commandId) {}", json);
   //       }
   //     }


      case Protocol.TYPE_HEARTBEAT -> {
        log.trace("HEARTBEAT received: {}", json);
      }

      case Protocol.TYPE_ERROR -> {
        log.warn("ERROR from broker: {}", msg.getOrDefault("message", json));
      }

      case Protocol.TYPE_REGISTER_ACK -> {
        log.info("REGISTER_ACK: {}", json);
      }

      default -> {
        log.debug("Unhandled message type: {}", type);
      }
    }
  }

  /**
  * Parses a string to a long value, returning a default value if
  * parsing fails.
  *
  * @param s the string to parse
  * @param def the default value to return if parsing fails
  * @return the parsed long value, or the default value if parsing
  */
  private static long parseLong(String s, long def) {
    try {
      return Long.parseLong(s);
    } catch (Exception e) {
      return def;
    }
  }

  /**
  * Sends an actuator command to a specific node through the broker.
  * This method is synchronized to ensure thread-safe access to the
  * output stream.
  *
  * @param targetNode the identifier of the target sensor node
  * @param actuator the identifier of the actuator to control
  * @param action the action to perform (e.g, "set", "on", "off")
  * @param valueOrNull optional value parameter for the action, or
  *                    null if not needed.
  * @throws IOException if an I/O error occurs when writing to the
  * output stream
  */
  public synchronized void sendActuatorCommand(String targetNode, String actuator,
                                               String action, String valueOrNull)
          throws IOException {
    final String actionField = (valueOrNull != null ? valueOrNull : action);
    String payload = JsonBuilder.build(
                    "type", Protocol.TYPE_ACTUATOR_COMMAND,
            "panelId", this.nodeId,
            "targetNode", targetNode,
            "actuator", actuator,
            "action", actionField
    );

    log.info("Sending ACTUATOR_COMMAND: {}", payload);

    if (out == null) {
      throw new IOException("OutputStream is null - not connected");
    }

    try {
      ClientFrameCodec.writeFrame(out, ClientFrameCodec.utf8(payload));
      out.flush();
      log.info("ACTUATOR_COMMAND sent successfully");
    } catch (IOException e) {
      log.error("Failed to send ACTUATOR_COMMAND: {}", payload, e);
      throw e;
    }
  }


  /**
  * Sends an actuator command with an integer value to a specific node
  * through the broker.
  *
  * @param targetNode the identifier of the target sensor node
  * @param actuator the identifier of the actuator to control
  * @param action the action to perform (e.g., "SET", "ON", "OFF")
  * @param value the integer value parameter for the action
  * @throws IOException IOException if an I/O error occurs when writing to the
  *                     output stream
  */
  public void sendActuatorCommand(String targetNode, String actuator, String action,
                                  int value) throws IOException {
    sendActuatorCommand(targetNode, actuator, action, String.valueOf(value));
  }

  /**
  * Sends an actuator command with a floating-point value to a specific
  * node through the broker.
   *
  * @param targetNode the identifier of the target sensor node
  * @param actuator the identifier of the actuator to control
  * @param action the action to perform (e.g., "SET", "ON", "OFF")
  * @param value the floating-point value parameter for the action
  * @throws IOException IOException if an I/O error occurs when writing to the
  *                     output stream
  */
  public void sendActuatorCommand(String targetNode, String actuator,
                                  String action, double value) throws IOException {
    sendActuatorCommand(targetNode, actuator, action, String.valueOf(value));
  }

  /**
  * Closes the panel agent and releases all associated resources.
  * Stops the reader thread and closes the socket connection to the broker.
  */
  @Override
  public void close() {
    running.set(false);
    try {
      if (in != null) {
        in.close();
      }
    } catch (Exception e) {
      log.debug("Error closing input stream: {}", e.getMessage());
    }
    try {
      if (out != null) {
        out.close();
      }
    } catch (Exception e) {
      log.debug("Error closing output stream: {}", e.getMessage());
    }
    try {
      if (socket != null) {
        socket.close();
      }
    } catch (Exception e) {
      log.debug("Error closing socket: {}", e.getMessage());
    }
  }
}

