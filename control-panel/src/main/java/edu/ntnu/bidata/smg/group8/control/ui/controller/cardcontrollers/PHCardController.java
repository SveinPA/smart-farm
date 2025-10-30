package edu.ntnu.bidata.smg.group8.control.ui.controller.cardcontrollers;

import edu.ntnu.bidata.smg.group8.common.util.AppLogger;
import edu.ntnu.bidata.smg.group8.control.ui.view.ControlCard;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import org.slf4j.Logger;

/**
* Controller for the pH control card.
* Handles pH value updates, statistics display, and zone-based styling.

* @author Andrea Sandnes
* @version 28.10.2025
*/
public class PHCardController {
  private static final Logger log = AppLogger.get(PHCardController.class);

  // pH scale range for visualization
  private static final double MIN_PH = 0.0;
  private static final double MAX_PH = 14.0;

  private static final double PH_VERY_ACIDIC = 4.5;
  private static final double PH_ACIDIC = 6.0;
  private static final double PH_SLIGHTLY_ACIDIC = 6.5;
  private static final double PH_NEUTRAL_HIGH = 7.5;
  private static final double PH_SLIGHTLY_ALKALINE = 8.5;
  private static final double PH_ALKALINE = 10.0;

  private final ControlCard card;
  private Label currentLabel;
  private Label statusLabel;
  private Label minLabel;
  private Label maxLabel;
  private Label avgLabel;
  private ProgressBar phBar;
  private Button historyButton;

  private String previousStatus = null;
  private String activeZoneClass = null;


  /**
  * Creates a new PHCardController with the specified UI components.
  *
  * @param card the main card container
  * @param currentLabel label displaying current pH value
  * @param statusLabel label displaying pH status (Acidic/Neutral/Alkaline)
  * @param phBar progress bar visualizing pH level
  * @param minLabel label displaying minimum pH (24h)
  * @param maxLabel label displaying maximum pH (24h)
  * @param avgLabel label displaying average pH (24h)
  * @param historyButton button to access historical data
  */
  public PHCardController(ControlCard card, Label currentLabel, Label statusLabel,
                          ProgressBar phBar, Label minLabel, Label maxLabel,
                          Label avgLabel, Button historyButton) {
    this.card = card;
    this.currentLabel = currentLabel;
    this.statusLabel = statusLabel;
    this.phBar = phBar;
    this.minLabel = minLabel;
    this.maxLabel = maxLabel;
    this.avgLabel = avgLabel;
    this.historyButton = historyButton;

    log.debug("PHCardController wired with range [{}, {}]", MIN_PH, MAX_PH);
  }

  /**
   * Initializes event handlers and starts any listeners required by this controller.
   */
  public void start() {
    log.info("Starting PHCardController");
    // TODO: Add initialization logic here
    log.debug("PHCardController started successfully");
  }

  /**
   * Stops this controller and cleans up resources/listeners.
   */
  public void stop() {
    log.info("Stopping PHCardController");
    // TODO: Add cleanup logic here
    log.debug("PHCardController stopped successfully");
  }

  /**
   * Updates the pH display.
   *
   * @param ph the current pH value (0-14)
   */
  public void updatePH(double ph) {
    log.debug("Updating pH display to: {}", String.format("%.2f", ph));

    fx(() -> {
      card.setValueText(String.format("%.1f", ph));
      currentLabel.setText(String.format("Current: %.1f", ph));

      // Update progress bar (normalized to 0-1 range)
      double progress = (ph - MIN_PH) / (MAX_PH - MIN_PH);
      progress = Math.max(0, Math.min(1, progress)); // Clamp to 0-1
      phBar.setProgress(progress);

      log.trace("pH progress bar updated: {} (pH {})",
              String.format("%.2f", progress),
              String.format("%.2f", ph));

      // Update status and color
      updatePHStatus(ph);
    });
  }

  /**
   * Updates the pH status label and bar color based on value.
   *
   * @param ph the current pH value
   */
  private void updatePHStatus(double ph) {
    String status;
    String zoneClass;

    if (ph < PH_VERY_ACIDIC) {
      status = "Very Acidic";
      zoneClass = "ph-very-acidic";
    } else if (ph < PH_ACIDIC) {
      status = "Acidic";
      zoneClass = "ph-acidic";
    } else if (ph < PH_SLIGHTLY_ACIDIC) {
      status = "Slightly Acidic (Good)";
      zoneClass = "ph-slightly-acidic";
    } else if (ph < PH_NEUTRAL_HIGH) {
      status = "Neutral (Optimal)";
      zoneClass = "ph-neutral";
    } else if (ph < PH_SLIGHTLY_ALKALINE) {
      status = "Slightly Alkaline";
      zoneClass = "ph-slightly-alkaline";
    } else if (ph < PH_ALKALINE) {
      status = "Alkaline";
      zoneClass = "ph-alkaline";
    } else {
      status = "Very Alkaline";
      zoneClass = "ph-very-alkaline";
    }

    if (!status.equals(previousStatus)) {
      log.info("pH status changed: {} -> {} (pH {})",
              previousStatus != null ? previousStatus : "UNKNOWN",
              status,
              String.format("%.2f", ph));
      previousStatus = status;
    }

    if (ph < PH_VERY_ACIDIC) {
      log.warn("CRITICAL: pH too acidic ({}) - Risk of nutrient lockout and root damage",
              String.format("%.2f", ph));
    } else if (ph > PH_ALKALINE) {
      log.warn("CRITICAL: pH too alkaline ({}) - Risk of nutrient deficiencies",
              String.format("%.2f", ph));
    }

    if (!zoneClass.equals(activeZoneClass)) {
      if (activeZoneClass != null) {
        statusLabel.getStyleClass().remove(activeZoneClass);
        phBar.getStyleClass().remove(activeZoneClass);
      }
      statusLabel.getStyleClass().add(zoneClass);
      phBar.getStyleClass().add(zoneClass);
      activeZoneClass = zoneClass;
    }
  }

  /**
   * Updates the statistics display.
   *
   * @param min minimum pH in last 24h
   * @param max maximum pH in last 24h
   * @param avg average pH in last 24h
   */
  public void updateStatistics(double min, double max, double avg) {
    log.debug("Updating pH statistics - Min: {}, Max: {}, Avg: {}",
            String.format("%.2f", min),
            String.format("%.2f", max),
            String.format("%.2f", avg));

    fx(() -> {
      minLabel.setText(String.format("Min: %.1f", min));
      maxLabel.setText(String.format("Max: %.1f", max));
      avgLabel.setText(String.format("Avg: %.1f", avg));

      log.trace("pH statistics labels updated successfully");
    });
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


}