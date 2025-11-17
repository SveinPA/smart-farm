package edu.ntnu.bidata.smg.group8.common.protocol.dto;

/**
 * Data Transfer Object for {@code HEARTBEAT} protocol messages.
 * <p>
 * Heartbeat messages are exchanged between clients and the server to verify
 * connection health and prevent idle timeout disconnections.
 * <p>
 * The server sends heartbeats after detecting idle periods (default: 30 seconds without messages).
 * Clients may also send heartbeats to indicate they are still active.
 * <p>
 * Example server-to-client heartbeat:
 * {@code {"type":"HEARTBEAT","direction":"SERVER_TO_CLIENT","protocolVersion":"1.0"}}
 * <p>
 * Example client-to-server heartbeat:
 * {@code {"type":"HEARTBEAT","direction":"CLIENT_TO_SERVER","nodeId":"dev-1"}}
 * 
 * @author Svein Antonsen
 * @since 1.0
 */
public final class HeartbeatMessage {
  private final String type;
  private final String direction; // "SERVER_TO_CLIENT" or "CLIENT_TO_SERVER"
  private final String protocolVersion; // May be null
  private final String nodeId; // May be null

  /**
   * Constructs a new HeartbeatMessage with the specified fields.
   *
   * @param type the message type (should be "HEARTBEAT")
   * @param direction the direction of the heartbeat ("SERVER_TO_CLIENT" or "CLIENT_TO_SERVER")
   * @param protocolVersion the protocol version string, may be {@code null}
   * @param nodeId the unique identifier of the node sending the heartbeat, may be {@code null}
   */
  public HeartbeatMessage(String type, String direction, String protocolVersion, String nodeId) {
    this.type = type;
    this.direction = direction;
    this.protocolVersion = protocolVersion;
    this.nodeId = nodeId;
  }

  /**
   * Returns the message type.
   * 
   * @return the message type (typically "HEARTBEAT")
   */
  public String getType() {
    return type;
  }

  /**
   * Returns the direction of the heartbeat.
   * 
   * @return "SERVER_TO_CLIENT" or "CLIENT_TO_SERVER"
   */
  public String getDirection() {
    return direction;
  }

  /**
   * Returns the protocol version.
   * 
   * @return the protocol version string (e.g., "1.0"), or {@code null} if not present
   */
  public String getProtocolVersion() {
    return protocolVersion;
  }

  /**
   * Returns the node identifier of the sender.
   * 
   * @return the node ID, or {@code null} if not present
   */
  public String getNodeId() {
    return nodeId;
  }

  @Override
  public String toString() {
    return "HeartbeatMessage{" +
           "type='" + type + '\'' +
           ", direction='" + direction + '\'' +
           ", protocolVersion='" + protocolVersion + '\'' +
           ", nodeId='" + nodeId + '\'' +
           "}"; 
  }
}

