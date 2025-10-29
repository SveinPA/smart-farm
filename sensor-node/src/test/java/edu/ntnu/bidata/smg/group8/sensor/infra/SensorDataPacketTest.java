package edu.ntnu.bidata.smg.group8.sensor.infra;

import edu.ntnu.bidata.smg.group8.sensor.entity.sensors.TemperatureSensor;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * <h3>Unit Tests for SensorDataPacket</h3>
 *
 * <p>This class contains unit tests for the SensorDataPacket class, specifically
 * testing the build method that constructs JSON packets for sensor data.</p>
 *
 * <p>The tests cover both positive scenarios, ensuring correct JSON structure
 * and value formatting, as well as negative scenarios that validate input
 * handling and exception throwing for invalid parameters.</p>
 *
 * <p>We test the following cases:</p>
 * <ul>
 *  <li><b>Positive Tests:</b>
 *   <ul>
 *    <li>Valid JSON structure with all required fields</li>
 *   <li>Correct value formatting to two decimal places</li>
 *  <li>Reasonable timestamp generation</li>
 *  </ul>
 * </li>
 *
 * <li><b>Negative Tests:</b>
 *  <ul>
 *   <li>Null, blank, or empty node ID</li>
 *  <li>Null sensor object</li>
 *  <li>Non-finite values (NaN, positive infinity, negative infinity)</li>
 * </ul>
 * </li>
 *
 * @author Ida Soldal
 * @version 29.10.2025
 */
class SensorDataPacketTest {

    // -------------- POSITIVE TESTS --------------

    /**
     * Tests that build returns a JSON string containing all required fields.
     *
     * <p> The test verifies that the JSON string produced by the build method
     * contains the expected fields: type, nodeId, sensorKey, value, unit, and timestamp.</p>
     */
    @Test
    void testBuildContainsAllRequiredFields() {
        TemperatureSensor sensor = new TemperatureSensor();
        String json = SensorDataPacket.build("node-1", sensor, 23.456);

        System.out.println("Generated JSON: " + json);

        assertTrue(json.contains("\"type\":\"SENSOR_DATA\""));
        assertTrue(json.contains("\"nodeId\":\"node-1\""));
        assertTrue(json.contains("\"sensorKey\":\"temp\""));
        assertTrue(json.contains("\"value\":\"23.46\""));  // formatted to 2 decimals
        assertTrue(json.contains("\"unit\":\"°C\""));
        assertTrue(json.contains("\"timestamp\":"));
    }

    /**
     * Tests that value formatting rounds correctly to two decimal places.
     *
     * <p> The test checks that the value is correctly rounded and formatted
     * to two decimal places in the resulting JSON string.</p>
     */
    @Test
    void testValueFormattingRoundsCorrectly() {
        TemperatureSensor sensor = new TemperatureSensor();
        String json = SensorDataPacket.build("node-1", sensor, 19.999);

        assertTrue(json.contains("\"value\":\"20.00\""));  // rounds up
    }

    /**
     * Tests that the timestamp is within a reasonable range (current time).
     *
     * <p> The test ensures that the timestamp included in the JSON string
     * is within the range of the current system time before and after the build call.</p>
     */
    @Test
    void testTimestampIsReasonable() {
        TemperatureSensor sensor = new TemperatureSensor();
        long before = System.currentTimeMillis();
        String json = SensorDataPacket.build("node-1", sensor, 25.0);
        long after = System.currentTimeMillis();

        // Extract timestamp (simple string search for testing)
        String timestampStr = json.split("\"timestamp\":\"")[1].split("\"")[0];
        long timestamp = Long.parseLong(timestampStr);

        assertTrue(timestamp >= before && timestamp <= after);
    }

    // -------------- NEGATIVE TESTS --------------

    /**
     * Tests that build throws exception when node ID is null.
     * <p> The test verifies that an IllegalArgumentException is thrown
     * when a null nodeId is provided to the build method.</p>
     */
    @Test
    void testBuildWithNullNodeId() {
        TemperatureSensor sensor = new TemperatureSensor();
        
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> SensorDataPacket.build(null, sensor, 25.0)
        );
        
        assertEquals("Node ID cannot be null or blank", exception.getMessage());
    }

    /**
     * Tests that build throws exception when node ID is blank.
     *
     * <p> The test verifies that an IllegalArgumentException is thrown
     * when a blank nodeId is provided to the build method.</p>
     */
    @Test
    void testBuildWithBlankNodeId() {
        TemperatureSensor sensor = new TemperatureSensor();
        
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> SensorDataPacket.build("   ", sensor, 25.0)
        );
        
        assertEquals("Node ID cannot be null or blank", exception.getMessage());
    }

    /**
     * Tests that build throws exception when node ID is empty.
     *
     * <p> The test verifies that an IllegalArgumentException is thrown
     * when an empty nodeId is provided to the build method.</p>
     */
    @Test
    void testBuildWithEmptyNodeId() {
        TemperatureSensor sensor = new TemperatureSensor();
        
        assertThrows(IllegalArgumentException.class,
            () -> SensorDataPacket.build("", sensor, 25.0)
        );
    }

    /**
     * Tests that build throws exception when sensor is null.
     *
     * <p> The test verifies that an IllegalArgumentException is thrown
     * when a null sensor is provided to the build method.</p>
     */
    @Test
    void testBuildWithNullSensor() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> SensorDataPacket.build("node-1", null, 25.0)
        );
        
        assertEquals("Sensor cannot be null", exception.getMessage());
    }

    /**
     * Tests that build throws exception when value is NaN.
     *
     * <p> The test verifies that an IllegalArgumentException is thrown
     * when a NaN value is provided to the build method.</p>
     */
    @Test
    void testBuildWithNaNValue() {
        TemperatureSensor sensor = new TemperatureSensor();
        
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> SensorDataPacket.build("node-1", sensor, Double.NaN)
        );
        
        assertTrue(exception.getMessage().contains("finite"));
    }

    /**
     * Tests that build throws exception when value is positive infinity.
     *
     * <p> The test verifies that an IllegalArgumentException is thrown
     * when a positive infinity value is provided to the build method.</p>
     */
    @Test
    void testBuildWithPositiveInfinity() {
        TemperatureSensor sensor = new TemperatureSensor();
        
        assertThrows(IllegalArgumentException.class,
            () -> SensorDataPacket.build("node-1", sensor, Double.POSITIVE_INFINITY)
        );
    }

    /**
     * Tests that build throws exception when value is negative infinity.
     *
     * <p> The test verifies that an IllegalArgumentException is thrown
     * when a negative infinity value is provided to the build method.</p>
     */
    @Test
    void testBuildWithNegativeInfinity() {
        TemperatureSensor sensor = new TemperatureSensor();
        
        assertThrows(IllegalArgumentException.class,
            () -> SensorDataPacket.build("node-1", sensor, Double.NEGATIVE_INFINITY)
        );
    }

}