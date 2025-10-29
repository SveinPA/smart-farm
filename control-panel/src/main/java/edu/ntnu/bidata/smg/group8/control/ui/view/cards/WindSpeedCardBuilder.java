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
* Builder for the wind speed control card.
* This builder creates a control card for wind speed monitoring.
*

* @author Andrea Sandnes
* @version 27.10.2025
*/
public class WindSpeedCardBuilder implements CardBuilder {
  private static final Logger log = AppLogger.get(WindSpeedCardBuilder.class);

  private final ControlCard card;
  private Label currentLabel;
  private Label statusLabel;
  private Label minLabel;
  private Label maxLabel;
  private Label avgLabel;
  private Label gustLabel;
  private ProgressBar windBar;
  private Button historyButton;

  // Wind speed range for visualization (m/s)
  private static final double MIN_WIND = 0.0;
  private static final double MAX_WIND = 30.0;

  private static final double WIND_CALM = 0.5;
  private static final double WIND_LIGHT_AIR = 3.3;
  private static final double WIND_LIGHT_BREEZE = 5.5;
  private static final double WIND_GENTLE_BREEZE = 8.0;
  private static final double WIND_MODERATE_BREEZE = 10.8;
  private static final double WIND_FRESH_BREEZE = 13.9;
  private static final double WIND_STRONG_BREEZE = 17.2;
  private static final double WIND_NEAR_GALE = 20.8;

  private static final double WIND_CAUTION_THRESHOLD = 13.9;
  private static final double WIND_WARNING_THRESHOLD = 17.2;
  private static final double WIND_DANGER_THRESHOLD = 20.8;

  private String previousStatus = null;

  /**
  * Constructs a new wind speed card builder.
  */
  public WindSpeedCardBuilder() {
    this.card = new ControlCard("Wind Speed");
    card.setValueText("-- m/s");

    log.debug("WindSpeedCardBuilder initialized with range [{} - {}] m/s", MIN_WIND, MAX_WIND);
  }

