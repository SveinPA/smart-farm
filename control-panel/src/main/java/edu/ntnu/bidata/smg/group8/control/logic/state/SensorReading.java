package edu.ntnu.bidata.smg.group8.control.logic.state;

import java.time.Instant;

/**
* Represents a sensor reading taken at a specific point in time.
*
* <p> A sensor measures physical conditions in the greenhouse, such as temperature,
* humidity, light level, or soil quality. This class stores the measured value
* along with its unit and the timestamp of the measurement.</p>
*
* <p>As a record, this class is immutable, making it safe to share between
* threads without additional synchronization.</p>
*
* @param nodeId the unique identifier of the node this sensor belongs to
* @param type the type or name of the sensor (e.g., "temperature", "humidity", "light")
* @param value the measured value as a string (e.g., "22.5", "65")
* @param unit the unit of measurement (e.g., "°C", "%", "lux")
* @param ts the timestamp when this reading was taken
*
* @author Andrea Sandnes
* @version 30.10.25
*/
public record SensorReading(String nodeId, String type, String value,
                            String unit, Instant ts) {
}
