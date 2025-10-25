package edu.ntnu.bidata.smg.group8.sensor.entity.sensors;

import edu.ntnu.bidata.smg.group8.common.sensor.AbstractSensor;

/**
 * <h3>Simulated Wind Speed Sensor</h3>
 * <p>This class represents an external wind speed sensor mounted on the greenhouse
 * structure, extending the {@link AbstractSensor} class to provide specific functionality
 * for ambient wind speed measurements.</p>
 *
 * <p><b>Wind Speed Ranges:</b> Measured in meters per second (m/s):
 * <ul>
 *     <li><b>0-5 m/s:</b> Calm to light breeze - safe for ventilation</li>
 *     <li><b>5-10 m/s:</b> Moderate breeze - caution with open vents</li>
 *     <li><b>10-15 m/s:</b> Strong breeze - close vents, secure structures</li>
 *     <li><b>15+ m/s:</b> Gale conditions - potential structural damage</li>
 * </ul>
 * <p><b>Sensor Location:</b> This sensor is mounted externally on the greenhouse structure
 * to measure ambient wind speed for weather monitoring and automated ventilation control.
 * It is <b>not</b> affected by potential internal fan actuators.</p>
 *
 * <p>The sensor simulates readings between 0 m/s (calm) and 25 m/s (storm conditions)
 * with a drift factor of <b>4%</b>, reflecting moderate wind variability as weather
 * patterns shift.</p>
 *
 * @author Ida Soldal
 * @version 24.10.2025
 */
public class WindSpeedSensor extends AbstractSensor {

    // Wind Speed Sensor Constants
    private static final String SENSOR_KEY = "wind";
    private static final String SENSOR_UNIT = "m/s";
    private static final double MIN_WIND_SPEED = 0.0;   // Calm conditions
    private static final double MAX_WIND_SPEED = 25.0;  // Strong storm conditions
    private static final double DRIFT_FACTOR = 0.04;    // 4% drift factor (moderate variability)

    /**
     * Constructor for WindSpeedSensor. Initializes the wind speed sensor
     * with its specific key, unit, and realistic value range
     * (0-25 m/s range, 4% drift factor).
     *
     * <p>The constructor calls the superclass constructor to set up
     * the common properties of a sensor.</p>
     *
     * @see AbstractSensor#AbstractSensor(String, String, double, double)
     */
    public WindSpeedSensor() {
        super(SENSOR_KEY, SENSOR_UNIT, MIN_WIND_SPEED, MAX_WIND_SPEED);
    }

    /**
     * Retrieves the current wind speed reading from the sensor.
     *
     * @return The current wind speed reading in meters per second (m/s).
     */
    @Override
    public double getReading() {
        return varyReading(DRIFT_FACTOR);
    }
}