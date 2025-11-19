package edu.ntnu.bidata.smg.group8.control.logic.state;

import java.time.Instant;

/**
* Represents the state of an actuator at a specific time.
*
* <p> An actuator is a device that can be controlled, such as a fan, heater,
* window, or valve. This class stores information about the actuator's current
* state along with the time when the state was recorded.</p>
*
* <p>As a record, this class is immutable, making it safe to share between
* threads without additional synchronization.</p>
*
* @param nodeId unique identifier of the actuator node
* @param type the type or name of the actuator (e.g., "fan", "heater", "window")
* @param state the current state of the actuator (e.g., "on", "off", "50%")
* @param ts the timestamp when the state was recorded
*
* @author Andrea Sandnes
* @version 1.0
* @since 30.10.25
*/
public record ActuatorReading(String nodeId, String type, String state,
                              Instant ts) {
}

