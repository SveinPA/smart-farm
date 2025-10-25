package edu.ntnu.bidata.smg.group8.sensor.entity.sensors;

import edu.ntnu.bidata.smg.group8.common.sensor.AbstractSensor;

/**
 * <h3>Simulated Humidity Sensor</h3>
 * <p>This class represents a simulated humidity sensor in a greenhouse environment,
 * extending the {@link AbstractSensor} class to provide specific functionality
 * for humidity measurements.</p>
 *
 * <p>The humidity sensor simulates readings within a realistic range for greenhouse conditions,
 * specifically between 30.0% and 90.0%, where 30.0% represents <b>dry air</b> and 90.0% represents <b>very humid air</b>.
 * The sensor's readings vary over time to mimic real-world fluctuations, with a drift factor of <b>2%</b>.</p>
 *
 * <p>Humidity levels can change rapidly due to factors such as watering plants or changes in ventilation,
 * hence the <b>higher drift factor.</b> This way, we can ensure that the humidity readings remain believable
 * and reflect typical environmental changes.</p>
 *
 * <p>By using this class, we can easily integrate humidity sensing capabilities
 * into our greenhouse monitoring system, allowing for effective tracking and management
 * of humidity conditions.</p>
 *
 * @author Ida Soldal
 * @version 24.10.2025
 */
public class HumiditySensor extends AbstractSensor {
    // Greenhouse Humidity Sensor Constants
    private static final String SENSOR_KEY = "hum";
    private static final String SENSOR_UNIT = "%";
    private static final double MIN_HUMIDITY = 30.0; // Minimum realistic humidity
    private static final double MAX_HUMIDITY = 90.0; // Maximum realistic humidity
    private static final double DRIFT_FACTOR = 0.02; // 2% drift factor (variation) for humidity

    /**
     * Constructor for HumiditySensor. Initializes the humidity sensor
     * with its specific key, unit, and realistic value range
     * (30-90% range, 2% drift factor).
     *
     * <p>The constructor calls the superclass constructor to set up
     * the common properties of a sensor.</p>
     *
     * @see AbstractSensor#AbstractSensor(String, String, double, double)
     */
    public HumiditySensor() {
        super(SENSOR_KEY, SENSOR_UNIT, MIN_HUMIDITY, MAX_HUMIDITY);
    }

    /**
     * Retrieves the current humidity reading from the sensor.
     *
     * @return The current humidity reading in percentage (%).
     */
    @Override
    public double getReading() {
        return varyReading(DRIFT_FACTOR);
    }
}
