package edu.ntnu.bidata.smg.group8.control.ui.controller.cardcontrollers;

import edu.ntnu.bidata.smg.group8.common.util.AppLogger;
import edu.ntnu.bidata.smg.group8.control.logic.history.HistoricalDataStore;
import edu.ntnu.bidata.smg.group8.control.logic.history.Statistics;
import edu.ntnu.bidata.smg.group8.control.ui.view.ControlCard;
import edu.ntnu.bidata.smg.group8.control.util.UiExecutors;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import org.slf4j.Logger;

/**
 * Controller for the Light control card. This controller
 * coordinates the interactions and data updates for the ambient light monitoring system.
 *
 * <p>This class handles ambient light level updates, user interactions, and
 * synchronization of status updates with the backend system. It also provides real-time
 * light level monitoring and color-coded feedback based on predefined light
 * level thresholds.</p>
 *
 *
 * @author Andrea Sandnes & Mona Amundsen
 * @version 28.10.2025
 */
public class LightCardController {
  private static final Logger log = AppLogger.get(LightCardController.class);

  // Ambient range for progress bar
  private static final double MIN_LUX = 0.0;
  private static final double MAX_LUX = 80000.0;

  // Light level thresholds color coding
  private static final double L_DARK = 1000.0;
  private static final double L_LOW = 20000.0;
  private static final double L_MEDIUM = 60000.0;
  private static final double L_BRIGHT = 70000.0;

  private final ControlCard card;
  private final Label minLabel;
  private final Label maxLabel;
  private final Label avgLabel;
  private final ProgressBar ambientBar;
  private final Button historyButton;
  private final List<String> historyEntries = new ArrayList<>();
  private Label statusLabel = new Label();

  private String activeZoneClass;

  private HistoricalDataStore historicalDataStore;
  private ScheduledFuture<?> statsUpdateTask;

  /**
   * Creates a new LightCardController with the specified UI components.
   *
   * @param card the control card UI component
   * @param ambientBar the progress bar representing ambient light level
   * @param minLabel the label for minimum light level
   * @param maxLabel the label for maximum light level
   * @param avgLabel the label for average light level
   * @param historyButton the button to show light history
   * @param statusLabel the label for light status
   */
  public LightCardController(ControlCard card, ProgressBar ambientBar, Label minLabel,
                             Label maxLabel, Label avgLabel,
                             Button historyButton, Label statusLabel) {
    this.card = card;
    this.ambientBar = ambientBar;
    this.minLabel = minLabel;
    this.maxLabel = maxLabel;
    this.avgLabel = avgLabel;
    this.historyButton = historyButton;
    this.statusLabel = statusLabel;

    log.debug("LightCardController wired with range [{}, {}] lx", MIN_LUX, MAX_LUX);
  }

  /**
   * Initializes event handlers and starts any listeners required by this controller.
   */
  public void start() {
    log.info("Starting LightCardController");
    historyButton.setOnAction(event -> {
      showHistoryDialog();
      log.info("Light history button clicked - showing light history dialog");
    });
    log.debug("LightCardController started successfully");
  }

  /**
   * Stops this controller and cleans up resources/listeners.
   */
  public void stop() {
    log.info("Stopping LightCardController");

    // Cancel statistics update task
    if (statsUpdateTask != null && !statsUpdateTask.isCancelled()) {
      statsUpdateTask.cancel(false);
      log.debug("Statistics update task cancelled");
    }

    historyButton.setOnAction(null);
    log.debug("Temperature history button action cleared");
    log.debug("LightCardController stopped successfully");
  }

  /**
   * Injects the historical data store and starts periodic statistics updates.
   *
   * <p>The controller will query the historical data store every 30 seconds
   * to update the 24-hour light statistics display.</p>

   * @param historicalDataStore the data store for querying 24h statistics
   */
  public void setHistoricalDataStore(HistoricalDataStore historicalDataStore) {
    this.historicalDataStore = historicalDataStore;

    // Start periodic statistics updates (every 30 seconds)
    statsUpdateTask = UiExecutors.scheduleAtFixedRate(
      this::updateStatsFromHistory,
      0, // Initial delay
      30, // Period
      TimeUnit.SECONDS
    );

    log.debug("LightCardController statistics updates started (every 30s)");
  }

  /**
   * Queries historical data store and updates statistics display.
   *
   * <p>This method retrieves the 24-hour light statistics from the
   * historical data store and updates the UI accordingly.</p>
   */
  private void updateStatsFromHistory() {
    if (historicalDataStore != null) {
      Statistics stats = historicalDataStore.getStatistics("light");

      if (stats.isValid()) {
        updateStatistics(stats.min(), stats.max(), stats.average());
      } else {
        log.trace("No valid temperature statistics available yet");
      }
    }
  }

  /**
   * Updates the light status label based on the current light level.
   *
   * <p>The status is determined by predefined light level thresholds
   * and updates the status label accordingly.</p>
   *
   * @param lightLevel the current light level in lux
   */
  private void updateLightStatus(double lightLevel) {
    String status;
    if (lightLevel < L_DARK) {
      status = "Very Dark";
    } else if (lightLevel < L_LOW) {
      status = "Dim light";
    } else if (lightLevel < L_MEDIUM) {
      status = "Comfortable Light";
    } else if (lightLevel < L_BRIGHT) {
      status = "Sunny";
    } else {
      status = "Direct sunlight";
    }
    statusLabel.setText("Status: " + status);
    log.info("Light status updated to: {}", status);
  }

