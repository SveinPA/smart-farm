package edu.ntnu.bidata.smg.group8.control.ui.controller.cardcontrollers;

import edu.ntnu.bidata.smg.group8.common.util.AppLogger;
import edu.ntnu.bidata.smg.group8.control.logic.command.CommandInputHandler;
import edu.ntnu.bidata.smg.group8.control.ui.view.ControlCard;
import edu.ntnu.bidata.smg.group8.control.util.UiExecutors;
import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Slider;
import javafx.scene.control.Spinner;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;



/**
* Controller class responsible for managing the "Windows" control card UI.
*
* <p>Handles switching between manual and automatic control modes,
* updating slider and preset button states, reacting to sensor inputs,
* and updating the visual state of the window control card.</p>

* @author Andrea Sandnes
* @version 28.10.2025
*/
public class WindowsCardController {
  private static final Logger log = AppLogger.get(WindowsCardController.class);

  public static final int POSITION_CLOSED = 0;
  public static final int POSITION_SLIGHT = 25;
  public static final int POSITION_HALF   = 50;
  public static final int POSITION_MOSTLY = 75;
  public static final int POSITION_OPEN   = 100;

  private int currentPosition = 0;

  private final ControlCard card;
  private RadioButton manualMode;
  private RadioButton autoMode;
  private ToggleGroup modeGroup;

  // Manual mode controls
  private Button closedButton;
  private Button slightButton;
  private Button halfButton;
  private Button mostlyButton;
  private Button openButton;
  private Slider openingSlider;
  private Label sliderLabel;

  private final VBox manualBox;
  private final VBox autoBox;

  // Auto mode controls
  private Spinner<Integer> tempSpinner;
  private Spinner<Integer> windSpinner;
  private Label autoStatusLabel;

  private Double latestTempC = null;
  private Double latestWindMs = null;

  private static final double WIND_CAUTION_FACTOR = 0.75;
  private static final int WIND_CAUTION_MAX_OPENING = POSITION_HALF;

  private ChangeListener<Object> modeListener;
  private ChangeListener<Number> sliderListener;
  private EventHandler<MouseEvent> sliderMouseReleasedHandler;
  private ChangeListener<Number> tempSpinnerListener;
  private ChangeListener<Number> windSpinnerListener;

  private CommandInputHandler cmdHandler;
  private String nodeId;
  private final String actuatorKey = "window";

  private volatile boolean suppressSend = false;
  private volatile Integer lastSentPosition = null;

  /**
   * Creates new WindowsCardController with the specified UI components.
   * This constructor wires together all UI elements that the controller
   * will manage throughout its lifecycle. The components are typically created
   * by the WindowsCardBuilder and passed to this controller for coordinated
   * updates and event handling.
   *
   * @param card the main card container
   * @param manualMode radio button for manual control mode
   * @param autoMode radio button for automatic control mode
   * @param modeGroup toggle group managing mode radio buttons
   * @param closedButton preset button for fully closed position (0%)
   * @param slightButton preset button for slightly open position (25%)
   * @param halfButton preset button for half open position (50%)
   * @param mostlyButton preset button for mostly open position (75%)
   * @param openButton preset button for fully open position (100%)
   * @param openingSlider continuous slider for custom positions (0-100%)
   * @param sliderLabel label showing current slider value
   * @param tempSpinner spinner for temperature threshold in auto mode (20-35°C)
   * @param windSpinner spinner for wind speed limit in auto mode (5-20 m/s)
   * @param autoStatusLabel label displaying automation status and reason
   * @param manualBox container for manual mode controls
   * @param autoBox container for automatic mode controls
   */
  public WindowsCardController(ControlCard card,
                               RadioButton manualMode, RadioButton autoMode, ToggleGroup modeGroup,
                               Button closedButton, Button slightButton, Button halfButton,
                               Button mostlyButton, Button openButton,
                               Slider openingSlider, Label sliderLabel,
                               Spinner<Integer> tempSpinner, Spinner<Integer> windSpinner,
                               Label autoStatusLabel, VBox manualBox,
                               VBox autoBox) {
    this.card = card;
    this.manualMode = manualMode;
    this.autoMode = autoMode;
    this.modeGroup = modeGroup;
    this.closedButton = closedButton;
    this.slightButton = slightButton;
    this.halfButton = halfButton;
    this.mostlyButton = mostlyButton;
    this.openButton = openButton;
    this.openingSlider = openingSlider;
    this.sliderLabel = sliderLabel;
    this.tempSpinner = tempSpinner;
    this.windSpinner = windSpinner;
    this.autoStatusLabel = autoStatusLabel;
    this.manualBox = manualBox;
    this.autoBox = autoBox;

    log.debug("WindowsCardController wired");
  }

