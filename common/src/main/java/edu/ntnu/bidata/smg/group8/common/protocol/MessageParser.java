package edu.ntnu.bidata.smg.group8.common.protocol;

import edu.ntnu.bidata.smg.group8.common.protocol.dto.ActuatorCommandMessage;
import edu.ntnu.bidata.smg.group8.common.protocol.dto.ActuatorStatusMessage;
import edu.ntnu.bidata.smg.group8.common.protocol.dto.HeartbeatMessage;
import edu.ntnu.bidata.smg.group8.common.protocol.dto.RegisterMessage;
import edu.ntnu.bidata.smg.group8.common.protocol.dto.SensorDataMessage;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Simple JSON message parser for the Smart Farm protocol.
 * <p>
 * This parser extracts fields from JSON messages and creates corresponding DTO objects.
 * It uses a lightweight regex-based approach suitable for the protocol's simple JSON structure.
 * <p>
 * The parser handles both quoted string values and unquoted numeric/boolean values.
 * Missing fields are represented as {@code null} in the resulting DTOs.
 * <p>
 * <b>Not a full JSON parser</b> - designed specifically for this protocol's message formats.
 * Does not handle nested objects, arrays, or complex JSON structures.
 *
 * @see RegisterMessage
 * @see SensorDataMessage
 * @see HeartbeatMessage
 * @see ActuatorCommandMessage
 */
public final class MessageParser {

  // Regex pattern to match both quoted strings and unquoted values (numbers, booleans)
  private static final Pattern FIELD_PATTERN =
            Pattern.compile("\"(\\w+)\"\\s*:\\s*(?:\"([^\"]*)\"|([^,}\\s]+))");

  // JSON field names used in protocol messages
  private static final String FIELD_TYPE = "type";
  private static final String FIELD_ROLE = "role";
  private static final String FIELD_NODE_ID = "nodeId";
  private static final String FIELD_PROTOCOL_VERSION = "protocolVersion";
  private static final String FIELD_MESSAGE = "message";
  private static final String FIELD_SENSOR_KEY = "sensorKey";
  private static final String FIELD_VALUE = "value";
  private static final String FIELD_UNIT = "unit";
  private static final String FIELD_TIMESTAMP = "timestamp";
  private static final String FIELD_DIRECTION = "direction";
  private static final String FIELD_TARGET_NODE = "targetNode";
  private static final String FIELD_ACTUATOR = "actuator";
  private static final String FIELD_ACTION = "action";

  private MessageParser() {
    // Util class - no instances allowed
  }

  /**
   * Extracts the message type from a JSON message.
   * <p>
   * This is typically the first operation when processing an incoming message,
   * as it determines which specific parser method to call.
   * 
   * @param json the JSON message string
   * @return the message type (e.g., "REGISTER_NODE", "SENSOR_DATA"), or {@code null} if not found
   */
  public static String getType(String json) {
    return getField(json, FIELD_TYPE);
  }

  /**
   * Parses a registration message (REGISTER_NODE, REGISTER_CONTROL_PANEL or REGISTER_ACK).
   * 
   * @param json the JSON message string
   * @return a RegisterMessage DTO containing the parsed fields
   */
  public static RegisterMessage parseRegister(String json) {
    String type = getField(json, FIELD_TYPE);
    String role = getField(json, FIELD_ROLE);
    String nodeId = getField(json, FIELD_NODE_ID);
    String protocolVersion = getField(json, FIELD_PROTOCOL_VERSION);
    String message = getField(json, FIELD_MESSAGE);

    return new RegisterMessage(type, role, nodeId, protocolVersion, message);
  }

