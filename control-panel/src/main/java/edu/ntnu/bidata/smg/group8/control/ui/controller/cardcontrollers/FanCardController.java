package edu.ntnu.bidata.smg.group8.control.ui.controller.cardcontrollers;

import edu.ntnu.bidata.smg.group8.common.util.AppLogger;
import edu.ntnu.bidata.smg.group8.control.logic.command.CommandInputHandler;
import edu.ntnu.bidata.smg.group8.control.ui.controller.ControlPanelController;
import edu.ntnu.bidata.smg.group8.control.ui.view.ControlCard;
import edu.ntnu.bidata.smg.group8.control.util.UiExecutors;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Slider;
import javafx.scene.control.Spinner;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import org.slf4j.Logger;

/**
 * Controller for the Fan control card.
 * This controller coordinates the interaction between the FanCardBuilder UI
 * and the underlying logic it is responsible for.
 *
 * <p>The FanCardController manages both manual and automatic fan speed control modes.
 * In manual mode, users can set the fan speed directly using preset buttons
 * or a custom slider. In automatic mode, the controller adjusts the fan speed
 * based on temperature and humidity thresholds defined by the user.
 * It also handles communication with the backend system to send fan speed commands
 * and receive updates.</p>

 * @author Andrea Sandnes & Mona Amundsen
 * @version 28.10.2025
 */
public class FanCardController {
  private static final Logger log = AppLogger.get(FanCardController.class);

  private static final int SPEED_LOW = 25;
  private static final int SPEED_MEDIUM = 50;
  private static final int SPEED_HIGH = 75;
  private static final int SPEED_FULL = 100;

  private final ControlCard card;
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
  private final Slider autoIntensitySlider;
  private final Label autoIntensityLabel;

  private int currentSpeed = 0;
  private boolean isManualMode = true;

  private double currentTemperature = 20.0;
  private double currentHumidity = 50.0;
  private int autoIntensity = 0;

  private ChangeListener<Toggle> modeChangeListener;
  private ChangeListener<Number> sliderChangeListener;
  private ChangeListener<Integer> tempSpinnerListener;
  private ChangeListener<Integer> humiditySpinnerListener;
  private EventHandler<ActionEvent> lowHandler;
  private EventHandler<ActionEvent> mediumHandler;
  private EventHandler<ActionEvent> highHandler;
  private EventHandler<ActionEvent> fullHandler;
  private EventHandler<ActionEvent> offHandler;
  private ChangeListener<Number> autoIntensityListener;

  private CommandInputHandler cmdHandler;
  private ControlPanelController controller;
  private final String actuatorKey = "fan";

  private volatile boolean suppressSend = false;
  private volatile Integer lastSentSpeed = null;

  /**
   * Creates a new FanCardController with the specified UI components.
   *
   * @param card the main card container
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
   * @param autoIntensitySlider slider for auto mode fan intensity
   * @param autoIntensityLabel label displaying auto intensity value
   */
  public FanCardController(ControlCard card, // Label speedLabel,
                           RadioButton manualMode, RadioButton autoMode,
                           ToggleGroup modeGroup, Button lowButton,
                           Button mediumButton, Button highButton,
                           Button fullButton, Button offButton,
                           Slider speedSlider, Label sliderLabel,
                           Spinner<Integer> tempSpinner,
                           Spinner<Integer> humiditySpinner,
                           Label autoStatusLabel,
                           Slider autoIntensitySlider,
                           Label autoIntensityLabel
  ) {
    this.card = card;
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
    this.autoIntensitySlider = autoIntensitySlider;
    this.autoIntensityLabel = autoIntensityLabel;
    this.autoStatusLabel = autoStatusLabel;

    log.debug("FanCardController wired");
  }

  /**
   * Initializes event handlers and starts any listeners required by this controller.
   *
   * <p>This method sets up listeners for mode changes, slider adjustments, and button
   * actions. It ensures that the UI components are responsive
   * to user interactions and that the fan speed is updated accordingly.</p>
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
        updateAutoStatus("Active", true);
        evaluateAutoMode();
      }
    };
    modeGroup.selectedToggleProperty().addListener(modeChangeListener);

    sliderChangeListener = (obs, oldVal, newVal) -> {
      int value = newVal.intValue();
      sliderLabel.setText("Custom: " + value + "%");
    };
    speedSlider.valueProperty().addListener(sliderChangeListener);

    speedSlider.setOnMouseReleased(e -> {
      int finalValue = (int) speedSlider.getValue();
      setFanSpeed(finalValue);
    });

    // Manual mode buttons
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

    // Auto mode
    autoIntensityListener = (obs, oldVal, newVal) -> {
      int intensity = newVal.intValue();
      autoIntensity = intensity;
      autoIntensityLabel.setText("Fan intensity: " + intensity + "%");
      log.info("Auto mode fan intensity set to: {}%", intensity);
    };
    autoIntensitySlider.valueProperty().addListener(autoIntensityListener);

    tempSpinnerListener = (obs, oldVal, newVal) -> {
      if (oldVal != null && newVal != null && !oldVal.equals(newVal)) {
        log.info("Auto mode temperature threshold changed: {}°C -> {}°C", oldVal, newVal);
        if (!isManualMode) {
          evaluateAutoMode();
        }
      }
    };

    tempSpinner.valueProperty().addListener(tempSpinnerListener);

    humiditySpinnerListener = (obs, oldVal, newVal) -> {
      if (oldVal != null && newVal != null && !oldVal.equals(newVal)) {
        log.info("Auto mode humidity threshold changed: {}% -> {}%", oldVal, newVal);
        if (!isManualMode) {
          evaluateAutoMode();
        }
      }
    };
    humiditySpinner.valueProperty().addListener(humiditySpinnerListener);

    log.debug("FanCardController started successfully - Mode: MANUAL, Speed: {}%", currentSpeed);
  }


  /**
   * Stops this controller and cleans up resources/listeners.
   *
   * <p>This method removes all event listeners and handlers
   * that were set up during the start() method. It ensures that no memory leaks occur
   * and that the controller can be safely discarded or restarted.</p>
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

    if (autoIntensityListener != null) {
      autoIntensitySlider.valueProperty().removeListener(autoIntensityListener);
      autoIntensityListener = null;
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

    log.debug("FanCardController stopped successfully");
  }

  /**
   * Sets the fan speed and updates all related UI elements.
   *
   * @param speed the speed percentage (0-100)
   */
  private void setFanSpeed(int speed) {
    final int s = Math.max(0, Math.min(100, speed));

    if (suppressSend) {
      log.debug("Fan speed set internally to: {}% (external update, no send)", s);
    } else {
      log.info("Fan speed set to: {}%", s);
    }

    fx(() -> {
      currentSpeed = s;
      speedSlider.setValue(s);
      updateCardValue(s);
    });

    if (!suppressSend && cmdHandler != null && controller != null) {
      if (lastSentSpeed != null && lastSentSpeed == s) {
        log.debug("Skipping duplicate fan speed send ({}%)", s);
        return;
      }
      sendFanSpeedAsync(s);
    }
  }

