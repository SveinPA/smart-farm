package edu.ntnu.bidata.smg.group8.control.ui.controller.cardcontrollers;

import edu.ntnu.bidata.smg.group8.common.util.AppLogger;
import edu.ntnu.bidata.smg.group8.control.ui.view.ControlCard;
import edu.ntnu.bidata.smg.group8.control.logic.history.HistoricalDataStore;
import edu.ntnu.bidata.smg.group8.control.logic.history.Statistics;
import edu.ntnu.bidata.smg.group8.control.util.UiExecutors;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.Region;
import org.slf4j.Logger;



/**
 * Controller for the Fertilizer control card UI component. This controller
 * coordinates the interactions and data updates for the fertilizer system - which
 * makes this controller responsible for connecting the UI with backend operations.
 *
 * <p>This class handles fertilizer dosing operations, user interactions
 * and synchronization of status updates with the backend system.
 * It also provides real-time nitrogen level monitoring and warnings
 * for potentially harmful dose levels</p>
 *
 * @author Andrea Sandnes & Mona Amundsen
 * @version 28.10.2025
 */
public class FertilizerCardController {
  private static final Logger log = AppLogger.get(FertilizerCardController.class);

  private static final List<String> FERT_ZONE_CLASSES = List.of(
      "fertilizer-zone-very-low",
      "fertilizer-zone-low",
      "fertilizer-zone-optimal",
      "fertilizer-zone-high",
      "fertilizer-zone-very-high"
  );

  private static final double NITROGEN_MIN = 0.0;
  private static final double NITROGEN_MAX = 300.0;

  private final ControlCard card;
  private final Label statusLabel;
  private final Label minLabel;
  private final Label maxLabel;
  private final Label avgLabel;
  private final Button historyButton;
  private final ProgressBar nitrogenBar;

  private final List<String> historyEntries = new ArrayList<>();
  private double currentNitrogenLevel = 0.0;

  private HistoricalDataStore historicalDataStore;
  private ScheduledFuture<?> statsUpdateTask;

  /**
   * Creates a new FertilizerCardController with the specified UI components.
   *
   * @param card the main card container
   * @param statusLabel label displaying fertilizer system status
   * @param minLabel label showing minimum nitrogen level in the last 24 hours
   * @param maxLabel label showing maximum nitrogen level in the last 24 hours
   * @param avgLabel label showing average nitrogen level in the last 24 hours
   * @param historyButton button to access scheduling configuration
   * @param nitrogenBar progress bar showing current nitrogen level
   */
  public FertilizerCardController(ControlCard card, Label statusLabel,
                                  Label minLabel, Label maxLabel, Label avgLabel,
                                  Button historyButton, ProgressBar nitrogenBar) {
    this.card = card;
    this.statusLabel = statusLabel;
    this.minLabel = minLabel;
    this.maxLabel = maxLabel;
    this.avgLabel = avgLabel;
    this.historyButton = historyButton;
    this.nitrogenBar = nitrogenBar;

    log.debug("FertilizerCardController wired");
  }

  /**
   * Initializes event handlers and starts any listeners required by this controller.
   */
  public void start() {
    log.info("Starting FertilizerCardController");
    historyButton.setOnAction(e -> {
      log.info("History button clicked - showing fertilizer history dialog");
      showHistoryDialog();
    });
    log.debug("FertilizerCardController started successfully");
  }

  /**
   * Stops this controller and cleans up resources/listeners.
   */
  public void stop() {
    log.info("Stopping FertilizerCardController");

    // Cancel statistics update task
    if (statsUpdateTask != null && !statsUpdateTask.isCancelled()) {
      statsUpdateTask.cancel(false);
      log.debug("Statistics update task cancelled");
    }

    historyButton.setOnAction(null);
    log.debug("FertilizerCardController stopped successfully");
  }

  /**
   * Injects the historical data store and starts periodic statistics updates.
   *
   * <p>The controller will query the historical data store every 30 seconds
   * to update the 24-hour nitrogen statistics display.</p>

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

    log.debug("FertilizerCardController statistics updates started (every 30s)");
  }

  /**
   * Queries historical data store and updates statistics display.
   *
   * <p>This method retrieves the 24-hour nitrogen statistics from the
   * historical data store and updates the UI accordingly.</p>
   */
  private void updateStatsFromHistory() {
    if (historicalDataStore != null) {
      Statistics stats = historicalDataStore.getStatistics("fert");

      if (stats.isValid()) {
        updateStatistics(stats.min(), stats.max(), stats.average());
      } else {
        log.trace("No valid nitrogen statistics available yet");
      }
    }
  }

