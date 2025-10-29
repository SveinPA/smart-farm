package edu.ntnu.bidata.smg.group8.control.ui.view.cards;

import edu.ntnu.bidata.smg.group8.common.util.AppLogger;
import edu.ntnu.bidata.smg.group8.control.ui.factory.ButtonFactory;
import edu.ntnu.bidata.smg.group8.control.ui.view.ControlCard;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Separator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;

/**
* Builder for the pH control card.
* This builder creates a control card for managing greenhouse pH,
*

* @author Andrea Sandnes
* @version 28.10.2025
*/
public class PHCardBuilder implements CardBuilder {
  private static final Logger log = AppLogger.get(PHCardBuilder.class);

  private final ControlCard card;
  private Label currentLabel;
  private Label statusLabel;
  private Label minLabel;
  private Label maxLabel;
  private Label avgLabel;
  private ProgressBar phBar;
  private Button historyButton;

  // pH scale range for visualization
  private static final double MIN_PH = 0.0;
  private static final double MAX_PH = 14.0;

  private static final double PH_VERY_ACIDIC = 4.5;
  private static final double PH_ACIDIC = 6.0;
  private static final double PH_SLIGHTLY_ACIDIC = 6.5;
  private static final double PH_NEUTRAL_HIGH = 7.5;
  private static final double PH_SLIGHTLY_ALKALINE = 8.5;
  private static final double PH_ALKALINE = 10.0;

  private String previousStatus = null;

  /**
  * Constructs a new pH card builder.
  */
  public PHCardBuilder() {
    this.card = new ControlCard("pH Level");
    card.setValueText("--");
    log.debug("PHCardBuilder initialized with range [{} - {}]", MIN_PH, MAX_PH);
  }

  /**
  * Builds and returns the complete pH control card.

  * @return the fully constructed ControlCard ready for display
  */
  @Override
  public ControlCard build() {
    log.info("Building pH control card");

    createCurrentLabel();
    createStatusLabel();
    createStatisticsLabels();
    createPHBar();
    createFooter();

    card.addContent(
            currentLabel,
            statusLabel,
            new Separator(),
            phBar,
            createStatisticsBox()
    );

    log.debug("pH control card built successfully");

    return card;
  }

  /**
  * Creates the control card instance.

  * @return the ControlCard instance
  */
  @Override
  public ControlCard getCard() {
    return card;
  }

  /**
  * Creates the current pH label.
  */
  private void createCurrentLabel() {
    currentLabel = new Label("Current: --");
    currentLabel.getStyleClass().add("card-subtle");
    currentLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
    log.trace("Current pH label created");
  }

  /**
  * Creates the pH status label (Acidic/Neutral/Alkaline).
  */
  private void createStatusLabel() {
    statusLabel = new Label("Status: --");
    statusLabel.getStyleClass().add("card-subtle");
    statusLabel.setStyle("-fx-font-size: 12px; -fx-font-style: italic;");
    log.trace("Status label created");
  }

  /**
  * Creates the statistics labels (min, max, average).
  */
  private void createStatisticsLabels() {
    minLabel = new Label("Min: --");
    minLabel.getStyleClass().add("card-subtle");
    minLabel.setStyle("-fx-font-size: 11px;");

    maxLabel = new Label("Max: --");
    maxLabel.getStyleClass().add("card-subtle");
    maxLabel.setStyle("-fx-font-size: 11px;");

    avgLabel = new Label("Avg: --");
    avgLabel.getStyleClass().add("card-subtle");
    avgLabel.setStyle("-fx-font-size: 11px;");

    log.trace("Statistics labels created");
  }

  /**
  * Creates the pH progress bar.
  */
  private void createPHBar() {
    phBar = new ProgressBar(0.5); // Start at neutral (7/14 = 0.5)
    phBar.setMaxWidth(Double.MAX_VALUE);
    phBar.setPrefHeight(20);

    phBar.setStyle("-fx-accent: #4caf50;");
    log.trace("pH progress bare created");
  }

  /**
  * Creates the statistics box with min/max/average values.
  *
  * @return VBox containing statistics
  */
  private VBox createStatisticsBox() {
    Label statsTitle = new Label("24h Statistics:");
    statsTitle.setStyle("-fx-font-size: 11px; -fx-font-weight: bold;");

    HBox statsRow1 = new HBox(15, minLabel, maxLabel);
    statsRow1.setAlignment(Pos.CENTER_LEFT);

    VBox statsBox = new VBox(4, statsTitle, statsRow1, avgLabel);
    statsBox.setAlignment(Pos.CENTER_LEFT);

    log.trace("Statistics box created");

    return statsBox;
  }

