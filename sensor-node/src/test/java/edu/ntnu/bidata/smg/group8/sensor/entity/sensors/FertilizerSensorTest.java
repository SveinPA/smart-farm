package edu.ntnu.bidata.smg.group8.sensor.entity.sensors;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * <h3>Unit Tests for FertilizerSensor</h3>
 *
 * <p>This class contains unit tests for the {@link FertilizerSensor} class,
 * verifying that readings stay within realistic bounds for nitrogen concentration
 * in soil or nutrient solution, and that readings change gradually according to
 * the 0.5% drift factor.</p>
 *
 * <p>We test the following aspects:</p>
 * <ul>
 *     <li>Correct key and unit of the sensor</li>
 *     <li>Readings remain within the expected range of 0 to 300 ppm</li>
 *     <li>Readings change gradually over time, simulating realistic nutrient stability</li>
 * </ul>
 *
 * @author Ida Soldal
 * @version 25.10.2025
 */
public class FertilizerSensorTest {

    /**
     * Test that the fertilizer sensor reports the correct key and unit.
     *
     * <p>This ensures the sensor is properly identified within the system
     * and uses the correct unit of measurement (ppm).</p>
     */
    @Test
    void testKeyAndUnit() {
        FertilizerSensor sensor = new FertilizerSensor();
        assertEquals("fertilizer", sensor.getKey());
        assertEquals("ppm", sensor.getUnit());
    }

    /**
     * Test that fertilizer readings remain within the valid range of 0 to 300 ppm.
     *
     * <p>This ensures that the sensor produces realistic nitrogen concentration
     * values and never outputs impossible readings.</p>
     *
     * @see FertilizerSensor#getReading()
     */
    @Test
    void testReadingsStayWithinRange() {
        FertilizerSensor sensor = new FertilizerSensor();
        for (int i = 0; i < 100; i++) {
            double value = sensor.getReading();
            assertTrue(value >= 0.0 && value <= 300.0,
                       "Fertilizer reading out of range: " + value);
        }
    }

    /**
     * Test that fertilizer readings change gradually, reflecting the 0.5% drift factor.
     *
     * <p>This ensures that the sensor simulates slow, realistic changes
     * in nutrient concentration over time, rather than sudden large jumps.</p>
     *
     * @see FertilizerSensor#getReading()
     */
    @Test
    void testReadingsChangeGradually() {
        FertilizerSensor sensor = new FertilizerSensor();
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

        // For 0.5% drift on 300 ppm range → about ±1.5 ppm per update
        double maxAllowedStep = 2.0; // generous upper bound for nutrient variation
        assertTrue(maxDiff <= maxAllowedStep, "Step too large: " + maxDiff);
        assertTrue(changed, "Readings did not change");
    }
}
