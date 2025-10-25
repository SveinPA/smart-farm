package edu.ntnu.bidata.smg.group8.sensor.entity.sensors;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * <h3>Unit Tests for LightSensor</h3>
 *
 * <p>This class contains unit tests for the {@link LightSensor} class,
 * verifying that readings stay within realistic bounds for greenhouse lighting
 * and that changes occur gradually according to the drift factor.</p>
 * 
 * <p> We test the following aspects:
 * <ul>
 * <li>Correct key and unit of the sensor</li>
 * <li>Readings remain within the expected range of 0 lx to 80 000 lx</li>
 * <li>Readings change gradually over time, simulating realistic light variations</li>
 * </ul>
 * </p>
 *
 * @author Ida Soldal
 * @version 25.10.2025
 */
public class LightSensorTest {

    /**
     * Test that the light sensor reports the correct key and unit.
     *
     * <p>This ensures the sensor is properly identified within the system
     * and uses the correct unit of measurement (lux).</p>
     */
    @Test
    void testKeyAndUnit() {
        LightSensor sensor = new LightSensor();
        assertEquals("light", sensor.getKey());
        assertEquals("lx", sensor.getUnit());
    }

    /**
     * Test that light readings remain within the valid range of 0 lx to 80 000 lx.
     *
     * <p>This ensures that the sensor produces realistic light intensity values
     * and never outputs physically impossible readings.</p>
     *
     * @see LightSensor#getReading()
     */
    @Test
    void testReadingsStayWithinRange() {
        LightSensor sensor = new LightSensor();
        for (int i = 0; i < 100; i++) {
            double value = sensor.getReading();
            assertTrue(value >= 0.0 && value <= 80000.0,
                       "Light reading out of range: " + value);
        }
    }

    /**
     * Test that light readings change gradually, reflecting the 2.5% drift factor.
     *
     * <p>This ensures the sensor simulates realistic fluctuations
     * (e.g., clouds or shading) without abrupt jumps in brightness.</p>
     *
     * @see LightSensor#getReading()
     */
    @Test
    void testReadingsChangeGradually() {
        LightSensor sensor = new LightSensor();
        double prev = sensor.getReading();

        double maxDiff = 0.0;
        boolean changed = false;

        for (int i = 0; i < 200; i++) {
            double cur = sensor.getReading();
            double diff = Math.abs(cur - prev);
            maxDiff = Math.max(maxDiff, diff);
            if (diff > 0.0001) changed = true; // ensure some variation occurs
            prev = cur;
        }

        // For 2.5% drift on 80 000 lx range → ~±1000 lx per update
        double maxAllowedStep = 1500.0; // generous safety bound
        assertTrue(maxDiff <= maxAllowedStep, "Step too large: " + maxDiff);
        assertTrue(changed, "Readings did not change");
    }
}
