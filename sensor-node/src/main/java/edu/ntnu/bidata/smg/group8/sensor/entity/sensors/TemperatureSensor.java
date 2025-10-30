package edu.ntnu.bidata.smg.group8.sensor.entity.sensors;

import edu.ntnu.bidata.smg.group8.common.sensor.AbstractSensor;
import edu.ntnu.bidata.smg.group8.sensor.entity.actuators.FanActuator;
import edu.ntnu.bidata.smg.group8.sensor.entity.actuators.HeaterActuator;
import edu.ntnu.bidata.smg.group8.sensor.entity.actuators.WindowActuator;
import edu.ntnu.bidata.smg.group8.sensor.logic.DeviceCatalog;

/**
 * <h3>Simulated Temperature Sensor</h3>
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
    private DeviceCatalog catalog;

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
     * <p>The reading is influenced by:
     * <ul>
     *   <li>Heater actuator: Adds ~5°C when ON</li>
     *   <li>Fan actuator: Reduces ~1.5°C when ON (air circulation)</li>
     *   <li>Window actuator: Reduces ~2°C when OPEN (outside air)</li>
     *   <li>Natural drift: 0.7% variation to simulate environmental changes</li>
     * </ul>
     *
     * @return The current temperature reading in degrees Celsius (°C)
     */
    @Override
    public double getReading() {
        // Start with natural reading (includes base value + natural drift)
        double temperature = varyReading(DRIFT_FACTOR);
        
        // Apply actuator effects if catalog is available
        if (catalog != null) {
            var heater = catalog.getActuator("heater");
            if (heater instanceof HeaterActuator) {
                HeaterActuator h = (HeaterActuator) heater;
                if (h.isOn()) {
                    temperature += 5.0; // Heater adds significant warmth
                }
            }
            var fan = catalog.getActuator("fan");
            if (fan instanceof FanActuator) {
                FanActuator f = (FanActuator) fan;
                if (f.isOn()) {
                    temperature -= 1.5; // Fan circulates air, slight cooling
                }
            }
            var window = catalog.getActuator("window");
            if (window instanceof WindowActuator) {
                WindowActuator w = (WindowActuator) window;
                if (w.isOpen()) {
                    temperature -= 2.0; // Outside air cools greenhouse
                }
            }
        }
        // Ensure temperature stays within physically possible range
        temperature = Math.max(MIN_TEMPERATURE - 5, Math.min(MAX_TEMPERATURE + 5, temperature));
        return Math.round(temperature * 10.0) / 10.0; // Round to 1 decimal
    }

    /**
     * Sets the device catalog for the sensor to access actuators.
     *
     * <p>This allows the sensor to consider the state of various actuators
     * (like heaters, fans, and windows) when calculating its readings.</p>
     *
     * @param catalog The DeviceCatalog instance containing actuators
     */
    public void setCatalog(DeviceCatalog catalog) {
        this.catalog = catalog;
    }
}
