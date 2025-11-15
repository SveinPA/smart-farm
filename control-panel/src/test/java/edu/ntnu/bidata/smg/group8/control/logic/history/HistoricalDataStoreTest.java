package edu.ntnu.bidata.smg.group8.control.logic.history;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for HistoricalDataStore
 * 
 * @author Svein Antonsen
 * @since 15.11.2025
 */
class HistoricalDataStoreTest {
  
  private HistoricalDataStore store;

  @BeforeEach
  void setUp() {
    store = new HistoricalDataStore();
  }

  @Test
  void testAddReadingAndGetCount() {
    // Add readings
    store.addReading("temperature", 22.5, Instant.now());
    store.addReading("temperature", 23.0, Instant.now());
    store.addReading("temperature", 21.5, Instant.now());

    // Verify count
    assertEquals(3, store.getReadingCount("temperature"));
  }

  @Test
  void testGetStatisticsWithNoData() {
    // Query non-existent sensor
    Statistics stats = store.getStatistics("nonexistent");

    // Should return empty statistics
    assertFalse(stats.isValid());
    assertTrue(Double.isNaN(stats.min()));
    assertTrue(Double.isNaN(stats.max()));
    assertTrue(Double.isNaN(stats.average()));
  }

  @Test
  void testGetStatisticsWithSingleReading() {
    // Add single reading
    store.addReading("temperature", 25.0, Instant.now());

    // Get statistics
    Statistics stats = store.getStatistics("temperature");

    // All values should equal the single reading
    assertTrue(stats.isValid());
    assertEquals(25.0, stats.min(), 0.001);
    assertEquals(25.0, stats.max(), 0.001);
    assertEquals(25.0, stats.average(), 0.001);
  }

  @Test
  void testGetStatisticsWithMultipleReadings() {
    // Add readings with known values
    Instant now = Instant.now();
    store.addReading("temperature", 10.0, now);
    store.addReading("temperature", 20.0, now);
    store.addReading("temperature", 30.0, now);

    // Get statistics
    Statistics stats = store.getStatistics("temperature");

    // Verify calculations
    assertTrue(stats.isValid());
    assertEquals(10.0, stats.min(), 0.001);
    assertEquals(30.0, stats.max(), 0.001);
    assertEquals(20.0, stats.average(), 0.001);
  }

  @Test
  void testPruningOfOldData() {
    Instant now = Instant.now();
    Instant old = now.minus(Duration.ofHours(25)); // >24 hours ago

    // Add old readings (should be pruned)
    store.addReading("temperature", 5.0, old);
    store.addReading("temperature", 6.0, old);

   // Add recent readings (should be kept)
   store.addReading("temperature", 20, now);
   store.addReading("temperature", 30, now);

   //verify count before pruning (4 readings total)
   assertEquals(4, store.getReadingCount("temperature"));

   // Get statistics (triggers pruning)
   Statistics stats = store.getStatistics("temperature");

   // Should only include recent data
   assertTrue(stats.isValid());
   assertEquals(20.0, stats.min(), 0.001);
   assertEquals(30.0, stats.max(), 0.001);
   assertEquals(25.0, stats.average(), 0.001);
  }

  @Test
  void testMultipleSensorKeys() {
    Instant now = Instant.now();

    // Add temperature readings
    store.addReading("temperature", 20.0, now);
    store.addReading("temperature", 25.0, now);

    // Add humidity readings
    store.addReading("humidity", 60.0, now);
    store.addReading("humidity", 80.0, now);

    // Get statistics for temperature
    Statistics tempStats = store.getStatistics("temperature");
    assertEquals(20.0, tempStats.min(), 0.001);
    assertEquals(25.0, tempStats.max(), 0.001);
    assertEquals(22.5, tempStats.average(), 0.001);

    // Get statistics for humidity
    Statistics humidStats = store.getStatistics("humidity");
    assertEquals(60.0, humidStats.min(), 0.001);
    assertEquals(80.0, humidStats.max(), 0.001);
    assertEquals(70.0, humidStats.average(), 0.001);

    // Verify counts are independent
    assertEquals(2, store.getReadingCount("temperature"));
    assertEquals(2, store.getReadingCount("humidity"));
  }

  @Test
  void testClear() {
    // Add readings
    store.addReading("temperature", 22.5, Instant.now());
    store.addReading("humidity", 65.0, Instant.now());

    // Verify data exists
    assertEquals(1, store.getReadingCount("temperature"));
    assertEquals(1, store.getReadingCount("humidity"));

    // Clear all data
    store.clear();

    // Verify all data is removed
    assertEquals(0, store.getReadingCount("temperature"));
    assertEquals(0, store.getReadingCount("humidity"));

    // Statistics should return empty
    assertFalse(store.getStatistics("temperature").isValid());
    assertFalse(store.getStatistics("humidity").isValid());
  }

  @Test
  void testGetReadingCountForNonexistentKey() {
    // Query count for sensor that doesn't exist
    assertEquals(0, store.getReadingCount("nonexistent"));
  }

  @Test
  void testStatisticsAfterAllDataPruned() {
    Instant old = Instant.now().minus(Duration.ofHours(25));

    // Add only old readings
    store.addReading("temperature", 10.0, old);
    store.addReading("temperature", 20.0, old);

    // Get statistics (all data should be pruned)
    Statistics stats = store.getStatistics("temperature");

    // Should return empty statistics (no valid data within 24h)
    assertFalse(stats.isValid());
  }
}
