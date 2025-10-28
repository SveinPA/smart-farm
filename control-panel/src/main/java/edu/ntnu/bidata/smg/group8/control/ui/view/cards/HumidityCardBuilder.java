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
* Builder for the humidity control card.
* This builder creates a control card for managing greenhouse humidity
*
*/
public class HumidityCardBuilder implements CardBuilder {
  private final ControlCard card;
  private Label currentLabel;
  private Label minLabel;
  private Label maxLabel;
  private Label avgLabel;
  private ProgressBar humidityBar;
  private Button historyButton;

  /**
  * Constructs a new humidity card builder.
  */
  public HumidityCardBuilder() {
    this.card = new ControlCard("Humidity");
    card.setValueText("--%");
  }

  /**
  * Builds and returns the complete humidity control card.

  * @return the fully constructed ControlCard ready for display
  */
  @Override
  public ControlCard build() {
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
  }

  /**
  * Creates the humidity progress bar.
  */
  private void createHumidityBar() {
    humidityBar = new ProgressBar(0);
    humidityBar.setMaxWidth(Double.MAX_VALUE);
    humidityBar.setPrefHeight(20);

    // Style based on humidity level
    humidityBar.setStyle("-fx-accent: #2196f3;"); // Blue for normal
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
  * Updates the humidity display.
  *
  * @param humidity the current relative humidity (0-100%)
  */
  public void updateHumidity(double humidity) {
    card.setValueText(String.format("%.0f%%", humidity));
    currentLabel.setText(String.format("Current: %.0f%%", humidity));

    // Update progress bar (humidity is already 0-100, convert to 0-1)
    double progress = humidity / 100.0;
    progress = Math.max(0, Math.min(1, progress)); // Clamp to 0-1
    humidityBar.setProgress(progress);

    // Update color based on humidity level
    updateHumidityBarColor(humidity);
  }

  /**
  * Updates the humidity bar color based on value.
  *
  * @param humidity the current humidity percentage
  */
  private void updateHumidityBarColor(double humidity) {
    String color;

    if (humidity < 30) {
      color = "#f44336"; // Red - too dry
    } else if (humidity < 40) {
      color = "#ff9800"; // Orange - dry
    } else if (humidity < 60) {
      color = "#4caf50"; // Green - optimal
    } else if (humidity < 70) {
      color = "#2196f3"; // Blue - slightly humid
    } else if (humidity < 80) {
      color = "#3f51b5"; // Dark blue - humid
    } else {
      color = "#9c27b0"; // Purple - too humid
    }

    humidityBar.setStyle("-fx-accent: " + color + ";");
  }

  /**
  * Updates the statistics display.
  *
  * @param min minimum humidity in last 24h
  * @param max maximum humidity in last 24h
  * @param avg average humidity in last 24h
  */
  public void updateStatistics(double min, double max, double avg) {
    minLabel.setText(String.format("Min: %.0f%%", min));
    maxLabel.setText(String.format("Max: %.0f%%", max));
    avgLabel.setText(String.format("Avg: %.0f%%", avg));
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