package edu.ntnu.bidata.smg.group8.common.protocol;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import edu.ntnu.bidata.smg.group8.common.protocol.dto.HeartbeatMessage;
import edu.ntnu.bidata.smg.group8.common.protocol.dto.RegisterMessage;
import edu.ntnu.bidata.smg.group8.common.protocol.dto.SensorDataMessage;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for MessageParser
 */
class MessageParserTest {

  // ========== getType() Tests ==========

  @Test
  void testGetType_RegisterNode() {
    String json = "{\"type\":\"REGISTER_NODE\",\"role\":\"SENSOR_NODE\",\"nodeId\":\"dev-1\"}";
    String type = MessageParser.getType(json);
    assertEquals("REGISTER_NODE", type);
  }

  @Test
  void testGetType_SensorData() {
    String json = "{\"type\":\"SENSOR_DATA\",\"nodeId\":\"dev-1\",\"sensorKey\":\"temp-1\",\"value\":\"22.5\"}";
    String type = MessageParser.getType(json);
    assertEquals("SENSOR_DATA", type);
  }

  @Test
  void testGetType_Heartbeat() {
    String json = "{\"type\":\"HEARTBEAT\",\"direction\":\"CLIENT_TO_SERVER\"}";
    String type = MessageParser.getType(json);
    assertEquals("HEARTBEAT", type);
  }

  @Test
  void testGetType_MissingType() {
    String json = "{\"role\":\"SENSOR_NODE\",\"nodeId\":\"dev-1\"}";
    String type = MessageParser.getType(json);
    assertNull(type);
  }

  @Test
  void testGetType_NullInput() {
    String type = MessageParser.getType(null);
    assertNull(type);
  }

  // ========== parseRegister() Tests ==========

  @Test
  void testParseRegister_RegisterNode() {
    String json = "{\"type\":\"REGISTER_NODE\",\"role\":\"SENSOR_NODE\",\"nodeId\":\"dev-1\"}";
    RegisterMessage msg = MessageParser.parseRegister(json);

    assertNotNull(msg);
    assertEquals("REGISTER_NODE", msg.getType());
    assertEquals("SENSOR_NODE", msg.getRole());
    assertEquals("dev-1", msg.getNodeId());
    assertNull(msg.getProtocolVersion());
    assertNull(msg.getMessage());
  }

  @Test
  void testParseRegister_RegisterControlPanel() {
    String json = "{\"type\":\"REGISTER_CONTROL_PANEL\",\"role\":\"CONTROL_PANEL\",\"nodeId\":\"ui-1\"}";
    RegisterMessage msg = MessageParser.parseRegister(json);

    assertNotNull(msg);
    assertEquals("REGISTER_CONTROL_PANEL", msg.getType());
    assertEquals("CONTROL_PANEL", msg.getRole());
    assertEquals("ui-1", msg.getNodeId());
    assertNull(msg.getProtocolVersion());
    assertNull(msg.getMessage());
  }

  @Test
  void testParseRegister_RegisterAck() {
    String json = "{\"type\":\"REGISTER_ACK\",\"protocolVersion\":\"1.0\",\"message\":\"Registration successful\"}";
    RegisterMessage msg = MessageParser.parseRegister(json);

    assertNotNull(msg);
    assertEquals("REGISTER_ACK", msg.getType());
    assertEquals("1.0", msg.getProtocolVersion());
    assertEquals("Registration successful", msg.getMessage());
    assertNull(msg.getRole());
    assertNull(msg.getNodeId());
  }

  @Test
  void testParseRegister_AllFields() {
    String json = "{\"type\":\"REGISTER_ACK\",\"role\":\"SENSOR_NODE\",\"nodeId\":\"dev-1\",\"protocolVersion\":\"1.0\",\"message\":\"OK\"}";
    RegisterMessage msg = MessageParser.parseRegister(json);

    assertNotNull(msg);
    assertEquals("REGISTER_ACK", msg.getType());
    assertEquals("SENSOR_NODE", msg.getRole());
    assertEquals("dev-1", msg.getNodeId());
    assertEquals("1.0", msg.getProtocolVersion());
    assertEquals("OK", msg.getMessage());
  }

  // ========== parseSensorData() Tests ==========

  @Test
  void testParseSensorData_AllFields() {
    String json = "{\"type\":\"SENSOR_DATA\",\"nodeId\":\"dev-1\",\"sensorKey\":\"temp-1\",\"value\":\"22.5\",\"unit\":\"°C\",\"timestamp\":\"2025-11-01T10:30:00Z\"}";
    SensorDataMessage msg = MessageParser.parseSensorData(json);

    assertNotNull(msg);
    assertEquals("SENSOR_DATA", msg.getType());
    assertEquals("dev-1", msg.getNodeId());
    assertEquals("temp-1", msg.getSensorKey());
    assertEquals("22.5", msg.getValue());
    assertEquals("°C", msg.getUnit());
    assertEquals("2025-11-01T10:30:00Z", msg.getTimestamp());
  }

