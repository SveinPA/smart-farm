package edu.ntnu.bidata.smg.group8.common.sensor;

/**
 * <h3>Interface that represents a generic sensor.</h3>
 *
 * <p>This interface defines the basic methods that all sensor types must implement,
 * including methods to get the <b>sensor's key, unit of measurement, and to read the simulated value</b>
 * from the sensor.</p>
 *
 * <p>By having an interface for sensors, we can easily add new sensor types in the future
 * without changing the existing code. Each sensor type can implement this interface
 * to provide its specific behavior.</p>
 *
 * @author Ida Soldal
 * @version 17.10.2025
 */
public interface Sensor {
    /**
     * Returns the short key identifying the sensor type.
     * <p><b>Example:</b> "temp" for temperature sensor, "hum" for humidity sensor.</p>
     *
     * @return the short key identifying the sensor type
     */
    String getKey();

    /**
     * Returns the unit of measurement for the sensor (°C, %, lux, etc.).
     *
     * @return the unit of measurement for the sensor
     */
    String getUnit();

    /**
     * Returns the current simulated reading from the sensor.
     *
     * <p>This method simulates reading a value from the sensor,
     * returning a random value within a realistic range for the sensor type.</p>
     *
     * @return the current simulated reading from the sensor
     */
    double getReading();
}
