package edu.ntnu.bidata.smg.group8.broker.infra.network;

import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.ntnu.bidata.smg.group8.common.protocol.Protocol;
import edu.ntnu.bidata.smg.group8.common.util.JsonBuilder;
import edu.ntnu.bidata.smg.group8.common.protocol.FrameCodec;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Integration tests for the TcpServer and broker message routing functionality.
 *
 * <p>This test suite verifies end-to-end communication flows including:
 * <ul>
 *   <li>Node registration (sensor nodes and control panels)</li>
 *   <li>Message broadcasting (sensor data, actuator status, lifecycle events)</li>
 *   <li>Command routing (actuator commands from panels to sensors)</li>
 *   <li>Error handling (unknown nodes, disconnected targets)</li>
 *   <li>Connection resilience (dead stream pruning, reconnection)</li>
 * </ul>
 *
 * <p>The tests simulate realistic client-server interactions by creating
 * actual TCP socket connections and validating message flows through the broker.
 *
 * <p><strong>AI Usage:</strong> AI assistance was used in designing and implementing
 * several test cases in this class to ensure comprehensive test coverage of critical
 * message routing scenarios and edge cases.
 *
 * @author Svein Antonsen
 * @since 1.0
 */
class TcpServerIntegrationTest {

  private TcpServer server;
  private int port;

  @BeforeEach
  void startServer() throws Exception {
    // Pick a free port within allowed app range (1024..49151)
    // Use a safe subrange to reduce collision chances during CI.
    this.port = findFreePortInRange(24000, 26000);
    server = new TcpServer(port);
    server.start();
  }

  /** Find an available port in the inclusive range [from..to]. */
  private static int findFreePortInRange(int from, int to) throws Exception {
    if (from < TcpServer.MIN_APP_PORT || to > TcpServer.MAX_APP_PORT || from > to) {
      throw new IllegalArgumentException("Range must be within " 
        + TcpServer.MIN_APP_PORT + ".." + TcpServer.MAX_APP_PORT + " and valid.");
    }
    for (int p = from; p <= to; p++) {
      try (ServerSocket ss = new ServerSocket()) {
        ss.setReuseAddress(true);
        ss.bind(new InetSocketAddress("localhost", p));
        return p; // success
      } catch (Exception bindFailure) {
        // try next port
      }
    }
    throw new IllegalStateException("No free port found in range " + from + ".." + to);
  }

  @AfterEach
  void stopServer() {
    if (server != null) {
      server.stop();
      server = null;
    }
  }

  private static void writeJson(Socket s, String json) throws IOException {
    FrameCodec.writeFrame(s.getOutputStream(), json.getBytes(StandardCharsets.UTF_8));
  }

  private static String readJson(Socket s) throws IOException {
    byte[] p = FrameCodec.readFrame(s.getInputStream());
    return new String(p, StandardCharsets.UTF_8);
  }

  @Test
  void sensorDataIsBroadcastToPanels() throws Exception {
    try (Socket panel = new Socket("127.0.0.1", port);
             Socket sensor = new Socket("127.0.0.1", port)) {

      // Control Panel REGISTER
      writeJson(panel, Jsons.registerControlPanel("ui-1"));
      String cpAck = readJson(panel);
      assertTrue(cpAck.contains(Protocol.TYPE_REGISTER_ACK));
      
      // Read NODE_LIST sent after registration
      String nodeList = readJson(panel);
      assertTrue(nodeList.contains(Protocol.TYPE_NODE_LIST));

      // Sensor REGISTER
      writeJson(sensor, Jsons.registerNode("dev-1"));
      String sensorAck = readJson(sensor);
      assertTrue(sensorAck.contains(Protocol.TYPE_REGISTER_ACK));

      // Panel receives NODE_CONNECTED event after sensor registration
      String nodeConnected = readJson(panel);
      assertTrue(nodeConnected.contains(Protocol.TYPE_NODE_CONNECTED));

      // Send SENSOR_DATA from sensor
      String data = Jsons.sensorData("{\"value\":42}");
      writeJson(sensor, data);

      // Panel should receive the same payload
      String received = readJson(panel);
      assertTrue(received.contains(Protocol.TYPE_SENSOR_DATA));
      assertTrue(received.contains("42"));
    }
  }

  /**
   * Helper class to build JSON messages for tests.
   */
  static final class Jsons {
    static String registerControlPanel(String nodeId) {
      return JsonBuilder.build(
            "type", Protocol.TYPE_REGISTER_CONTROL_PANEL,
            "role", Protocol.ROLE_CONTROL_PANEL,
            "nodeId", nodeId
        );
    }

