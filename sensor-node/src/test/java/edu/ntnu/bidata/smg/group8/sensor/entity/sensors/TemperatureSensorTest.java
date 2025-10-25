package edu.ntnu.bidata.smg.group8.sensor.entity.sensors;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * <h3>Unit Tests for TemperatureSensor</h3>
 *
 * <p>This class contains unit tests for the {@link TemperatureSensor} class,
 * ensuring that the sensor behaves as expected in terms of key, unit,
 * reading ranges, and gradual changes in readings.</p>
 *
 * <p> We test the following aspects:
 * <ul>
 * <li>Correct key and unit of the sensor</li>
 * <li>Readings remain within the expected range of 10°C to 35°C</li>
 * <li>Readings change gradually over time, simulating realistic temperature variations</li>
 * </ul>
 * </p>
 *
 * <p>These tests help verify the reliability and accuracy of the temperature sensor's
 * implementation.</p>
 *
 * @author Ida Soldal
 * @version 25.10.2025
 */
public class TemperatureSensorTest {

    /**
     * Test the key and unit of the temperature sensor.
     *
     * <p>This ensures that the sensor is correctly identified
     * and uses the appropriate unit of measurement.</p>
     */
    @Test
    void testKeyAndUnit() {
        TemperatureSensor sensor = new TemperatureSensor();
        assertEquals("temp", sensor.getKey());
        assertEquals("°C", sensor.getUnit());
    }

    /**
     * Test that readings stay within the expected range of 10°C to 35°C.
     *
     * <p>This ensures the sensor is functioning correctly and providing valid data.</p>
     *
     * @see TemperatureSensor#getReading()
     */
    @Test
    void testReadingsStayWithinRange() {
        TemperatureSensor sensor = new TemperatureSensor();
        for (int i = 0; i < 100; i++) {
            double value = sensor.getReading();
            assertTrue(value >= 10.0 && value <= 35.0,
                       "Temperature out of range: " + value);
        }
    }

    /**
     * Test that readings change gradually over time, reflecting the 0.7% drift factor.
     *
     * <p>This ensures that the sensor simulates realistic temperature variations.</p>
     *
     * @see TemperatureSensor#getReading()
     */
    @Test
    void testReadingsChangeGradually() {
        TemperatureSensor sensor = new TemperatureSensor();
        double prev = sensor.getReading();

        double maxDiff = 0.0; // maximum difference observed between consecutive readings
        boolean changed = false; // flag to ensure at least some change occurs

        for (int i = 0; i < 200; i++) { // 200 iterations to observe changes, better statistics
            double cur = sensor.getReading(); // get current reading
            double diff = Math.abs(cur - prev); // calculate difference from previous reading
            maxDiff = Math.max(maxDiff, diff); // update max difference if current is larger
            if (diff > 0.0001) changed = true; // ensure some variation in readings
            prev = cur; // update previous reading for next iteration
        }


        // Generous bound for ~0.7% drift of a 25°C range (~0.175°C per step)
        double maxAllowedStep = 0.6; // maximum allowed step size
        assertTrue(maxDiff <= maxAllowedStep, "Step too large: " + maxDiff); // check max step size
        assertTrue(changed, "Readings did not change"); // ensure readings changed
    }
}
