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
  private final String panelId;
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
  * @param panelId the unique identifier for this control panel
  * @param state the state store for managing sensor data and system state
  * @throws NullPointerException if host, panelId, or state is null
  */
  public PanelAgent(String host, int port, String panelId, StateStore state) {
    this.host = Objects.requireNonNull(host);
    this.port = port;
    this.panelId = Objects.requireNonNull(panelId);
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
              "panelId", panelId);

      ClientFrameCodec.writeFrame(out, ClientFrameCodec.utf8(reg));
      log.info("REGISTER_CONTROL_PANEL sent for {}", panelId);

      reader = new Thread(this::readLoop, "panel-agent-reader");
      reader.setDaemon(true);
      reader.start();
    } catch (IOException e) {
      running.set(false);
      try {
        if (socket != null) {
          socket.close();
        }
      } catch (Exception ignored) {}
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
    String type = msg.get("type");
    if (type == null) {
      log.debug("Ignoring message without type: {}", json);
      return;
    }

    switch (type) {
      case Protocol.TYPE_SENSOR_DATA ->  {
        String nodeId = msg.get("nodeId");
        String sensorKey = msg.get("sensorKey");
        String value = msg.get("value");
        String unit = msg.get("unit");
        long ts = parseLong(msg.get("timestamp"), System.currentTimeMillis());
        state.applySensor(nodeId, sensorKey, value, unit, Instant.ofEpochMilli(ts));
      }

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
  * @param nodeId the identifier of the target sensor node
  * @param actuator the identifier of the actuator to control
  * @param action the action to perform (e.g, "set", "on", "off")
  * @param valueOrNull optional value parameter for the action, or
  *                    null if not needed.
  * @throws IOException if an I/O error occurs when writing to the
  * output stream
  */
  public synchronized void sendActuatorCommand(String nodeId, String actuator,
                                               String action, String valueOrNull)
          throws IOException {
    String payload = (valueOrNull != null)
            ? JsonBuilder.build(
                    "type", Protocol.TYPE_ACTUATOR_COMMAND,
            "panelId", panelId,
            "nodeId", nodeId,
            "actuator", actuator,
            "action", action,
            "value", valueOrNull
    )
            : JsonBuilder.build(
                    "type", Protocol.TYPE_ACTUATOR_COMMAND,
            "panelId", panelId,
            "nodeId", nodeId,
            "actuator", actuator,
            "action", action
    );

    ClientFrameCodec.writeFrame(out, ClientFrameCodec.utf8(payload));
    log.debug("ACTUATOR_COMMAND sent: {}", payload);
  }

  /**
  * Sends an actuator command with an integer value to a specific node
  * through the broker.
  *
  * @param nodeId the identifier of the target sensor node
  * @param actuator the identifier of the actuator to control
  * @param action the action to perform (e.g., "SET", "ON", "OFF")
  * @param value the integer value parameter for the action
  * @throws IOException IOException if an I/O error occurs when writing to the
  *                     output stream
  */
  public void sendActuatorCommand(String nodeId, String actuator, String action,
                                  int value) throws IOException {
    sendActuatorCommand(nodeId, actuator, action, String.valueOf(value));
  }

  /**
  * Sends an actuator command with a floating-point value to a specific
  * node through the broker.
   *
  * @param nodeId the identifier of the target sensor node
  * @param actuator the identifier of the actuator to control
  * @param action the action to perform (e.g., "SET", "ON", "OFF")
  * @param value the floating-point value parameter for the action
  * @throws IOException IOException if an I/O error occurs when writing to the
  *                     output stream
  */
  public void sendActuatorCommand(String nodeId, String actuator,
                                  String action, double value) throws IOException {
    sendActuatorCommand(nodeId, actuator, action, String.valueOf(value));
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
    } catch (Exception ignored) {}
    try {
      if (out != null) {
        out.close();
      }
    } catch (Exception ignored) {}
    try {
      if (socket != null) {
        socket.close();
      }
    } catch (Exception ignored) {}
  }
}

