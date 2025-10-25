package edu.ntnu.bidata.smg.group8.sensor.entity.actuators;

import edu.ntnu.bidata.smg.group8.common.actuator.AbstractActuator;

/**
 * Represents a fan actuator in the greenhouse environment.
 *
 * This class extends the {@link AbstractActuator} to provide
 * specific functionality for controlling a fan.
 *
 * <p>The fan actuator has the following features: </p>
 * <ul>
 *     <li>Speed range from 0% (off) to 100% (full speed).</li>
 *     <li>Methods to check if the fan is on, set speed,
 *     turn on full, and turn off.</li>
 * </ul>
 *
 * @author Mona Amundsen
 * @version 25.10.25
 */
public class FanActuator extends AbstractActuator {
    private static final String KEY = "fan";
    private static final String UNIT = "%";
    private static final double MIN_VALUE = 0.0;
    private static final double MAX_VALUE = 100.0;

    /**
     * Constructor for the fan actuator.
     * Initialize the fan with speed range 0-100%.
     */
    public FanActuator() {
        super(KEY, UNIT, MIN_VALUE, MAX_VALUE);
    }

    /**
     * Check if the fan is currently on (any speed above 0).
     *
     * @return true if the fan speed is greater than 0, false otherwise
     */
    public boolean isOn() {
        return getCurrentValue() > 0;
    }

    /**
     * Check if the fan is currently off (speed is 0).
     *
     * @return true if the fan speed is 0, false otherwise
     */
    public boolean isOff() {
        return getCurrentValue() <= MIN_VALUE;
    }

    /**
     * Check if the fan is at full speed (100%).
     *
     * @return true if the fan speed is 100%, false otherwise
     */
    public boolean isAtFullSpeed() {
        return getCurrentValue() >= MAX_VALUE;
    }

    /**
     * Turn the fan on to a specified speed.
     *
     * <p>This method sets the fan speed to the given percentage value,
     * ranging from 0% (off) to 100% (full speed). The method
     * calls the {@link #act(double)}  method
     * to perform the action of changing the fan speed.</p>
     *
     * @param speed The desired speed to set the fan (0-100%).
     */
    public void setSpeed(double speed) {
        act(speed);
    }

    /**
     * Turn the fan on to full speed (100%).
     *
     * <p>This method sets the fan speed to the maximum value of 100%.
     * It calls the {@link #act(double)} method to perform
     * the action of changing the fan speed.</p>
     */
    public void turnOnFull() {
        act(MAX_VALUE);
    }

    /**
     * Turn the fan on to a low speed (25%).
     * Useful for gentle air circulation.
     *
     * <p>This method sets the fan speed to 25%.
     * It calls the {@link #act(double)} method to perform
     * the action of changing the fan speed.</p>
     */
    public void turnOnLow() {
        act(25.0);
    }

    /**
     * Turn the fan on to a medium speed (50%).
     * Useful for moderate air circulation.
     *
     * <p>This method sets the fan speed to 50%.
     * It calls the {@link #act(double)} method to perform
     * the action of changing the fan speed.</p>
     */
    public void turnOnMedium() {
        act(50.0);
    }

    /**
     * Turn the fan on to a high speed (75%).
     * Useful for strong air circulation.
     *
     * <p>This method sets the fan speed to 75%.
     * It calls the {@link #act(double)} method to perform
     * the action of changing the fan speed.</p>
     */
    public void turnOnHigh() {
        act(75.0);
    }

    /**
     * Turn the fan off (set speed to 0%).
     *
     * <p>This method sets the fan speed to 0%, effectively turning it off.
     * It calls the {@link #act(double)} method to perform
     * the action of changing the fan speed.</p>
     */
    public void turnOff() {
        act(MIN_VALUE);
    }
}
