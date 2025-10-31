package edu.ntnu.bidata.smg.group8.control.logic.state;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
* Tests for the StateSnapshot class that verifies immutability and defensive copying.
*
* <p>StateSnapshot is designed to be an immutable, thread-safe representation of
* the system state at a specific moment in time. These tests ensure that once a
* snapshot is created, it cannot be modified - either from the outside or by
* changes to the original lists that were passed in.</p>
*
* @author Andrea Sandnes
* @version 31.10.2025
*/
public class StateSnapshotTest {

  /**
  * This test verifies that the StateSnapshot constructor creates defensive
  * copies of the input lists and rejects null arguments.
  *
  * Defensive copying means that the constructor doesn't just store
  * references to the list we pass in - it creates new lists with copies
  * of the data. This prevents the "original owner" of the lists from
  * modifying the snapshot's after creation.
  */
  @Test
  void testConstructorCopiesAndRejectsNull() {
    List<SensorReading> sensors = new ArrayList<>();
    List<ActuatorReading> actuators = new ArrayList<>();
    sensors.add(new SensorReading("7","t","1", "C", Instant.now()));

    // Create snapshot
    StateSnapshot snap = new StateSnapshot(sensors, actuators);

    // Modify the original list - this should NOT affect the snapshot
    sensors.clear();
    assertEquals(1, snap.sensors().size());

    // Verify that null inputs are rejected
    assertThrows(NullPointerException.class, () ->
            new StateSnapshot(null, actuators));
    assertThrows(NullPointerException.class, () ->
            new StateSnapshot(sensors, null));
  }

  /**
  * This test verifies that the lists returned by the getters
  * are immutable copied that cannot be modified.
  */
  @Test
  void testListsAreImmutableCopies() {
    List<SensorReading> sensors = List.of(new SensorReading(
            "7","t","1", "C", Instant.now()));
    List<ActuatorReading> actuators = List.of();

    StateSnapshot snap = new StateSnapshot(sensors, actuators);

    // Try to modify sensor list - should fail
    assertThrows(UnsupportedOperationException.class, () ->
            snap.sensors().add(new SensorReading("x", "y", "z", "u", Instant.now()))
    );
    // Try to modify actuator list - should fail
    assertThrows(UnsupportedOperationException.class, () ->
            snap.actuators().add(new ActuatorReading("x", "y", "z", Instant.now()))
    );
  }
}