  /**
  * Initializes event handlers and starts any listeners required by this controller.
  */
  public void start() {
    log.info("Starting WindowsCardController");

    modeListener = (obs, oldToggle, newToggle) -> {
      boolean manual = manualMode.isSelected();
      fx(() -> applyMode(manual));
      log.debug("Mode changed to {}", manual ? "MANUAL" : "AUTO");
      if (!manual) {
        onThresholdChanged();
      }
    };
    modeGroup.selectedToggleProperty().addListener(modeListener);

    sliderListener = (obs, oldVal, newVal) -> {
      int pos = clampToPercent(newVal.intValue());
      if (!suppressSend) {
        log.trace("Slider moved to {}%", pos);
      }
      fx(() -> applyPositionFromSlider(pos));
    };
    openingSlider.valueProperty().addListener(sliderListener);

    sliderMouseReleasedHandler = e -> {
      int finalPosition = (int) openingSlider.getValue();
      sendWindowPositionIfNeeded(finalPosition);
    };
    openingSlider.setOnMouseReleased(sliderMouseReleasedHandler);

    closedButton.setOnAction(e -> setManualPosition(POSITION_CLOSED));
    slightButton.setOnAction(e -> setManualPosition(POSITION_SLIGHT));
    halfButton.setOnAction(e -> setManualPosition(POSITION_HALF));
    mostlyButton.setOnAction(e -> setManualPosition(POSITION_MOSTLY));
    openButton.setOnAction(e -> setManualPosition(POSITION_OPEN));

    tempSpinnerListener = (o, ov, nv) -> onThresholdChanged();
    tempSpinner.valueProperty().addListener(tempSpinnerListener);

    windSpinnerListener = (o, ov, nv) -> onThresholdChanged();
    windSpinner.valueProperty().addListener(windSpinnerListener);

    fx(() -> {
      applyMode(true); // Manual by default
      applyPosition(currentPosition);
    });

    log.debug("WindowsCardController started successfully");
  }

  /**
  * Stops this controller and cleans up resources/listeners.
  */
  public void stop() {
    log.info("Stopping WindowsCardController");

    if (modeListener != null) {
      modeGroup.selectedToggleProperty().removeListener(modeListener);
      modeListener = null;
    }
    if (sliderListener != null) {
      openingSlider.valueProperty().removeListener(sliderListener);
      sliderListener = null;
    }
    if (sliderMouseReleasedHandler != null) {
      openingSlider.setOnMouseReleased(null);
      sliderMouseReleasedHandler = null;
    }
    if (tempSpinnerListener != null) {
      tempSpinner.valueProperty().removeListener(tempSpinnerListener);
      tempSpinnerListener = null;
    }
    if (windSpinnerListener != null) {
      windSpinner.valueProperty().removeListener(windSpinnerListener);
      windSpinnerListener = null;
    }

    closedButton.setOnAction(null);
    slightButton.setOnAction(null);
    halfButton.setOnAction(null);
    mostlyButton.setOnAction(null);
    openButton.setOnAction(null);

    log.debug("WindowsCardController stopped successfully");
  }

  /**
  * Updates the window position display in the UI.
  *
  * @param position the current window opening percentage (0-100)
  */
  public void updateWindowPosition(int position) {
    int clamped = clampToPercent(position);
    if (!suppressSend) {
      log.debug("Window position updated to {}%", clamped);
    }
    fx(() -> applyPosition(clamped));
  }

  /**
  * Updates the window position externally (e.g., from backend).
  * Does not trigger a new command send (prevents echo).
  *
  * @param position window opening percentage (0-100)
  */
  public void updateWindowPositionExternal(int position) {
    log.info("External window position update: {}%", position);
    Platform.runLater(() -> {
      suppressSend = true;
      updateWindowPosition(position);

      UiExecutors.schedule(() -> {
        suppressSend = false;
      }, 100, TimeUnit.MILLISECONDS);
    });
  }

