package edu.ntnu.bidata.smg.group8.sensor.entity.actuators;

import edu.ntnu.bidata.smg.group8.common.actuator.AbstractActuator;
import edu.ntnu.bidata.smg.group8.common.util.AppLogger;
import org.slf4j.Logger;

/**
 * Represents a window actuator in the greenhouse environment.
 *
 * <p>This class extends the {@link AbstractActuator} to provide
 * specific functionality for controlling a window.</p>
 *
 * <p>The window actuator has functions such as:</p>
 * <ul>
 *     <li>Opening range from 0% (closed) to 100% (fully open).</li>
 *     <li>Methods to check if the window is open, fully open, or closed.</li>
 *     <li>Methods to open, close, and set specific opening percentages.</li>
 * </ul>
 *
 * <p>The logger is used to log important events and state changes
 * * related to the window's operation, such as opening and closing actions.</p>
 *
 * @author Mona Amundsen
 * @version 25.10.25
 */
public class WindowActuator extends AbstractActuator {
  // Logger for logging window actuator activities
  private static final Logger log = AppLogger.get(WindowActuator.class);
  private static final String KEY = "window";
  private static final String UNIT = "%";
  private static final double MIN_VALUE = 0.0; // Closed
  private static final double MAX_VALUE = 100.0; // Fully open

  /**
   * Constructor for the window actuator.
   * Initialize the window with opening range 0-100%.
   *
   * <p>Initial state is 50% open midpoint.</p>
   */
  public WindowActuator() {
    super(KEY, UNIT, MIN_VALUE, MAX_VALUE);
    log.info ("Window actuator initialized with opening at {} %", getCurrentValue());
  }

  /**
   * Check if the window is currently open (any opening above 0%).
   *
   * @return true if the window is open, false if closed
   */
  public boolean isOpen() {
    return getCurrentValue() > MIN_VALUE;
  }

  /**
   * Check if the window is fully open (100%).
   *
   * @return true if window is equal to 100% opening
   */
  public boolean isFullyOpen() {
    return getCurrentValue() == MAX_VALUE;
  }

  /**
   * Check if the window is closed (0%).
   *
   * @return true if window is equal to 0% opening
   */
  public boolean isClosed() {
    return getCurrentValue() == MIN_VALUE;
  }

  /**
   * Fully open the window (100%).
   */
  public void open() {
    act(MAX_VALUE);
  }

  /**
   * Close the window (0%).
   */
  public void close() {
    act(MIN_VALUE);
  }

  /**
   * Open the window partially to a specific percentage.
   * Opening should be between 0% (closed) and 100% (fully open).
   *
   * @param percentage The desired opening percentage for the window.
   * @throws IllegalArgumentException if percentage is out of bounds (0-100).
   */
  public void openPercentage(double percentage) {
    if (percentage < MIN_VALUE || percentage > MAX_VALUE) {
        log.error("Invalid window opening percentage: {} % (valid range: {}-{} %)",
                percentage, MIN_VALUE, MAX_VALUE);
      throw new IllegalArgumentException("Percentage must be between 0 and 100.");
    }
    log.info("Window opening changed from {} % to {} %",
            getCurrentValue(), percentage);
    act(percentage);
  }

  /**
   * Get the window slightly for minimal ventilation (25% open).
   * Useful for gentle air circulation.
   *
   * <p>This method use the {@link #act(double)} method
   * to set the window to 25% open.</p>
   */
  public void openSlightly() {
    act(25.0);
  }

  /**
   * Get the window half open (50%).
   * Useful for moderate ventilation.
   *
   * <p>This method use the {@link #act(double)} method
   * to set the window to 50% open.</p>
   */
  public void openHalfway() {
    act(50.0);
  }

  /**
   * Get the window mostly open (75%).
   * Useful for high ventilation.
   *
   * <p>This method use the {@link #act(double)} method
   * to set the window to 75% open.</p>
   */
  public void openMostly() {
    act(75.0);
  }
}
