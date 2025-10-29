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
* Builder for the humidity control card.
* This builder creates a control card for managing greenhouse humidity
*
*/
public class HumidityCardBuilder implements CardBuilder {
  private static final Logger log = AppLogger.get(HumidityCardBuilder.class);

  private final ControlCard card;
  private Label currentLabel;
  private Label minLabel;
  private Label maxLabel;
  private Label avgLabel;
  private ProgressBar humidityBar;
  private Button historyButton;

  private static final double HUMIDITY_VERY_DRY = 30.0;
  private static final double HUMIDITY_DRY = 40.0;
  private static final double HUMIDITY_OPTIMAL_HIGH = 60.0;
  private static final double HUMIDITY_SLIGHTLY_HUMID = 70.0;
  private static final double HUMIDITY_HUMID = 80.0;

  // Critical thresholds
  private static final double HUMIDITY_CRITICAL_LOW = 30.0;
  private static final double HUMIDITY_CRITICAL_HIGH = 80.0;

  private String previousLevel = null;

  /**
  * Constructs a new humidity card builder.
  */
  public HumidityCardBuilder() {
    this.card = new ControlCard("Humidity");
    card.setValueText("--%");
    log.debug("HumidityCardBuilder initialized with range [0% - 100%]");
  }

  /**
  * Builds and returns the complete humidity control card.

  * @return the fully constructed ControlCard ready for display
  */
  @Override
  public ControlCard build() {
    log.info("Building Humidity control card");

    createCurrentLabel();
    createStatisticsLabels();
    createHumidityBar();
    createFooter();

    card.addContent(
            currentLabel,
            new Separator(),
            humidityBar,
            createStatisticsBox()
    );

    log.debug("Humidity control card built successfully");

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
  * Creates the current humidity label.
  */
  private void createCurrentLabel() {
    currentLabel = new Label("Current: --%");
    currentLabel.getStyleClass().add("card-subtle");
    currentLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
    log.trace("Current humidity label created");
  }

  /**
  * Creates the statistics labels (min, max, average).
  */
  private void createStatisticsLabels() {
    minLabel = new Label("Min: --%");
    minLabel.getStyleClass().add("card-subtle");
    minLabel.setStyle("-fx-font-size: 11px;");

    maxLabel = new Label("Max: --%");
    maxLabel.getStyleClass().add("card-subtle");
    maxLabel.setStyle("-fx-font-size: 11px;");

    avgLabel = new Label("Avg: --%");
    avgLabel.getStyleClass().add("card-subtle");
    avgLabel.setStyle("-fx-font-size: 11px;");

    log.trace("Statistics labels created");
  }

  /**
  * Creates the humidity progress bar.
  */
  private void createHumidityBar() {
    humidityBar = new ProgressBar(0);
    humidityBar.setMaxWidth(Double.MAX_VALUE);
    humidityBar.setPrefHeight(20);

    // Style based on humidity level
    humidityBar.setStyle("-fx-accent: #2196f3;");
    log.trace("Humidity progress bar created with default style");
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
  * Updates the humidity display.
  *
  * @param humidity the current relative humidity (0-100%)
  */
  public void updateHumidity(double humidity) {
    log.debug("Updating humidity to: {:.1f}%", humidity);

    Runnable ui = () -> {
      if (currentLabel == null || humidityBar == null) {
        log.warn("updateHumidity called before build() - skipping UI update");
        return;
      }

      card.setValueText(String.format("%.0f%%", humidity));
      currentLabel.setText(String.format("Current: %.0f%%", humidity));

      // Update progress bar (humidity is already 0-100, convert to 0-1)
      double progress = humidity / 100.0;
      progress = Math.max(0, Math.min(1, progress)); // Clamp to 0-1
      humidityBar.setProgress(progress);

      log.trace("Humidity progress bar updated: {:.2f} ({:.1f}%)", progress, humidity);

    // Update color based on humidity level
    updateHumidityBarColor(humidity);

      checkCriticalLevels(humidity);
    };

    if (javafx.application.Platform.isFxApplicationThread()) {
      ui.run();
    } else {
      javafx.application.Platform.runLater(ui);
    }
  }

  /**
  * Updates the humidity bar color based on value.
  *
  * @param humidity the current humidity percentage
  */
  private void updateHumidityBarColor(double humidity) {
    String level;
    String color;

    if (humidity < HUMIDITY_VERY_DRY) {
      level = "VERY_DRY";
      color = "#f44336"; // Red - too dry
    } else if (humidity < HUMIDITY_DRY) {
      level = "DRY";
      color = "#ff9800"; // Orange - dry
    } else if (humidity < HUMIDITY_OPTIMAL_HIGH) {
      level = "OPTIMAL";
      color = "#4caf50"; // Green - optimal
    } else if (humidity < HUMIDITY_SLIGHTLY_HUMID) {
      level = "SLIGHTLY_HUMID";
      color = "#2196f3"; // Blue - slightly humid
    } else if (humidity < HUMIDITY_HUMID) {
      level = "HUMID";
      color = "#3f51b5"; // Dark blue - humid
    } else {
      level = "VERY_HUMID";
      color = "#9c27b0"; // Purple - too humid
    }

    // Log level changes
    if (!level.equals(previousLevel)) {
      log.info("Humidity level changed: {} -> {} ({:.1f}%)",
              previousLevel != null ? previousLevel : "UNKNOWN",
              level,
              humidity);
      previousLevel = level;
    }

    humidityBar.setStyle("-fx-accent: " + color + ";");
  }

  /**
   * Checks for critical humidity levels and logs warnings.
   *
   * @param humidity the current humidity percentage
   */
  private void checkCriticalLevels(double humidity) {
    if (humidity < HUMIDITY_CRITICAL_LOW) {
      log.warn("CRITICAL: Humidity too low ({:.1f}%) - Risk of plant stress and wilting. Consider misting or irrigation.",
              humidity);
    } else if (humidity > HUMIDITY_CRITICAL_HIGH) {
      log.warn("CRITICAL: Humidity too high ({:.1f}%) - Risk of fungal diseases and mold. Increase ventilation.",
              humidity);
    } else if (humidity < HUMIDITY_DRY) {
      log.info("NOTICE: Humidity low ({:.1f}%) - Monitor plant conditions", humidity);
    } else if (humidity > HUMIDITY_SLIGHTLY_HUMID) {
      log.info("NOTICE: Humidity high ({:.1f}%) - Consider increasing ventilation", humidity);
    }
  }

  /**
  * Updates the statistics display.
  *
  * @param min minimum humidity in last 24h
  * @param max maximum humidity in last 24h
  * @param avg average humidity in last 24h
  */
  public void updateStatistics(double min, double max, double avg) {
    log.debug("Updating humidity statistics - Min: {:.1f}%, Max: {:.1f}%, Avg: {:.1f}%",
            min, max, avg);

    Runnable ui = () -> {
      if (minLabel == null || maxLabel == null || avgLabel == null) {
        log.warn("updateStatistics called before build() - skipping UI update");
        return;
      }

      minLabel.setText(String.format("Min: %.0f%%", min));
      maxLabel.setText(String.format("Max: %.0f%%", max));
      avgLabel.setText(String.format("Avg: %.0f%%", avg));

      log.trace("Humidity statistics labels updated successfully");

      // Check 24h extremes
      if (min < HUMIDITY_CRITICAL_LOW || max > HUMIDITY_CRITICAL_HIGH) {
        log.warn("24h humidity range includes critical levels - Min: {:.1f}%, Max: {:.1f}%", min, max);
      }

      // Check for large fluctuations
      double range = max - min;
      if (range > 40.0) {
        log.info("Large humidity fluctuation in 24h period: {:.1f}% range (Min: {:.1f}%, Max: {:.1f}%)",
                range, min, max);
      }
    };

    if (javafx.application.Platform.isFxApplicationThread()) {
      ui.run();
    } else {
      javafx.application.Platform.runLater(ui);
    }
  }


  // Getters for controller access

  /**
  * Gets the current humidity label.
  *
  * @return the current label
  */
  public Label getCurrentLabel() {
    return currentLabel;
  }

  /**
  * Gets the minimum humidity label.
  *
  * @return the min label
  */
  public Label getMinLabel() {
    return minLabel;
  }

  /**
  * Gets the maximum humidity label.
  *
  * @return the max label
  */
  public Label getMaxLabel() {
    return maxLabel;
  }

  /**
  * Gets the average humidity label.
  *
  * @return the average label
  */
  public Label getAvgLabel() {
    return avgLabel;
  }

  /**
  * Gets the humidity progress bar.
  *
  * @return the progress bar
  */
  public ProgressBar getHumidityBar() {
    return humidityBar;
  }

  /**
  * Gets the history button.
  *
  * @return the history button
  */
  public Button getHistoryButton() {
    return historyButton;
  }
}