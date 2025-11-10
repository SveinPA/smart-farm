package edu.ntnu.bidata.smg.group8.common.protocol;

import java.awt.desktop.PrintFilesEvent;

import javax.sound.sampled.SourceDataLine;

/**
 * Protocol constants for the Smart Farm Messaging Protocol.
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

  // Roles
  public static final String ROLE_SENSOR_NODE = "SENSOR_NODE";
  public static final String ROLE_CONTROL_PANEL = "CONTROL_PANEL";

  // Heartbeat direction
  public static final String HB_SERVER_TO_CLIENT = "SERVER_TO_CLIENT";
  public static final String HB_CLIENT_TO_SERVER = "CLIENT_TO_SERVER";
  
}
