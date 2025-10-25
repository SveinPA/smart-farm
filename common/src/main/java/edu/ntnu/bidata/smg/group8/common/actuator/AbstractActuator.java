package edu.ntnu.bidata.smg.group8.common.actuator;

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
 * @author Mona Amundsen
 * @version 21.10.25
 */
public abstract class AbstractActuator implements Actuator {
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
    if (value < minValue) {
      value = minValue;
    }
    if (value > maxValue) {
      value = maxValue;
    }
    this.targetValue = value;
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
    double diff = targetValue - currentValue;
    double step = (maxValue - minValue) * 0.05;

    // If the difference is smaller than the step, set to target
    // else, move by step towards target
    if (Math.abs(diff) <= step) {
      currentValue = targetValue;
    } else {
      currentValue += Math.copySign(step, diff);
    }
  }
}