  /**
  * Builds and returns the complete wind speed control card.

  * @return the fully constructed ControlCard ready for display
  */
  @Override
  public ControlCard build() {
    log.info("Building Wind Speed control card");

    createCurrentLabel();
    createStatusLabel();
    createGustLabel();
    createStatisticsLabels();
    createWindBar();
    createFooter();

    card.addContent(
            currentLabel,
            statusLabel,
            gustLabel,
            new Separator(),
            windBar,
            createStatisticsBox()
    );

    log.debug("Wind Speed control card built successfully");

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
  * Creates the current wind speed label.
  */
  private void createCurrentLabel() {
    currentLabel = new Label("Current: -- m/s");
    currentLabel.getStyleClass().add("card-subtle");
    currentLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
    log.trace("Current wind speed label created");
  }

  /**
  * Creates the wind status label (Calm/Breeze/Strong/Gale).
  */
  private void createStatusLabel() {
    statusLabel = new Label("Status: --");
    statusLabel.getStyleClass().add("card-subtle");
    statusLabel.setStyle("-fx-font-size: 12px; -fx-font-style: italic;");
    log.trace("Wind status label created");
  }

  /**
  * Creates the wind gust label.
  */
  private void createGustLabel() {
    gustLabel = new Label("Gust: -- m/s");
    gustLabel.getStyleClass().add("card-subtle");
    gustLabel.setStyle("-fx-font-size: 11px;");
    log.trace("Wind gust label created");
  }

  /**
  * Creates the statistics labels (min, max, average).
  */
  private void createStatisticsLabels() {
    minLabel = new Label("Min: -- m/s");
    minLabel.getStyleClass().add("card-subtle");
    minLabel.setStyle("-fx-font-size: 11px;");

    maxLabel = new Label("Max: -- m/s");
    maxLabel.getStyleClass().add("card-subtle");
    maxLabel.setStyle("-fx-font-size: 11px;");

    avgLabel = new Label("Avg: -- m/s");
    avgLabel.getStyleClass().add("card-subtle");
    avgLabel.setStyle("-fx-font-size: 11px;");

    log.trace("Statistics labels created");
  }

  /**
  * Creates the wind speed progress bar.
  */
  private void createWindBar() {
    windBar = new ProgressBar(0);
    windBar.setMaxWidth(Double.MAX_VALUE);
    windBar.setPrefHeight(20);

    // Default calm color
    windBar.setStyle("-fx-accent: #4caf50;");
    log.trace("Wind speed progress bar created with calm default");
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
  * Updates the wind speed display.
  *
  * @param speed the current wind speed in m/s
  */
  public void updateWindSpeed(double speed) {
    log.debug("Updating wind speed to: {:.2f} m/s", speed);

    Runnable ui = () -> {
      if (currentLabel == null || windBar == null) {
        log.warn("updateWindSpeed called before build() - skipping UI update");
        return;
      }

      card.setValueText(String.format("%.1f m/s", speed));
      currentLabel.setText(String.format("Current: %.1f m/s", speed));

      // Update progress bar (normalized to 0-1 range)
      double progress = (speed - MIN_WIND) / (MAX_WIND - MIN_WIND);
      progress = Math.max(0, Math.min(1, progress)); // Clamp to 0-1
      windBar.setProgress(progress);

      log.trace("Wind speed progress bar updated: {:.2f} ({:.1f} m/s)", progress, speed);

      // Update status and color
      updateWindStatus(speed);
    };

    if (javafx.application.Platform.isFxApplicationThread()) {
      ui.run();
    } else {
      javafx.application.Platform.runLater(ui);
    }
  }

  /**
  * Updates the wind gust display.
  *
  * @param gust the maximum gust speed in m/s
  */
  public void updateGust(double gust) {
    log.debug("Updating wind gust to: {:.2f} m/s", gust);

    Runnable ui = () -> {
      if (gustLabel == null) {
        log.warn("updateGust called before build() - skipping UI update");
        return;
      }
      gustLabel.setText(String.format("Gust: %.1f m/s", gust));

      if (gust > WIND_DANGER_THRESHOLD) {
        log.warn("DANGER: Severe wind gust detected ({:.1f} m/s) - Risk to greenhouse structures!", gust);
      } else if (gust > WIND_WARNING_THRESHOLD) {
        log.warn("WARNING: Strong wind gust detected ({:.1f} m/s) - Consider closing windows", gust);
      }
    };

    if (javafx.application.Platform.isFxApplicationThread()) {
      ui.run();
    } else {
      javafx.application.Platform.runLater(ui);
    }
  }

  /**
  * Updates the wind status label and bar color based on value.
  *
  * @param speed the current wind speed in m/s
  */
  private void updateWindStatus(double speed) {
    String status;
    String color;

    if (speed < WIND_CALM) {
      status = "Calm";
      color = "#4caf50"; // Green
    } else if (speed < WIND_LIGHT_AIR) {
      status = "Light Air";
      color = "#8bc34a"; // Light green
    } else if (speed < WIND_LIGHT_BREEZE) {
      status = "Light Breeze";
      color = "#2196f3"; // Blue
    } else if (speed < WIND_GENTLE_BREEZE) {
      status = "Gentle Breeze";
      color = "#2196f3"; // Blue
    } else if (speed < WIND_MODERATE_BREEZE) {
      status = "Moderate Breeze";
      color = "#ff9800"; // Orange
    } else if (speed < WIND_FRESH_BREEZE) {
      status = "Fresh Breeze";
      color = "#ff9800"; // Orange
    } else if (speed < WIND_STRONG_BREEZE) {
      status = "Strong Breeze (Caution)";
      color = "#f44336"; // Red
    } else if (speed < WIND_NEAR_GALE) {
      status = "Near Gale (Warning!)";
      color = "#f44336"; // Red
    } else {
      status = "Gale (DANGER!)";
      color = "#d32f2f"; // Dark red
    }

    if (!status.equals(previousStatus)) {
      log.info("Wind status changed: {} -> {} ({:.2f} m/s)",
              previousStatus != null ? previousStatus : "UNKNOWN",
              status,
              speed);
      previousStatus = status;
    }

    // Warn about dangerous wind conditions
    if (speed >= WIND_DANGER_THRESHOLD) {
      log.warn("DANGER: Gale force winds ({:.1f} m/s) - Immediate action required! Close all windows and secure greenhouse",
              speed);
    } else if (speed >= WIND_WARNING_THRESHOLD) {
      log.warn("WARNING: Strong winds ({:.1f} m/s) - Consider closing windows to prevent damage", speed);
    } else if (speed >= WIND_CAUTION_THRESHOLD) {
      log.info("CAUTION: Fresh breeze ({:.1f} m/s) - Monitor wind conditions", speed);
    }

    statusLabel.setText("Status: " + status);
    statusLabel.setStyle("-fx-font-size: 12px; -fx-font-style: "
            + "italic; -fx-text-fill: " + color + ";");
    windBar.setStyle("-fx-accent: " + color + ";");

  }

  /**
  * Updates the statistics display.
  *
  * @param min minimum wind speed in last 24h
  * @param max maximum wind speed in last 24h
  * @param avg average wind speed in last 24h
  */
  public void updateStatistics(double min, double max, double avg) {
    log.debug("Updating wind statistics - Min: {:.2f} m/s, Max: {:.2f} m/s, Avg: {:.2f} m/s",
            min, max, avg);

    Runnable ui = () -> {
      if (minLabel == null || maxLabel == null || avgLabel == null) {
        log.warn("updateStatistics called before build() - skipping UI update");
        return;
      }

      minLabel.setText(String.format("Min: %.1f m/s", min));
      maxLabel.setText(String.format("Max: %.1f m/s", max));
      avgLabel.setText(String.format("Avg: %.1f m/s", avg));

      log.trace("Wind statistics labels updated successfully");

      // Note if max wind was dangerous
      if (max > WIND_WARNING_THRESHOLD) {
        log.info("24h max wind speed was significant: {:.1f} m/s", max);
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
  * Gets the current wind speed label.
  *
  * @return the current label
  */
  public Label getCurrentLabel() {
    return currentLabel;
  }

  /**
  * Gets the wind status label.
  *
  * @return the status label
  */
  public Label getStatusLabel() {
    return statusLabel;
  }

  /**
  * Gets the gust speed label.
  *
  * @return the gust label
  */
  public Label getGustLabel() {
    return gustLabel;
  }

  /**
  * Gets the minimum wind speed label.
  *
  * @return the min label
  */
  public Label getMinLabel() {
    return minLabel;
  }

  /**
  * Gets the maximum wind speed label.
  *
  * @return the max label
  */
  public Label getMaxLabel() {
    return maxLabel;
  }

  /**
  * Gets the average wind speed label.
  *
  * @return the average label
  */
  public Label getAvgLabel() {
    return avgLabel;
  }

  /**
  * Gets the wind speed progress bar.
  *
  * @return the progress bar
  */
  public ProgressBar getWindBar() {
    return windBar;
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