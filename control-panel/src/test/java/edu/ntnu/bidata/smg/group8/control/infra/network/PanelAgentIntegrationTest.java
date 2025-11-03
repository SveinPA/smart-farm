package edu.ntnu.bidata.smg.group8.control.infra.network;

import edu.ntnu.bidata.smg.group8.common.protocol.Protocol;
import edu.ntnu.bidata.smg.group8.control.logic.state.StateSnapshot;
import edu.ntnu.bidata.smg.group8.control.logic.state.StateStore;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
* Integration tests for PanelAgent that verify end-to-end communication
* with a mock broker server.
*
* <p>These tests simulate real network communication by spinning up a local server
* that acts as a broker, then connecting a PanelAgent to it. This allows us to
* test the complete message flow without depending on an actual broker being
* available.</p>
*
* <p>Each test starts a mock broker on a random port, creates a PanelAgent that
* connects to it, and then verifies the message exchange using CountDownLatch
* to coordinate between threads. The tests have a 5 second timeout to
* prevent hanging if something goes wrong.</p>
*
* @author Andrea Sandnes
* @Version 31.10.2025
*/
public class PanelAgentIntegrationTest {

  /**
  * This tests the full lifecycle of PanelAgent communication with a broker.
  *
  * <p>This test walks through a realistic scenario:</p>
  * <ul>
  *   <li>A mock broker starts listening on a random port</li>
  *   <li>A PanelAgent connects and sends a registration message</li>
  *   <li>The broker acknowledges and sends back sensor data</li>
  *   <li>The agent stores the sensor data in its StateStore</li>
  *   <li>The agent sends an actuator command to the broker</li>
  *   <li>The broker receives and validates the command</li>
  * </ul>
  *
  * @throws Exception if network errors occur, timeouts are exceeded, or assertions fail
  */
  @Test
  @Timeout(5)
  void panelAgentRegistersReceivesSensorDataAndSendActuatorCommand()
          throws Exception {

    // Starting a mock broker on a random available port
    try (ServerSocket server = new ServerSocket(0)) {
      int port = server.getLocalPort();
      CountDownLatch registerReceived = new CountDownLatch(1);
      CountDownLatch commandReceived = new CountDownLatch(1);

      // Mock broker runs in a separate thread
      Thread broker = new Thread(() -> {
        try (Socket client = server.accept()) {
          InputStream in = client.getInputStream();
          OutputStream out = client.getOutputStream();

          // Read and validate the registration message
          byte[] regFrame = ClientFrameCodec.readFrame(in);
          String regJson = new String(regFrame, StandardCharsets.UTF_8);
          if (regJson.contains(Protocol.TYPE_REGISTER_CONTROL_PANEL)) {
            registerReceived.countDown();
          }

          // Send back some sensor data for the agent to process
          String sensorJson =
                  "{\"type\":\"" + Protocol.TYPE_SENSOR_DATA + "\","
                  + "\"nodeId\":\"7\",\"sensorKey\":\"temperature\",\"value\":\"22.9\","
                  + "\"unit\":\"C\",\"timestamp\":\"1730380000000\"}";
          ClientFrameCodec.writeFrame(out, sensorJson.getBytes(StandardCharsets.UTF_8));

          // Read the actuator command that the agent sends
          byte[] cmdFrame = ClientFrameCodec.readFrame(in);
          String cmdJson = new String(cmdFrame, StandardCharsets.UTF_8);
          if (cmdJson.contains(Protocol.TYPE_ACTUATOR_COMMAND)
                  && cmdJson.contains("\"nodeId\":\"7\"")
                  && cmdJson.contains("\"actuator\":\"heater\"")
                  && cmdJson.contains("\"action\":\"SET\"")
                  && cmdJson.contains("\"value\":\"30\"")) {
            commandReceived.countDown();
          }
        } catch (Exception ignored) { }
      }, "mock-broker");
      broker.setDaemon(true);
      broker.start();

      // Create and starts the PanelAgent
      StateStore store = new StateStore();
      PanelAgent agent = new PanelAgent("127.0.0.1", port, "panel-1", store);
      agent.start();

      // Verify that registration happened
      assertTrue(registerReceived.await(1, TimeUnit.SECONDS), "Did not receive REGISTER");

      // Give the agent a moment to process the sensor data
      Thread.sleep(150);

      // Verify the sensor data was stored correctly
      StateSnapshot snap = store.snapshot();
      assertEquals(1, snap.sensors().size());
      assertEquals("temperature", snap.sensors().get(0).type());
      assertEquals("22.9", snap.sensors().get(0).value());

      // send an actuator command
      agent.sendActuatorCommand("7", "heater", "SET", 30);

      // Verify that the command was received by the broker
      assertTrue(commandReceived.await(1, TimeUnit.SECONDS), "Did not receive ACTUATOR_COMMAND");

      agent.close();
    }
  }
}

