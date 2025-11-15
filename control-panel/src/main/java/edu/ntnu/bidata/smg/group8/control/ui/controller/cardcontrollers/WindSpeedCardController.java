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
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.Region;
import org.slf4j.Logger;



/**
* Controller for the Wind Speed control card.
* This controller manages the wind speed display card, handling real-time updates
* of wind speed measurements, gust information and statistical data. It applies
* visual styling to provide intuitive feedback about current wind conditions

* @author Andrea Sandnes & Mona Amundsen
* @version 28.10.2025
*/
public class WindSpeedCardController {
  private static final Logger log = AppLogger.get(WindSpeedCardController.class);

  private static final double MIN_WIND = 0.0;
  private static final double MAX_WIND = 30.0;

  private final ControlCard card;
  private Label statusLabel;
  private Label minLabel;
  private Label maxLabel;
  private Label avgLabel;
  private Label gustLabel;
  private ProgressBar windBar;
  private final List<String> changeHistory = new ArrayList<>();
  private final Button historyButton;

  private EventHandler<ActionEvent> historyButtonHandler;

  private HistoricalDataStore historicalDataStore;
  private ScheduledFuture<?> statsUpdateTask;

  /**
   * Creates a new WindSpeedCardController with the specified UI components.
   * This constructor wires together all the UI elements that the controller
   * will manage. The components are typically created by the WindSpeedCardBuilder
   * and passed to this controller for lifecycle management.
   *
   * @param card the main control card container
   * @param statusLabel label displaying the wind status text (e.g, "Calm")
   * @param gustLabel label displaying the maximum gust speed
   * @param windBar progress bar visualizing wind speed intensity
   * @param minLabel label displaying the 24h minimum wind speed
   * @param maxLabel label displaying the 24h maximum wind speed
   * @param avgLabel label displaying the 24h average wind speed
   * @param historyButton button to access wind speed history
   */
  public WindSpeedCardController(ControlCard card, Label statusLabel,
                                 Label gustLabel, ProgressBar windBar, Label minLabel,
                                 Label maxLabel, Label avgLabel, Button historyButton) {
    this.card = card;
    this.statusLabel = statusLabel;
    this.gustLabel = gustLabel;
    this.windBar = windBar;
    this.minLabel = minLabel;
    this.maxLabel = maxLabel;
    this.avgLabel = avgLabel;
    this.historyButton = historyButton;
    log.debug("WindSpeedCardController wired");
  }

  /**
  * Initializes event handlers and starts any listeners required by this controller.
  */
  public void start() {
    log.info("Starting WindSpeedCardController");
    historyButtonHandler = e -> {
      log.info("History button clicked - showing wind change history");
      showHistoryDialog();
    };
    historyButton.setOnAction(historyButtonHandler);
  }

  /**
  * Stops this controller and cleans up resources/listeners.
  */
  public void stop() {
    log.info("Stopping WindSpeedCardController");

    // Cancel statistics update task
    if (statsUpdateTask != null && !statsUpdateTask.isCancelled()) {
      statsUpdateTask.cancel(false);
      log.debug("Statistics update task cancelled");
    }

    if (historyButtonHandler != null) {
      historyButton.setOnAction(null);
      historyButtonHandler = null;
    }
    log.debug("WindSpeedCardController stopped successfully");
  }

  /**
   * Injects the historical data store and starts periodic statistics updates.
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

    log.debug("WindSpeedCardController statistics updates started (every 30s)");
  }

  /**
   * Queries historical data store and updates statistics display
   */
  private void updateStatsFromHistory() {
    if (historicalDataStore != null) {
      Statistics stats = historicalDataStore.getStatistics("wind");

      if (stats.isValid()) {
        updateStatistics(stats.min(), stats.max(), stats.average());
      } else {
        log.trace("No valid temperature statistics available yet");
      }
    }
  }

  /**
  * Updates the wind speed display and status.
  *
  * @param speed the current wind speed in m/s
  */
  public void updateWindSpeed(double speed) {
    log.info("Wind speed updated: {} m/s", String.format("%.1f", speed));

    fx(() -> {
      card.setValueText(String.format("%.1f m/s", speed));

      double progress = (speed - MIN_WIND) / (MAX_WIND - MIN_WIND);
      windBar.setProgress(Math.max(0, Math.min(1, progress)));

      updateWindStatus(speed);
    });
    addHistoryEntry(String.format("Wind speed updated to %.1f m/s", speed));
    log.info("Wind speed display and status updated");
  }

