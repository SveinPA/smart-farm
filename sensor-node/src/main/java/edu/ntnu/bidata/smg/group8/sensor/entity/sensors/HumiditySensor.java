package edu.ntnu.bidata.smg.group8.sensor.entity.sensors;

import edu.ntnu.bidata.smg.group8.common.sensor.AbstractSensor;
import edu.ntnu.bidata.smg.group8.sensor.entity.actuators.ValveActuator;
import edu.ntnu.bidata.smg.group8.sensor.entity.actuators.WindowActuator;
import edu.ntnu.bidata.smg.group8.sensor.logic.DeviceCatalog;

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
    private DeviceCatalog catalog;

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
     * <p>The reading is influenced by:
     * <ul>
     *   <li>Valve actuator: Adds ~15% when watering (ON)</li>
     *   <li>Window actuator: Reduces ~10% when OPEN (ventilation)</li>
     *   <li>Natural drift: 2% variation to simulate environmental changes</li>
     * </ul>
     *
     * @return The current humidity reading in percentage (%)
     */
    @Override
    public double getReading() {
        // Start with natural reading (includes base value + natural drift)
        double humidity = varyReading(DRIFT_FACTOR);
        
        // Apply actuator effects if catalog is available
        if (catalog != null) {
            var valve = catalog.getActuator("valve");
            if (valve instanceof ValveActuator) {
                ValveActuator v = (ValveActuator) valve;
                if (v.isOpen()) {
                    humidity += 15.0; // Watering significantly increases humidity
                }
            }
            var window = catalog.getActuator("window");
            if (window instanceof WindowActuator) {
                WindowActuator w = (WindowActuator) window;
                if (w.isOpen()) {
                    humidity -= 10.0; // Ventilation dries out the air
                }
            }
        }
        // Clamp to valid humidity range (0-100%)
        humidity = Math.max(0, Math.min(100, humidity));
        return Math.round(humidity * 10.0) / 10.0; // Round to 1 decimal
    }

    /**
     * Sets the device catalog for the sensor to access actuators.
     *
     * <p>This allows the sensor to consider the state of actuators
     * (like valves and windows) when calculating its readings.</p>
     *
     * @param catalog The DeviceCatalog containing actuators
     */
    public void setCatalog(DeviceCatalog catalog) {
        this.catalog = catalog;
    }
}