  @Test
  void testParseSensorData_MinimalFields() {
    String json = "{\"type\":\"SENSOR_DATA\",\"nodeId\":\"dev-1\",\"sensorKey\":\"humidity-2\",\"value\":\"65\"}";
    SensorDataMessage msg = MessageParser.parseSensorData(json);

    assertNotNull(msg);
    assertEquals("SENSOR_DATA", msg.getType());
    assertEquals("dev-1", msg.getNodeId());
    assertEquals("humidity-2", msg.getSensorKey());
    assertEquals("65", msg.getValue());
    assertNull(msg.getUnit());
    assertNull(msg.getTimestamp());
  }

  @Test
  void testParseSensorData_UnquotedNumericValue() {
    String json = "{\"type\":\"SENSOR_DATA\",\"nodeId\":\"dev-1\",\"sensorKey\":\"temp-1\",\"value\":22.5}";
    SensorDataMessage msg = MessageParser.parseSensorData(json);

    assertNotNull(msg);
    assertEquals("22.5", msg.getValue());  // Should handle unquoted numbers
  }

  // ========== parseHeartbeat() Tests ==========

  @Test
  void testParseHeartbeat_ServerToClient() {
    String json = "{\"type\":\"HEARTBEAT\",\"direction\":\"SERVER_TO_CLIENT\",\"protocolVersion\":\"1.0\"}";
    HeartbeatMessage msg = MessageParser.parseHeartbeat(json);

    assertNotNull(msg);
    assertEquals("HEARTBEAT", msg.getType());
    assertEquals("SERVER_TO_CLIENT", msg.getDirection());
    assertEquals("1.0", msg.getProtocolVersion());
    assertNull(msg.getNodeId());
  }

  @Test
  void testParseHeartbeat_ClientToServer() {
    String json = "{\"type\":\"HEARTBEAT\",\"direction\":\"CLIENT_TO_SERVER\",\"nodeId\":\"dev-1\"}";
    HeartbeatMessage msg = MessageParser.parseHeartbeat(json);

    assertNotNull(msg);
    assertEquals("HEARTBEAT", msg.getType());
    assertEquals("CLIENT_TO_SERVER", msg.getDirection());
    assertEquals("dev-1", msg.getNodeId());
    assertNull(msg.getProtocolVersion());
  }

  @Test
  void testParseHeartbeat_AllFields() {
    String json = "{\"type\":\"HEARTBEAT\",\"direction\":\"CLIENT_TO_SERVER\",\"protocolVersion\":\"1.0\",\"nodeId\":\"dev-1\"}";
    HeartbeatMessage msg = MessageParser.parseHeartbeat(json);

    assertNotNull(msg);
    assertEquals("HEARTBEAT", msg.getType());
    assertEquals("CLIENT_TO_SERVER", msg.getDirection());
    assertEquals("1.0", msg.getProtocolVersion());
    assertEquals("dev-1", msg.getNodeId());
  }

  // ========== Edge Cases ==========

  @Test
  void testParseRegister_EmptyJson() {
    String json = "{}";
    RegisterMessage msg = MessageParser.parseRegister(json);

    assertNotNull(msg);
    assertNull(msg.getType());
    assertNull(msg.getRole());
    assertNull(msg.getNodeId());
    assertNull(msg.getProtocolVersion());
    assertNull(msg.getMessage());
  }

  @Test
  void testParseRegister_ExtraWhitespace() {
    String json = "{ \"type\" : \"REGISTER_NODE\" , \"role\" : \"SENSOR_NODE\" , \"nodeId\" : \"dev-1\" }";
    RegisterMessage msg = MessageParser.parseRegister(json);

    assertNotNull(msg);
    assertEquals("REGISTER_NODE", msg.getType());
    assertEquals("SENSOR_NODE", msg.getRole());
    assertEquals("dev-1", msg.getNodeId());
  }

  @Test
  void testParseSensorData_BooleanValue() {
    String json = "{\"type\":\"SENSOR_DATA\",\"nodeId\":\"dev-1\",\"sensorKey\":\"active\",\"value\":true}";
    SensorDataMessage msg = MessageParser.parseSensorData(json);

    assertNotNull(msg);
    assertEquals("true", msg.getValue());  // Should convert boolean to string
  }
}
