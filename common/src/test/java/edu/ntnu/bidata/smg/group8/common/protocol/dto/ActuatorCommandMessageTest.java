package edu.ntnu.bidata.smg.group8.common.protocol.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for ActuatorCommandMessage DTO.
 */
class ActuatorCommandMessageTest {
  
  @Test
  void testConstructor_AllFieldsPresent() {
    // Arrange
    String type = "ACTUATOR_COMMAND";
    String targetNode = "42";
    String actuator = "fan";
    String action = "ON";
    String value = "75";

    // Act
    ActuatorCommandMessage msg = new ActuatorCommandMessage(type, targetNode, actuator, action, value);

    // Assert
    assertNotNull(msg);
    assertEquals(type, msg.getType());
    assertEquals(targetNode, msg.getTargetNode());
    assertEquals(actuator, msg.getActuator());
    assertEquals(action, msg.getAction());
  }

  @Test
  void testConstructor_NullTargetNode() {
    // Arrange - simulates a message forwarded to sensor node (no targetNode)
    String type = "ACTUATOR_COMMAND";
    String actuator = "window";
    String action = "open";
    String value = "50";

    // Act
    ActuatorCommandMessage msg = new ActuatorCommandMessage(type, null, actuator, action, value);

    // Assert
    assertNotNull(msg);
    assertEquals(type, msg.getType());
    assertNull(msg.getTargetNode());
    assertEquals(actuator, msg.getActuator());
    assertEquals(action, msg.getAction());
    assertEquals(value, msg.getValue());
  }

  @Test
  void testToString_AllFieldsPresent() {
    // Arrange
    ActuatorCommandMessage msg = new ActuatorCommandMessage(
      "ACTUATOR_COMMAND",
      "42", 
      "heater", 
      "OFF",
      "0"
      );

    // Act
    String result = msg.toString();

    // Assert
    assertNotNull(result);
    assertTrue(result.contains("ACTUATOR_COMMAND"));
    assertTrue(result.contains("42"));
    assertTrue(result.contains("heater"));
    assertTrue(result.contains("OFF"));
    // Value is *not* printed in toString(), so we do NOT check for it
  }

  @Test
  void testToString_NullTargetNode() {
    // Arrange
    ActuatorCommandMessage msg = new ActuatorCommandMessage(
      "ACTUATOR_COMMAND", 
      null, 
      "fan", 
      "ON",
      "100"
      );

    // Act
    String result = msg.toString();

    // Assert
    assertNotNull(result);
    assertTrue(result.contains("ACTUATOR_COMMAND"));
    assertTrue(result.contains("null")); // toString should handle null gracefully
    assertTrue(result.contains("fan"));
    assertTrue(result.contains("ON"));
  }

  @Test
  void testDifferentActuatorTypes() {
    // Arrange and Act
    ActuatorCommandMessage fan = new ActuatorCommandMessage(
      "ACTUATOR_COMMAND", "1", "fan", "ON", "100");
    ActuatorCommandMessage window = new ActuatorCommandMessage(
      "ACTUATOR_COMMAND", "2", "window", "open", "50");
    ActuatorCommandMessage heater = new ActuatorCommandMessage(
      "ACTUATOR_COMMAND", "3", "heater", "OFF", "0");
    ActuatorCommandMessage valve = new ActuatorCommandMessage(
      "ACTUATOR_COMMAND", "4", "valve", "close", "0");

    // Assert
    assertEquals("fan", fan.getActuator());
    assertEquals("window", window.getActuator());
    assertEquals("heater", heater.getActuator());
    assertEquals("valve", valve.getActuator());
  }
}