  /**
   * Parses a sensor data message (SENSOR_DATA).
   * 
   * @param json the JSON message string
   * @return a SensorDataMessage DTO containing the parsed fields
   */
  public static SensorDataMessage parseSensorData(String json) {
    String type = getField(json, FIELD_TYPE);
    String nodeId = getField(json, FIELD_NODE_ID);
    String sensorKey = getField(json, FIELD_SENSOR_KEY);
    String value = getField(json, FIELD_VALUE);
    String unit = getField(json, FIELD_UNIT);
    String timestamp = getField(json, FIELD_TIMESTAMP);

    return new SensorDataMessage(type, nodeId, sensorKey, value, unit, timestamp);
  }

  /**
   * Parses a heartbeat message (HEARTBEAT).
   * 
   * @param json the JSON message string
   * @return a HeartbeatMessage DTO containing the parsed fields
   */
  public static HeartbeatMessage parseHeartbeat(String json) {
    String type = getField(json, FIELD_TYPE);
    String direction = getField(json, FIELD_DIRECTION);
    String protocolVersion = getField(json, FIELD_PROTOCOL_VERSION);
    String nodeId = getField(json, FIELD_NODE_ID);

    return new HeartbeatMessage(type, direction, protocolVersion, nodeId);
  }

  /**
   * Parses an actuator command message (ACTUATOR_COMMAND).
   * <p>
   * The {@code targetNode} field is used by the broker for routing,
   * but may be {@code null} in messages forwarded to sensor nodes.
   * 
   * @param json the JSON message string
   * @return an ActuatorCommandMessage DTO containing the parsed fields
   */
  public static ActuatorCommandMessage parseActuatorCommand(String json) {
    String type = getField(json, FIELD_TYPE);
    String targetNode = getField(json, FIELD_TARGET_NODE);
    String actuator = getField(json, FIELD_ACTUATOR);
    String action = getField(json, FIELD_ACTION);
    String value = getField(json, FIELD_VALUE);

    return new ActuatorCommandMessage(type, targetNode, actuator, action, value);
  }

  /**
   * Extracts a field value from a JSON string.
   * <p>
   * Handles both:
   * <ul>
   *    <li>Quoted string values: {@code "key":"value"} -> returns {@code "value"}</li>
   *    <li>Unquoted values: {@code "key":123} or {@code "key":true} -> returns {@code "123"} or {@code "true"}</li>
   * </ul>
   * 
   * @param json the JSON message string
   * @param key the field key to extract
   * @return the field value as a string, or {@code null} if the field is not found
   */
  private static String getField(String json, String key) {
    if (json == null || key == null) {
      return null;
    }

    Matcher matcher = FIELD_PATTERN.matcher(json);
    while (matcher.find()) {
      String fieldName = matcher.group(1);
      if (key.equals(fieldName)) {
        // Group 2 = quoted value, Group 3 = unquoted value
        String quotedValue = matcher.group(2);
        String unquotedValue = matcher.group(3);
        return quotedValue != null ? quotedValue : unquotedValue;
      }
    }
    return null;
  }

  /**
   * Parses an ACTUATOR_STATUS message into a DTO.
   *
   * <p>The ACTUATOR_STATUS message provides updates on the status of an actuator,
   * which is used to report the current actuator state after executing a command
   * or due to other changes, to the control panel.</p>
   *
   * @param json The JSON message string
   * @return Parsed {@link ActuatorStatusMessage}
   */
  public static ActuatorStatusMessage parseActuatorStatus(String json) {
      String nodeId = getField(json, "nodeId");
      String actuatorKey = getField(json, "actuatorKey");
      String status = getField(json, "status");
      String valueStr = getField(json, "value");
      String timestampStr = getField(json, "timestamp");
      
      // Guard against null values and provide defaults
      double value = valueStr != null ? Double.parseDouble(valueStr) : 0.0; // Default to 0.0 if value is missing
      long timestamp = timestampStr != null ? Long.parseLong(timestampStr) : 0L; // Default to 0L if timestamp is missing
      
      return new ActuatorStatusMessage(nodeId, actuatorKey, status, value, timestamp);
  }
}
