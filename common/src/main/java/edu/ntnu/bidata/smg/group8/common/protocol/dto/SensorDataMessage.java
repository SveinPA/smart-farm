package edu.ntnu.bidata.smg.group8.common.protocol.dto;

/**
 * Data Transfer Object for {@code SENSOR_DATA} protocol messages.
 * <p>
 * Represents sensor readings transmitted from sensor nodes to the broker,
 * which are then forwarded to all connected control panels.
 * <p>
 * Sensor data messages are typically sent periodically,
 * or when significant value changes are detected.
 * <p>
 * Example message:
 * {@code {"type":"SENSOR_DATA","nodeId":"dev-1","sensorKey":"temp-1","value":"22.5","unit":"°C","timestamp":"2025-11-01T10:30:00Z"}}
 * 
 * @author Svein Antonsen
 * @since 1.0
 */
public final class SensorDataMessage {
  private final String type;
  private final String nodeId;
  private final String sensorKey; // Example: "temp-1", "humidity-2"
  private final String value; // sensor reading value
  private final String unit; // unit of measurement (may be null)
  private final String timestamp; // Timestamp (may be null)
  
  /**
   * Constructs a new SensorDataMessage with the specified fields.
   * 
   * @param type the message type (should be "SENSOR_DATA")
   * @param nodeId the unique identifier of the sensor node
   * @param sensorKey the unique key identifying the specific sensor ("temp-1", "humidity-2")
   * @param value the sensor reading value as a string
   * @param unit the unit of measurement (e.g., "°C", "%"), may be {@code null}
   * @param timestamp the ISO 8601 timestamp of the reading, may be {@code null}
   */
  public SensorDataMessage (String type, String nodeId, String sensorKey, String value, String unit, String timestamp) {
    this.type = type;
    this.nodeId = nodeId;
    this.sensorKey = sensorKey;
    this.value = value;
    this.unit = unit;
    this.timestamp = timestamp;
  }

  /**
   * Returns the message type.
   *
   * @return the message type (typically "SENSOR_DATA")
   */
  public String getType() {
    return type;
  }

  /**
   * Returns the unique identifier of the sensor node.
   * 
   * @return the node ID
   */
  public String getNodeId() {
    return nodeId;
  }

  /**
   * Returns the unique key identifying the specific sensor.
   * 
   * @return the sensor key (e.g., "temp-1", "humidity-2")
   */
  public String getSensorKey() {
    return sensorKey;
  }

  /**
   * Returns the sensor reading value.
   * 
   * @return the sensor value as a string
   */
  public String getValue() {
    return value;
  }

  /**
   * Returns the unit of measurement.
   * 
   * @return the unit (e.g., "°C", "%"), or {@code null} if not provided
   */
  public String getUnit() {
    return unit;
  }

  /**
   * Returns the timestamp of the sensor reading.
   * 
   * @return the ISO 8601 timestamp string, or {@code null} if not provided
   */
  public String getTimestamp() {
    return timestamp;
  }

  @Override
  public String toString() {
    return "SensorDataMessage{" +
           "type='" + type + '\'' +
           ", nodeId='" + nodeId + '\'' +
           ", sensorKey='" + sensorKey + '\'' +
           ", value='" + value + '\'' +
           ", unit='" + unit + '\'' +
           ", timestamp='" + timestamp + '\'' +
           "}";
  }
}
