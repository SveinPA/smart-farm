package edu.ntnu.bidata.smg.group8.control.logic.state;

import edu.ntnu.bidata.smg.group8.common.util.AppLogger;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import org.slf4j.Logger;

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
  private static final Logger log = AppLogger.get(StateStore.class);

  private final Map<String, SensorReading> sensors = new ConcurrentHashMap<>();
  private final Map<String, ActuatorReading> actuators = new ConcurrentHashMap<>();

  private final CopyOnWriteArrayList<Consumer<SensorReading>> sensorSinks =
          new CopyOnWriteArrayList<>();
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
    SensorReading sr = new SensorReading(nodeId, type, value, unit, ts);
    sensors.put(nodeId + ":" + type, sr);

    // Notify all sensor sinks
    for (Consumer<SensorReading> sink : sensorSinks) {
      try {
        sink.accept(sr);
      } catch (Exception e) {
        log.error("Error in sensor sink for nodeId={} type={}", nodeId, type, e);
      }
    }
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

    // Notify all actuator sinks
    for (Consumer<ActuatorReading> sink : actuatorSinks) {
      try {
        sink.accept(ar);
      } catch (Exception e) {
        log.error("Error in actuator sink for nodeId={} type={}", nodeId, type, e);
      }
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
    log.debug("Actuator sink added. Total sinks: {}", actuatorSinks.size());
  }

  /**
  * Unregisters a previously added actuator listener.
  *
  * @param sink a consumer that handles actuator updates, must not be null
  */
  public void removeActuatorSink(Consumer<ActuatorReading> sink) {
    actuatorSinks.remove(sink);
    log.debug("Actuator sink removed. Total sinks: {}", actuatorSinks.size());
  }

  /**
  * Registers a new listener (sink) that will be notified whenever
  * a sensor reading is called.
  *
  * @param sink a consumer that handles sensor updates, must not be null
  */
  public void addSensorSink(Consumer<SensorReading> sink) {
    sensorSinks.add(sink);
    log.debug("Sensor sink added. Total sinks: {}", sensorSinks.size());
  }

  /**
  * Unregisters a previously added sensor listener.
  *
  * @param sink a consumer that handles sensor updates, must not be null
  */
  public void removeSensorSink(Consumer<SensorReading> sink) {
    sensorSinks.remove(sink);
    log.debug("Sensor sink removed. Total sinks: {}", sensorSinks.size());
  }
}
