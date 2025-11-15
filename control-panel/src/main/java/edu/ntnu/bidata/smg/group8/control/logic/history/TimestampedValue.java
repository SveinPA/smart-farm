package edu.ntnu.bidata.smg.group8.control.logic.history;

import java.time.Instant;

/**
 * Represents a single sensor reading at a specific point in time.
 * Immuatable record for storing historical sensor data.
 * 
 * @author Svein Antonsen
 * @since 15.11.2025
 * 
 * @param value The sensor reading value
 * @param timestamp When the reading was taken
 */
public record TimestampedValue(double value, Instant timestamp) {
}
