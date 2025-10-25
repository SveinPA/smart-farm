package edu.ntnu.bidata.smg.group8.common.actuator;

/**
 * An interface for actuators.
 * This interface defines common behaviors for different types of actuators.
 *
 * <p>All actuators should: </p>
 * <ul>
 *     <li>Have a unique key to identify the actuator type.</li>
 *     <li>Have a unit of measurement.</li>
 *     <li>Provide methods to get current, target, minimum, and maximum values.</li>
 *     <li>Provide a method to actuate the actuator with a given value.</li>
 *     <li>Provide a method to update the actuator's state.</li>
 * </ul>
 *
 * @author Mona Amundsen
 * @version 21.10.25
 */
public interface Actuator {

  /**
   * Returns the unique key of the actuator.
   *
   * @return unique key
   */
  String getKey();

  /**
   * Returns the unit of measurement for the actuator.
   *
   * @return unit of measurement
   */
  String getUnit();

  /**
   * Get the current value of the actuator.
   *
   * @return the current value
   */
  double getCurrentValue();

  /**
   * Get the target value of the actuator.
   *
   * @return the target value
   */
  double getTargetValue();

  /**
   * Get the minimum value of the actuator.
   *
   * @return the minimum value
   */
  double getMinValue();

  /**
   * Get the maximum value of the actuator.
   *
   * @return the maximum value
   */
  double getMaxValue();

  /**
   * Actuate the actuator with the given value.
   *
   * @param value the value to actuate with
   */
  void act(double value);

  /**
   * Update the actuator's state.
   */
  void update();
}
