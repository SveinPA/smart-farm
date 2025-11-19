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
import javafx.scene.control.Spinner;
import org.slf4j.Logger;

/**
 * Controller for the Heater control card. This controller coordinates
 * the interactions and data updates for the heater system- and is responsible
 * for logic related to heater temperature settings in the GUI.
 *
 * <p>This class handles heater temperature settings, user interactions,
 * and synchronization of status updates with the backend system.
 * It also provides warnings for potentially harmful temperature settings.</p>
 *
 * @author Andrea Sandnes & Mona Amundsen
 * @version 28.10.2025
 */
public class HeaterCardController {
  private static final Logger log = AppLogger.get(HeaterCardController.class);

  private static final int TEMP_COOL = 18;
  private static final int TEMP_MODERATE = 22;
  private static final int TEMP_WARM = 26;

  private static final int TEMP_VERY_LOW = 10;
  private static final int TEMP_LOW = 15;
  private static final int TEMP_HIGH = 30;
  private static final int TEMP_VERY_HIGH = 35;

  private final ControlCard card;
  private Spinner<Integer> tempSpinner;
  private Button applyButton;
  private Button coolButton;
  private Button moderateButton;
  private Button warmButton;
  private Button offButton;

  private boolean isOn = false;
  private Integer currentTargetTemp = null;

  private ChangeListener<Integer> spinnerChangeListener;
  private EventHandler<ActionEvent> applyHandler;
  private EventHandler<ActionEvent> coolHandler;
  private EventHandler<ActionEvent> moderateHandler;
  private EventHandler<ActionEvent> warmHandler;
  private EventHandler<ActionEvent> offHandler;

  private CommandInputHandler cmdHandler;
  private ControlPanelController controller;
  private final String actuatorKey = "heater";

  private volatile boolean suppressSend = false;
  private volatile Integer lastSentTemp = null;

  /**
   * Creates a new HeaterCardController with the specified UI components.
   *
   * @param card the main card container
   * @param tempSpinner spinner for setting custom temperature
   * @param applyButton button to apply custom temperature
   * @param coolButton button for cool preset (18°C)
   * @param moderateButton button for moderate preset (22°C)
   * @param warmButton button for warm preset (26°C)
   * @param offButton button to turn heater off
   */
  public HeaterCardController(ControlCard card,
                              Spinner<Integer> tempSpinner, Button applyButton,
                              Button coolButton, Button moderateButton,
                              Button warmButton, Button offButton) {
    this.card = card;
    this.tempSpinner = tempSpinner;
    this.applyButton = applyButton;
    this.coolButton = coolButton;
    this.moderateButton = moderateButton;
    this.warmButton = warmButton;
    this.offButton = offButton;

    log.debug("HeaterCardController wired");
  }

  /**
   * Initializes event handlers and starts any listeners required by this controller.
   *
   * <p>This method sets up the necessary event handlers for user interactions
   * with the heater control card, including temperature adjustments and preset buttons.</p>
   */
  public void start() {
    log.info("Starting HeaterCardController");

    spinnerChangeListener = (obs, oldVal, newVal) -> {
      if (oldVal != null && newVal != null && !oldVal.equals(newVal)) {
        log.trace("Temperature spinner value changed: {}°C -> {}°C", oldVal, newVal);
      }
    };
    tempSpinner.valueProperty().addListener(spinnerChangeListener);
    log.debug("HeaterCardController started successfully");

    applyHandler = e -> {
      int temperature = tempSpinner.getValue();
      setTargetTemperature(temperature, "Manual");
    };

    applyButton.setOnAction(applyHandler);

    coolHandler = e -> setTargetTemperature(TEMP_COOL, "Preset Cool");
    coolButton.setOnAction(coolHandler);

    moderateHandler = e -> setTargetTemperature(TEMP_MODERATE, "Preset Moderate");
    moderateButton.setOnAction(moderateHandler);

    warmHandler = e -> setTargetTemperature(TEMP_WARM, "Preset Warm");
    warmButton.setOnAction(warmHandler);

    offHandler = e -> turnOff();
    offButton.setOnAction(offHandler);

    log.debug("HeaterCardController started successfully");
  }

  /**
   * Stops this controller and cleans up resources/listeners.
   *
   * <p>This method removes all event handlers and listeners
   * to prevent memory leaks and unintended behavior when the controller
   * is no longer needed.</p>
   */
  public void stop() {
    log.info("Stopping HeaterCardController");

    if (spinnerChangeListener != null) {
      tempSpinner.valueProperty().removeListener(spinnerChangeListener);
      spinnerChangeListener = null;
    }

    if (applyHandler != null) {
      applyButton.setOnAction(null);
      applyHandler = null;
    }

    if (coolHandler != null) {
      coolButton.setOnAction(null);
      coolHandler = null;
    }

    if (moderateHandler != null) {
      moderateButton.setOnAction(null);
      moderateHandler = null;
    }

    if (warmHandler != null) {
      warmButton.setOnAction(null);
      warmHandler = null;
    }

    if (offHandler != null) {
      offButton.setOnAction(null);
      offHandler = null;
    }

    log.debug("HeaterCardController stopped successfully");
  }

