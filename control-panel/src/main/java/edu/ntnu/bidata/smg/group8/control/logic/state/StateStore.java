package edu.ntnu.bidata.smg.group8.control.logic.state;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
* Thread-safe storage for the current state of sensors and actuators
* in the smart greenhouse system.
*
* <p>This class maintains an in-memory cache of the most recent reading
* from all sensors and actuators across all nodes. Each reading is
* identified by a composite key of nodeId and sensor/actuator type.</p>
*
* <p>Thread-safety is ensured through the use of ConcurrentHashMap for
* internal storage, allowing concurrent reads and writes from multiple
* threads without external synchronization.</p>
*
* @author Andrea Sandnes
* @version 30.10.25
*/
public class StateStore {
  private final Map<String, SensorReading> sensors = new ConcurrentHashMap<>();
  private final Map<String, ActuatorReading> actuators = new ConcurrentHashMap<>();
  private final CopyOnWriteArrayList<Consumer<ActuatorReading>> actuatorSinks =
          new CopyOnWriteArrayList<>();

  /**
  * Updates or stores a new sensor reading in the state store.
  *
  * <p>If a reading for the same node and sensor type already exists,
  * it will be replaced with the new reading. The composite key format
  * is "nodeId:type"</p>
  *
  * @param nodeId the unique identifier of the sensor node
  * @param type the type or name of the sensor (e.g., "temperature", "humidity")
  * @param value the sensor reading value as a string
  * @param unit the unit of measurement (e.g., "°C", "%", "lux")
  * @param ts the timestamp when the reading was taken
  */
  public void applySensor(String nodeId, String type, String value, String unit, Instant ts) {
    sensors.put(nodeId + ":" + type, new SensorReading(nodeId, type, value, unit, ts));
  }

  /**
  * Updates or stores a new actuator reading in the state store.
  *
  * <p>If a reading for the same node and actuator type already exists,
  * it will be replaced with the new reading. The composite key format
  * is "nodeId:type"</p>
  *
  * @param nodeId the unique identifier of the actuator node
  * @param type the type or name of the actuator (e.g., "fan", "heater", "window")
  * @param state the current state of the actuator (e.g., "on", "off", "50%")
  * @param ts the timestamp when the state was recorded
  */
  public void applyActuator(String nodeId, String type, String state, Instant ts) {
    ActuatorReading ar = new ActuatorReading(nodeId, type, state, ts);
    actuators.put(nodeId + ":" + type, ar);
    for (Consumer<ActuatorReading> sink : actuatorSinks) {
      sink.accept(ar);
    }
  }

  /**
  * Creates an immutable snapshot of the current state of all sensors and actuators.
  *
  * <p>The snapshot contains copies of all current readings at the time this
  * method is called. Subsequent updates to the state store will not affect
  * the returned snapshot.</p>
  *
  * @return a StateSnapshot containing immutable copies of all current sensor
  * and actuator readings
  */
  public StateSnapshot snapshot() {
    return new StateSnapshot(List.copyOf(sensors.values()), List.copyOf(actuators.values()));
  }

  /**
  * Registers a new listener (sink) that will be notified whenever
  * an actuator reading is called.
  *
  * @param sink a consumer that handles actuator updates, must not be null
  */
  public void addActuatorSink(Consumer<ActuatorReading> sink) {
    actuatorSinks.add(sink);
  }

  /**
  * Unregisters a previously added actuator listener.
  *
  * @param sink a consumer that handles actuator updates, must not be null
  */
  public void removeActuatorSink(Consumer<ActuatorReading> sink) {
    actuatorSinks.remove(sink);
  }


}