  /**
   * This method sends a fan speed command asynchronously to the connected node.
   * It creates a new background thread to avoid blocking the JavaFX UI thread
   * during network or I/O operations. It communicates the desired fan speed
   * to the system through CommandInputHandler.
   *
   * @param speed the fan speed percentage (0–100) to send to the backend node
   */
  private void sendFanSpeedAsync(int speed) {
    UiExecutors.execute(() -> {
      try {
        String nodeId = controller != null ? controller.getSelectedNodeId() : null;
        if (nodeId == null) {
          log.warn("Cannot send fan speed command: no node selected");
          return;
        }

        log.debug("Attempting to send fan speed command nodeId={} speed={}", nodeId, speed);
        cmdHandler.setValue(nodeId, actuatorKey, speed);
        lastSentSpeed = speed;
        log.info("Fan speed command sent successfully nodeId={} speed={}", nodeId, speed);
      } catch (IOException e) {
        log.error("Failed to send fan speed command speed={}", speed, e);
      }
    });
  }

  /**
   * Updates the card's main value text based on speed.
   *
   * <p>This method translates the numeric speed value into a human-readable
   * status text (e.g., OFF, LOW, MEDIUM, HIGH, FULL) and updates
   * the ControlCard display accordingly.</p>
   *
   * @param speed the speed percentage (0-100)
   */
  private void updateCardValue(int speed) {
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
   * <p>This method is used to reflect changes in fan speed
   * that originate outside of the manual controls, such as automatic
   * adjustments based on sensor data. It temporarily suppresses sending commands
   * back to the backend to avoid feedback loops.</p>
   *
   * @param speed the speed percentage (0-100)
   */
  public void updateFanSpeed(int speed) {
    log.info("External fan speed update: {}%", speed);
    Platform.runLater(() -> {
      suppressSend = true;
      setFanSpeed(speed);

      UiExecutors.schedule(() -> {
        suppressSend = false;
      }, 100, TimeUnit.MILLISECONDS);
    });
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
   * Ensures the given runnable executes on the JavaFX Application Thread.
   *
   * <p>If the current thread is the FX Application Thread,
   * the runnable is executed immediately. Otherwise, it is scheduled
   * to run later on the FX thread.</p>
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

  /**
   * Evaluates the conditions for auto mode and adjusts fan speed accordingly.
   * This method checks the current temperature and humidity against
   *
   * <p>the user-defined thresholds. If either condition is met,
   * the fan speed is set to the auto intensity level.
   * If neither condition is met, the fan is turned off.</p>
   */
  private void evaluateAutoMode() {
    if (isManualMode) {
      return;
    }

    // Dont evaluate if sensor data is not available
    if (currentTemperature < 0 || currentHumidity < 0) {
      log.debug("Waiting for sensor data before evaluating auto mode");
      updateAutoStatus("Waiting for sensor data", false);
      return;
    }

    int tempThreshold = tempSpinner.getValue();
    int humidityThreshold = humiditySpinner.getValue();

    boolean tempConditionMet = currentTemperature >= tempThreshold;
    boolean humidityConditionMet = currentHumidity >= humidityThreshold;

    if (tempConditionMet || humidityConditionMet) {
      log.info("Auto mode conditions met - Temp: {}°C (threshold: {}°C), "
                      + "Humidity: {}% (threshold: {})",
              currentTemperature, tempThreshold, currentHumidity, humidityThreshold);
      setFanSpeed(autoIntensity);
      updateAutoStatus("Active - Fan ON", true);
    } else {
      log.info("Auto mode condition not met - Fan OFF");
      setFanSpeed(0);
      updateAutoStatus("Active - Conditions not met", true);
    }
  }

  /**
   * Updates the current humidity reading.
   *
   * @param humidity the new humidity value to set
   */
  public void updateHumidity(double humidity) {
    this.currentHumidity = humidity;
    log.debug("Humidity updated: {}%", humidity);

    if (!isManualMode) {
      evaluateAutoMode();
    }
  }

  /**
   * Updates the current temperature reading.
   *
   * @param temperature the new temperature value to set
   */
  public void updateTemperature(double temperature) {
    this.currentTemperature = temperature;
    log.debug("Temperature updated: {}°C", temperature);

    if (!isManualMode) {
      evaluateAutoMode();
    }
  }
}
