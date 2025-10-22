package edu.ntnu.bidata.smg.group8.common.sensor;

import java.util.Random;

/**
 * <h3>Abstract Class for Sensors.</h3>
 * <p>This abstract class provides a common foundation for all sensor types,
 * implementing the {@link Sensor} interface. It includes shared properties such as
 * the sensor's key, unit of measurement, and realistic value ranges.</p>
 *
 * <p>Subclasses must implement the {@link Sensor#getReading()} method to provide
 * specific behavior for reading sensor values.</p>
 *
 * <p>The following methods are implemented in this abstract class:</p>
 * <ul>
 *    <li>{@link Sensor#getKey()}</li>
 *   <li>{@link Sensor#getUnit()}</li>
 *  <li>Protected method {@link #varyReading(double)} to simulate realistic variations in sensor readings over time.</li>
 * </ul>
 *
 * <p>By using this abstract class, we ensure that all sensor types share common functionality
 * while allowing for specific implementations in subclasses. That way, we can easily scale
 * our sensor system by adding new sensor types without duplicating code.</p>
 *
 * @author Ida Soldal
 * @version 17.10.2025
 */
public abstract class AbstractSensor implements Sensor {
    private final String key;
    private final String unit;
    private final double minValue;
    private final double maxValue;
    private double currentValue;
    private final Random random = new Random();

    /**
     * Constructor for AbstractSensor. This constructor initializes the sensor with its key, unit,
     * minimum and maximum realistic values.
     *
     * <p>The current value is initialized to the midpoint between minValue and maxValue.</p>
     * <p>Subclasses call this constructor to set up the common properties of the sensor.</p>
     * @param key       the short key identifying the sensor type (e.g., "temp" for temperature sensor)
     * @param unit      the unit of measurement for the sensor (e.g., "°C", "%", "lux")
     * @param minValue  the minimum realistic value for the sensor
     * @param maxValue  the maximum realistic value for the sensor
     */
    protected AbstractSensor(String key, String unit, double minValue, double maxValue) {
        this.key = key;
        this.unit = unit;
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.currentValue = minValue + (maxValue - minValue) / 2;
    }

    /**
     * Returns the key identifying the sensor type.
     *
     * @return the key identifying the sensor type
     */
    @Override
    public String getKey() {
        return key;
    }

    /**
     * Returns the unit of measurement for the sensor.
     *
     * @return the unit of measurement for the sensor
     */
    @Override
    public String getUnit() {
        return unit;
    }

    /**
     * Applies a small random variation to the current sensor value
     * to simulate real-world fluctuations. We use this method to generate more
     * <b>realistic</b> sensor readings over time.
     *
     * <p>The {@code factor} parameter controls how quickly or slowly the sensor value changes:</p>
     * <ul>
     *     <li>A smaller factor (e.g., 0.01) results in slower, more gradual and stable changes.</li>
     *    <li>A larger factor (e.g., 0.1) allows for quicker, more significant changes.</li>
     * </ul>
     *
     * <p>We can adjust the factor based on the type of sensor and how rapidly we expect its readings to change in reality.
     * <b>For example:</b> temperature sensors might use a smaller factor for gradual changes,
     * while light sensors might use a larger factor for rapid changes.</p>
     *
     * <p>The method is typically called each time a new reading is requested by the {@link #getReading()} method,
     * allowing the sensor value to evolve over time in a realistic manner.</p>
     * @param factor the factor between 0.0 and 1.0 controlling the rate of change per update
     * @return the new sensor value after applying the variation
     */
    protected double varyReading(double factor) {
        double change = (random.nextDouble() - 0.5) * (maxValue - minValue) * factor; // Get a small change
        currentValue = Math.max(minValue, Math.min(maxValue, currentValue + change)); // Apply change within bounds
        return currentValue; // Return the updated value
    }
}
