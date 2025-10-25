package edu.ntnu.bidata.smg.group8.sensor.entity.sensors;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * <h3>Unit Tests for PhSensor</h3>
 *
 * <p>This class contains unit tests for the {@link PhSensor} class,
 * verifying that readings stay within realistic bounds for greenhouse
 * soil or nutrient solution pH and that changes occur gradually according
 * to the 0.3% drift factor.</p>
 *
 * <p>We test the following aspects:</p>
 * <ul>
 *     <li>Correct key and unit of the sensor</li>
 *     <li>Readings remain within the expected range of 4.5 to 8.5 pH</li>
 *     <li>Readings change gradually over time, simulating realistic pH stability</li>
 * </ul>
 *
 * @author Ida Soldal
 * @version 25.10.2025
 */
public class PhSensorTest {

    /**
     * Test that the pH sensor reports the correct key and unit.
     *
     * <p>This ensures the sensor is properly identified within the system
     * and uses the correct unit of measurement (pH).</p>
     */
    @Test
    void testKeyAndUnit() {
        PhSensor sensor = new PhSensor();
        assertEquals("ph", sensor.getKey());
        assertEquals("pH", sensor.getUnit());
    }

    /**
     * Test that pH readings remain within the valid range of 4.5 to 8.5.
     *
     * <p>This ensures that the sensor produces realistic acidity/alkalinity
     * values and never outputs impossible readings.</p>
     *
     * @see PhSensor#getReading()
     */
    @Test
    void testReadingsStayWithinRange() {
        PhSensor sensor = new PhSensor();
        for (int i = 0; i < 100; i++) {
            double value = sensor.getReading();
            assertTrue(value >= 4.5 && value <= 8.5,
                       "pH reading out of range: " + value);
        }
    }

    /**
     * Test that pH readings change gradually, reflecting the 0.3% drift factor.
     *
     * <p>This ensures that the sensor simulates slow, realistic changes in pH
     * over time rather than sudden jumps.</p>
     *
     * @see PhSensor#getReading()
     */
    @Test
    void testReadingsChangeGradually() {
        PhSensor sensor = new PhSensor();
        double prev = sensor.getReading();

        double maxDiff = 0.0;
        boolean changed = false;

        for (int i = 0; i < 200; i++) {
            double cur = sensor.getReading();
            double diff = Math.abs(cur - prev);
            maxDiff = Math.max(maxDiff, diff);
            if (diff > 0.0001) changed = true; // ensure at least slight variation
            prev = cur;
        }

        // For 0.3% drift on 4.0 pH range → ±0.012 per update
        double maxAllowedStep = 0.05; // safe upper bound
        assertTrue(maxDiff <= maxAllowedStep, "Step too large: " + maxDiff);
        assertTrue(changed, "Readings did not change");
    }
}
