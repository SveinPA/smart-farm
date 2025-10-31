package edu.ntnu.bidata.smg.group8.control.logic.state;

import java.util.List;
import java.util.Objects;

/**
* An immutable snapshot of the entire system state at a specific point in time.
*
* <p>This class contains complete lists of all sensor readings and actuator
* states that were known when the snapshot was taken. It is typically used
* to:</p>
* <ul>
*     <li>Update the user interface without blocking ongoing data updates</li>
*     <li>Store historical states for logging or analysis</li>
*     <li>Compare system state over time</li>
* </ul>
*
* <p>The snapshot is a copy of the state, so changes to the underlying
* system do not affect this object.</p>
*
* @author Andrea Sandnes
* @version 30.10.25
*/
public record StateSnapshot(List<SensorReading> sensors, List<ActuatorReading> actuators) {

  /**
  * Canonical constructor that defensively copies the provided lists.
  *
  * @param sensors   list of all sensor readings at snapshot time (must not be null)
  * @param actuators list of all actuator states at snapshot time (must not be null)
  * @throws NullPointerException if sensors or actuators is null
  */
  public StateSnapshot {
    sensors   = List.copyOf(Objects.requireNonNull(sensors, "sensors"));
    actuators = List.copyOf(Objects.requireNonNull(actuators, "actuators"));
  }
}