  /**
   * Updates the statistics display.
   *
   * <p>This method updates the minimum, maximum, and average
   * nitrogen level labels based on the provided statistics.</p>
   *
   * @param min minimum nitrogen level in last 24h
   * @param max maximum nitrogen level in last 24h
   * @param avg average nitrogen level in last 24h
   */
  public void updateStatistics(double min, double max, double avg) {
    log.debug("Updating 24h statistics - Min: {} ppm, Max: {} ppm, Avg: {} ppm",
            String.format("%.1f", min),
            String.format("%.1f", max),
            String.format("%.1f", avg));

    fx(() -> {
      minLabel.setText(String.format("Min: %.1f ppm", min));
      maxLabel.setText(String.format("Max: %.1f ppm", max));
      avgLabel.setText(String.format("Avg: %.1f ppm", avg));

      log.trace("Statistics labels updated successfully");
    });
  }

  /**
   * Updates the current nitrogen level from sensor reading.
   * This is called when sensor data arrives from the backend.
   *
   * <p>The method updates the UI components to reflect the new nitrogen level,
   * including the progress bar and status label. It also logs the update
   * and adds an entry to the fertilizer change history.</p>
   *
   * @param nitrogenPpm nitrogen level in parts per million
   */
  public void updateNitrogenLevel(double nitrogenPpm) {
    log.info("Nitrogen level updated: {} ppm", String.format("%.1f", nitrogenPpm));
    currentNitrogenLevel = nitrogenPpm;

    fx(() -> {
      double clamped = Math.max(NITROGEN_MIN, Math.min(NITROGEN_MAX, nitrogenPpm));
      card.setValueText(String.format("%.1f ppm", clamped));

      if (nitrogenBar != null) {
        double progress = clamped / 300.0;
        nitrogenBar.setProgress(progress);
      }

      String zoneClass;

      if (nitrogenPpm < 50) {
        statusLabel.setText("Status: \n Very Low - Deficiency Risk");
        zoneClass = "fertilizer-very-low";
      } else if (nitrogenPpm <= 100) {
        statusLabel.setText("Status: \n Low - Supplement Recommended");
        zoneClass = "fertilizer-low";
      } else if (nitrogenPpm <= 150) {
        statusLabel.setText("Status: \n Optimal Range");
        zoneClass = "fertilizer-optimal";
      } else if (nitrogenPpm <= 200) {
        statusLabel.setText("Status: \n High - Good for Heavy Feeders");
        zoneClass = "fertilizer-high";
      } else {
        statusLabel.setText("Status: \n Very High - Burn Risk");
        zoneClass = "fertilizer-very-high";
      }

      if (nitrogenBar != null) {
        ObservableList<String> classes = nitrogenBar.getStyleClass();
        classes.removeAll(FERT_ZONE_CLASSES);
        classes.add(zoneClass);
      }
    });
    addHistoryEntry("Sensor reading: " + String.format("%.1f ppm", nitrogenPpm));
  }

  /**
   * Ensures the given runnable executes on the JavaFX Application Thread.
   *
   * <p>If already on the FX thread, the runnable is executed immediately.
   * Otherwise, it is scheduled to run later on the FX thread.</p>
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
   * Adds an entry to the fertilizer change history.
   *
   * <p>This method prepends a timestamped message to the change history list,
   * which can later be displayed in a dialog.</p>
   *
   * @param message the message describing the change
   */
  private void addHistoryEntry(String message) {
    String timestamp = LocalDateTime.now()
            .truncatedTo(ChronoUnit.SECONDS)
            .toString();
    historyEntries.addFirst(timestamp + "- " + message);
  }

  /**
   * Displays a dialog showing the fertilizer change history.
   * This dialog lists all recorded changes made during the current session.
   *
   * <p>The dialog is modal and blocks interaction with the main UI
   * until closed.</p>
   */
  private void showHistoryDialog() {
    Dialog<Void> dialog = new Dialog<>();
    dialog.setTitle("Fertilizer History");
    dialog.setHeaderText("Changes made during this session:");

    dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

    ListView<String> listView = new ListView<>();
    listView.getItems().setAll(historyEntries);

    int visibleRows = Math.min(historyEntries.size(), 10);
    double rowHeight = 24;

    listView.setPrefHeight(visibleRows * rowHeight);
    listView.setMinHeight(Region.USE_PREF_SIZE);
    listView.setMaxHeight(Region.USE_PREF_SIZE);

    listView.setPrefWidth(500);
    listView.setPrefHeight(300);
    dialog.getDialogPane().setPrefWidth(520);
    dialog.getDialogPane().setPrefHeight(350);

    dialog.getDialogPane().setContent(listView);
    dialog.showAndWait();
  }
}
