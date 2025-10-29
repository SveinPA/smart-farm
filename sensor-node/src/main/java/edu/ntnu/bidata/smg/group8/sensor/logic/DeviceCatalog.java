package edu.ntnu.bidata.smg.group8.sensor.logic;

import edu.ntnu.bidata.smg.group8.common.actuator.Actuator;
import edu.ntnu.bidata.smg.group8.common.sensor.Sensor;
import edu.ntnu.bidata.smg.group8.common.util.AppLogger;
import org.slf4j.Logger;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * <h3>Device Catalog - Registry of Sensors and Actuators</h3>
 *
 * <p>The DeviceCatalog manages all sensors and actuators attached to a sensor node.
 * In accordance with project requirements, each sensor node should:</p>
 * <ul>
 *   <li>Have multiple sensors attached</li>
 *   <li>Support different sensor types</li>
 *   <li>Act as an actuator node with several actuators</li>
 *   <li>Support different actuator types</li>
 * </ul>
 *
 * <p>This catalog starts empty, allowing each node to have a different configuration
 * of sensors and actuators. We use a {@link Map} to store devices by their unique keys
 * (e.g., "temp" for temperature sensor, "heater" for heater actuator).</p>
 *
 * <p><b>Responsibilities:</b></p>
 * <ul>
 *   <li>Store and retrieve sensors by key</li>
 *   <li>Store and retrieve actuators by key</li>
 *   <li>Provide bulk operations (e.g., get all sensor readings)</li>
 *   <li>Manage device lifecycle (add/remove devices)</li>
 * </ul>
 *
 * <p>The purpose of this class is to encapsulate all logic related to
 * managing the collection of sensors and actuators, providing a clean
 * interface for other components (like {@link NodeAgent}) to interact with the
 * devices without needing to know the details of how they are stored or managed.</p>
 *
 * <p>Due to the sensors and actuators being abstract, we can easily
 * add new types of devices in the future without modifying this class.</p>
 *
 * @author Ida Soldal
 * @version 29.10.2025
 */
public class DeviceCatalog {
    
    private static final Logger log = AppLogger.get(DeviceCatalog.class);
    
    private final Map<String, Sensor> sensors;
    private final Map<String, Actuator> actuators;
    
    /**
     * Creates a new empty DeviceCatalog.
     *
     * <p>Devices must be added using {@link #addSensor(Sensor)} and
     * {@link #addActuator(Actuator)}. This allows different nodes to have
     * different sensor/actuator configurations.</p>
     */
    public DeviceCatalog() {
        this.sensors = new HashMap<>();
        this.actuators = new HashMap<>();
        log.debug("DeviceCatalog created");
    }
    
    // ============================================================
    // SENSOR MANAGEMENT
    
    /**
     * Adds a sensor to the catalog.
     *
     * @param sensor The sensor to add (must not be null)
     * @throws IllegalArgumentException if sensor is null
     */
    public void addSensor(Sensor sensor) {
        if (sensor == null) {
            throw new IllegalArgumentException("Sensor cannot be null");
        }
        
        String key = sensor.getKey();
        if (sensors.containsKey(key)) { // Warn if replacing existing sensor
            log.warn("Replacing existing sensor with key: {}", key);
        }
        
        sensors.put(key, sensor); // Add or replace sensor
        log.info("Added sensor: {} ({})", key, sensor.getUnit());
    }
    
    /**
     * Retrieves a sensor by its key.
     *
     * @param sensorKey The key of the sensor (e.g., "temp", "hum")
     * @return The sensor, or null if not found
     */
    public Sensor getSensor(String sensorKey) {
        return sensors.get(sensorKey);
    }
    
    /**
     * Gets all sensor readings as a map.
     *
     * <p>This is typically called by NodeAgent when sending periodic
     * sensor data to the broker.</p>
     *
     * @return A map of sensor keys to their current readings
     */
    public Map<String, Double> getAllSensorReadings() {
        Map<String, Double> readings = new HashMap<>();
        
        for (Map.Entry<String, Sensor> entry : sensors.entrySet()) { // Iterate over all sensors
            String key = entry.getKey();
            Sensor sensor = entry.getValue();
            
            try { // Try to read sensor value
                double reading = sensor.getReading();
                readings.put(key, reading);
                log.debug("Read sensor {}: {}{}", key, reading, sensor.getUnit());
            } catch (Exception e) {
                log.error("Failed to read sensor {}: {}", key, e.getMessage());
            }
        }
        
        return readings;
    }
    
    /**
     * Returns all registered sensors.
     *
     * <p>We use {@link Collections} to prevent modification of the returned collection. This way,
     * other components cannot accidentally alter the internal state of the catalog.</p>
     *
     * @return An unmodifiable collection of all sensors
     */
    public Collection<Sensor> getAllSensors() {
        return Collections.unmodifiableCollection(sensors.values());
    }
    
    /**
     * Returns the number of registered sensors.
     *
     * @return The amount of sensors
     */
    public int getSensorCount() {
        return sensors.size();
    }
    
    // ============================================================
    // ACTUATOR MANAGEMENT
    
    /**
     * Adds an actuator to the catalog.
     *
     * @param actuator The actuator to add (must not be null)
     * @throws IllegalArgumentException if actuator is null
     */
    public void addActuator(Actuator actuator) {
        if (actuator == null) {
            throw new IllegalArgumentException("Actuator cannot be null");
        }
        
        String key = actuator.getKey();
        if (actuators.containsKey(key)) { // Warn if replacing existing actuator
            log.warn("Replacing existing actuator with key: {}", key);
        }
        
        actuators.put(key, actuator);
        log.info("Added actuator: {}", key);
    }
    
    /**
     * Retrieves an actuator by its key.
     *
     * @param actuatorKey The key of the actuator (e.g., "heater", "fan")
     * @return The actuator, or null if not found
     */
    public Actuator getActuator(String actuatorKey) {
        return actuators.get(actuatorKey);
    }
    
    /**
     * Returns all registered actuators.
     *
     * @return An unmodifiable collection of all actuators
     */
    public Collection<Actuator> getAllActuators() {
        return Collections.unmodifiableCollection(actuators.values());
    }
    
    /**
     * Returns the number of registered actuators.
     *
     * @return The actuator count
     */
    public int getActuatorCount() {
        return actuators.size();
    }
    
    // ============================================================
    // UTILITY METHODS
    
    /**
     * Returns a summary of the device catalog for debugging.
     *
     * <p>Useful for startup logs to see what devices are registered.</p>
     *
     * @return Formatted string summary of sensors and actuators
     */
    public String summary() {
        return String.format(
                "DeviceCatalog[sensors=%d (%s), actuators=%d (%s)]",
                sensors.size(),
                String.join(", ", sensors.keySet()),
                actuators.size(),
                String.join(", ", actuators.keySet())
        );
    }
    
    /**
     * Checks if the catalog is empty (no sensors or actuators).
     *
     * @return true if the catalog contains no devices
     */
    public boolean isEmpty() {
        return sensors.isEmpty() && actuators.isEmpty();
    }
}