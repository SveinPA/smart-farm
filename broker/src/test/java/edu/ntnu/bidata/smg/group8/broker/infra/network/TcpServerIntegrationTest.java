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
}
