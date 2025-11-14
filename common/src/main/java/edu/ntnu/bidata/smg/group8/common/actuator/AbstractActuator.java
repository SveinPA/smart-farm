package edu.ntnu.bidata.smg.group8.common.actuator;

import edu.ntnu.bidata.smg.group8.common.util.AppLogger;
import org.slf4j.Logger;

/**
 * Abstract class for actuators.
 * This class implements the {@link Actuator} interface and provides
 * common functionality for different types of actuators.
 *
 * <p>Provides shared fields and behaviors:
 * <ul>
 *     <li>Unique key to identify the actuator type.</li>
 *     <li>Unit of measurement.</li>
 *     <li>Current value, target value, minimum and maximum values.</li>
 *     <li>Methods to actuate and update the actuator state.</li>
 * </ul>
 *
 * <p>The logger is used to log important events and state changes
 * related to the actuator's operation (such as act and update methods).</p>
 *
 * @author Mona Amundsen
 * @version 21.10.25
 */
public abstract class AbstractActuator implements Actuator {
  // Logger for logging actuator activities
  private static final Logger log = AppLogger.get(AbstractActuator.class);

  private final String key;
  private final String unit;
  private final double minValue;
  private final double maxValue;
  private double currentValue;
  protected double targetValue;

  /**
   * Constructor for the abstract Actuator.
   */
  public AbstractActuator(String key, String unit,
                          double minValue, double maxValue) {
    this.key = key;
    this.unit = unit;
    this.minValue = minValue;
    this.maxValue = maxValue;
    this.currentValue = minValue + (maxValue - minValue) / 2;
    this.targetValue = this.currentValue;

    log.debug("Initialized {} actuator: range=[{}, {}] {}, inital= {}",
            key, minValue, maxValue, unit, currentValue);
  }

  /**
   * Returns the unique key of the actuator.
   *
   * <p>This key identifies what actuator we have.</p>
   *
   * @return the type of actuator
   */
  @Override
  public String getKey() {
    return key;
  }

  /**
   * Returns the unit of measurement for the actuator.
   *
   * @return unit of measurement
   */
  @Override
  public String getUnit() {
    return unit;
  }

  /**
   * Get the current value of the actuator.
   *
   * @return the current value
   */
  @Override
  public double getCurrentValue() {
    return currentValue;
  }

  /**
   * Get the target value of the actuator.
   *
   * @return the target value
   */
  @Override
  public double getTargetValue() {
    return targetValue;
  }

  /**
   * Get the minimum value of the actuator.
   *
   * @return the minimum value
   */
  @Override
  public double getMinValue() {
    return minValue;
  }

  /**
   * Get the maximum value of the actuator.
   *
   * @return the maximum value
   */
  @Override
  public double getMaxValue() {
    return maxValue;
  }

  /**
   * Actuate the actuator with the given value.
   *
   * <p>If a value less than the minimum value is given,
   * the target value is set to the minimum value. This also
   * applies to values greater than the maximum value.</p>
   *
   * @param value the value to actuate with
   */
  @Override
  public void act(double value) {
    double originalValue = value;
    if (value < minValue) {
      // Log a warning if value is below minimum
      log.warn("{} actuator: request value {} below minimum {}, clamping to min",
              key, originalValue, minValue);
      value = minValue;
    }
    // Log a warning if value is above maximum
    if (value > maxValue) {
      log.warn("{} actuator: request value {} above maximum {}, clamping to max",
              key, originalValue, maxValue);
      value = maxValue;
    }
    this.targetValue = value;
    this.currentValue = value;

    // Log the actuation event, debug = Useful debug info during development
    if (originalValue != value) {
      log.debug("{} actuator: target value set to {} {} (requested {})",
              key, targetValue, unit, originalValue);
    } else {
      // Log the actuation event, debug = Useful debug info during development
      log.debug("{} actuator: target value set to {} {}",
              key, targetValue, unit);
    }
  }

  /**
   * Update the actuator's state.
   *
   * <p>The current value is moved towards the target value
   * by a fixed step size (5% of the full range) each update call.
   * If the difference between the current and target value
   * is smaller than the step size, the current value is set
   * to the target value.</p>
   */
  @Override
  public void update() {
    double previousValue = currentValue;
    double diff = targetValue - currentValue;
    double step = (maxValue - minValue) * 0.05;

    // If already at target, do nothing
    if (Math.abs(diff) < 0.001) {
      // trace is used for: Very detailed internal steps, temporary debugging
      log.trace("{} actuator: already at target {} {}", key, currentValue, unit);
      return;
    }

    // If the difference is smaller than the step, set to target directly
    if (Math.abs(diff) <= step) {
      currentValue = targetValue;
      log.debug("{} actuator: reached target {} {} (from {})",
              key, currentValue, unit, previousValue);

      // else move towards target by step size
    } else {
      currentValue += Math.copySign(step, diff);
      log.trace("{} actuator: updating {} {} -> {} {} (target: {})",
              key, previousValue, unit, currentValue, unit, targetValue);
    }
  }
}