package edu.ntnu.bidata.smg.group8.sensor.entity.actuators;

import edu.ntnu.bidata.smg.group8.common.actuator.AbstractActuator;

/**
 * Represents a valve actuator in the greenhouse environment.
 *
 * This class extends the {@link AbstractActuator} to provide
 * specific functionality for controlling a valve,
 * including methods to open, close, and check the valve state.
 *
 * A valve actuator has the following possibles:
 * <ul>
 *     <li>Opening range from 0% (closed) to 100% (fully open).</li>
 *     <li>Methods to check if the valve is open, fully open, or closed.</li>
 *</ul>
 *
 * @author Mona Amundsen
 * @version 25.10.25
 */
public class ValveActuator extends AbstractActuator {
    private static final String KEY = "valve";
    private static final String UNIT = "%";
    private static final double MIN_VALUE= 0.0; // Closed
    private static final double MAX_VALUE = 100.0; // Fully open

    /**
     * Constructor for the valve actuator.
     * Initialize the valve with opening range 0-100%.
     */
    public ValveActuator() {
        super(KEY, UNIT, MIN_VALUE, MAX_VALUE);
    }

    /**
     * Check if the valve is currently open (any flow)
     *
     * @return true if the valve is open, false if closed
     */
    public boolean isOpen() {
        return getCurrentValue() > 0;
    }

    /**
     * Check if the valve is fully open.
     *
     * @return true if valve is at 100% opening
     */
    public boolean isFullyOpen() {
        return getCurrentValue() >= MAX_VALUE;
    }

    /**
     * Check if the valve is closed.
     *
     * @return true if valve is at 0% opening
     */
    public boolean isClosed() {
        return getCurrentValue() <= MIN_VALUE;
    }

    /**
     * Fully open the valve (100% flow).
     *
     * <p>This method uses the {@link #act(double)} method
     * to set the valve to its maximum opening value.</p>
     */
    public void open() {
        act(MAX_VALUE);
    }

    /**
     * Close the valve (0% flow).
     *
     * <p>This method uses the {@link #act(double)} method
     * to set the valve to its minimum opening value.</p>
     */
    public void close() {
        act(MIN_VALUE);
    }

    /**
     * Set the valve to a specific opening percentage.
     * Useful for controlling flow rate.
     *
     * <p>This method calls the {@link #act(double)} method
     * to set the valve to the desired opening percentage.</p>
     *
     * @param percentage the desired opening percentage (0-100%)
     */
    public void setOpening(double percentage) {
        act(percentage);
    }
}