  /**
  * Updates the latest temperature reading and triggers auto-mode evaluation.
  *
  * @param tempC current temperature in degrees Celsius
  */
  public void updateTemperature(double tempC) {
    this.latestTempC = tempC;
    if (latestWindMs != null) {
      updateFromSensors(latestTempC, latestWindMs);
    }
  }

  /**
   * Updates the latest wind speed reading and triggers auto-mode evaluation.
   *
   * @param windMs current wind speed in m/s
   */
  public void updateWindSpeed(double windMs) {
    this.latestWindMs = windMs;
    if (latestTempC != null) {
      updateFromSensors(latestTempC, latestWindMs);
    }
  }

  /**
   * Re-evaluates auto-mode when threshold change.
   * If missing sensor data, show passive status.
   */
  private void onThresholdChanged() {
    if (!autoMode.isSelected()) {
      return;
    }
    if (latestTempC == null || latestWindMs == null) {
      updateAutoStatus(false, "Waiting for sensor data");
      return;
    }
    updateFromSensors(latestTempC, latestWindMs);
  }



  /**
  * Processes sensor reading and updates window position in automatic mode.
  * This method implements the automatic control logic that responds to environmental
  * conditions.

  * @param currentTemp current temperature reading in degrees Celsius
  * @param currentWind current wind speed reading in m/s
  */
  public void updateFromSensors(double currentTemp, double currentWind) {
    if (!autoMode.isSelected()) {
      log.trace("Ignoring sensor update - not in auto mode");
      return;
    }

    int tempThreshold = tempSpinner.getValue();
    int windLimit = windSpinner.getValue();
    double windCautionThreshold = windLimit * WIND_CAUTION_FACTOR;

    log.debug("Processing sensor data - Temp: {}°C (threshold: {}°C), Wind: {} m/s (limit: {} m/s)",
            String.format("%.1f", currentTemp), tempThreshold,
            String.format("%.1f", currentWind), windLimit);

    int target;
    if (currentWind > windLimit) {
      target = POSITION_CLOSED;
      updateAutoStatus(true, String.format("Strong wind (%.1f m/s) - safety closure", currentWind));
    } else if (currentWind > windCautionThreshold) {
      if (currentTemp >= tempThreshold) {
        target = WIND_CAUTION_MAX_OPENING;
        updateAutoStatus(true, String.format(
                "Moderate wind (%.1f m/s) - limited opening", currentWind));
      } else {
        target = POSITION_CLOSED;
        updateAutoStatus(true, String.format("Moderate wind (%.1f m/s) - closing", currentWind));
      }
    } else if (currentTemp >= tempThreshold) {
      target = POSITION_OPEN;
      updateAutoStatus(true, "High temperature");
    } else {
      target = currentPosition;
      updateAutoStatus(false, "Within threshold");
      return;
    }
    if (target != currentPosition) {
      updateWindowPosition(target);
      sendWindowPositionIfNeeded(target);
    }
  }

  /**
  * Gets the current selected control mode.
  *
  * @return MANUAL if manual mode is selected, AUTO if auto mode is selected
  */
  public String getCurrentMode() {
    return manualMode.isSelected() ? "MANUAL" : "AUTO";
  }

  /**
  * Gets the current window opening position from the slider.
  *
  * @return the current position percentage (0-100)
  */
  public int getCurrentPosition() {
    return (int) openingSlider.getValue();
  }

  /**
  * Gets the temperature threshold for automatic window opening.
  *
  * @return the temperature threshold in degrees Celsius (°C), range 20-35
  */
  public int getTemperatureThreshold() {
    return tempSpinner.getValue();
  }

  /**
  * Gets the wind speed limit for automatic window closing.

  * @return the wind speed limit in m/s
  */
  public int getWindSpeedLimit() {
    return windSpinner.getValue();
  }

