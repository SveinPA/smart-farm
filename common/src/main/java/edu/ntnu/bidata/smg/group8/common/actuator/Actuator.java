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
     * Returns the id of the actuator.
     *
     * @return id of the actuator
     */
    int getId();

    /**
     * Returns the type of the actuator.
     *
     * @return type of the actuator
     */
    String getType();

    /**
     * Checks if the actuator is active.
     *
     * @return true if the actuator is active, false otherwise
     */
    boolean isActive();

    /**
     * Activates the actuator.
     */
    void activate();

    /**
     * Deactivates the actuator.
     */
    void deactivate();

    /**
     * Performs the action of the actuator.
     */
    void performAction();
}