  /**
  * Updates the wind gust display.
  *
  * @param gust the maximum gust speed in m/s
  */
  public void updateGust(double gust) {
    log.info("Wind gust updated: {} m/s", String.format("%.1f", gust));

    fx(() -> {
      gustLabel.setText(String.format("Gust: %.1f m/s", gust));
    });
  }

  /**
  * Updates the 24-hour statistical summary display.
  * This method updates the minimum, maximum, and average wind speed labels
  * with values calculated over the last 24 hours. These statistics provide
  * historical context for current readings and help operators assess overall
  * wind patterns and trends.
  *
  * @param min minimum wind speed in last 24h
  * @param max maximum wind speed in last 24h
  * @param avg average wind speed in last 24h
  */
  public void updateStatistics(double min, double max, double avg) {
    log.debug("Wind statistics updated: min={}, max={}, avg={}", String.format("%.1f", min),
            String.format("%.1f", max), String.format("%.1f", avg));

    fx(() -> {
      minLabel.setText(String.format("Min: %.1f m/s", min));
      maxLabel.setText(String.format("Max: %.1f m/s", max));
      avgLabel.setText(String.format("Avg: %.1f m/s", avg));
    });
  }

  /**
  * Updates the wind status label and bar color based on wind speed.
  *
  * @param speed the current wind speed in m/s
  */
  private void updateWindStatus(double speed) {
    String status;
    String color;

    if (speed < 0.5) {
      status = "Calm";
      color = "wind-speed-calm";
    } else if (speed < 3.3) {
      status = "Light Air";
      color = "wind-speed-light";
    } else if (speed < 5.5) {
      status = "Light Breeze";
      color = "wind-speed-breeze";
    } else if (speed < 8.0) {
      status = "Gentle Breeze";
      color = "wind-speed-breeze";
    } else if (speed < 10.8) {
      status = "Moderate Breeze";
      color = "wind-speed-moderate";
    } else if (speed < 13.9) {
      status = "Fresh Breeze";
      color = "wind-speed-moderate";
    } else if (speed < 17.2) {
      status = "Strong Breeze (Caution)";
      color = "wind-speed-strong";
    } else if (speed < 20.8) {
      status = "Near Gale (Warning!)";
      color = "wind-speed-strong";
    } else {
      status = "Gale (DANGER!)";
      color = "wind-speed-gale";
    }

    statusLabel.setText("Status: " + status);

    statusLabel.getStyleClass().removeAll("wind-speed-calm",
            "wind-speed-light", "wind-speed-breeze", "wind-speed-moderate",
            "wind-speed-strong", "wind-speed-gale");
    windBar.getStyleClass().removeAll("wind-speed-calm",
            "wind-speed-light", "wind-speed-breeze", "wind-speed-moderate",
            "wind-speed-strong", "wind-speed-gale");

    statusLabel.getStyleClass().add(color);
    windBar.getStyleClass().add(color);

    log.debug("Wind status updated to '{}' with CSS class '{}'", status, color);
  }

  /**
  * Ensures the given runnable executes on the JavaFX Application Thread.
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
   * Adds a new entry to the wind change history.
   * The entry includes a timestamp and a descriptive message.
   *
   * @param message the message describing the change
   */
  private void addHistoryEntry(String message) {
    String timestamp = LocalDateTime.now()
            .truncatedTo(ChronoUnit.SECONDS)
            .toString();
    changeHistory.addFirst(timestamp + " - " + message);
  }

  /**
   * Displays a dialog showing the wind change history.
   * This dialog lists all recorded changes made during the current session.
   */
  private void showHistoryDialog() {
    Dialog<Void> dialog = new Dialog<>();
    dialog.setTitle("Wind History");
    dialog.setHeaderText("Wind changes made during this session:");

    dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

    ListView<String> listView = new ListView<>();
    listView.getItems().setAll(changeHistory);

    int visibleRows = Math.min(changeHistory.size(), 10);
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
