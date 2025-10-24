package edu.ntnu.bidata.smg.group8.sensor.entity.sensors;

import edu.ntnu.bidata.smg.group8.common.sensor.AbstractSensor;

/**
 * <h3>Simulated Temperature Sensor.</h3>
 * <p>This class represents a simulated temperature sensor in a greenhouse environment,
 * extending the {@link AbstractSensor} class to provide specific functionality
 * for temperature measurements.</p>
 *
 * <p>The temperature sensor simulates readings within a realistic range for greenhouse conditions,
 * specifically between 10.0°C and 35.0°C. The sensor's readings
 * vary over time to mimic real-world fluctuations, with a drift factor of <b>0.7%</b>. This way,
 * we can ensure that the temperature readings remain believable and reflect typical environmental changes.</p>
 *
 * <p>By using this class, we can easily integrate temperature sensing capabilities
 * into our greenhouse monitoring system, allowing for effective tracking and management
 * of temperature conditions.</p>
 *
 * @author Ida Soldal
 * @version 24.10.2025
 */
public class TemperatureSensor extends AbstractSensor {
    // Greenhouse Temperature Sensor Constants
    private static final String SENSOR_KEY = "temp";
    private static final String SENSOR_UNIT = "°C";
    private static final double MIN_TEMPERATURE = 10.0; // Minimum realistic temperature
    private static final double MAX_TEMPERATURE = 35.0; // Maximum realistic temperature
    private static final double DRIFT_FACTOR = 0.007; // 0.7% drift factor (variation) for temperature

    /**
     * Constructor for TemperatureSensor. Initializes the temperature sensor
     * with its specific key, unit, and realistic value range
     * (10-35°C range, 0.7% drift factor).
     *
     * <p>The constructor calls the superclass constructor to set up
     * the common properties of a sensor.</p>
     *
     * @see AbstractSensor#AbstractSensor(String, String, double, double)
     */
    public TemperatureSensor() {
        super(SENSOR_KEY, SENSOR_UNIT, MIN_TEMPERATURE, MAX_TEMPERATURE);
    }

    /**
     * Retrieves the current temperature reading from the sensor.
     *
     * @return The current temperature reading in degrees Celsius (°C).
     */
    @Override
    public double getReading() {
        return varyReading(DRIFT_FACTOR);
    }
}
