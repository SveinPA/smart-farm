package edu.ntnu.bidata.smg.group8.sensor.infra;

import edu.ntnu.bidata.smg.group8.common.sensor.Sensor;
import edu.ntnu.bidata.smg.group8.common.util.JsonBuilder;

/**
 * <h3>Sensor Data Packet</h3>
 *
 * <p>This utility class provides a simple, reusable way to create JSON-formatted
 * messages representing sensor readings for transmission to the broker.</p>
 *
 * <p>It uses the shared {@link JsonBuilder} utility to create a lightweight JSON string,
 * without relying on external libraries. Each message includes essential information such as:</p>
 * <ul>
 *   <li><b>type</b>: always {@code SENSOR_DATA}</li>
 *   <li><b>nodeId</b>: the unique ID of the sensor node</li>
 *   <li><b>sensorKey</b>: the sensor's key (e.g., "temp", "hum")</li>
 *  <li><b>value</b>: the sensor's current reading, formatted to two decimal places</li>
 *  <li><b>unit</b>: the unit of measurement (e.g., "°C", "%")</li>
 * <li><b>timestamp</b>: the time the reading was taken, in milliseconds.</li>
 * </ul>
 *
 * <p>This class cannot be instantiated and only contains static methods.</p>
 *
 * @author Ida Soldal
 * @version 28.10.2025
 */
public class SensorDataPacket {

    /**
     * <b>Private constructor</b> to prevent instantiation,
     * as this class only contains static methods.
     *
     * <p>This is a common design pattern for utility classes in Java.</p>
     */
    private SensorDataPacket() {
        // Private constructor to prevent instantiation
    }

    /**
     * Builds a JSON representation of a sensor data packet.
     *
     * @param nodeId The unique identifier of the sensor node.
     * @param sensor The sensor that produced the reading.
     * @param value  The sensor's most recent reading.
     * @return A JSON string representing the sensor data packet, ready for transmission.
     */
    public static String build(String nodeId, Sensor sensor, double value) {
        return JsonBuilder.build(
            "type", "SENSOR_DATA",
            "nodeId", nodeId,
            "sensorKey", sensor.getKey(),
            "value", String.format("%.2f", value),
            "unit", sensor.getUnit(),
            "timestamp", String.valueOf(System.currentTimeMillis()) // Current time in milliseconds
        );
    }
}
