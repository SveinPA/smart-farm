package edu.ntnu.bidata.smg.group8.control.ui.view.cards;

import edu.ntnu.bidata.smg.group8.control.ui.factory.ButtonFactory;
import edu.ntnu.bidata.smg.group8.control.ui.view.ControlCard;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Separator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
* Builder for the temperature control card.
* This builder creates a control card for managing greenhouse temperature,
*

* @author Andrea Sandnes
* @version 27.10.2025
*/
public class TemperatureCardBuilder implements CardBuilder {
  private final ControlCard card;
  private Label currentLabel;
  private Label minLabel;
  private Label maxLabel;
  private Label avgLabel;
  private ProgressBar temperatureBar;
  private Button historyButton;

  // Temperature range for progress bar
  private static final double MIN_TEMP = 0.0;
  private static final double MAX_TEMP = 50.0;

  /**
  * Constructs a new temperature card builder.
  */
  public TemperatureCardBuilder() {
    this.card = new ControlCard("Temperature");
    card.setValueText("--°C");
  }

  /**
  * Builds and returns the complete temperature control card.

  * @return the fully constructed ControlCard ready for display
  */
  @Override
  public ControlCard build() {
    createCurrentLabel();
    createStatisticsLabels();
    createTemperatureBar();
    createFooter();

    card.addContent(
            currentLabel,
            new Separator(),
            temperatureBar,
            createStatisticsBox()
    );

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
  * Creates the current temperature label.
  */
  private void createCurrentLabel() {
    currentLabel = new Label("Current: --°C");
    currentLabel.getStyleClass().add("card-subtle");
    currentLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
  }

  /**
  * Creates the statistics labels (min, max, average).
  */
  private void createStatisticsLabels() {
    minLabel = new Label("Min: --°C");
    minLabel.getStyleClass().add("card-subtle");
    minLabel.setStyle("-fx-font-size: 11px;");

    maxLabel = new Label("Max: --°C");
    maxLabel.getStyleClass().add("card-subtle");
    maxLabel.setStyle("-fx-font-size: 11px;");

    avgLabel = new Label("Avg: --°C");
    avgLabel.getStyleClass().add("card-subtle");
    avgLabel.setStyle("-fx-font-size: 11px;");
  }

  /**
  * Creates the temperature progress bar.
  */
  private void createTemperatureBar() {
    temperatureBar = new ProgressBar(0);
    temperatureBar.setMaxWidth(Double.MAX_VALUE);
    temperatureBar.setPrefHeight(20);

    // Style based on temperature ranges
    temperatureBar.setStyle("-fx-accent: #2196f3;"); // Blue for normal
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

    return statsBox;
  }

  /**
  * Creates the footer with history button.
  */
  private void createFooter() {
    historyButton = ButtonFactory.createButton("History...");
    card.getFooter().getChildren().add(historyButton);
  }

  /**
  * Updates the temperature display.
  *
  * @param temperature the current temperature in Celsius
  */
  public void updateTemperature(double temperature) {
    card.setValueText(String.format("%.1f°C", temperature));
    currentLabel.setText(String.format("Current: %.1f°C", temperature));

    // Update progress bar (normalized to 0-1 range)
    double progress = (temperature - MIN_TEMP) / (MAX_TEMP - MIN_TEMP);
    progress = Math.max(0, Math.min(1, progress)); // Clamp to 0-1
    temperatureBar.setProgress(progress);

    // Update color based on temperature
    updateTemperatureBarColor(temperature);
  }

  /**
  * Updates the temperature bar color based on value.
  *
  * @param temperature the current temperature
  */
  private void updateTemperatureBarColor(double temperature) {
    String color;

    if (temperature < 15) {
      color = "#2196f3"; // Blue - cold
    } else if (temperature < 20) {
      color = "#4caf50"; // Green - cool
    } else if (temperature < 25) {
      color = "#8bc34a"; // Light green - optimal
    } else if (temperature < 30) {
      color = "#ff9800"; // Orange - warm
    } else {
      color = "#f44336"; // Red - hot
    }

    temperatureBar.setStyle("-fx-accent: " + color + ";");
  }

  /**
  * Updates the statistics display.
  *
  * @param min minimum temperature in last 24h
  * @param max maximum temperature in last 24h
  * @param avg average temperature in last 24h
  */
  public void updateStatistics(double min, double max, double avg) {
    minLabel.setText(String.format("Min: %.1f°C", min));
    maxLabel.setText(String.format("Max: %.1f°C", max));
    avgLabel.setText(String.format("Avg: %.1f°C", avg));
  }

  // Getters for controller access

  /**
  * Gets the current temperature label.
  *
  * @return the current label
  */
  public Label getCurrentLabel() {
    return currentLabel;
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