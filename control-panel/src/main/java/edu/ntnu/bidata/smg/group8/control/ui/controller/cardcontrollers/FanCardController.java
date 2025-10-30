package edu.ntnu.bidata.smg.group8.control.ui.controller.cardcontrollers;

import edu.ntnu.bidata.smg.group8.common.util.AppLogger;
import edu.ntnu.bidata.smg.group8.control.ui.view.ControlCard;
import edu.ntnu.bidata.smg.group8.control.ui.view.cards.FanCardBuilder;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;

/**
* Controller for the Fan control card.
* This controller coordinates the interaction between the FanCardBuilder UI
* and the underlying logic it is responsible for.

* @author Andrea Sandnes
* @version 28.10.2025
*/
public class FanCardController {
  private static final Logger log = AppLogger.get(FanCardController.class);

  private static final int SPEED_LOW = 25;
  private static final int SPEED_MEDIUM = 50;
  private static final int SPEED_HIGH = 75;
  private static final int SPEED_FULL = 100;

  private static final int DEFAULT_TEMP_THRESHOLD = 26;
  private static final int DEFAULT_HUMIDITY_THRESHOLD = 75;

  private final ControlCard card;
  private final Label speedLabel;
  private final RadioButton manualMode;
  private final RadioButton autoMode;
  private final ToggleGroup modeGroup;
  private final Button lowButton;
  private final Button mediumButton;
  private final Button highButton;
  private final Button fullButton;
  private final Button offButton;
  private final Slider speedSlider;
  private final Label sliderLabel;
  private final Spinner<Integer> tempSpinner;
  private final Spinner<Integer> humiditySpinner;
  private final Label autoStatusLabel;
  private final Button scheduleButton;

  private int currentSpeed = 0;
  private boolean isManualMode = true;

  private ChangeListener<Toggle> modeChangeListener;
  private ChangeListener<Number> sliderChangeListener;
  private ChangeListener<Integer> tempSpinnerListener;
  private ChangeListener<Integer> humiditySpinnerListener;
  private EventHandler<ActionEvent> lowHandler;
  private EventHandler<ActionEvent> mediumHandler;
  private EventHandler<ActionEvent> highHandler;
  private EventHandler<ActionEvent> fullHandler;
  private EventHandler<ActionEvent> offHandler;

  /**
   * Creates a new FanCardController with the specified UI components.
   *
   * @param card the main card container
   * @param speedLabel label displaying current fan speed
   * @param manualMode radio button for manual mode
   * @param autoMode radio button for automatic mode
   * @param modeGroup toggle group for mode selection
   * @param lowButton button for low speed preset (25%)
   * @param mediumButton button for medium speed preset (50%)
   * @param highButton button for high speed preset (75%)
   * @param fullButton button for full speed preset (100%)
   * @param offButton button to turn fan off
   * @param speedSlider slider for custom speed control
   * @param sliderLabel label displaying slider value
   * @param tempSpinner spinner for temperature threshold in auto mode
   * @param humiditySpinner spinner for humidity threshold in auto mode
   * @param autoStatusLabel label displaying auto mode status
   * @param scheduleButton button to access scheduling configuration
   */
  public FanCardController(ControlCard card, Label speedLabel,
                           RadioButton manualMode, RadioButton autoMode,
                           ToggleGroup modeGroup, Button lowButton,
                           Button mediumButton, Button highButton,
                           Button fullButton, Button offButton,
                           Slider speedSlider, Label sliderLabel,
                           Spinner<Integer> tempSpinner,
                           Spinner<Integer> humiditySpinner,
                           Label autoStatusLabel, Button scheduleButton) {
    this.card = card;
    this.speedLabel = speedLabel;
    this.manualMode = manualMode;
    this.autoMode = autoMode;
    this.modeGroup = modeGroup;
    this.lowButton = lowButton;
    this.mediumButton = mediumButton;
    this.highButton = highButton;
    this.fullButton = fullButton;
    this.offButton = offButton;
    this.speedSlider = speedSlider;
    this.sliderLabel = sliderLabel;
    this.tempSpinner = tempSpinner;
    this.humiditySpinner = humiditySpinner;
    this.autoStatusLabel = autoStatusLabel;
    this.scheduleButton = scheduleButton;

    log.debug("FanCardController wired");
  }

