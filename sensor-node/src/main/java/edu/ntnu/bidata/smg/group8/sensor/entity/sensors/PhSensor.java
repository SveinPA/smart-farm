package edu.ntnu.bidata.smg.group8.sensor.entity.sensors;

import edu.ntnu.bidata.smg.group8.common.sensor.AbstractSensor;

/**
 * <h3>Simulated pH Sensor</h3>
 * <p>This class represents a pH sensor in a greenhouse environment,
 * extending the {@link AbstractSensor} class to provide specific functionality
 * for pH measurements of soil or nutrient solution.</p>
 *
 * <p><b>pH Basics:</b> The pH scale (0-14) measures acidity/alkalinity. Values below 7 are
 * acidic, 7 is neutral, and above 7 is alkaline (basic). Most greenhouse crops prefer
 * slightly acidic to neutral soil (pH 6.0-7.0) for optimal nutrient absorption.</p>
 *
 * <p>The pH sensor simulates readings within a realistic range for greenhouse conditions,
 * specifically between 4.5 (acidic) and 8.5 (alkaline). The drift factor is set to <b>0.3%</b>,
 * reflecting the very gradual nature of pH changes in real greenhouse environments due to
 * soil buffering capacity. Rapid pH shifts only occur during significant events like
 * fertilizer application or irrigation.</p>
 *
 * <p>By using this class, we can easily integrate pH monitoring capabilities
 * into our greenhouse management system, allowing for effective tracking of soil conditions.</p>
 *
 * @author Ida Soldal
 * @version 24.10.2025
 */
public class PhSensor extends AbstractSensor {

    // Greenhouse pH Sensor Constants
    private static final String SENSOR_KEY = "ph";
    private static final String SENSOR_UNIT = "pH";
    private static final double MIN_PH = 4.5;  // Minimum realistic pH (acidic)
    private static final double MAX_PH = 8.5;  // Maximum realistic pH (alkaline)
    private static final double DRIFT_FACTOR = 0.003;  // 0.3% drift factor (very gradual changes)

    /**
     * Constructor for PHSensor. Initializes the pH sensor
     * with its specific key, unit, and realistic value range
     * (4.5-8.5 pH range, 0.3% drift factor).
     *
     * <p>The constructor calls the superclass constructor to set up
     * the common properties of a sensor.</p>
     *
     * @see AbstractSensor#AbstractSensor(String, String, double, double)
     */
    public PhSensor() {
        super(SENSOR_KEY, SENSOR_UNIT, MIN_PH, MAX_PH);
    }

    /**
     * Retrieves the current pH reading from the sensor.
     *
     * @return The current pH reading (unitless, pH scale 4.5-8.5).
     */
    @Override
    public double getReading() {
        return varyReading(DRIFT_FACTOR);
    }
}