  /**
   * Updated the light/ambient display.
   *
   * <p>This method is called when new sensor data arrives from the backend.
   * It updates the UI components to reflect the new ambient light level,
   * including the progress bar and status label. It also logs the update
   * and adds an entry to the light change history.</p>
   *
   * @param lightLevel the current light level in lux
   */
  public void updateAmbientLight(double lightLevel) {
    log.info("Updating light: {} lx", String.format("%.1f", lightLevel));

    fx(() -> {
      double clamped = Math.max(MIN_LUX, Math.min(MAX_LUX, lightLevel));
      card.setValueText(String.format("%.1f lx", clamped));

      double progress = (lightLevel - MIN_LUX) / (MAX_LUX - MIN_LUX);
      progress = Math.max(0, Math.min(1, progress)); // Clamp to 0-1
      ambientBar.setProgress(progress);

      updateLightStatus(lightLevel);

      log.trace("Progress bar updated: {} ({} lx)",
              String.format("%.2f", progress),
              String.format("%.1f", lightLevel));

      // Update color based on light level
      applyLightLevelStyle(lightLevel);

      // Add to history
      String zoneText = statusLabel.getText();
      addHistoryEntry(clamped, zoneText);
    });
  }

  /**
   * Applies appropriate style class to the ambient bar based on light level.
   *
   * <p>This method changes the CSS class of the ambient bar to reflect
   * the current light level zone, providing visual feedback to the user.</p>
   *
   * @param lightLevel the current light level in lux
   */
  private void applyLightLevelStyle(double lightLevel) {
    String newClass;
    String zone;

    if (lightLevel < L_DARK) {
      newClass = "light-very-low";
      zone = "DARK";
    } else if (lightLevel < L_LOW) {
      newClass = "light-low";
      zone = "LOW";
    } else if (lightLevel < L_MEDIUM) {
      newClass = "light-moderate";
      zone = "MEDIUM";
    } else if (lightLevel < L_BRIGHT) {
      newClass = "light-high";
      zone = "BRIGHT";
    } else {
      newClass = "light-very-high";
      zone = "VERY BRIGHT";
    }

    if (!newClass.equals(activeZoneClass)) {
      if (activeZoneClass != null) {
        ambientBar.getStyleClass().remove(activeZoneClass);
      }
      ambientBar.getStyleClass().add(newClass);
      log.info("Light level zone changed: {} -> {} ({} lx)",
              activeZoneClass != null ? activeZoneClass : "none",
              zone,
              lightLevel);
      activeZoneClass = newClass;
    }
    updateLightStatus(lightLevel);
  }

  /**
   * Updates the statistics display.
   *
   * <p>This method updates the minimum, maximum, and average
   * light level labels based on the provided statistics.</p>
   *
   * @param min minimum light level in last 24h
   * @param max maximum light level in last 24h
   * @param avg average light level in last 24h
   */
  public void updateStatistics(double min, double max, double avg) {
    log.debug("Updating 24h statistics - Min: {} lx, Max: {} lx, Avg: {} lx",
            String.format("%.1f", min),
            String.format("%.1f", max),
            String.format("%.1f", avg));

    fx(() -> {
      minLabel.setText(String.format("Min: %.1f lx", min));
      maxLabel.setText(String.format("Max: %.1f lx", max));
      avgLabel.setText(String.format("Avg: %.1f lx", avg));

      log.trace("Statistics labels updated successfully");
    });
  }

  /**
   * Ensures the given runnable executes on the JavaFX Application Thread.
   *
   * <p>If already on the FX thread, runs immediately; otherwise,
   * schedules for later execution.</p>
   *
   *
   * @param r the runnable to execute on the FX thread
   */
  private static void fx(Runnable r) {
    if (Platform.isFxApplicationThread()) {
      r.run();
    } else {
      Platform.runLater(r);
    }
  }

  /**
   * Adds a new entry to the light history.
   *
   * <p>This method prepends a timestamped light level reading
   * to the history entries list.</p>
   *
   * @param lightLevel the current light level in lux
   * @param zoneText the status zone associated with the humidity value
   */
  private void addHistoryEntry(double lightLevel, String zoneText) {
    String time = LocalTime.now()
            .truncatedTo(ChronoUnit.SECONDS)
            .toString();
    String entry = time + " – " + String.format("%.1f lx", lightLevel);
    historyEntries.addFirst(entry);
  }

  /**
   * Displays the light history dialog.
   *
   * <p>The dialog shows a list of past light level readings with timestamps, where
   * the most recent entries appear at the top.</p>
   */
  private void showHistoryDialog() {
    Dialog<Void> dialog = new Dialog<>();
    dialog.setTitle("Ambient Light History");
    dialog.setHeaderText("Past Light Level Readings");

    dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

    ListView<String> listView = new ListView<>();
    listView.getItems().setAll(historyEntries);

    listView.setPrefSize(450, 300);
    dialog.getDialogPane().setPrefSize(470, 340);
    dialog.setResizable(true);

    dialog.getDialogPane().setContent(listView);
    dialog.showAndWait();
  }
}