  /**
  * Creates the footer with history button.
  */
  private void createFooter() {
    historyButton = ButtonFactory.createButton("History...");
    card.getFooter().getChildren().add(historyButton);
    log.trace("Footer with history button created");
  }

  /**
  * Updates the pH display.
  *
  * @param ph the current pH value (0-14)
  */
  public void updatePH(double ph) {
    log.debug("Updating pH display to: {:.2f}", ph);

    Runnable ui = () -> {
      if (currentLabel == null || phBar == null) {
      log.warn("updatePH called before build() - Skipping UI update");
      return;
    }

    card.setValueText(String.format("%.1f", ph));
    currentLabel.setText(String.format("Current: %.1f", ph));

    // Update progress bar (normalized to 0-1 range)
    double progress = (ph - MIN_PH) / (MAX_PH - MIN_PH);
    progress = Math.max(0, Math.min(1, progress)); // Clamp to 0-1
    phBar.setProgress(progress);

      log.trace("pH progress bar updated: {:.2f} (pH {:.2f})", progress, ph);

    // Update status and color
    updatePHStatus(ph);
  };

    if (javafx.application.Platform.isFxApplicationThread()) {
      ui.run();
    } else {
      javafx.application.Platform.runLater(ui);
    }
  }

  /**
  * Updates the pH status label and bar color based on value.
  *
  * @param ph the current pH value
  */
  private void updatePHStatus(double ph) {
    String status;
    String color;

    if (ph < PH_VERY_ACIDIC) {
      status = "Very Acidic";
      color = "#f44336"; // Red
    } else if (ph < PH_ACIDIC) {
      status = "Acidic";
      color = "#ff9800"; // Orange
    } else if (ph < PH_SLIGHTLY_ACIDIC) {
      status = "Slightly Acidic (Good)";
      color = "#8bc34a"; // Light green
    } else if (ph < PH_NEUTRAL_HIGH) {
      status = "Neutral (Optimal)";
      color = "#4caf50"; // Green
    } else if (ph < PH_SLIGHTLY_ALKALINE) {
      status = "Slightly Alkaline";
      color = "#2196f3"; // Blue
    } else if (ph < PH_ALKALINE) {
      status = "Alkaline";
      color = "#3f51b5"; // Dark blue
    } else {
      status = "Very Alkaline";
      color = "#9c27b0"; // Purple
    }

    if (!status.equals(previousStatus)) {
      log.info("pH status changed: {} -> {} (pH {:-2f})", previousStatus != null ? previousStatus : "UNKNOWN",
              status,
              ph);
      previousStatus = status;
    }

    if (ph < PH_VERY_ACIDIC){
      log.warn("CRITICAL: pH too acidic ({:.2f}) - Risk of nutrient lockout and root damage", ph);
    } else if (ph > PH_ALKALINE) {
      log.warn("CRITICAL: pH too alkaline ({:.2f}) - Risk of nutrient deficiencies", ph);
    }


    statusLabel.setText("Status: " + status);
    statusLabel.setStyle("-fx-font-size: 12px; -fx-font-style: "
            + "italic; -fx-text-fill: " + color + ";");
    phBar.setStyle("-fx-accent: " + color + ";");
  }

  /**
  * Updates the statistics display.
  *
  * @param min minimum pH in last 24h
  * @param max maximum pH in last 24h
  * @param avg average pH in last 24h
  */
  public void updateStatistics(double min, double max, double avg) {
    log.debug("Updating pH statistics - Min: {:.2f}, Max: {:.2f}, Avg: {:.2f}",
            min, max, avg);

    Runnable ui = () -> {
      if (minLabel == null || maxLabel == null || avgLabel == null) {
        log.warn("updateStatistics called before build() - skipping UI update");
        return;
      }

      minLabel.setText(String.format("Min: %.1f", min));
      maxLabel.setText(String.format("Max: %.1f", max));
      avgLabel.setText(String.format("Avg: %.1f", avg));

      log.trace("pH statistics labels updated successfully");
    };

    if (javafx.application.Platform.isFxApplicationThread()) {
      ui.run();
    } else {
      javafx.application.Platform.runLater(ui);
    }
  }

  // Getters for controller access

  /**
  * Gets the current pH label.
  *
  * @return the current label
  */
  public Label getCurrentLabel() {
    return currentLabel;
  }

  /**
  * Gets the pH status label.
  *
  * @return the status label
  */
  public Label getStatusLabel() {
    return statusLabel;
  }

  /**
  * Gets the minimum pH label.
  *
  * @return the min label
  */
  public Label getMinLabel() {
    return minLabel;
  }

  /**
  * Gets the maximum pH label.
  *
  * @return the max label
  */
  public Label getMaxLabel() {
    return maxLabel;
  }
}