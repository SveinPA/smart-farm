package edu.ntnu.bidata.smg.group8.sensor.logic;

import edu.ntnu.bidata.smg.group8.common.protocol.Protocol;
import edu.ntnu.bidata.smg.group8.sensor.entity.actuators.FanActuator;
import edu.ntnu.bidata.smg.group8.sensor.entity.actuators.HeaterActuator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * <h3>Unit Tests for CommandHandler.</h3>
 *
 * <p>This class contains unit tests for the {@link CommandHandler} class,
 * specifically testing the handling of incoming JSON messages from the broker.</p>
 *
 * <p>The tests cover both positive scenarios, ensuring correct processing
 * of valid messages, as well as negative scenarios that validate
 * error handling for malformed or invalid inputs.</p>
 *
 * <h4>Positive Tests:</h4>
 * <ul>
 *     <li>Handling of heartbeat messages</li>
 *     <li>Handling of valid actuator command messages</li>
 * </ul>
 *
 * <h4>Negative Tests:</h4>
 * <ul>
 *     <li>Handling of unknown message types</li>
 *     <li>Handling of malformed JSON input</li>
 *     <li>Handling of empty or null JSON input</li>
 *     <li>Handling of actuator commands with missing fields</li>
 *     <li>Handling of actuator commands with invalid values</li>
 * </ul>
 *
 * @author Mona Amundsen
 * @version 03.11.25
 */
public class CommandHandlerTest {
  private static  final String NODE_ID = "test-node-1";
  private DeviceCatalog catalog;
  private CommandHandler handler;
  private HeaterActuator heater;
  private FanActuator fan;

  /**
   * Sets up the environment before each test, by
   * initializing a DeviceCatalog and CommandHandler with
   * a Heater and Fan actuator.
   */
  @BeforeEach
  void setUp() {
    catalog = new DeviceCatalog();
    heater = new HeaterActuator();
    fan = new FanActuator();
    // Add actuators to catalog
    catalog.addActuator(heater);
    catalog.addActuator(fan);
    handler = new CommandHandler(catalog, NODE_ID);
  }

  // ----------------- POSITIVE TESTS -----------------

  /**
   * Tests handling of a heartbeat message.
   *
   * <p>Verifies that when a heartbeat message is received,
   * the handler processes it correctly and returns no response.</p>
   */
  @Test
  void testHandleHeartbeatMessage() {
    String heartbeatMessage = "{\"type\":\"" + Protocol.TYPE_HEARTBEAT + "\"}";
    String response = handler.handleMessage(heartbeatMessage);

    assertNull(response, "Heartbeat should not return a response");
  }

  /**
   * Tests handling of a valid actuator command message.
   *
   * <p>Verifies that when a valid actuator command message is received,
   * the handler processes it correctly and returns an acknowledgment
   * message with status OK.</p>
   */
  @Test
  void testHandleValidActuatorCommand() {
    String commandMessage = "{\"type\":\"" + Protocol.TYPE_ACTUATOR_COMMAND + "\","
            + "\"actuatorKey\":\"heater\",\"value\":\"25.0\"}";
    String response = handler.handleMessage(commandMessage);

    assertNotNull(response);
    assertTrue(response.contains("\"status\":\"OK\""));
    assertTrue(response.contains("\"actuatorKey\":\"heater\""));
    assertTrue(response.contains("\"value\":25.0"));
  }

  // ----------------- NEGATIVE TESTS -----------------

  /**
   * Tests handling of an unknown message type.
   *
   * <p>Expected outcome: The handler should return null
   * for unknown message types.</p>
   */
  @Test
  void testHandleUnknownMessageType() {
    String unknownMessage = "{\"type\":\"UNKNOWN_TYPE\",\"foo\":\"bar\"}";
    String response = handler.handleMessage(unknownMessage);

    assertNull(response, "Unknown message types should return null");
  }

  /**
   * Tests handling of malformed JSON input.
   *
   * <p>Expected outcome: The handler should return
   * an error response indicating a parsing error.</p>
   */
  @Test
  void testHandleMalformedJson() {
    String malformedJson = "{\"type\":\"ACTUATOR_COMMAND\",\"actuatorKey\":\"heater\",\"value\":";
    String response = handler.handleMessage(malformedJson);

    assertNotNull(response);
    assertTrue(response.contains("\"status\":\"ERROR\""));
    assertTrue(response.contains("Error handling message"));
  }

  /**
   * Test handling of empty JSON input.
   *
   * <p>Expected outcome: The handler should return
   * an error response indicating a parsing error.</p>
   */
  @Test
  void testHandleEmptyJson() {
    String emptyJson = "";
    String response = handler.handleMessage(emptyJson);

    assertNotNull(response);
    assertTrue(response.contains("\"status\":\"ERROR\""));
    assertTrue(response.contains("Error handling message"));
  }

  /**
   * Test handling of null JSON input.
   *
   * <p>Expected outcome: The handler should return
   * an error response indicating a parsing error.</p>
   */
  @Test
  void testHandleNullJson() {
    String response = handler.handleMessage(null);

    assertNotNull(response);
    assertTrue(response.contains("\"status\":\"ERROR\""));
    assertTrue(response.contains("Error handling message"));
  }

  /**
   * Test handling of actuator command missing actuatorKey.
   *
   * <p>Expected outcome: The handler should return an error
   * response indicating missing actuatorKey.</p>
   */
  @Test
  void testMissingActuatorKeyInCommand() {
    String command = "{\"type\":\"ACTUATOR_COMMAND\",\"value\":\"25.0\"}";
    String response = handler.handleMessage(command);

    assertNotNull(response);
    assertTrue(response.contains("\"status\":\"ERROR\""));
    assertTrue(response.contains("Missing actuatorKey in command"));
  }

  /**
   * Test handling of actuator command missing value.
   *
   * <p>Expected outcome: The handler should return an error, when
   * the value field is missing in the command.</p>
   */
  @Test
  void testMissingValueInCommand() {
      String command = "{\"type\":\"ACTUATOR_COMMAND\",\"actuatorKey\":\"heater\"}";
      String response = handler.handleMessage(command);

      assertNotNull(response);
      assertTrue(response.contains("\"status\":\"ERROR\""));
      assertTrue(response.contains("Missing value in command"));
  }

  /**
   * Test handling of actuator command with invalid numeric value.
   *
   * <p>Expected outcome: The handler should return an error response
   * indicating invalid numeric value.</p>
   */
  @Test
  void testInvalidNumericValue() {
    String command = "{\"type\":\"ACTUATOR_COMMAND\","
            + "\"actuatorKey\":\"heater\",\"value\":\"not-a-number\"}";
    String response = handler.handleMessage(command);

    assertNotNull(response);
    assertTrue(response.contains("\"status\":\"ERROR\""));
    assertTrue(response.contains("Invalid numeric value"));
  }
}
