package edu.ntnu.bidata.smg.group8.control.ui.view.cards;

import edu.ntnu.bidata.smg.group8.common.util.AppLogger;
import edu.ntnu.bidata.smg.group8.control.ui.factory.ButtonFactory;
import edu.ntnu.bidata.smg.group8.control.ui.view.ControlCard;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;

/**
* Builder for the temperature control card.
* This builder creates a control card for managing greenhouse temperature with
* current value, progress indicator and 24h statistics

* @author Andrea Sandnes
* @version 27.10.2025
*/
public class TemperatureCardBuilder implements CardBuilder {
  private static final Logger log = AppLogger.get(TemperatureCardBuilder.class);

  private final ControlCard card;
  private Label minLabel;
  private Label maxLabel;
  private Label avgLabel;
  private ProgressBar temperatureBar;
  private Button historyButton;

  // Temperature range for progress bar
  private static final double MIN_TEMP = 0.0;
  private static final double MAX_TEMP = 50.0;

  // Temperature threshold color coding
  private static final double T_COLD = 15.0;
  private static final double T_COOL = 20.0;
  private static final double T_OPTIMAL = 25.0;
  private static final double T_WARM = 30.0;

  private String activeZoneClass;

  /**
  * Constructs a new temperature card builder.
  */
  public TemperatureCardBuilder() {
    this.card = new ControlCard("Temperature");
    card.setValueText("--°C");

    log.debug("TemperatureCardBuilder initialized with range [{}..{}]°C", MIN_TEMP, MAX_TEMP);
  }

  /**
  * Builds and returns the complete temperature control card.

  * @return the fully constructed ControlCard ready for display
  */
  @Override
  public ControlCard build() {
    log.info("Building Temperature control card");

    createStatisticsLabels();
    createTemperatureBar();
    createFooter();

    card.addContent(
            new Separator(),
            temperatureBar,
            createStatisticsBox()
    );

    log.debug("Temperature control card built successfully");

    return card;
  }

  /**
  * Gets the control card instance.

  * @return the ControlCard instance
  */
  @Override
  public ControlCard getCard() {
    return card;
  }

  /**
  * Creates the statistics labels (min, max, average).
  */
  private void createStatisticsLabels() {
    minLabel = new Label("Min: --°C");
    minLabel.getStyleClass().addAll("card-subtle", "temp-stat");

    maxLabel = new Label("Max: --°C");
    maxLabel.getStyleClass().addAll("card-subtle", "temp-stat");

    avgLabel = new Label("Avg: --°C");
    avgLabel.getStyleClass().addAll("card-subtle", "temp-stat");

    log.trace("Statistics labels created");
  }

  /**
  * Creates the temperature progress bar.
  */
  private void createTemperatureBar() {
    temperatureBar = new ProgressBar(0);
    temperatureBar.setMaxWidth(Double.MAX_VALUE);
    temperatureBar.setPrefHeight(20);
    temperatureBar.getStyleClass().addAll("temp-bar", "temp-cool");
    Tooltip.install(temperatureBar, new Tooltip("Status for temperature"));

    log.trace("Temperature progress bar created");
  }

  /**
  * Creates the statistics box with min/max/average values.
  *
  * @return VBox containing statistics
  */
  private VBox createStatisticsBox() {
    Label statsTitle = new Label("24h Statistics:");
    statsTitle.getStyleClass().add("temp-stats-title");
    statsTitle.setMaxWidth(Double.MAX_VALUE);
    statsTitle.setAlignment(Pos.CENTER);

    minLabel.setMaxWidth(Double.MAX_VALUE);
    minLabel.setAlignment(Pos.CENTER);

    avgLabel.setMaxWidth(Double.MAX_VALUE);
    avgLabel.setAlignment(Pos.CENTER);

    maxLabel.setMaxWidth(Double.MAX_VALUE);
    maxLabel.setAlignment(Pos.CENTER);

    VBox statsBox = new VBox(6, statsTitle, new Separator(), minLabel, avgLabel, maxLabel);
    statsBox.setAlignment(Pos.CENTER);
    statsBox.getStyleClass().add("temp-stats-box");

    log.trace("Statistics box created");

    return statsBox;
  }

