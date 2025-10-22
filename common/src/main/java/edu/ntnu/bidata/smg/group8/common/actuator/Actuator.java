package edu.ntnu.bidata.smg.group8.common.actuator;

/**
 * An interface for actuators.
 * This interface defines what all actuators should be able to do.
 *
 * @author Group 8, MLTA
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
     * Actuate the actuator with the given value.
     *
     * @param value the value to actuate with
     */
    void act(double value);
}
