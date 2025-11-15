package edu.ntnu.bidata.smg.group8.control.logic.command;

import edu.ntnu.bidata.smg.group8.common.util.AppLogger;
import edu.ntnu.bidata.smg.group8.control.infra.network.PanelAgent;
import java.io.IOException;
import java.util.Objects;
import org.slf4j.Logger;

/**
* This class handles user commands for controlling actuators on remote nodes.
*
* <p>Provides a high-level interface for sending control commands to actuators
* through the panel agent. It validates input parameters and logs all command
* requests before forwarding them to the network layer.</p>
*
* @author Andrea Sandnes
* @version 31.10.25
*/
public class CommandInputHandler {
  private static final Logger log = AppLogger.get(CommandInputHandler.class);

  private final PanelAgent agent;

  /**
  * Creates a new command input handler.
  *
  * @param agent the panel agent used to communicate with remote nodes
  * @throws NullPointerException if agent is null
  */
  public CommandInputHandler(PanelAgent agent) {
    this.agent = Objects.requireNonNull(agent, "agent");
  }

  /**
  * Turns on the specified actuator by setting its value to 1.
  *
  * @param nodeId the unique identifier of the node containing the actuator
  * @param actuatorKey the key identifying the actuator control
  * @throws IllegalArgumentException if nodeId or actuatorKey is null or blank
  * @throws IOException if communication with the node fails
  */
  public void turnOn(String nodeId, String actuatorKey) throws IOException {
    validate(nodeId, actuatorKey);
    log.info("Request: turn ON actuator={} on node={}", actuatorKey, nodeId);
    log.debug("Sending ACTUATOR_COMMAND nodeId={} "
            + "actuator={} action=SET value=1", nodeId, actuatorKey);
    agent.sendActuatorCommand(nodeId, actuatorKey, "SET", 1);
  }

  /**
  * Turns off the specified actuator by setting its value to 0.
  *
  * @param nodeId the unique identifier of the node containing the actuator
  * @param actuatorKey the key identifying the actuator control
  * @throws IllegalArgumentException if nodeId or actuatorKey is null or blank
  * @throws IOException if communication with the node fails
  */
  public void turnOff(String nodeId, String actuatorKey) throws IOException {
    validate(nodeId, actuatorKey);
    log.info("Request: turn OFF actuator={} on node={}", actuatorKey, nodeId);
    log.debug("Sending ACTUATOR_COMMAND nodeId={} "
            + "actuator={} action=SET value=0", nodeId, actuatorKey);
    agent.sendActuatorCommand(nodeId, actuatorKey, "SET", 0);
  }

  /**
  * Sets the specified actuator to a custom numeric value.
  * This method is useful for actuators that support a range of values,
  * such as dimmers, fan speed controllers, or temperature setpoints.
  *
  * @param nodeId the unique identifier of the node containing the actuator
  * @param actuatorKey the key identifying the actuator control
  * @param value the target value to set (interpretation depends on actuator type)
  * @throws IllegalArgumentException if nodeId or actuatorKey is null or blank
  * @throws IOException if communication with the node fails
  */
  public void setValue(String nodeId, String actuatorKey, int value) throws IOException {
    validate(nodeId, actuatorKey);
    log.info("Request: set actuator={} on node={} to {}", actuatorKey, nodeId, value);
    log.debug("Sending ACTUATOR_COMMAND nodeId={} "
            + "actuator={} action=SET value(int)={}", nodeId, actuatorKey, value);
    agent.sendActuatorCommand(nodeId, actuatorKey, "SET", value);
  }

  /**
  * Validates that nodeId and actuatorKey are not null or blank.
  *
  * @param nodeId the node identifier to validate
  * @param actuatorKey the actuator key to validate
  * @throws IllegalArgumentException if either parameter is null or blank
  */
  private static void validate(String nodeId, String actuatorKey) {
    if (nodeId == null || nodeId.isBlank()) {
      throw new IllegalArgumentException("NodeId must not be blank");
    }
    if (actuatorKey == null || actuatorKey.isBlank()) {
      throw new IllegalArgumentException("actuatorKey must not be blank");
    }
  }

  /**
   * Sends a command to the specified actuator to move to the given position.
   *
   * @param actuatorType the type of actuator
   * @param position the target position (0-100)
   * @throws IllegalArgumentException if actuatorType is null/empty
   * or position is out of range
   */
  public void sendActuatorCommand(String actuatorType, int position) {
    if (actuatorType == null || actuatorType.isEmpty()) {
      throw new IllegalArgumentException("Actuator type cannot be null or empty");
    }
    if (position < 0 || position > 100) {
      throw new IllegalArgumentException("Position must be between 0 and 100");
    }
  }
}

