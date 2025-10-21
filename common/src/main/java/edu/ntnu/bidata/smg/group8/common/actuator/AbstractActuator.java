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

    /**
     * Constructor for AbstractActuator.
     *
     * @param id the identifier of the actuator
     * @param type the type of the actuator
     */
    public AbstractActuator(int id, String type) {
        validateInput(id, type);
        this.id = id;
        this.type = type;
        this.isActive = false; // Default state is inactive
    }

    /**
     * Validates the input parameters for the actuator.
     * This method is called in the constructor of subclasses, in order to
     * ensure that the input parameters are valid, when creating an actuator.
     *
     * @param id the identifier of the actuator
     * @param type the type of the actuator
     *
     * @throws IllegalArgumentException if the id is negative or type is null/empty
     * @throws IllegalArgumentException if the type is null or empty
     */
    private void validateInput(int id, String type) {
        if (id < 0) {
            throw new IllegalArgumentException("Actuator ID cannot be negative.");
        }
        if (type == null || type.isEmpty()) {
            throw new IllegalArgumentException("Actuator type cannot be null or empty.");
        }
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
     *
     * @throws IllegalStateException if the actuator is already active
     */
    public void activate() {
        if (isActive) {
            throw new IllegalStateException("Actuator is already active.");
        }
        this.isActive = true;
    }

    /**
     * Deactivates the actuator.
     *
     * @throws IllegalStateException if the actuator is already inactive
     */
    public void deactivate() {
        if (!isActive) {
            throw new IllegalStateException("Actuator is already inactive.");
        }
        this.isActive = false;
    }

    /**
     * For subclasses to implement further if needed.
     */
    public abstract void performAction();


}