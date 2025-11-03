package edu.ntnu.bidata.smg.group8.control.ui.controller.cardcontrollers;

import edu.ntnu.bidata.smg.group8.common.util.AppLogger;
import edu.ntnu.bidata.smg.group8.control.ui.view.ControlCard;
import edu.ntnu.bidata.smg.group8.control.ui.view.cards.TemperatureCardBuilder;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import org.slf4j.Logger;


/**
* Controller for the Temperature control card.
* This controller coordinates the interaction between the TemperatureCardBuilder UI
* and the underlying logic it is responsible for.

* @author Andrea Sandnes
* @version 28.10.2025
*/
public class TemperatureCardController {
  private static final Logger log = AppLogger.get(TemperatureCardController.class);

  // Temperature range for progress bar
  private static final double MIN_TEMP = 0.0;
  private static final double MAX_TEMP = 50.0;

  // Temperature threshold color coding
  private static final double T_COLD = 15.0;
  private static final double T_COOL = 20.0;
  private static final double T_OPTIMAL = 25.0;
  private static final double T_WARM = 30.0;

  private final ControlCard card;
  private Label minLabel;
  private Label maxLabel;
  private Label avgLabel;
  private ProgressBar temperatureBar;
  private Button historyButton;

  private String activeZoneClass;


  /**
  * Creates a new TemperatureCardController with the specified UI components.
  *
  * @param card the main card container
  * @param temperatureBar progress bar visualizing temperature
  * @param minLabel label displaying the minimum temperature (24h)
  * @param maxLabel label displaying the maximum temperature (24h)
  * @param avgLabel label displaying the average temperature (24h)
  * @param historyButton button to access historical data
  */
  public TemperatureCardController(ControlCard card, ProgressBar temperatureBar, Label minLabel,
                                   Label maxLabel, Label avgLabel, Button historyButton) {
    this.card = card;
    this.temperatureBar = temperatureBar;
    this.minLabel = minLabel;
    this.maxLabel = maxLabel;
    this.avgLabel = avgLabel;
    this.historyButton = historyButton;

    log.debug("TemperatureCardController wired with range [{}, {}]°C", MIN_TEMP, MAX_TEMP);
  }

  /**
  * Initializes event handlers and starts any listeners required by this controller.
  */
  public void start() {
    log.info("Starting TemperatureCardController");
    // TODO: Add initialization logic here
    log.debug("TemperatureCardController started successfully");
  }

  /**
  * Stops this controller and cleans up resources/listeners.
  */
  public void stop() {
    log.info("Stopping TemperatureCardController");
    // TODO: Add cleanup logic here
    log.debug("TemperatureCardController stopped successfully");
  }

  /**
  * Updates the temperature display.
  *
  * @param temperature the current temperature in Celsius
  */
  public void updateTemperature(double temperature) {
    log.info("Temperature updated: {}°C", String.format("%.1f", temperature));

    fx(() -> {
      card.setValueText(String.format("%.1f°C", temperature));

      // Update progress bar (normalized to 0-1 range)
      double progress = (temperature - MIN_TEMP) / (MAX_TEMP - MIN_TEMP);
      progress = Math.max(0, Math.min(1, progress)); // Clamp to 0-1
      temperatureBar.setProgress(progress);

      log.trace("Progress bar updated: {} ({}°C)",
              String.format("%.2f", progress),
              String.format("%.1f", temperature));

      // Update color based on temperature
      applyTemperatureStyle(temperature);
    });
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
      log.info("Temperature zone changed: {} -> {} ({}°C)",
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
    log.debug("Updating 24h statistics - Min: {}°C, Max: {}°C, Avg: {}°C",
            String.format("%.1f", min),
            String.format("%.1f", max),
            String.format("%.1f", avg));

    fx(() -> {
      minLabel.setText(String.format("Min: %.1f°C", min));
      maxLabel.setText(String.format("Max: %.1f°C", max));
      avgLabel.setText(String.format("Avg: %.1f°C", avg));

      log.trace("Statistics labels updated successfully");
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
