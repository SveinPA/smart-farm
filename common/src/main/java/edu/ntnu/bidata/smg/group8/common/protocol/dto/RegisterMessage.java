package edu.ntnu.bidata.smg.group8.common.protocol.dto;

/**
 * Data Transfer Object for registration-related protocol messages.
 * <p>
 * Represents three message types:
 * <ul>
 *    <li>{@code REGISTER_NODE} - Sensor node registration request</li>
 *    <li>{@code REGISTER_CONTROL_PANEL} - Control panel registration request</li>
 *    <li>{@code REGISTER_ACK} - Server acknowledgment of successful registration</li>
 * </ul>
 * <p>
 * Not all fields are present in every message type. Use {@code null} for absent fields.
 * <p>
 * Example REGISTER_NODE message:
 * {@code {"type":"REGISTER_NODE","role":"SENSOR_NODE","nodeId":"dev-1"}}
 * <p>
 * Example REGISTER_ACK message:
 * {@code {"type":"REGISTER_ACK","protocolVersion":"1.0","message":"Registration successful"}}
 * 
 * @author Svein Antonsen
 * @since 1.0
 */
public final class RegisterMessage {
  private final String type;
  private final String role;
  private final String nodeId;
  private final String protocolVersion;
  private final String message;

  /**
   * Constructs a new RegisterMessage with the specified fields.
   * 
   * @param type the message type (e.g., "REGISTER_NODE", "REGISTER_ACK")
   * @param role the role of the registering node ("SENSOR_NODE", "CONTROL_PANEL"),
   *             may be {@code null} for REGISTER_ACK messages.
   * @param nodeId the unique identifier for the node, may be {@code null}
   * @param protocolVersion the protocol version string, may be {@code null} for registration requests
   * @param message a readable message (typically used in REGISTER_ACK), may be {@code null}
   */
  public RegisterMessage(String type, String role, String nodeId, String protocolVersion, String message) {
    this.type = type;
    this.role = role;
    this.nodeId = nodeId;
    this.protocolVersion = protocolVersion;
    this.message = message;
  }

  /**
   * Returns the message type.
   *
   * @return the message type (example: "REGISTER_NODE")
   */
  public String getType() {
    return type;
  }

  /**
   * Returns the role of the node being registered.
   * 
   * @return the role ("SENSOR_NODE", "CONTROL_PANEL"), or {@code null} if not present.
   */
  public String getRole() {
    return role;
  }

  /**
   * Returns the unique node identifier.
   * 
   * @return the node ID, or {@code null} if not present
   */
  public String getNodeId() {
    return nodeId;
  }

  /**
   * Returns the protocol version.
   * 
   * @return the protocol version string (e.g., "1.0"), or {@code null}, if not present
   */
  public String getProtocolVersion() {
    return protocolVersion;
  }

  /**
   * Returns the readable message.
   * 
   * @return the message text, or {@code null} if not present
   */
  public String getMessage() {
    return message;
  }

  @Override
  public String toString() {
    return "RegisterMessage{" +
           "type='" + type + '\'' +
           ", role='" + role + '\'' +
           ", nodeId='" + nodeId + '\'' +
           ", protocolVersion='" + protocolVersion + '\'' +
           ", message='" + message + '\'' +
           "}";
  }
}