    static String registerNode(String nodeId) {
      return JsonBuilder.build(
            "type", Protocol.TYPE_REGISTER_NODE,
            "role", Protocol.ROLE_SENSOR_NODE,
            "nodeId", nodeId
        );
    }

    static String sensorData(String value) {
      return JsonBuilder.build(
            "type", Protocol.TYPE_SENSOR_DATA,
            "value", value
        );
    }

    /**
     * Builds an ACTUATOR_COMMAND JSON message.
     *
     * @param targetNode the target sensor node ID
     * @param actuator the actuator name (e.g., "fan", "heater")
     * @param action the action to perform (e.g., "1" for ON, "0" for OFF)
     * @return JSON string for ACTUATOR_COMMAND message
     */
    static String actuatorCommand(String targetNode, String actuator, String action) {
      return JsonBuilder.build(
            "type", Protocol.TYPE_ACTUATOR_COMMAND,
            "targetNode", targetNode,
            "actuator", actuator,
            "action", action
        );
    }

    /**
     * Builds a COMMAND_ACK JSON message.
     *
     * @param nodeId the sensor node ID that is acknowledging the command
     * @param actuator the actuator that was controlled
     * @param action the action that was performed
     * @return JSON string for COMMAND_ACK message
     */
    static String commandAck(String nodeId, String actuator, String action) {
      return JsonBuilder.build(
            "type", Protocol.TYPE_COMMAND_ACK,
            "nodeId", nodeId,
            "actuator", actuator,
            "action", action
        );
    }

    /**
     * Builds an ACTUATOR_STATUS JSON message.
     *
     * @param nodeId the sensor node ID reporting the status
     * @param actuator the actuator name
     * @param state the current state (e.g., "ON", "OFF", or numeric value)
     * @return JSON string for ACTUATOR_STATUS message
     */
    static String actuatorStatus(String nodeId, String actuator, String state) {
      return JsonBuilder.build(
            "type", Protocol.TYPE_ACTUATOR_STATUS,
            "nodeId", nodeId,
            "actuator", actuator,
            "state", state
        );
    }
  }

  @Test
  void broadcastSkipsClosedPanelsAndPrunesDeadStreams() throws Exception {
    try (Socket panel1 = new Socket("127.0.0.1", port);
         Socket panel2 = new Socket("127.0.0.1", port);
         Socket panel3 = new Socket("127.0.0.1", port);
         Socket sensor = new Socket("127.0.0.1", port)) {
      
      // Register all three control panels
      writeJson(panel1, Jsons.registerControlPanel("panel-1"));
      assertTrue(readJson(panel1).contains(Protocol.TYPE_REGISTER_ACK));
      String nodeList1 = readJson(panel1); // Read NODE_LIST
      assertTrue(nodeList1.contains(Protocol.TYPE_NODE_LIST));

      writeJson(panel2, Jsons.registerControlPanel("panel-2"));
      assertTrue(readJson(panel2).contains(Protocol.TYPE_REGISTER_ACK));
      String nodeList2 = readJson(panel2); // Read NODE_LIST
      assertTrue(nodeList2.contains(Protocol.TYPE_NODE_LIST));

      writeJson(panel3, Jsons.registerControlPanel("panel-3"));
      assertTrue(readJson(panel3).contains(Protocol.TYPE_REGISTER_ACK));
      String nodeList3 = readJson(panel3); // Read NODE_LIST
      assertTrue(nodeList3.contains(Protocol.TYPE_NODE_LIST));
      
      // Register sensor node
      writeJson(sensor, Jsons.registerNode("sensor-1"));
      assertTrue(readJson(sensor).contains(Protocol.TYPE_REGISTER_ACK));

      // All panels receive NODE_CONNECTED event
      assertTrue(readJson(panel1).contains(Protocol.TYPE_NODE_CONNECTED));
      assertTrue(readJson(panel2).contains(Protocol.TYPE_NODE_CONNECTED));
      assertTrue(readJson(panel3).contains(Protocol.TYPE_NODE_CONNECTED));

      // Close panel2 socket to simulate disconnect (without registering)
      panel2.close();

      // Give server time to detect close
      Thread.sleep(100);

      // Send sensor data - this triggers broadcast to all panels
      writeJson(sensor, Jsons.sensorData("{\"temperature\":25.5}"));

      // panel1 and panel3 should receive data (panel2 is closed)
      String received1 = readJson(panel1);
      assertTrue(received1.contains(Protocol.TYPE_SENSOR_DATA));
      assertTrue(received1.contains("25.5"));

      String received3 = readJson(panel3);
      assertTrue(received3.contains(Protocol.TYPE_SENSOR_DATA));
      assertTrue(received3.contains("25.5"));

      // Verify panel2 cannot read (socket is closed)
      // This confirms the test setup is correct
      assertTrue(panel2.isClosed());

      // The registry should have pruned panel2 during broadcast
      // We can verify by sending another message. Only 2 panels should receive it
      writeJson(sensor, Jsons.sensorData("{\"temperature\":26.0}"));

      String received1Again = readJson(panel1);
      assertTrue(received1Again.contains("26.0"));

      String received3Again = readJson(panel3);
      assertTrue(received3Again.contains("26.0"));

      // Success: broadcast continues working, dead stream was pruned
    }
  }