  /**
  * Initializes event handlers and starts any listeners required by this controller.
  */
  public void start() {
    log.info("Starting FanCardController");

    modeChangeListener = (obs, oldToggle, newToggle) -> {
      if (newToggle == manualMode) {
        isManualMode = true;
        log.info("Fan mode changed to: MANUAL");
      } else if (newToggle == autoMode) {
        isManualMode = false;
        log.info("Fan mode changed to: AUTO");
      }
    };
    modeGroup.selectedToggleProperty().addListener(modeChangeListener);

    sliderChangeListener = (obs, oldVal, newVal) -> {
      int value = newVal.intValue();
      sliderLabel.setText("Custom: " + value + "%");
      setFanSpeed(value);
    };
    speedSlider.valueProperty().addListener(sliderChangeListener);

    lowHandler = e -> setFanSpeed(SPEED_LOW);
    lowButton.setOnAction(lowHandler);

    mediumHandler = e -> setFanSpeed(SPEED_MEDIUM);
    mediumButton.setOnAction(mediumHandler);

    highHandler = e -> setFanSpeed(SPEED_HIGH);
    highButton.setOnAction(highHandler);

    fullHandler = e -> setFanSpeed(SPEED_FULL);
    fullButton.setOnAction(fullHandler);

    offHandler = e -> setFanSpeed(0);
    offButton.setOnAction(offHandler);

    tempSpinnerListener = (obs, oldVal, newVal) -> {
      if (oldVal != null && newVal != null && !oldVal.equals(newVal)) {
        log.info("Auto mode temperature threshold changed: {}°C -> {}°C", oldVal, newVal);
      }
    };
    tempSpinner.valueProperty().addListener(tempSpinnerListener);

    humiditySpinnerListener = (obs, oldVal, newVal) -> {
      if (oldVal != null && newVal != null && !oldVal.equals(newVal)) {
        log.info("Auto mode humidity threshold changed: {}% -> {}%", oldVal, newVal);
      }
    };
    humiditySpinner.valueProperty().addListener(humiditySpinnerListener);

    scheduleButton.setOnAction(e -> {
      log.info("Schedule button clicked (not implemented)");
      // TODO: Open scheduling dialog
    });

    log.debug("FanCardController started successfully - Mode: MANUAL, Speed: {}%", currentSpeed);
  }


  /**
  * Stops this controller and cleans up resources/listeners.
  */
  public void stop() {
    log.info("Stopping FanCardController");

    if (modeChangeListener != null) {
      modeGroup.selectedToggleProperty().removeListener(modeChangeListener);
      modeChangeListener = null;
    }

    if (sliderChangeListener != null) {
      speedSlider.valueProperty().removeListener(sliderChangeListener);
      sliderChangeListener = null;
    }

    if (tempSpinnerListener != null) {
      tempSpinner.valueProperty().removeListener(tempSpinnerListener);
      tempSpinnerListener = null;
    }

    if (humiditySpinnerListener != null) {
      humiditySpinner.valueProperty().removeListener(humiditySpinnerListener);
      humiditySpinnerListener = null;
    }

    // Clean up button handlers
    if (lowHandler != null) {
      lowButton.setOnAction(null);
      lowHandler = null;
    }

    if (mediumHandler != null) {
      mediumButton.setOnAction(null);
      mediumHandler = null;
    }

    if (highHandler != null) {
      highButton.setOnAction(null);
      highHandler = null;
    }

    if (fullHandler != null) {
      fullButton.setOnAction(null);
      fullHandler = null;
    }

    if (offHandler != null) {
      offButton.setOnAction(null);
      offHandler = null;
    }

    scheduleButton.setOnAction(null);

    log.debug("FanCardController stopped successfully");
  }

  /**
   * Sets the fan speed and updates all related UI elements.
   *
   * @param speed the speed percentage (0-100)
   */
  private void setFanSpeed(int speed) {
    log.info("Fan speed set to: {}%", speed);

    fx(() -> {
      currentSpeed = speed;
      speedSlider.setValue(speed);
      updateCardValue(speed);
    });

    // TODO: Send command to backend
  }

  /**
   * Updates the card's main value text based on speed.
   *
   * @param speed the speed percentage (0-100)
   */
  private void updateCardValue(int speed) {
    speedLabel.setText("Speed: " + speed + "%");

    String statusText;
    if (speed == 0) {
      statusText = "OFF";
    } else if (speed <= 25) {
      statusText = "LOW (" + speed + "%)";
    } else if (speed <= 50) {
      statusText = "MEDIUM (" + speed + "%)";
    } else if (speed <= 75) {
      statusText = "HIGH (" + speed + "%)";
    } else {
      statusText = "FULL (" + speed + "%)";
    }

    card.setValueText(statusText);

    log.trace("Fan status updated: {}", statusText);
  }

  /**
   * Updates the fan speed externally (e.g., from auto mode or backend).
   *
   * @param speed the speed percentage (0-100)
   */
  public void updateFanSpeed(int speed) {
    log.debug("External fan speed update: {}%", speed);
    setFanSpeed(speed);
  }

  /**
   * Updates the auto mode status display.
   *
   * @param status the status message to display
   * @param isActive true if auto control is active, false otherwise
   */
  public void updateAutoStatus(String status, boolean isActive) {
    log.debug("Auto mode status update: {} (Active: {})", status, isActive);

    fx(() -> {
      autoStatusLabel.setText("Auto-control: " + status);
      String color = isActive ? "#4caf50" : "#ff9800";
      autoStatusLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: " + color + ";");
    });
  }

  /**
   * Gets the current fan speed.
   *
   * @return the current speed percentage (0-100)
   */
  public int getCurrentSpeed() {
    return currentSpeed;
  }

  /**
   * Gets whether the fan is in manual mode.
   *
   * @return true if in manual mode, false if in auto mode
   */
  public boolean isManualMode() {
    return isManualMode;
  }

  /**
   * Gets the current temperature threshold for auto mode.
   *
   * @return the temperature threshold in Celsius
   */
  public int getTempThreshold() {
    return tempSpinner.getValue();
  }

  /**
   * Gets the current humidity threshold for auto mode.
   *
   * @return the humidity threshold percentage
   */
  public int getHumidityThreshold() {
    return humiditySpinner.getValue();
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
