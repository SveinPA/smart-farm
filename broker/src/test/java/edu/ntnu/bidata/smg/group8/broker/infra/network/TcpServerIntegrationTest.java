package edu.ntnu.bidata.smg.group8.broker.infra.network;

import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.ntnu.bidata.smg.group8.common.protocol.Protocol;
import edu.ntnu.bidata.smg.group8.common.util.JsonBuilder;
import java.io.IOException;
import java.io.OutputStream;
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

      // Sensor REGISTER
      writeJson(sensor, Jsons.registerNode("dev-1"));
      String sensorAck = readJson(sensor);
      assertTrue(sensorAck.contains(Protocol.TYPE_REGISTER_ACK));

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
}
