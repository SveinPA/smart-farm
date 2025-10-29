package edu.ntnu.bidata.smg.group8.control.ui.controller.cardcontrollers;

import edu.ntnu.bidata.smg.group8.common.util.AppLogger;
import edu.ntnu.bidata.smg.group8.control.ui.view.ControlCard;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import org.slf4j.Logger;

/**
* Controller for the Wind Speed control card.
* This controller manages the wind speed display card, handling real-time updates
* of wind speed measurements, gust information and statistical data. It applies
* visual styling to provide intuitive feedback about current wind conditions

* @author Andrea Sandnes
* @version 28.10.2025
*/
public class WindSpeedCardController {
  private static final Logger log = AppLogger.get(WindSpeedCardController.class);

  private static final double MIN_WIND = 0.0;
  private static final double MAX_WIND = 30.0;

  private final ControlCard card;
  private Label currentLabel;
  private Label statusLabel;
  private Label minLabel;
  private Label maxLabel;
  private Label avgLabel;
  private Label gustLabel;
  private ProgressBar windBar;


  /**
   * Creates a new WindSpeedCardController with the specified UI components.
   * This constructor wires together all the UI elements that the controller
   * will manage. The components are typically created by the WindSpeedCardBuilder
   * and passed to this controller for lifecycle management.
   *
   * @param card the main control card container
   * @param currentLabel label displaying current wind speed
   * @param statusLabel label displaying the wind status text (e.g, "Calm")
   * @param gustLabel label displaying the maximum gust speed
   * @param windBar progress bar visualizing wind speed intensity
   * @param minLabel label displaying the 24h minimum wind speed
   * @param maxLabel label displaying the 24h maximum wind speed
   * @param avgLabel label displaying the 24h average wind speed
   */
  public WindSpeedCardController(ControlCard card, Label currentLabel, Label statusLabel,
                                 Label gustLabel, ProgressBar windBar, Label minLabel, Label maxLabel,
                                 Label avgLabel) {
    this.card = card;
    this.currentLabel = currentLabel;
    this.statusLabel = statusLabel;
    this.gustLabel = gustLabel;
    this.windBar = windBar;
    this.minLabel = minLabel;
    this.maxLabel = maxLabel;
    this.avgLabel = avgLabel;
    log.debug("WindSpeedCardController wired");
  }

  /**
  * Initializes event handlers and starts any listeners required by this controller.
  */
  public void start() {
    log.info("Starting WindSpeedCardController");
    // TODO: Add initialization logic here
    log.debug("WindSpeedCardController started successfully");
  }



  /**
  * Stops this controller and cleans up resources/listeners.
  */
  public void stop() {
    log.info("Stopping WindSpeedCardController");
    // TODO: Add cleanup logic here
    log.debug("WindSpeedCardController stopped successfully");
  }

  /**
  * Updates the wind speed display and status.
  *
  * @param speed the current wind speed in m/s
  */
  public void updateWindSpeed(double speed) {
    card.setValueText(String.format("%.1f m/s", speed));
    currentLabel.setText(String.format("Current: %.1f m/s", speed));

    double progress = (speed - MIN_WIND) / (MAX_WIND - MIN_WIND);
    windBar.setProgress(Math.max(0, Math.min(1, progress)));

    updateWindStatus(speed);
    log.trace("Wind speed updated to {:.1f} m/s", speed);
  }

  /**
  * Updates the wind gust display.
  *
  * @param gust the maximum gust speed in m/s
  */
  public void updateGust(double gust) {
    gustLabel.setText(String.format("Gust: %.1f m/s", gust));
    log.trace("Wind gust updated to {:.1f} m/s", gust);
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
    minLabel.setText(String.format("Min: %.1f m/s", min));
    maxLabel.setText(String.format("Max: %.1f m/s", max));
    avgLabel.setText(String.format("Avg: %.1f m/s", avg));
    log.trace("Wind statistics updated: min={:.1f}, max={:.1f}, avg={:.1f}", min, max, avg);
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
            "wind-speed-light","wind-speed-breeze","wind-speed-moderate",
            "wind-speed-strong","wind-speed-gale");
    windBar.getStyleClass().removeAll("wind-speed-calm",
            "wind-speed-light","wind-speed-breeze","wind-speed-moderate",
            "wind-speed-strong","wind-speed-gale");

    statusLabel.getStyleClass().add(color);
    windBar.getStyleClass().add(color);

    log.trace("Wind status updated to '{}' with CSS class '{}'", status, color);
  }
}
