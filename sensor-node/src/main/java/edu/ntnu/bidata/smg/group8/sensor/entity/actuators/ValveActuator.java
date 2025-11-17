package edu.ntnu.bidata.smg.group8.sensor.entity.actuators;

import edu.ntnu.bidata.smg.group8.common.actuator.AbstractActuator;
import edu.ntnu.bidata.smg.group8.common.util.AppLogger;
import org.slf4j.Logger;

/**
 * Represents a valve actuator in the greenhouse environment.
 *
 * <p>This class extends the {@link AbstractActuator} to provide
 * specific functionality for controlling a valve,
 * including methods to open, close, and check the valve state.</p>
 *
 * <p>A valve actuator has the following possibles:</p>
 * <ul>
 *     <li>Opening range from 0% (closed) to 100% (fully open).</li>
 *     <li>Methods to check if the valve is open, fully open, or closed.</li>
 *     <li>Methods to open, close, and set specific opening percentages.</li>
 *</ul>
 *
 * <p>The logger is used to log important events and state changes
 * * related to the valve's operation, such as opening and closing actions.</p>
 *
 * @author Mona Amundsen
 * @version 25.10.25
 */
public class ValveActuator extends AbstractActuator {
  // Logger for logging valve actuator activities
  private static final Logger log = AppLogger.get(ValveActuator.class);
  private static final String KEY = "valve";
  private static final String UNIT = "%";
  private static final double MIN_VALUE = 0.0; // Closed
  private static final double MAX_VALUE = 100.0; // Fully open

  /**
   * Constructor for the valve actuator.
   * Initialize the valve with opening range 0-100%.
   */
  public ValveActuator() {
    super(KEY, UNIT, MIN_VALUE, MAX_VALUE);
    log.info("Valve actuator initialized with opening at {} %", getCurrentValue());
  }

  /**
   * Check if the valve is currently open (any flow).
   *
   * @return true if the valve is open, false if closed
   */
  public boolean isOpen() {
    return getCurrentValue() > 0;
  }

  /**
   * Check if the valve is fully open.
   *
   * @return true if valve is at 100% opening
   */
  public boolean isFullyOpen() {
    return getCurrentValue() >= MAX_VALUE;
  }

  /**
   * Check if the valve is closed.
   *
   * @return true if valve is at 0% opening
   */
  public boolean isClosed() {
    return getCurrentValue() <= MIN_VALUE;
  }

  /**
   * Fully open the valve (100% flow).
   *
   * <p>This method uses the {@link #act(double)} method
   * to set the valve to its maximum opening value.</p>
   */
  public void open() {
    act(MAX_VALUE);
  }

  /**
   * Close the valve (0% flow).
   *
   * <p>This method uses the {@link #act(double)} method
   * to set the valve to its minimum opening value.</p>
   */
  public void close() {
    act(MIN_VALUE);
  }

  /**
   * Set the valve to a specific opening percentage.
   * Useful for controlling flow rate.
   *
   * <p>This method calls the {@link #act(double)} method
   * to set the valve to the desired opening percentage.</p>
   *
   * @param percentage the desired opening percentage (0-100%)
   * @throws IllegalArgumentException if percentage is out of bounds (0-100).
   */
  public void setOpening(double percentage) {
    if (percentage < MIN_VALUE || percentage > MAX_VALUE) {
      log.error("Invalid valve opening percentage: {} % (valid range: {} % - {} %)",
              percentage, MIN_VALUE, MAX_VALUE);
      throw new IllegalArgumentException("Percentage must be between 0 and 100.");
    }
    log.info("Valve opening changed from {} % to {} %",
            getCurrentValue(), percentage);
    act(percentage);
  }
}
