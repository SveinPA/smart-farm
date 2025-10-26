package edu.ntnu.bidata.smg.group8.broker.infra.network;

import edu.ntnu.bidata.smg.group8.common.protocol.Protocol;
import edu.ntnu.bidata.smg.group8.common.util.JsonBuilder;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
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
    void startServer() throws IOException {
        try (ServerSocket tmp = new ServerSocket(0)) {
            port = tmp.getLocalPort();
        }
        server = new TcpServer(port);
        server.start();
    }

    @AfterEach
    void stopServer() {
        server.stop();
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
            String sAck = readJson(sensor);
            assertTrue(sAck.contains(Protocol.TYPE_REGISTER_ACK));

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
