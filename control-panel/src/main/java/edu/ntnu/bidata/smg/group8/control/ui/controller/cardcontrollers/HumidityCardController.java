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
 * Controller for the Humidity control card. This controller is responsible for
 * coordinating the interactions and data updates for the humidity monitoring
 * system-which makes this controller responsible for connecting the UI with backend operations.
 *
 * <p>This class handles real-time humidity monitoring, user interactions,
 * and synchronization of status updates with the backend system.
 * It also provides warnings for potentially harmful humidity levels.</p>

 * @author Andrea Sandnes & Mona Amundsen
 * @version 28.10.2025
 */
public class HumidityCardController {
  private static final Logger log = AppLogger.get(HumidityCardController.class);

  private static final double HUMIDITY_VERY_DRY = 30.0;
  private static final double HUMIDITY_DRY = 40.0;
  private static final double HUMIDITY_OPTIMAL_HIGH = 60.0;
  private static final double HUMIDITY_SLIGHTLY_HUMID = 70.0;
  private static final double HUMIDITY_HUMID = 80.0;

  // Critical thresholds
  private static final double HUMIDITY_CRITICAL_LOW = 30.0;
  private static final double HUMIDITY_CRITICAL_HIGH = 80.0;

  private final ControlCard card;
  private final Label minLabel;
  private final Label maxLabel;
  private final Label avgLabel;
  private final ProgressBar humidityBar;
  private final Button historyButton;
  private final List<String> historyEntries = new ArrayList<>();
  private Label statusLabel = new Label();

  private String previousLevel = null;
  private String activeZoneClass = null;

  private HistoricalDataStore historicalDataStore;
  private ScheduledFuture<?> statsUpdateTask;

  /**
   * Creates a new HumidityCardController with the specified UI components.
   *
   * @param card the main card container
   * @param humidityBar progress bar visualizing humidity level
   * @param minLabel label displaying minimum humidity (24h)
   * @param maxLabel label displaying maximum humidity (24h)
   * @param avgLabel label displaying average humidity (24h)
   * @param historyButton  button to access historical data
   * @param statusLabel label displaying current humidity status
   */
  public HumidityCardController(ControlCard card, ProgressBar humidityBar,
                                Label minLabel, Label maxLabel, Label avgLabel,
                                Button historyButton, Label statusLabel) {
    this.card = card;
    this.humidityBar = humidityBar;
    this.minLabel = minLabel;
    this.maxLabel = maxLabel;
    this.avgLabel = avgLabel;
    this.historyButton = historyButton;
    this.statusLabel = statusLabel;

    log.debug("HumidityCardController wired with range [0%, 100%]");
  }

  /**
   * Initializes event handlers and starts any listeners required by this controller.
   */
  public void start() {
    log.info("Starting HumidityCardController");
    historyButton.setOnAction(e -> {
      showHistoryDialog();
      log.info("Humidity history button clicked - showing humidity history dialog");
    });
    log.debug("HumidityCardController started successfully");
  }

  /**
   * Stops this controller and cleans up resources/listeners.
   */
  public void stop() {
    log.info("Stopping HumidityCardController");

    // Cancel statistics update task
    if (statsUpdateTask != null && !statsUpdateTask.isCancelled()) {
      statsUpdateTask.cancel(false);
      log.debug("Statistics update task cancelled");
    }

    historyButton.setOnAction(null);
    log.debug("HumidityCardController stopped successfully");
  }

  /**
   * Injects the historical data store and starts periodic statistics updates.
   *
   * <p>The controller will query the historical data store every 30 seconds
   * to update the 24-hour humidity statistics display.</p>
   *
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

    log.debug("HumidityCardController statistics updates started (every 30s)");
  }

  /**
   * Queries historical data store and updates statistics display.
   *
   * <p>This method retrieves the 24-hour humidity statistics from the
   * historical data store and updates the UI accordingly.</p>
   */
  private void updateStatsFromHistory() {
    if (historicalDataStore != null) {
      Statistics stats = historicalDataStore.getStatistics("hum");

      if (stats.isValid()) {
        updateStatistics(stats.min(), stats.max(), stats.average());
      } else {
        log.trace("No valid temperature statistics available yet");
      }
    }
  }

  /**
   * Updates the humidity display.
   *
   * <p>This method is called when new humidity data arrives from the backend.
   * It updates the UI components to reflect the new humidity level, including the
   * progress bar and status label. It also logs the update
   * and checks for critical humidity levels.</p>
   *
   * @param humidity the current relative humidity (0-100%)
   */
  public void updateHumidity(double humidity) {
    log.debug("Updating humidity to: {}%", String.format("%.1f", humidity));

    fx(() -> {
      double clamped = Math.max(0, Math.min(100, humidity));
      card.setValueText(String.format("%.1f %% RH", clamped));

      double progress = humidity / 100.0;
      progress = Math.max(0, Math.min(1, progress));
      humidityBar.setProgress(progress);

      log.trace("Humidity progress bar updated: {} ({}%)",
              String.format("%.2f", progress),
              String.format("%.1f", humidity));

      // Update color based on humidity level
      updateHumidityZone(humidity);

      String zoneText = statusLabel.getText();
      addHistoryEntry(clamped, zoneText);

      checkCriticalLevels(humidity);
    });
  }

