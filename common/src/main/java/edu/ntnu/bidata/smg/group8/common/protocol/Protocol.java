package edu.ntnu.bidata.smg.group8.common.protocol;


/**
 * Protocol constants for the Smart Farm Messaging Protocol.  
 * 
 *
 * <p>This class defines all string constants used in the JSON-based protocol for
 * communication between sensor nodes, control panels, and the broker. The protocol
 * uses length-prefixed JSON messages over TCP.
 *
 * <p><strong>Constant Categories:</strong>
 * <ul>
 *   <li><strong>Protocol Version:</strong> Current protocol version identifier</li>
 *   <li><strong>Message Types:</strong> All supported message type identifiers (13 types)</li>
 *   <li><strong>Node Roles:</strong> Client role identifiers (SENSOR_NODE, CONTROL_PANEL)</li>
 *   <li><strong>Heartbeat Directions:</strong> Heartbeat message direction indicators</li>
 * </ul>
 *
 * <p><strong>Message Type Categories:</strong>
 * <ul>
 *   <li><em>Registration:</em> REGISTER_NODE, REGISTER_CONTROL_PANEL, REGISTER_ACK, NODE_LIST</li>
 *   <li><em>Data Transfer:</em> SENSOR_DATA, ACTUATOR_STATUS, ACTUATOR_STATE</li>
 *   <li><em>Commands:</em> ACTUATOR_COMMAND, COMMAND_ACK</li>
 *   <li><em>Lifecycle Events:</em> NODE_CONNECTED, NODE_DISCONNECTED</li>
 *   <li><em>Connection Management:</em> HEARTBEAT, ERROR</li>
 * </ul>
 *
 * <p>For complete protocol specification, see the protocol documentation in
 * {@code smart-farm/protocol.md}.
 *
 * @author Svein Antonsen
 * @since 1.0
 * @see MessageParser
 * @see FrameCodec
 */
public final class Protocol {
  private Protocol() {
    // No instances allowed
  }

  // Version of the protocol
  public static final String PROTOCOL_VERSION = "1.0";

  // Types of messages
  public static final String TYPE_REGISTER_NODE = "REGISTER_NODE";
  public static final String TYPE_REGISTER_CONTROL_PANEL = "REGISTER_CONTROL_PANEL";
  public static final String TYPE_REGISTER_ACK = "REGISTER_ACK";
  public static final String TYPE_SENSOR_DATA = "SENSOR_DATA";
  public static final String TYPE_ACTUATOR_COMMAND = "ACTUATOR_COMMAND";
  public static final String TYPE_ACTUATOR_STATE = "ACTUATOR_STATE";
  public static final String TYPE_HEARTBEAT = "HEARTBEAT";
  public static final String TYPE_ERROR = "ERROR";
  public static final String TYPE_NODE_CONNECTED = "NODE_CONNECTED";
  public static final String TYPE_NODE_DISCONNECTED = "NODE_DISCONNECTED";
  public static final String TYPE_ACTUATOR_STATUS = "ACTUATOR_STATUS";
  public static final String TYPE_COMMAND_ACK = "COMMAND_ACK";
  public static final String TYPE_NODE_LIST = "NODE_LIST";
  public static final String TARGET_ALL = "ALL";

  // Roles
  public static final String ROLE_SENSOR_NODE = "SENSOR_NODE";
  public static final String ROLE_CONTROL_PANEL = "CONTROL_PANEL";

  // Heartbeat direction
  public static final String HB_SERVER_TO_CLIENT = "SERVER_TO_CLIENT";
  public static final String HB_CLIENT_TO_SERVER = "CLIENT_TO_SERVER";
  
}