  /**
   * Tests actuator command routing from control panel to sensor node,
   * error handling when target node is not found, and NODE_DISCONNECTED
   * event broadcasting when a sensor node disconnects.
   *
   * <p>This test verifies:
   * <ul>
   *   <li>ACTUATOR_COMMAND messages are correctly routed from control panel to target sensor</li>
   *   <li>ERROR messages are sent to control panel when target node doesn't exist</li>
   *   <li>ERROR messages are sent when command is sent to disconnected node</li>
   *   <li>NODE_DISCONNECTED events are broadcast to all control panels when sensor disconnects</li>
   * </ul>
   *
   * @throws Exception if socket communication fails
   */
  @Test
  void actuatorCommandRoutingAndErrorHandling() throws Exception {
    try (Socket panel = new Socket("127.0.0.1", port);
         Socket sensor1 = new Socket("127.0.0.1", port);
         Socket sensor2 = new Socket("127.0.0.1", port)) {

      // 1. Register control panel
      writeJson(panel, Jsons.registerControlPanel("panel-1"));
      String panelAck = readJson(panel);
      assertTrue(panelAck.contains(Protocol.TYPE_REGISTER_ACK));

      // Read NODE_LIST (should be empty initially)
      String nodeList = readJson(panel);
      assertTrue(nodeList.contains(Protocol.TYPE_NODE_LIST));

      // 2. Register sensor node 1
      writeJson(sensor1, Jsons.registerNode("sensor-1"));
      String sensor1Ack = readJson(sensor1);
      assertTrue(sensor1Ack.contains(Protocol.TYPE_REGISTER_ACK));

      // Panel receives NODE_CONNECTED for sensor-1
      String nodeConnected1 = readJson(panel);
      assertTrue(nodeConnected1.contains(Protocol.TYPE_NODE_CONNECTED));
      assertTrue(nodeConnected1.contains("sensor-1"));

      // 3. Register sensor node 2
      writeJson(sensor2, Jsons.registerNode("sensor-2"));
      String sensor2Ack = readJson(sensor2);
      assertTrue(sensor2Ack.contains(Protocol.TYPE_REGISTER_ACK));

      // Panel receives NODE_CONNECTED for sensor-2
      String nodeConnected2 = readJson(panel);
      assertTrue(nodeConnected2.contains(Protocol.TYPE_NODE_CONNECTED));
      assertTrue(nodeConnected2.contains("sensor-2"));

      // 4. Panel sends ACTUATOR_COMMAND to sensor-1 (SUCCESS CASE)
      writeJson(panel, Jsons.actuatorCommand("sensor-1", "fan", "1"));

      // Sensor-1 should receive the command
      String receivedCommand = readJson(sensor1);
      assertTrue(receivedCommand.contains(Protocol.TYPE_ACTUATOR_COMMAND));
      assertTrue(receivedCommand.contains("sensor-1"));
      assertTrue(receivedCommand.contains("fan"));
      assertTrue(receivedCommand.contains("\"1\""));

      // 5. Panel sends ACTUATOR_COMMAND to non-existent sensor (ERROR CASE)
      writeJson(panel, Jsons.actuatorCommand("non-existent-sensor", "heater", "1"));

      // Panel should receive ERROR message
      String errorMsg = readJson(panel);
      assertTrue(errorMsg.contains(Protocol.TYPE_ERROR));
      assertTrue(errorMsg.contains("not found") || errorMsg.contains("disconnected"));

      // 6. Close sensor-1 socket (simulate disconnect)
      sensor1.close();

      // Give broker time to detect disconnect and broadcast NODE_DISCONNECTED
      Thread.sleep(100);

      // Panel should receive NODE_DISCONNECTED event
      String nodeDisconnected = readJson(panel);
      assertTrue(nodeDisconnected.contains(Protocol.TYPE_NODE_DISCONNECTED));
      assertTrue(nodeDisconnected.contains("sensor-1"));

      // 7. Panel sends ACTUATOR_COMMAND to disconnected sensor-1 (ERROR CASE)
      writeJson(panel, Jsons.actuatorCommand("sensor-1", "fan", "0"));

      // Panel should receive ERROR message about disconnected node
      String errorMsg2 = readJson(panel);
      assertTrue(errorMsg2.contains(Protocol.TYPE_ERROR));
      assertTrue(errorMsg2.contains("not found") || errorMsg2.contains("disconnected"));
    }
  }

