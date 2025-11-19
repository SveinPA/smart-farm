package edu.ntnu.bidata.smg.group8.common.protocol.dto;

/**
 * Data Transfer Object representing an ACTUATOR_COMMAND message.
 * <p>
 * Sent from control panels to the broker, then routed to the target sensor node.
 * The broker uses {@code targetNode} to determine routing, but this field is
 * typically removed before forwarding to the sensor node (since the node knows its own ID).
 * </p>
 * 
 * <h2> Example Message from Control Panel:</h2>
 * <pre>
 * {"type":"ACTUATOR_COMMAND","targetNode":"42","actuator":"fan","action":"ON"}
 * </pre>
 * 
 * <h2>Example Message Forwarded to Sensor Node:</h2>
 * <pre>
 * {"type":"ACTUATOR_COMMAND","actuator":"fan","action":"ON"}
 * </pre>
 * 
 * @see edu.ntnu.bidata.smg.group8.common.protocol.Protocol#ACTUATOR_COMMAND
 */
public class ActuatorCommandMessage {
  private final String type;
  private final String targetNode;  // May be null if message is already routed
  private final String actuator;
  private final String action;
  private final String value;

  /**
   * Constructs a new ActuatorCommandMessage.
   * 
   * @param type message type (should be "ACTUATOR_COMMAND")
   * @param targetNode the node ID to send the command to (may be null for forwarded messages)
   * @param actuator the actuator to control (e.g., "fan", "window")
   * @param action the action to perform (e.g., "ON", "OFF", "open")
   */
  public ActuatorCommandMessage(String type, String targetNode, String actuator, String action, String value) {
    this.type = type;
    this.targetNode = targetNode;
    this.actuator = actuator;
    this.action = action;
    this.value = value;
  }

  /**
   * Returns the message type (e.g., "ACTUATOR_COMMAND")
   * 
   * @return the message type
   */
  public String getType() {
    return type;
  }

  /**
   * Returns the target node ID, or null if not present.
   * 
   * @return the target node ID
   */
  public String getTargetNode() {
    return targetNode;
  }

  /**
   * Returns the actuator name (e.g., "fan", "window", "heater")
   * 
   * @return the actuator name
   */
  public String getActuator() {
    return actuator;
  }

  /**
   * Returns the action to perform (e.g., "ON", "OFF", "open", "close")
   * 
   * @return the action to perform
   */
  public String getAction() {
    return action;
  }

  @Override
  public String toString() {
    return "ActuatorCommandMessage{" +
           "type='" + type + '\'' +
           ", targetNode='" + targetNode + '\'' +
           ", actuator='" + actuator + '\'' +
           ", action='" + action + '\'' +
           "}";
  }

  /**
   * Returns the value associated with the actuator command (e.g., "75.0" for setting a numeric value).
   *
   * @return the value as a String
   */
  public String getValue() {
    return value;
  }
  
}
