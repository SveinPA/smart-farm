package edu.ntnu.bidata.smg.group8.control.logic.history;

import java.time.Duration;
import java.time.Instant;
import java.util.Deque;
import java.util.DoubleSummaryStatistics;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * Stores historical sensor reading with a sliding 24-hour window.
 * Thread safe implementation using concurrent data structures.
 * 
 * <p>Data older than 24 hours is lazily pruned when querying statistics.
 * Statistics (min/max/avg) are calculated on-demand using streams.
 * 
 * <p><strong>AI Usage:</strong> Developed with AI assistance (Claude Code) for designing
 * the concurrent data structure pattern (ConcurrentHashMap + ConcurrentLinkedDeque) to support
 * thread-safe sliding window operations, and for the lazy pruning strategy decision (on-demand
 * cleanup vs background thread trade-offs). Stream-based statistics calculation approach and
 * thread-safety during concurrent read/write/prune operations discussed with AI guidance.
 * All implementation and testing by Svein Antonsen.
 * 
 * @author Svein Antonsen
 * @since 1.0
 */
public class HistoricalDataStore {
  private static final Duration RETENTION_PERIOD = Duration.ofHours(24);

  // Map: sensorKey -> deque of timestamped values
  private final ConcurrentHashMap<String, Deque<TimestampedValue>> dataStore;

  /**
   * Creates a new empty historical data store.
   */
  public HistoricalDataStore() {
    this.dataStore = new ConcurrentHashMap<>();
  }

  /**
   * Adds a sensor reading to the store.
   * 
   * @param sensorKey The sensor identifier (e.g., "temperature", "humidity")
   * @param value The sensor reading value
   * @param timestamp When the reading was taken
   */
  public void addReading(String sensorKey, double value, Instant timestamp) {
    dataStore.computeIfAbsent(sensorKey, k -> new ConcurrentLinkedDeque<>())
        .addLast(new TimestampedValue(value, timestamp));
  }

  /**
   * Calculates statistics for a sensor over the last 24 hours.
   * Automatically prunes data older than 24 hours.
   * 
   * @param sensorKey The sensor identifier
   * @return Statistics (min/max/avg) or Statistics.empty() if no data available
   */
  public Statistics getStatistics(String sensorKey) {
    Deque<TimestampedValue> readings = dataStore.get(sensorKey);

    if(readings == null || readings.isEmpty()) {
      return Statistics.empty();
    }

    // Prune old data (lazy cleanup)
    pruneOldData(readings);

    // Calculate statistics using streams
    DoubleSummaryStatistics stats = readings.stream()
        .mapToDouble(TimestampedValue::value)
        .summaryStatistics();
    
    if (stats.getCount() == 0) {
      return Statistics.empty();
    }

    return new Statistics(stats.getMin(), stats.getMax(), stats.getAverage());
  }

  /**
   * Removes readings older than 24 hours from the deque
   * 
   * @param readings The deque to prune
   */
  private void pruneOldData(Deque<TimestampedValue> readings) {
    Instant cutoff = Instant.now().minus(RETENTION_PERIOD);

    // Remove from front of deque (oldest entries)
    while (!readings.isEmpty() && readings.peekFirst().timestamp().isBefore(cutoff)) {
      readings.pollFirst();
    }
  }

  /**
   * Gets the number of readings stored for a specific sensor.
   * Useful for testing and monitoring memory usage.
   * 
   * @param sensorKey The sensor identifier
   * @return Number of readings  (including old data not yet pruned)
   */
  public int getReadingCount(String sensorKey) {
    Deque<TimestampedValue> readings = dataStore.get(sensorKey);
    return readings == null ? 0 : readings.size();
  }

  /**
   * Clears all historical data.
   * Useful for testing.
   */
  public void clear() {
    dataStore.clear();
  }
}
