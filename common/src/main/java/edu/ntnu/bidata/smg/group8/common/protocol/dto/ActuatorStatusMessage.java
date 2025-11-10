package edu.ntnu.bidata.smg.group8.common.protocol.dto;

/**
 * Data Transfer Object for actuator status messages.
 *
 * <p>Reports the current state of an actuator (ON/OFF, OPEN/CLOSED, etc.)
 * from a sensor node to the broker/control panel.</p>
 *
 * <p><b>Example JSON:</b></p>
 * <pre>
 * {
 *   "type": "ACTUATOR_STATUS",
 *   "nodeId": "greenhouse-1",
 *   "actuatorKey": "heater",
 *   "status": "ON",
 *   "value": "1.00",
 *   "timestampMillis
 ": "1699876543210"
 * }
 * </pre>
 *
 * <p>We make the class a <b>record</b> to get immutability, equals, hashCode and toString for free.
 * This way, we don't have to write tons of code just to hold data.</p>
 *
 * @param nodeId      The unique identifier of the sensor node
 * @param actuatorKey The actuator identifier (e.g., "heater", "fan")
 * @param status      Human-readable status (e.g., "ON", "OFF", "OPEN", "CLOSED")
 * @param value       Numeric value (0.0 = off/closed, 1.0 = on/open)
 * @param timestampMillis Unix timestamp in milliseconds
 *
 * @author Ida Soldal
 * @version 10.11.2025
 */
public record ActuatorStatusMessage(
    String nodeId,
    String actuatorKey,
    String status,
    double value,
    long timestampMillis
) {}