  /**
  * Updates the auto control status label.
  * This method updates the status indicator to show the current state
  * of the automation system. It is typically called when the automation
  * system takes action based on sensor readings.
  *
  * @param isActive true if auto control is actively adjusting windows
  * @param reason the reason for the current state (e.g., "High temperature", "Strong wind")
   */
  public void updateAutoStatus(boolean isActive, String reason) {
    log.debug("Auto control status update - Active: {}, Reason: {}", isActive, reason);

    fx(() -> {
      if (isActive) {
        autoStatusLabel.setText("Auto-control: " + reason);
        autoStatusLabel.getStyleClass().remove("standby");
        if (!autoStatusLabel.getStyleClass().contains("active")) {
          autoStatusLabel.getStyleClass().add("active");
        }
      } else {
        autoStatusLabel.setText("Auto-control: Standby");
        autoStatusLabel.getStyleClass().remove("active");
        if (!autoStatusLabel.getStyleClass().contains("standby")) {
          autoStatusLabel.getStyleClass().add("standby");
        }
      }
    });
  }

  /**
  * Sets a specific manual position if manual mode is active.
  *
  * @param position target window position (0-100)
  */
  private void setManualPosition(int position) {
    if (!manualMode.isSelected()) {
      log.trace("Preset ignored: not in manual mode");
      return;
    }
    if (!suppressSend) {
      log.info("Window position set to: {}%", position);
    }
    updateWindowPosition(position);
    sendWindowPositionIfNeeded(position);
  }

  /**
  * Sends window command asynchronously to avoid blocking UI thread.
  *
  * @param position window opening percentage (0-100)
  */
  private void sendWindowCommandAsync(int position) {
    UiExecutors.execute(() -> {
      try {
        log.debug("Attempting to send window command nodeId={} "
                + "position={}%", nodeId, position);
        cmdHandler.setValue(nodeId, actuatorKey, position);
        lastSentPosition = position;
        log.info("Window command sent successfully nodeId={} "
                + "position={}%", nodeId, position);
      } catch (IOException e) {
        log.error("Failed to send window command nodeId={} "
                + "position={}%", nodeId, position, e);
      }
    });
  }

  /**
  * Sends window position command if conditions are met.
  *
  * @param position target position (0-100)
  */
  private void sendWindowPositionIfNeeded(int position) {
    if (!suppressSend && cmdHandler != null && nodeId != null) {
      if (lastSentPosition != null && lastSentPosition == position) {
        log.debug("Skipping duplicate window position send ({}%)", position);
        return;
      }
      sendWindowCommandAsync(position);
    }
  }

  /**
  * Applies position changes triggered by the slider component.
  *
  * @param pos slider value in percent
  */
  private void applyPositionFromSlider(int pos) {
    applyPosition(pos);
  }

  /**
  * Applies a new window position to both UI labels and card display.
  *
  * @param pos target position in percent
  */
  private void applyPosition(int pos) {
    currentPosition = clampToPercent(pos);

    openingSlider.setValue(currentPosition);
    sliderLabel.setText("Custom: " + currentPosition + "%");

    if (currentPosition == 0) {
      card.setValueText("CLOSED");
    } else {
      card.setValueText("OPEN " + currentPosition + "%");
    }
  }

  /**
  * Switches between manual and automatic modes in the UI.
  *
  * @param manual true to activate manual controls, false to activate automatic
  */
  private void applyMode(boolean manual) {
    manualBox.setDisable(!manual);
    manualBox.setVisible(manual);
    manualBox.setManaged(manual);

    autoBox.setDisable(manual);
    autoBox.setVisible(!manual);
    autoBox.setManaged(!manual);
  }

  /**
  * Clamps a value to the valid range [0,100].
  *
  * @param v input value
  * @return clamped value
  */
  private static int clampToPercent(int v) {
    return Math.max(0, Math.min(100, v));
  }

  /**
  * Injects required dependencies for this window card controller.
  *
  * @param cmdHandler the command input handler
  * @param nodeId the node ID this controller manages
  */
  public void setDependencies(CommandInputHandler cmdHandler, String nodeId) {
    this.cmdHandler = Objects.requireNonNull(cmdHandler, "cmdHandler");
    this.nodeId = Objects.requireNonNull(nodeId, "nodeId");
    log.debug("WindowsCardController dependencies injected (nodeId={})", nodeId);
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

