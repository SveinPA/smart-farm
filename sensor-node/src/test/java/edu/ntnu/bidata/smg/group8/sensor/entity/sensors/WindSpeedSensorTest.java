package edu.ntnu.bidata.smg.group8.sensor.entity.sensors;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * <h3>Unit Tests for WindSpeedSensor</h3>
 *
 * <p>This class contains unit tests for the {@link WindSpeedSensor} class,
 * verifying that readings stay within realistic bounds for outdoor wind
 * conditions and that changes occur gradually according to the 4% drift factor.</p>
 *
 * <p>We test the following aspects:</p>
 * <ul>
 *     <li>Correct key and unit of the sensor</li>
 *     <li>Readings remain within the expected range of 0 to 25 m/s</li>
 *     <li>Readings change gradually over time, simulating realistic wind variation</li>
 * </ul>
 *
 * @author Ida Soldal
 * @version 25.10.2025
 */
public class WindSpeedSensorTest {

    /**
     * Test that the wind speed sensor reports the correct key and unit.
     *
     * <p>This ensures the sensor is properly identified within the system
     * and uses the correct unit of measurement (m/s).</p>
     */
    @Test
    void testKeyAndUnit() {
        WindSpeedSensor sensor = new WindSpeedSensor();
        assertEquals("wind", sensor.getKey());
        assertEquals("m/s", sensor.getUnit());
    }

    /**
     * Test that wind speed readings remain within the valid range of 0 to 25 m/s.
     *
     * <p>This ensures that the sensor produces realistic wind speed values
     * and never outputs physically impossible readings.</p>
     *
     * @see WindSpeedSensor#getReading()
     */
    @Test
    void testReadingsStayWithinRange() {
        WindSpeedSensor sensor = new WindSpeedSensor();
        for (int i = 0; i < 100; i++) {
            double value = sensor.getReading();
            assertTrue(value >= 0.0 && value <= 25.0,
                       "Wind speed out of range: " + value);
        }
    }

    /**
     * Test that wind speed readings change gradually, reflecting the 4% drift factor.
     *
     * <p>This ensures the sensor simulates realistic outdoor wind conditions,
     * with moderate variation between readings but no sudden large jumps.</p>
     *
     * @see WindSpeedSensor#getReading()
     */
    @Test
    void testReadingsChangeGradually() {
        WindSpeedSensor sensor = new WindSpeedSensor();
        double prev = sensor.getReading();

        double maxDiff = 0.0;
        boolean changed = false;

        for (int i = 0; i < 200; i++) {
            double cur = sensor.getReading();
            double diff = Math.abs(cur - prev);
            maxDiff = Math.max(maxDiff, diff);
            if (diff > 0.0001) changed = true; // ensure some variation
            prev = cur;
        }

        // For 4% drift on 25 m/s range → about ±1 m/s per update
        double maxAllowedStep = 1.5; // generous upper bound for wind fluctuation
        assertTrue(maxDiff <= maxAllowedStep, "Step too large: " + maxDiff);
        assertTrue(changed, "Readings did not change");
    }
}
