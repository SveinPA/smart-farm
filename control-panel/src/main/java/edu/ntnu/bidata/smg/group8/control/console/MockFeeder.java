package edu.ntnu.bidata.smg.group8.control.console;

import edu.ntnu.bidata.smg.group8.common.util.AppLogger;
import edu.ntnu.bidata.smg.group8.control.logic.state.StateStore;
import edu.ntnu.bidata.smg.group8.control.util.FlatJson;
import java.io.BufferedReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Locale;
import java.util.Map;
import org.slf4j.Logger;

/**
* <h3>Mock Feeder - Test Data Simulator</h3>
*
* <p>Simulates incoming data by reading mock and actuator messages from a file.
* The MockFeeder class is primarily intended for testing and demonstration
* purposes. It reads lines from a given text file, parses them as flat JSON
* key–value pairs, and updates the StateStore as if they were received
* from a live network connection.</p>
*
* <p><strong>AI Usage:</strong> Flexible JSON field mapping strategy (firstNonNull pattern
* for handling multiple key naming conventions) and robust timestamp parsing (epoch millis
* vs ISO format with graceful fallback) discussed with AI guidance. Case-insensitive key
* lookup approach and file reading best practices (BufferedReader with UTF-8) explored
* with AI assistance. All implementation by Andrea Sandnes.
*
* @author Andrea Sandnes
* @version 1.0
* @since 02.11.2025
* @see StateStore
* @see FlatJson
*/
public class MockFeeder implements Runnable {
  private static final Logger log = AppLogger.get(MockFeeder.class);

  private final StateStore stateStore;
  private final String filePath;
  private final long delayMs;

  /**
  * Creates a new mock data feeder.
  *
  * @param stateStore the StateStore instance that receives updates
  *                   for simulated sensors and actuators.
  * @param filePath path to the mock data file to read
  * @param delayMs optional delay (in milliseconds) between processing
  *                each line
  */
  public MockFeeder(StateStore stateStore, String filePath, long delayMs) {
    this.stateStore = stateStore;
    this.filePath = filePath;
    this.delayMs = delayMs;
  }

  /**
  * Continuously reads and processes mock data lines from the file.
  * For each line, the method attempts to parse the JSON string into a map of
  * key–value pairs, determine the message type, and apply the corresponding
  * state update to the StateStore
  */
  @Override
  public void run() {
    log.info("Starting MockFeeder file={} delayMs={}", filePath, delayMs);
    try (BufferedReader br = Files.newBufferedReader(
            Paths.get(filePath), StandardCharsets.UTF_8)) {
      String line;

      // Process each line in the file
      while ((line = br.readLine()) != null) {
        line = line.trim();

        // Skip empty lines and comments
        if (line.isEmpty() || line.startsWith("#")) {
          continue;
        }

        // Parse JSON into key-value map
        Map<String, String> m = FlatJson.parse(line);
        if (m.isEmpty()) {
          continue;
        }

        // Extract required fields
        String type = val(m, "type");
        String nodeId = val(m, "nodeId");
        if (type == null || nodeId == null) {
          continue;
        }
        Instant ts = parseTs(m.get("timestamp"));


        // Route message to appropriate handler based on type
        switch (type.toUpperCase(Locale.ROOT)) {
          case "SENSOR_DATA" -> {
            String key   = firstNonNull(m, "sensorKey", "sensorType", "key", "typeKey");
            String value = val(m, "value");
            String unit  = val(m, "unit");
            if (key != null && value != null) {
              stateStore.applySensor(nodeId, key, value, unit, ts);
            } else {
              log.warn("Skipping SENSOR_DATA (missing key/value): {}", line);
            }
          }

          case "ACTUATOR_STATE" -> {
            String actuator = firstNonNull(m, "actuator", "actuatorType", "key");
            String state    = val(m, "state");
            if (actuator != null && state != null) {
              stateStore.applyActuator(nodeId, actuator, state, ts);
            } else {
              log.warn("Skipping ACTUATOR_STATE (missing actuator/state): {}", line);
            }
          }
          default -> {
            log.debug("Unknown mock message type: {}", type);
          }
        }

        // Optional delay to simulate realistic data arrival rate
        if (delayMs > 0) {
          try {
            Thread.sleep(delayMs);
          } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            break;
          }
        }
      }
    } catch (Exception e) {
      log.error("MockFeeder failed", e);
    } finally {
      log.info("MockFeeder stopped");
    }
  }

  /**
  * Finds a value from a map by key, ignore case.
  *
  * @param m the key-value map
  * @param k the key to search for
  * @return the corresponding value, or null if not found
  */
  private static String val(Map<String, String> m, String k) {
    String v = m.get(k);
    if (v != null) {
      return v;
    }
    for (var e : m.entrySet()) {
      if (e.getKey().equalsIgnoreCase(k)) {
        return e.getValue();
      }
    }
    return null;
  }

  /**
  * Returns the first non-null and non-blank value
  * for the given list of keys.
  *
  * @param m the key-value map
  * @param keys possible key names to check
  * @return the first matching value, or null if none found
  */
  private static String firstNonNull(Map<String, String> m, String... keys) {
    for (String k : keys) {
      String v = val(m, k);
      if (v != null && !v.isBlank()) {
        return v;
      }
    }
    return null;
  }

  /**
  * Parses a timestamp string into an Instant.
  *
  * @param ts the timestamp string, may be null
  * @return a valid Instant, never null
  */
  private static Instant parseTs(String ts) {
    if (ts == null || ts.isBlank()) {
      return Instant.now();
    }
    try {
      long millis = Long.parseLong(ts);
      return Instant.ofEpochMilli(millis);
    } catch (NumberFormatException ignore) {
      try {
        return Instant.parse(ts);
      } catch (Exception ex) {
        return Instant.now();
      }
    }
  }
}

