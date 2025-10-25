package edu.ntnu.bidata.smg.group8.sensor.entity.sensors;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * <h3>Unit Tests for HumiditySensor</h3>
 *
 * <p>This class contains unit tests for the {@link HumiditySensor} class,
 * verifying that humidity readings stay within valid limits, change gradually
 * according to the drift factor, and use correct metadata (key and unit).</p>
 * 
 * <p> We test the following aspects:
 * <ul>
 * <li>Correct key and unit of the sensor</li>
 * <li>Readings remain within the expected range of 30% to 90%</li>
 * <li>Readings change gradually over time, simulating realistic humidity variations</li>
 * </ul>
 * </p>
 *
 * @author Ida Soldal
 * @version 25.10.2025
 */
public class HumiditySensorTest {

    /**
     * Test that the humidity sensor reports the correct key and unit.
     *
     * <p>This ensures that the sensor is properly identified within the system
     * and reports humidity in percentage (%).</p>
     */
    @Test
    void testKeyAndUnit() {
        HumiditySensor sensor = new HumiditySensor();
        assertEquals("hum", sensor.getKey());
        assertEquals("%", sensor.getUnit());
    }

    /**
     * Test that humidity readings remain within the valid range of 30% to 90%.
     *
     * <p>This ensures that the sensor behaves realistically and never outputs
     * physically impossible values.</p>
     *
     * @see HumiditySensor#getReading()
     */
    @Test
    void testReadingsStayWithinRange() {
        HumiditySensor sensor = new HumiditySensor();
        for (int i = 0; i < 100; i++) {
            double value = sensor.getReading();
            assertTrue(value >= 30.0 && value <= 90.0,
                       "Humidity out of range: " + value);
        }
    }

    /**
     * Test that humidity readings change gradually, reflecting the 2% drift factor.
     *
     * <p>This ensures the sensor simulates slow, realistic changes
     * rather than erratic jumps.</p>
     *
     * @see HumiditySensor#getReading()
     */
    @Test
    void testReadingsChangeGradually() {
        HumiditySensor sensor = new HumiditySensor();
        double prev = sensor.getReading();

        double maxDiff = 0.0;
        boolean changed = false;

        for (int i = 0; i < 200; i++) {
            double cur = sensor.getReading();
            double diff = Math.abs(cur - prev);
            maxDiff = Math.max(maxDiff, diff);
            if (diff > 0.0001) changed = true; // ensure at least small variation
            prev = cur;
        }

        // For 2% drift on 60% range → about 1.2% per update (~1.2 absolute % change)
        double maxAllowedStep = 1.5;
        assertTrue(maxDiff <= maxAllowedStep, "Step too large: " + maxDiff);
        assertTrue(changed, "Readings did not change");
    }
}
