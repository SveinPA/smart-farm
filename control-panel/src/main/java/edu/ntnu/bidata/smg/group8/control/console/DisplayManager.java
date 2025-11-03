package edu.ntnu.bidata.smg.group8.control.console;

import edu.ntnu.bidata.smg.group8.common.util.AppLogger;
import edu.ntnu.bidata.smg.group8.control.logic.state.ActuatorReading;
import edu.ntnu.bidata.smg.group8.control.logic.state.SensorReading;
import edu.ntnu.bidata.smg.group8.control.logic.state.StateSnapshot;
import edu.ntnu.bidata.smg.group8.control.logic.state.StateStore;
import java.time.Instant;
import java.time.ZoneId;
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

  private static final DateTimeFormatter TIME_FORMATTER =
          DateTimeFormatter.ofPattern("HH:mm:ss").withZone(ZoneId.systemDefault());

  private static final DateTimeFormatter TIMESTAMP_FORMATTER =
          DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault());

  private final StateStore stateStore;
  private final ScheduledExecutorService scheduler;

  private volatile boolean clearScreen = true;
  private volatile boolean paused = false;
  private volatile Instant lastUpdate = Instant.EPOCH;

  /**
  * Creates a new DisplayManager bound to the given StateStore.
  *
  * @param stateStore the data source providing current sensor
   *                   and actuator states
  */
  public DisplayManager(StateStore stateStore) {
    this.stateStore = stateStore;
    this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
      Thread t = new Thread(r, "console-display");
      t.setDaemon(true);
      return t;
    });
  }

  /**
  * Starts the periodic console display updates.
  */
  public void start() {
    log.info("Starting DisplayManager");
    scheduler.scheduleAtFixedRate(this::refresh, 0, 1, TimeUnit.SECONDS);
  }

  /**
  * Stops the display updates and shuts down the background thread.
  */
  public void stop() {
    log.info("Stopping DisplayManager");
    scheduler.shutdown();
    try {
      if (!scheduler.awaitTermination(2, TimeUnit.SECONDS)) {
        scheduler.shutdownNow();
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      scheduler.shutdownNow();
    }
  }

  /**
  * Pauses the rendering process temporarily.
  * This method sets the internal state to paused, which prevents
  * any ongoing rendering updates or visual refreshes from occurring while
  * the user is typing or performing another input sensitive action.
  */
  public void pause() {
    this.paused = true;
  }

  /**
  * Resumes the rendering process after it has been paused.
  */
  public void resume() {
    this.paused = false;
  }

  /**
  * Enables or disables clearing the console before each render.
  *
  * @param clear true to clear console, false to append output
  */
  public void setClearScreen(boolean clear) {
    this.clearScreen = clear;
  }

  /**
  * Refreshes the console display with the current state snapshot.
  * This method is called periodically by the scheduler.
  */
  public void refresh() {
    if (paused) {
      return;
    }

    StateSnapshot snap = stateStore.snapshot();
    lastUpdate = Instant.now();

    if (clearScreen) {
      clearScreen();
    }

    printHeader();
    printSensors(snap);
    printActuators(snap);
    printFooter();
  }

  /**
  * Prints the display header with timestamp information.
  */
  private void printHeader() {
    System.out.println("--------------------------------------------------------------");
    System.out.println("Smart Greenhouse - Console Control Panel (live view)");
    System.out.printf("Last updated: %s%n", TIMESTAMP_FORMATTER.format(lastUpdate));
    System.out.println("--------------------------------------------------------------");
  }

  /**
  * Prints the sensor reading table.
  *
  * @param snap the current StateSnapshot containing all available
  *             SensorReading objects to display
  */
  private void printSensors(StateSnapshot snap) {
    System.out.println("\nSensors:");
    System.out.printf("%-12s %-16s %-18s %-12s%n",
            "NodeId", "Type", "Value", "Time");

    snap.sensors().stream()
            .sorted(Comparator.comparing(SensorReading::nodeId)
                    .thenComparing(SensorReading::type))
            .forEach(sr -> System.out.printf("%-12s %-16s %-18s %-12s%n",
                    sr.nodeId(),
                    expandSensorType(sr.type()),
                    sr.value() + " " + sr.unit(),
                    TIME_FORMATTER.format(sr.ts())));
  }

  /**
  * Prints the actuator states table.
  *
  * @param snap the current StateSnapshot containing all available
  *             Actuator state objects to display
  */
  private void printActuators(StateSnapshot snap) {
    System.out.println("\nActuators:");
    System.out.printf("%-12s %-16s %-18s %-12s%n",
            "NodeId", "Type", "State", "Time");

    var actuators = snap.actuators();
    if (actuators.isEmpty()) {
      System.out.println("  (no actuator states yet)");
    } else {
      actuators.stream()
            .sorted(Comparator.comparing(ActuatorReading::nodeId)
                    .thenComparing(ActuatorReading::type))
            .forEach(ar -> System.out.printf("%-12s %-16s %-18s %-12s%n",
                    ar.nodeId(),
                    ar.type(),
                    ar.state(),
                    TIME_FORMATTER.format(ar.ts())));
    }
  }

  /**
  * Prints the display footer with helpful hints.
  */
  private void printFooter() {
    System.out.println("\n--------------------------------------------------------------");
    System.out.println("Press Enter to return to INPUT mode");
    System.out.println("--------------------------------------------------------------");
  }

  /**
  * Clears the console screen (platform-independent fallback).
  */
  private void clearScreen() {
    try {
      if (System.getProperty("os.name").toLowerCase().contains("win")) {
        System.out.print("\033[H\033[2J");
        System.out.flush();
      } else {
        System.out.print("\033[H\033[2J");
        System.out.flush();
      }
    } catch (Exception e) {

      for (int i = 0; i < 50; i++) {
        System.out.println();
      }
    }
  }

  /**
  * Expands abbreviated sensor type names to their full form.
  *
  * @param type the sensor type
  * @return the expanded type name
  */
  private String expandSensorType(String type) {
    if (type == null) {
      return "";
    }
    return switch (type.toLowerCase()) {
      case "temp" -> "temperature";
      case "hum" -> "humidity";
      default -> type;
    };
  }
}