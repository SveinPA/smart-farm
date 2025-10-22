package edu.ntnu.bidata.smg.group8.common.actuator;

import static java.lang.Math.clamp;
import static java.lang.Math.min;

/**
 * Abstract class for actuators.
 *
 * <p>Provides shared fields and behaviors:
 * key/unit, min/max, current/target, enable flag,
 * clamping, and a rate-limited ramp towards the target.</p>
 *
 * @author Group 8, MLTA
 * @version 21.10.25
 */
public abstract class AbstractActuator implements Actuator{
    private final String key;
    private final String unit;
    private final double minValue;
    private final double maxValue;

    private boolean enabled = false;
    private double currentValue;
    private double targetValue;

    /**
     * Constructor for the abstract Actuator.
     */
    public AbstractActuator(String key, String unit,
                            double minValue, double maxValue, double initial) {
        this.key = key;
        this.unit = unit;
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.currentValue = clamp(initial, minValue, maxValue);
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
     * Set the actuator to have a new target value.
     *
     * @param value the new target value
     */
    @Override
    public void act(double value) {
        if (!enabled) {
            return; //If off do nothing
        }

        double rate = ratePerAct();
        double difference = targetValue - currentValue;

        if (Math.abs(difference) <= rate) {
            currentValue = targetValue;
        } else {
            currentValue += Math.signum(difference) * min(Math.abs(difference), rate);
        }
        currentValue = clamp(currentValue, minValue, maxValue);
    }

    /**
     * Sets the actuator as enabled.
     */
    public void enable() {
        this.enabled = true;
    }

    /**
     * Sets the actuator as disabled.
     */
    public void disable() {
        this.enabled = false;
    }

    /**
     * Gets the enabled status of the actuator.
     *
     * @return true if enabled, false otherwise
     */
    public boolean isEnabled() {
        return this.enabled;
    }

    /**
     * Returns the rate of change per actuation step.
     *
     * @return the rate of change per actuation step
     */
    protected double ratePerAct() {
        return Double.POSITIVE_INFINITY;
    }

    /**
     * Clamps a value within the actuator's min and max range.
     *
     * @param value the value to clamp
     * @return the clamped value
     */
    private static double clamp(double value, double min, double max) {
        if (min > max) {
            throw new IllegalArgumentException("Min cannot be greater than max.");
        }
        return Math.max(min, Math.min(max, value));
    }
}