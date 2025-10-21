package edu.ntnu.bidata.smg.group8.common.actuator;

/**
 * Abstract class for actuators.
 * In order to reduce code duplication, an abstract class for actuators is created.
 * This class can be extended by all actuators to inherit common properties and methods.
 *
 * @author Group 8, MLTA
 * @version 21.10.25
 */
public abstract class AbstractActuator {
    private final int id;
    private  final String type;
    private boolean isActive;

    // Constructor
    public AbstractActuator(int id, String type) {
        this.id = id;
        this.type = type;
        this.isActive = false; // Default state is inactive
    }

    /**
     * Returns the id of the actuator.
     *
     * @return identifier of the actuator
     */
    public int getIdentifier() {
        return id;
    }

    /**
     * Returns the type of the actuator.
     *
     * @return type of the actuator
     */
    public String getType() {
        return type;
    }

    /**
     * Returns whether the actuator is active.
     *
     * @return true if the actuator is active, false otherwise
     */
    public boolean isActive() {
        return isActive;
    }

    /**
     * Activates the actuator.
     */
    public void activate() {
        if (!isActive) {
            this.isActive = true;
        }
    }

    /**
     * Deactivates the actuator.
     */
    public void deactivate() {
        if (isActive) {
            this.isActive = false;
        }
    }

    /**
     * For subclasses to implement further if needed.
     */
    public abstract void performAction();


}