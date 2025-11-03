package edu.ntnu.bidata.smg.group8.control.logic.state;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
* This class tests the StateStore class and verifies the state
* management and snapshot behaviour.
*
* <p> StateStore is the central repository for all current sensor and
* actuator states in the control panel system. It need to handle concurrent
* updates (from the network thread receiving data) while also providing
* consistent snapshots (for the UI thread to display).</p>
*
* @author Andrea Sandnes
* @version 31.10.2025
*/
public class StateStoreTest {

  private StateStore store;

  /**
  * Creates a fresh StateStore before each test to ensure test isolation.
  */
  @BeforeEach
  void setup() {
    store = new StateStore();
  }

  /**
  * This test verifies that the sensor and actuator data can be added
  * to the store and retrieved through a snapshot with all details
  * intact.
  */
  @Test
  void testApplySensorAndActuatorUpdatesStore() {
    // Add a sensor reading
    store.applySensor("7", "temperature",
            "22.8", "C", Instant.ofEpochMilli(1000));

    // Add an actuator state
    store.applyActuator("7", "heater",
            "ON(23)", Instant.ofEpochMilli(1100));

    // Take a snapshot and verify the data is there
    StateSnapshot snap = store.snapshot();
    assertEquals(1, snap.sensors().size());
    assertEquals(1, snap.actuators().size());

    // Verify all sensor fields are correct
    SensorReading s = snap.sensors().get(0);
    assertEquals("7", s.nodeId());
    assertEquals("temperature", s.type());
    assertEquals("22.8", s.value());
    assertEquals("C", s.unit());

    // Verify all actuator fields are correct
    ActuatorReading a = snap.actuators().get(0);
    assertEquals("7", a.nodeId());
    assertEquals("heater", a.type());
    assertEquals("ON(23)", a.state());
  }

  /**
  * This test verifies that snapshots are truly immutable and
  * isolated from the store.
  */
  @Test
  void testSnapShotIsImmutable() {

    // Add initial data and take a snapshot
    store.applySensor("7", "humidity", "55", "%", Instant.now());
    StateSnapshot snap = store.snapshot();

    // Try to modify the snapshot directly
    assertThrows(UnsupportedOperationException.class, () ->
            snap.sensors().add(new SensorReading("x","y","z","u", Instant.now())
            ));

    // Verify that changes to the store don't affect the snapshot.
    int before = snap.sensors().size();
    store.applySensor("7", "light", "1000", "lux", Instant.now());
    assertEquals(before, snap.sensors().size());
  }
}
