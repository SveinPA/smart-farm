package edu.ntnu.bidata.smg.group8.sensor.entity.actuators;

import edu.ntnu.bidata.smg.group8.common.actuator.AbstractActuator;
import edu.ntnu.bidata.smg.group8.common.util.AppLogger;
import org.slf4j.Logger;
/**
 * Represents a heater actuator in the greenhouse environment.
 *
 * <p>This class extends the {@link AbstractActuator} to provide
 * specific functionality for controlling a heater.</p>
 *
 *<p>The heater actuator has the following features:</p>
 *<ul>
 *     <li>Temperature range from 0°C (off) to 40°C (maximum).</li>
 *     <li>Methods to check if the heater is on, off, or at maximum temperature.</li>
 *     <li>Methods to set target temperature and turn off the heater.</li>
 * </ul>
 *
 * <p>The logger is used to log important events and state changes
 * related to the heater's operation such as temperature adjustments.</p>
 *
 * @author Mona Amundsen
 * @version 25.10.25
 */

public class HeaterActuator extends AbstractActuator {
  // Logger for logging heater actuator activities
  private static final Logger log = AppLogger.get(HeaterActuator.class);

  private static final String KEY = "heater";
  private static final String UNIT = "°C";
  private static final double MIN_VALUE = 0.0; // Minimum temperature
  private static final double MAX_VALUE = 40.0; // Maximum temperature

  /**
   * Constructor for the heater actuator.
   * Initialize the heater with temperature range 0-40°C.
   * Initial state is 25°C.
   */
  public HeaterActuator() {
    super(KEY, UNIT, MIN_VALUE, MAX_VALUE);
    // Set initial target value to 25°C
    this.targetValue = 25.0;
    log.info("Heater actuator initialized with default target temperature: 25°C");
  }

  /**
   * Check if the heater is currently on (temperature above minimum).
   *
   * @return true if the heater temperature is greater than minimum, false otherwise
   */
  public boolean isOn() {
    return getCurrentValue() > MIN_VALUE;
  }

  /**
   * Check if the heater is currently off (temperature at minimum).
   *
   * @return true if the heater temperature is at minimum, false otherwise
   */
  public boolean isOff() {
    return getCurrentValue() <= MIN_VALUE;
  }

  /**
   * Check if the heater is at maximum temperature.
   *
   * @return true if the heater temperature is at maximum, false otherwise
   */
  public boolean isAtMaxTemperature() {
    return getCurrentValue() >= MAX_VALUE;
  }

  /**
   * Set the heater to a specific target temperature (thermostat setting).
   * This sets the temperature that the heater will try to maintain.
   *
   * <p>The temperature must be within the valid range of
   * 0°C to 40°C.</p>
   *
   * <p>The method uses the {@link #act(double)} method
   * to set the heater to the desired temperature.</p>

   * @param temperature the desired target temperature
   * @throws IllegalArgumentException if the temperature is out of range
   */
  public void setTargetTemperature(double temperature) {
    if (temperature < MIN_VALUE || temperature > MAX_VALUE) {
      log.error("Invalid heater temperature: {} °C (valid range: {}-{} °C)",
              temperature, MIN_VALUE, MAX_VALUE);
      throw new IllegalArgumentException("Temperature out of range");
    }
    log.info("Heater target temperature changed from {} °C to {} °C",
            getTargetValue(), temperature);
    act(temperature);
  }

  /**
   * Turn the heater off (sets temperature to minimum).
   *
   * <p>This method uses the {@link #act(double)} method
   * to set the heater to its minimum temperature value (0°C).</p>
   */
  public void turnOff() {
    log.info("Turning heater OFF (current: {} °C)", getCurrentValue());
    act(MIN_VALUE);
  }

  /**
   * Get the current target temperature of the heater.
   *
   * @return the target temperature in °C
   */
  public double getTargetTemperature() {
    return getTargetValue();
  }
}