  /**
   * Sets the target temperature and updates UI.
   *
   * <p>This method updates the UI to reflect the new target temperature,
   * and sends the command to the backend system if applicable.</p>
   *
   * @param temperature the target temperature in Celsius
   */
  private void setTargetTemperature(int temperature, String source) {
    if (!suppressSend) {
      log.info("Heater target temperature set to {}°C (Source: {})", temperature, source);
    }

    fx(() -> {
      tempSpinner.getValueFactory().setValue(temperature);
      card.setValueText("ON (" + temperature + "°C)");

      boolean wasOff = !isOn;
      isOn = true;
      currentTargetTemp = temperature;

      if (!suppressSend) {
        if (wasOff) {
          log.info("Heater turned ON - Target: {}°C", temperature);
        } else {
          log.debug("Heater target adjusted: {}°C", temperature);
        }
        checkTemperatureWarnings(temperature);
      }
    });

    if (!suppressSend && cmdHandler != null && controller != null) {
      if (lastSentTemp != null && lastSentTemp.equals(temperature)) {
        log.debug("Skipping duplicate heater temperature send ({}°C)", temperature);
        return;
      }
      sendHeaterCommandAsync(temperature);
    }
  }

  /**
   * Turns off the heater and updates UI.
   *
   * <p>This method updates the UI to reflect that the heater is turned off,
   * and sends the OFF command to the backend system if applicable.</p>
   */
  private void turnOff() {
    if (!suppressSend) {
      log.info("Heater turned OFF");
    }

    fx(() -> {
      card.setValueText("OFF");

      if (!suppressSend && isOn) {
        log.debug("Heater deactivated - Previous target: {}°C",
                currentTargetTemp != null ? currentTargetTemp : "N/A");
      }

      isOn = false;
      currentTargetTemp = null;
    });

    if (!suppressSend && cmdHandler != null && controller != null) {
      if (lastSentTemp != null && lastSentTemp == 0) {
        log.debug("Skipping duplicate heater OFF send");
        return;
      }
      sendHeaterCommandAsync(0); // 0 = OFF
    }
  }

  /**
   * Sends heater command asynchronously to avoid blocking UI thread.
   *
   * <p>This method retrieves the selected node ID from the controller
   * and sends the heater command with the specified temperature
   * to the backend system using the command handler.</p>
   *
   * @param temperature target temperature (0 = OFF)
   */
  private void sendHeaterCommandAsync(int temperature) {
    UiExecutors.execute(() -> {
      try {
        String nodeId = controller != null ? controller.getSelectedNodeId() : null;
        if (nodeId == null) {
          log.warn("Cannot send heater command: no node selected");
          return;
        }

        log.debug("Attempting to send heater command nodeId={} temp={}°C", nodeId, temperature);
        cmdHandler.setValue(nodeId, actuatorKey, temperature);
        lastSentTemp = temperature;
        log.info("Heater command sent successfully nodeId={} temp={}°C", nodeId, temperature);
      } catch (IOException e) {
        log.error("Failed to send heater command temp={}°C", temperature, e);
      }
    });
  }

  /**
   * Checks for temperature warnings and logs them.
   *
   * @param temperature the target temperature
   */
  private void checkTemperatureWarnings(int temperature) {
    if (temperature <= TEMP_VERY_LOW) {
      log.warn("CAUTION: Very low target temperature ({}°C)"
                      + " - Risk of plant damage from cold stress",
              temperature);
    } else if (temperature < TEMP_LOW) {
      log.info("NOTICE: Low target temperature ({}°C)"
                      + " - Suitable for cool-season crops only",
              temperature);
    } else if (temperature >= TEMP_VERY_HIGH) {
      log.warn("CAUTION: Very high target temperature ({}°C)"
                      + " - Risk of plant stress and excessive energy use",
              temperature);
    } else if (temperature > TEMP_HIGH) {
      log.info("NOTICE: High target temperature ({}°C)"
                      + " - Monitor plant conditions closely",
              temperature);
    }
  }

  /**
   * Updates the heater temperature externally.
   *
   * <p>This method is called when an external update for the heater
   * temperature is received from the backend system. It updates the UI
   * accordingly without sending a command back to the backend.</p>
   *
   * @param temperature target temperature in Celsius (0 = OFF)
   */
  public void updateHeaterTemperature(int temperature) {
    log.info("External heater temperature update: {}°C", temperature);
    Platform.runLater(() -> {
      suppressSend = true;
      if (temperature > 0) {
        setTargetTemperature(temperature, "External");
      } else {
        turnOff();
      }
      UiExecutors.schedule(() -> {
        suppressSend = false;
      }, 100, TimeUnit.MILLISECONDS);
    });
  }

  /**
   * Ensures the given runnable executes on the JavaFX Application Thread.
   *
   * <p>If already on the FX thread, the runnable is executed immediately.
   * Otherwise, it is scheduled to run later on the FX thread.</p>
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
