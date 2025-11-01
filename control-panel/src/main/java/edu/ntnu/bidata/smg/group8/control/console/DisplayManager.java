package edu.ntnu.bidata.smg.group8.control.console;

import edu.ntnu.bidata.smg.group8.common.util.AppLogger;
import edu.ntnu.bidata.smg.group8.control.logic.state.ActuatorReading;
import edu.ntnu.bidata.smg.group8.control.logic.state.SensorReading;
import edu.ntnu.bidata.smg.group8.control.logic.state.StateSnapshot;
import edu.ntnu.bidata.smg.group8.control.logic.state.StateStore;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;

/**
* This class handles periodic rendering of the current greenhouse system
* state to the console. It is read-only, which means it does not process
* user input.
*
* <p>The DisplayManager retrieves data from StateStore and displays
* sensor and actuator information in a structured table format. It
* runs on a background daemon thread using a ScheduledExecutorService,
* refreshing the console output every second.</p>
*
* <p>Example output:
* <pre>
* Smart Greenhouse - Console Control Panel (read-only view)
* Timestamp format: ISO_INSTANT
* --------------------------------------------------------------
* Sensors:
* NodeId       Type             Value          Timestamp
* node-1       temperature      24.5 °C        2025-11-01T14:35:22Z
* node-1       humidity         61.0 %         2025-11-01T14:35:22Z
* node-1       wind             2.8 m/s        2025-11-01T14:35:22Z
* node-1       fertilizer       120.0 ppm      2025-11-01T14:35:22Z
* node-1       ph               6.5            2025-11-01T14:35:22Z
* node-1       light            900 lux        2025-11-01T14:35:22Z
*
* Actuators:
* NodeId       Type             State          Timestamp
* node-1       fan              45             2025-11-01T14:35:10Z
* node-1       heater           22             2025-11-01T14:35:10Z
* node-1       valve            1              2025-11-01T14:35:10Z
* node-1       window           50             2025-11-01T14:35:10Z
* node-1       artificial_light 75             2025-11-01T14:35:10Z
* </pre>
* </p>
*
*
* @author Andrea Sandnes
* @version 01.11.2025
*/
public class DisplayManager {
  private static final Logger log = AppLogger.get(DisplayManager.class);

  private static final String CLEAR_HOME = "\u001b[H\u001b[2J";

  private final StateStore stateStore;

  private final ScheduledExecutorService scheduler =
      Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "console-display");
        t.setDaemon(true);
        return t;
      });

  private volatile boolean clearScreen = true;

  /**
  * Creates a new DisplayManager bound to the given StateStore.
  *
  * @param stateStore the data source providing current sensor
   *                   and actuator states
  */
  public DisplayManager(StateStore stateStore) {
    this.stateStore = stateStore;
  }

  /**
  * Enables or disables clearing the console before each render.
  *
  * @param clear true tor clear console, false to append output
  */
  public void setClearScreen(boolean clear) {
    this.clearScreen = clear;
  }

  /**
  * Starts the periodic console display updates.
  */
  public void start() {
    log.info("Starting DisplayManager");
    scheduler.scheduleAtFixedRate(this::renderSafe, 0, 1, TimeUnit.SECONDS);
  }

  /**
  * Stops the console display updates and terminates the scheduler thred.
  */
  public void stop() {
    log.info("Stopping DisplayManager");
    scheduler.shutdownNow();
  }

  /**
  * Renders the current state safely, catching any exceptions
  * to prevent scheduler thread from crashing.
  */
  private void renderSafe() {
    try {
      render();
    } catch (Exception e) {
      log.warn("Display render failed", e);
    }
  }

  /**
  * Retrieves the latest StateSnapshot and prints its contents
  * to the console. The output includes all sensor and actuator
  * readings in a tabular format, sorted by type and node ID.
  */
  private void render() {
    StateSnapshot snap = stateStore.snapshot();

    if (clearScreen) {
      System.out.print(CLEAR_HOME);
      System.out.flush();
    } else {
      System.out.println("\n--------------------------------------------------------------");
    }
    System.out.println("Smart Greenhouse - Console Control Panel (read-only view)");
    System.out.println("Timestamp format: ISO_INSTANT");
    System.out.println("--------------------------------------------------------------");

    // Sensors
    System.out.println("\nSensors:");
    System.out.printf("%-12s %-16s %-14s %-26s%n",
        "NodeId", "Type", "Value", "Timestamp");

    snap.sensors().stream().map(sr -> new Object[] {
        sr.nodeId(),
        canonicalType(sr.type()),
        sr.value(),
        normalizeUnit(canonicalType(sr.type()), sr.unit()),
        sr.ts()
        })
        .collect(java.util.stream.Collectors.toMap(
            o -> o[0] + ":" + o[1],
            o -> o,
            (a, b) -> ((java.time.Instant) a[4]).isAfter((java.time.Instant) b[4]) ? a : b))
            .values().stream()
            .sorted(Comparator
            .<Object[], String>comparing(o -> (String) o[1])
            .thenComparing(o -> (String) o[0]))
            .forEach(o -> System.out.printf("%-12s %-16s %-14s %-26s%n",
                 o[0],
                 o[1],
                 o[2] + (((String) o[3]).isBlank() ? "" : " " + o[3]),
                 DateTimeFormatter.ISO_INSTANT.format((java.time.Instant) o[4])
            ));

    // Actuators
    System.out.println("\nActuators:");
    System.out.printf("%-12s %-16s %-14s %-26s%n",
        "NodeId", "Type", "State", "Timestamp");
    snap.actuators().stream()
        .sorted(Comparator.comparing(ActuatorReading::type).thenComparing(ActuatorReading::nodeId))
        .forEach(ar -> System.out.printf("%-12s %-16s %-14s %-26s%n",
        ar.nodeId(),
        ar.type(),
        ar.state(),
        DateTimeFormatter.ISO_INSTANT.format(ar.ts())));
  }

  /**
  * Converts a shorthand or inconsistent sensor type string into
  * its canonical form.
  *
  * <p>For example:
  * <ul>
  *   <li> "temp" → "temperature"</li>
  *   <li> "hum" → "humidity"</li>
  *</ul>
  * This ensures that the system treats equivalent abbreviations as the same logical type.
  * </p>
  *
  * @param t the raw or shorthand sensor type
  * @return the canonicalized, lowercase sensor type name
  */
  private static String canonicalType(String t) {
    if (t == null) {
      return "";
    }
    return switch (t.toLowerCase()) {
      case "temp" -> "temperature";
      case "hum"  -> "humidity";
      default     -> t.toLowerCase();
    };
  }

  /**
  * Returns a standardized unit for a given sensor type.
  *
  * <p>This helps normalize data from heterogeneous sources. If the type is known,
  * a default unit is returned; otherwise, the provided unit is used as-is.
  * </p>
  *
  * @param type the canonicalized sensor type
  * @param unit the original unit string from the data source
  * @return a normalized unit string suitable for display or storage
  */
  private static String normalizeUnit(String type, String unit) {
    String lt = type == null ? "" : type.toLowerCase();
    return switch (lt) {
      case "temperature" -> "°C";
      case "humidity"    -> "%";
      case "wind"        -> "m/s";
      case "light"       -> "lux";
      case "fertilizer"  -> "ppm";
      case "ph"          -> "";
      default            -> unit == null ? "" : unit;
    };
  }
}