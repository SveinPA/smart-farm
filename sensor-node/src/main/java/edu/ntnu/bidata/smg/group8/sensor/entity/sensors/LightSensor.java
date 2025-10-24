package edu.ntnu.bidata.smg.group8.sensor.entity.sensors;

import edu.ntnu.bidata.smg.group8.common.sensor.AbstractSensor;

/**
 * <h3>Simulated Light Sensor.</h3>
 * <p>This class represents a simulated light sensor in a greenhouse environment,
 * extending the {@link AbstractSensor} class to provide specific functionality
 * for light measurements.</p>
 *
 * <p>The light sensor simulates readings within a realistic range for greenhouse conditions,
 * specifically between <b>0 lx and 80000 lx</b>, where 0 lx represents <b>darkness</b>
 * and 80000 lx represents <b>bright sunlight</b>. Since it is situated in a greenhouse,
 * the maximum light level is capped at 80000 lx to account for shading and glass filtering.</p>
 *
 * <p>The drift factor is set to <b>2.5%</b>, representing higher variability in light levels
 * typical of a cloudy day when illumination fluctuates significantly.</p>
 *
 * <p>By using this class, we can easily integrate light sensing capabilities
 * into our greenhouse monitoring system, allowing for effective tracking and management
 * of light conditions.</p>
 *
 * @author Ida Soldal
 * @version 24.10.2025
 */
public class LightSensor extends AbstractSensor {
    // Greenhouse Light Sensor Constants
    private static final String SENSOR_KEY = "light";
    private static final String SENSOR_UNIT = "lx";
    private static final double MIN_LIGHT = 0.0; // Minimum realistic light level
    private static final double MAX_LIGHT = 80000.0; // Maximum realistic light level
    private static final double DRIFT_FACTOR = 0.025; // 2.5% drift factor (variation) for light

    /**
     * Constructor for LightSensor. Initializes the light sensor
     * with its specific key, unit, and realistic value range
     * (0-80000 lx range, 2.5% drift factor).
     *
     * <p>The constructor calls the superclass constructor to set up
     * the common properties of a sensor.</p>
     *
     * @see AbstractSensor#AbstractSensor(String, String, double, double)
     */
    public LightSensor() {
        super(SENSOR_KEY, SENSOR_UNIT, MIN_LIGHT, MAX_LIGHT);
    }

    /**
     * Retrieves the current light reading from the sensor.
     *
     * @return The current light reading in lux (lx).
     */
    @Override
    public double getReading() {
        return varyReading(DRIFT_FACTOR);
    }
}