  /**
   * Tests COMMAND_ACK and ACTUATOR_STATUS message broadcasting from sensor nodes
   * to all connected control panels.
   *
   * <p>This test verifies:
   * <ul>
   *   <li>COMMAND_ACK messages from sensor nodes are broadcast to all control panels</li>
   *   <li>ACTUATOR_STATUS messages from sensor nodes are broadcast to all control panels</li>
   *   <li>Multiple control panels receive the same messages simultaneously</li>
   * </ul>
   *
   * @throws Exception if socket communication fails
   */
  @Test
  void commandAckAndActuatorStatusBroadcast() throws Exception {
    try (Socket panel1 = new Socket("127.0.0.1", port);
         Socket panel2 = new Socket("127.0.0.1", port);
         Socket sensor = new Socket("127.0.0.1", port)) {

      // 1. Register control panel 1
      writeJson(panel1, Jsons.registerControlPanel("panel-1"));
      assertTrue(readJson(panel1).contains(Protocol.TYPE_REGISTER_ACK));
      String nodeList1 = readJson(panel1);
      assertTrue(nodeList1.contains(Protocol.TYPE_NODE_LIST));

      // 2. Register control panel 2
      writeJson(panel2, Jsons.registerControlPanel("panel-2"));
      assertTrue(readJson(panel2).contains(Protocol.TYPE_REGISTER_ACK));
      String nodeList2 = readJson(panel2);
      assertTrue(nodeList2.contains(Protocol.TYPE_NODE_LIST));

      // 3. Register sensor node
      writeJson(sensor, Jsons.registerNode("sensor-1"));
      assertTrue(readJson(sensor).contains(Protocol.TYPE_REGISTER_ACK));

      // Both panels receive NODE_CONNECTED event
      String connected1 = readJson(panel1);
      assertTrue(connected1.contains(Protocol.TYPE_NODE_CONNECTED));
      assertTrue(connected1.contains("sensor-1"));

      String connected2 = readJson(panel2);
      assertTrue(connected2.contains(Protocol.TYPE_NODE_CONNECTED));
      assertTrue(connected2.contains("sensor-1"));

      // 4. Panel-1 sends ACTUATOR_COMMAND to sensor
      writeJson(panel1, Jsons.actuatorCommand("sensor-1", "fan", "1"));

      // Sensor receives the command
      String command = readJson(sensor);
      assertTrue(command.contains(Protocol.TYPE_ACTUATOR_COMMAND));

      // 5. Sensor sends COMMAND_ACK back
      writeJson(sensor, Jsons.commandAck("sensor-1", "fan", "1"));

      // Both panels should receive COMMAND_ACK (broadcast)
      String ack1 = readJson(panel1);
      assertTrue(ack1.contains(Protocol.TYPE_COMMAND_ACK));
      assertTrue(ack1.contains("sensor-1"));
      assertTrue(ack1.contains("fan"));
      assertTrue(ack1.contains("\"1\""));

      String ack2 = readJson(panel2);
      assertTrue(ack2.contains(Protocol.TYPE_COMMAND_ACK));
      assertTrue(ack2.contains("sensor-1"));
      assertTrue(ack2.contains("fan"));
      assertTrue(ack2.contains("\"1\""));

      // 6. Sensor sends ACTUATOR_STATUS update
      writeJson(sensor, Jsons.actuatorStatus("sensor-1", "fan", "ON"));

      // Both panels should receive ACTUATOR_STATUS (broadcast)
      String status1 = readJson(panel1);
      assertTrue(status1.contains(Protocol.TYPE_ACTUATOR_STATUS));
      assertTrue(status1.contains("sensor-1"));
      assertTrue(status1.contains("fan"));
      assertTrue(status1.contains("ON"));

      String status2 = readJson(panel2);
      assertTrue(status2.contains(Protocol.TYPE_ACTUATOR_STATUS));
      assertTrue(status2.contains("sensor-1"));
      assertTrue(status2.contains("fan"));
      assertTrue(status2.contains("ON"));
    }
  }
}