  /**
  * Creates the footer with history button.
  */
  private void createFooter() {
    historyButton = ButtonFactory.createButton("History...");
    historyButton.setTooltip(new Tooltip("View temperature history (24h)"));
    card.getFooter().getChildren().add(historyButton);

    log.trace("Footer with history button created");
  }

  /**
  * Updates the temperature display.
  *
  * @param temperature the current temperature in Celsius
  */
  public void updateTemperature(double temperature) {
    Runnable ui = () -> {
      if (temperatureBar == null) {
        log.warn("updateTemperature called before build() - skipping UI update");
        return;
      }
      card.setValueText(String.format("%.1f°C", temperature));

      // Update progress bar (normalized to 0-1 range)
      double progress = (temperature - MIN_TEMP) / (MAX_TEMP - MIN_TEMP);
      progress = Math.max(0, Math.min(1, progress)); // Clamp to 0-1
      temperatureBar.setProgress(progress);

      log.trace("Progress bar updated: {:.2f} ({}°C)", progress, temperature);

      // Update color based on temperature
      applyTemperatureStyle(temperature);
      };

      if (javafx.application.Platform.isFxApplicationThread()) {
        ui.run();
      } else {
        javafx.application.Platform.runLater(ui);
      }
    }

  /**
  * Applies CSS style classes based on temperature zones.
  *
  * @param temperature the current temperature
  */
  private void applyTemperatureStyle(double temperature) {
    String newClass;
    String zone;

    if (temperature < T_COLD) {
      newClass = "temp-cold";
      zone = "COLD";
    } else if (temperature < T_COOL) {
      newClass = "temp-cool";
      zone = "COOL";
    } else if (temperature < T_OPTIMAL) {
      newClass = "temp-optimal";
      zone = "OPTIMAL";
    } else if (temperature < T_WARM) {
      newClass = "temp-warm";
      zone = "WARM";
    } else {
      newClass = "temp-hot";
      zone = "HOT";
    }

    if (!newClass.equals(activeZoneClass)) {
      if (activeZoneClass != null) {
        temperatureBar.getStyleClass().remove(activeZoneClass);
      }
      temperatureBar.getStyleClass().add(newClass);
      log.debug("Temperature zone changed: {} -> {} ({:.1f}°C)",
              activeZoneClass != null ? activeZoneClass : "none",
              zone,
              temperature);
      activeZoneClass = newClass;

    }
  }

  /**
  * Updates the statistics display.
  *
  * @param min minimum temperature in last 24h
  * @param max maximum temperature in last 24h
  * @param avg average temperature in last 24h
  */
  public void updateStatistics(double min, double max, double avg) {
    log.debug("Updating 24h statistics - Min: {:.1f}°C, Max: {:.1f}°C, Avg: {:.1f}°C",
            min, max, avg);

    Runnable ui = () -> {
      if (minLabel == null || maxLabel == null || avgLabel == null) {
        log.warn("updateStatistics called before build() - skipping UI update");
        return;
      }
      minLabel.setText(String.format("Min: %.1f°C", min));
      maxLabel.setText(String.format("Max: %.1f°C", max));
      avgLabel.setText(String.format("Avg: %.1f°C", avg));

      log.trace("Statistics labels updated successfully");
    };

    if (javafx.application.Platform.isFxApplicationThread()) {
      ui.run();
    } else {
      javafx.application.Platform.runLater(ui);
    }
  }

  /**
  * Gets the minimum temperature label.
  *
  * @return the min label
  */
  public Label getMinLabel() {
    return minLabel;
  }

  /**
  * Gets the maximum temperature label.
  *
  * @return the max label
  */
  public Label getMaxLabel() {
    return maxLabel;
  }

  /**
  * Gets the average temperature label.
  *
  * @return the average label
  */
  public Label getAvgLabel() {
    return avgLabel;
  }

  /**
  * Gets the temperature progress bar.
  *
  * @return the progress bar
  */
  public ProgressBar getTemperatureBar() {
    return temperatureBar;
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