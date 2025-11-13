package edu.ntnu.bidata.smg.group8.sensor.infra;


import edu.ntnu.bidata.smg.group8.sensor.entity.actuators.HeaterActuator;
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * <h3>Unit Tests for ActuatorStatePacket.</h3>
 *
 * <p>This class test the behaviour of the {@link ActuatorStatePacket}class.
 * By testing the static build method, we ensure that JSON packets
 * representing actuator states are correctly constructed.</p>
 *
 * <h4>Positive Tests:</h4>
 * <ul>
 *     <li>Valid JSON structure with all required fields</li>
 *     <li>Correct value formatting to two decimal places</li>
 *     <li>Reasonable timestamp generation</li>
 * </ul>
 *
 * <h4>Negative Tests:</h4>
 * <ul>
 *     <li>Null, blank, or empty node ID</li>
 *     <li>Null actuator object</li>
 * </ul>
 *
 * @author Mona Amundsen
 * @version 03.11.25
 */
public class ActuatorStatePacketTest {

  // -------------- POSITIVE TESTS --------------
  /**
   * Tests that build returns a JSON string containing all required fields.
   *
   * <p>The test verifies that the JSON string produced by the build method
   * contains the expected fields: type, nodeId, actuatorKey,
   * currentValue, unit, and timestamp.</p>
   */
  @Test
  void testBuildContainsAllRequiredFields() {
    var actuator = new HeaterActuator();
    String json = ActuatorStatePacket.build("node-1", actuator);

    System.out.println("Generated JSON (actuator): " + json);
    assertTrue(json.contains("\"type\":\"ACTUATOR_COMMAND\""));
    assertTrue(json.contains("\"nodeId\":\"node-1\""));
    assertTrue(json.contains("\"actuatorKey\":\"heater\""));

    // Verify currentValue is present and formatted to two decimals
    String expectedFormatted = String.format(Locale.US, "\"currentValue\":\"%.2f\"",
            actuator.getCurrentValue());
    assertTrue(json.contains(expectedFormatted));
    assertTrue(json.contains("\"unit\":\"°C\""));
    assertTrue(json.contains("\"timestamp\":"));
  }

  /**
   * Tests that value formatting uses two decimal places.
   *
   * <p>We assert the currentValue string matches the actuator´s current
   * value formatted with Locale.Us and two decimals</p>
   */
  @Test
  void testBuildValueFormatting() {
    var actuator = new HeaterActuator();
    String json = ActuatorStatePacket.build("node-1", actuator);

    String expectedFormatted = String.format(Locale.US, "\"currentValue\":\"%.2f\"",
            actuator.getCurrentValue());
    assertTrue(json.contains(expectedFormatted));
  }

  /**
   * Tests that the timestamp is within a reasonable range.
   *
   * <p>Expected outcome: The timestamp in the generated JSON
   * should be between the time just before and just after
   * the build call.</p>
   */
  @Test
  void testTimestampIsReasonable() {
    var actuator = new HeaterActuator();
    long before = System.currentTimeMillis();
    String json = ActuatorStatePacket.build("node-1", actuator);
    long after = System.currentTimeMillis();

    // Extract timestamp (simple string search for testing)
    String[] parts = json.split("\"timestamp\":\"");
    assertTrue(parts.length > 1, "timestamp field not found");
    String timestampStr = parts[1].split("\"")[0];
    long timestamp = Long.parseLong(timestampStr);

    assertTrue(timestamp >= before && timestamp <= after);
  }

  // -------------- NEGATIVE TESTS --------------

  /**
   * Test that build throws exception when node ID is null.
   *
   * <p>Expected outcome: An IllegalArgumentException is thrown
   * when a null nodeId is provided to the build method.</p>
   */
  @Test
  void testBuildWithNullNodeId() {
    var actuator = new HeaterActuator();

    IllegalArgumentException exception = assertThrows(
          IllegalArgumentException.class,
          () -> ActuatorStatePacket.build(null, actuator)
    );
    assertEquals("Node ID cannot be null or blank", exception.getMessage());
  }

  /**
   * Tests that build throws exception when node ID is blank.
   *
   * <p>Expected outcome: An IllegalArgumentException is thrown
   * when a blank nodeId is provided to the build method.</p>
   */
  @Test
  void testBuildWithBlankNodeId() {
    var actuator = new HeaterActuator();
    IllegalArgumentException exception = assertThrows(
          IllegalArgumentException.class,
          () -> ActuatorStatePacket.build("   ", actuator)
    );
    assertEquals("Node ID cannot be null or blank", exception.getMessage());
  }

  /**
   * Test that build throws exception when node ID is empty.
   *
   * <p> Expected outcome: An IllegalArgumentException is thrown
   * when an empty nodeId is provided to the build method.</p>
   */
  @Test
  void testBuildWithEmptyNodeId() {
    var actuator = new HeaterActuator();

    IllegalArgumentException exception = assertThrows(
          IllegalArgumentException.class,
          () -> ActuatorStatePacket.build("", actuator)
    );
    assertEquals("Node ID cannot be null or blank", exception.getMessage());
  }

  /**
   * Tests that build throws exception when actuator is null.
   *
   * <p> Expected outcome: An IllegalArgumentException is thrown
   * when a null actuator is provided to the build method.</p>
   */
  @Test
  void testBuildWithNullActuator() {
    IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> ActuatorStatePacket.build("node-1", null)
    );
    assertEquals("Actuator cannot be null", exception.getMessage());
  }
}