  /**
   * Updates the humidity bar color based on value.
   *
   * <p>Changes the styling of the humidity bar to reflect
   * the current humidity zone.</p>
   *
   * @param humidity the current humidity percentage
   */
  private void updateHumidityZone(double humidity) {
    String level;
    String zoneClass;

    if (humidity < HUMIDITY_VERY_DRY) {
      level = "VERY_DRY";
      zoneClass = "humidity-very-dry";
    } else if (humidity < HUMIDITY_DRY) {
      level = "DRY";
      zoneClass = "humidity-dry";
    } else if (humidity < HUMIDITY_OPTIMAL_HIGH) {
      level = "OPTIMAL";
      zoneClass = "humidity-optimal";
    } else if (humidity < HUMIDITY_SLIGHTLY_HUMID) {
      level = "SLIGHTLY_HUMID";
      zoneClass = "humidity-slightly-humid";
    } else if (humidity < HUMIDITY_HUMID) {
      level = "HUMID";
      zoneClass = "humidity-humid";
    } else {
      level = "VERY_HUMID";
      zoneClass = "humidity-very-humid";
    }

    // Log level changes
    if (!level.equals(previousLevel)) {
      log.info("Humidity level changed: {} -> {} ({}%)",
              previousLevel != null ? previousLevel : "UNKNOWN",
              level,
              String.format("%.1f", humidity));
      previousLevel = level;
    }

    // updates styling for dynamic coloring
    if (!zoneClass.equals(activeZoneClass)) {
      if (activeZoneClass != null) {
        humidityBar.getStyleClass().remove(activeZoneClass);
      }
      humidityBar.getStyleClass().add(zoneClass);
      activeZoneClass = zoneClass;
    }
  }

  /**
   * Checks for critical humidity levels and logs warnings.
   *
   * <p>Logs warnings if humidity is outside safe ranges,
   * indicating potential risks to plant health.</p>
   *
   * @param humidity the current humidity percentage
   */
  private void checkCriticalLevels(double humidity) {
    if (humidity < HUMIDITY_CRITICAL_LOW) {
      log.warn("CRITICAL: Humidity too low ({}%) - Risk of plant stress and wilting."
                      + " Consider misting or irrigation.",
              String.format("%.1f", humidity));
    } else if (humidity > HUMIDITY_CRITICAL_HIGH) {
      log.warn("CRITICAL: Humidity too high ({}%) - Risk of fungal diseases and mold. "
                      + "Increase ventilation.",
              String.format("%.1f", humidity));
    } else if (humidity < HUMIDITY_DRY) {
      log.info("NOTICE: Humidity low ({}%) - Monitor plant conditions",
              String.format("%.1f", humidity));
    } else if (humidity > HUMIDITY_SLIGHTLY_HUMID) {
      log.info("NOTICE: Humidity high ({}%) - Consider increasing ventilation",
              String.format("%.1f", humidity));
    }
  }

  /**
   * Updates the statistics display.
   *
   * <p>This method updates the minimum, maximum, and average humidity
   * labels based on the provided statistics. It also logs the updated
   * values and checks for critical levels and large fluctuations.</p>
   *
   * @param min minimum humidity in last 24h
   * @param max maximum humidity in last 24h
   * @param avg average humidity in last 24h
   */
  public void updateStatistics(double min, double max, double avg) {
    log.debug("Updating humidity statistics - Min: {}%, Max: {}%, Avg: {}%",
            String.format("%.1f", min),
            String.format("%.1f", max),
            String.format("%.1f", avg));

    fx(() -> {
      minLabel.setText(String.format("Min: %.0f%%", min));
      maxLabel.setText(String.format("Max: %.0f%%", max));
      avgLabel.setText(String.format("Avg: %.0f%%", avg));

      log.trace("Humidity statistics labels updated successfully");

      // Check 24h extremes
      if (min < HUMIDITY_CRITICAL_LOW || max > HUMIDITY_CRITICAL_HIGH) {
        log.warn("24h humidity range includes critical levels - Min: {}%, Max: {}%",
                String.format("%.1f", min),
                String.format("%.1f", max));
      }

      // Check for large fluctuations
      double range = max - min;
      if (range > 40.0) {
        log.info("Large humidity fluctuation in 24h period: {}% range (Min: {}%, Max: {}%)",
                String.format("%.1f", range),
                String.format("%.1f", min),
                String.format("%.1f", max));
      }
    });
  }

  /**
   * Ensures the given runnable executes on the JavaFX Application Thread.
   *
   * <p>If already on the FX thread, runs immediately; otherwise,
   * schedules for later execution.</p>
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
   * Adds a new entry to the humidity history.
   *
   * <p>The entry includes the timestamp, humidity value.</p>
   *
   * @param humidity the humidity value to record
   * @param zoneText the status zone associated with the humidity value
   */
  private void addHistoryEntry(double humidity, String zoneText) {
    String time = LocalTime.now()
            .truncatedTo(ChronoUnit.SECONDS)
            .toString();
    String entry = time + " – " + String.format("%.1f %% RH", humidity);
    historyEntries.addFirst(entry);
  }

  /**
   * Displays a dialog with the humidity history entries.
   *
   * <p>The dialog shows a list of recorded humidity readings along with their timestamps
   * and status zones. Users can close the dialog when done.</p>
   */
  private void showHistoryDialog() {
    Dialog<Void> dialog = new Dialog<>();
    dialog.setTitle("Humidity History");
    dialog.setHeaderText("Humidity Readings History");

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
