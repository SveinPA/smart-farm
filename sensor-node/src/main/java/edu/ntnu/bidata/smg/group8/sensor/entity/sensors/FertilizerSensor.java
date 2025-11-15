package edu.ntnu.bidata.smg.group8.sensor.entity.sensors;

import edu.ntnu.bidata.smg.group8.common.sensor.AbstractSensor;

/**
 * <h3>Simulated Fertilizer Sensor</h3>
 * <p>This class represents a fertilizer sensor in a greenhouse environment,
 * extending the {@link AbstractSensor} class to provide specific functionality
 * for nitrogen content measurements in soil or nutrient solution.</p>
 *
 * <p><b>Understanding Nitrogen Levels:</b> This sensor measures nitrogen (N), one of the three
 * primary plant nutrients. Measured in parts per million (ppm):
 * <ul>
 *     <li><b>0-50 ppm:</b> Very low - nutrient deficiency, yellowing leaves</li>
 *     <li><b>50-150 ppm:</b> Optimal range for most vegetables</li>
 *     <li><b>150-200 ppm:</b> High - good for heavy feeders (tomatoes, peppers)</li>
 *     <li><b>200+ ppm:</b> Very high - risk of nutrient burn</li>
 * </ul>
 * Proper nitrogen monitoring ensures healthy plant growth without over-fertilization.</p>
 *
 * <p>The sensor simulates readings between 0 ppm (depleted) and 300 ppm (very high fertilization)
 * with a drift factor of <b>0.5%</b>, reflecting the very gradual nature of nutrient changes.
 * Nitrogen levels change slowly as plants uptake nutrients and fertilizer dissolves over time.</p>
 *
 * @author Ida Soldal
 * @version 24.10.2025
 */
public class FertilizerSensor extends AbstractSensor {

    // Fertilizer (Nitrogen) Sensor Constants
    private static final String SENSOR_KEY = "fert";
    private static final String SENSOR_UNIT = "ppm";
    private static final double MIN_FERTILIZER = 0.0;     // Depleted/no fertilizer
    private static final double MAX_FERTILIZER = 300.0;   // Very high fertilization
    private static final double DRIFT_FACTOR = 0.005;     // 0.5% drift factor (very gradual changes)

    /**
     * Constructor for FertilizerSensor. Initializes the fertilizer sensor
     * with its specific key, unit, and realistic value range
     * (0-300 ppm range, 0.5% drift factor).
     *
     * <p>The constructor calls the superclass constructor to set up
     * the common properties of a sensor.</p>
     *
     * @see AbstractSensor#AbstractSensor(String, String, double, double)
     */
    public FertilizerSensor() {
        super(SENSOR_KEY, SENSOR_UNIT, MIN_FERTILIZER, MAX_FERTILIZER);
    }

    /**
     * Retrieves the current fertilizer (nitrogen) reading from the sensor.
     *
     * @return The current nitrogen reading in parts per million (ppm).
     */
    @Override
    public double getReading() {
        return varyReading(DRIFT_FACTOR);
    }
}