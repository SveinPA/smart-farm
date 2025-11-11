package edu.ntnu.bidata.smg.group8.control.ui.controller.cardcontrollers;

import edu.ntnu.bidata.smg.group8.common.util.AppLogger;
import edu.ntnu.bidata.smg.group8.control.logic.command.CommandInputHandler;
import edu.ntnu.bidata.smg.group8.control.ui.view.ControlCard;
import edu.ntnu.bidata.smg.group8.control.ui.view.cards.HeaterCardBuilder;
import java.io.IOException;
import java.util.Objects;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import org.slf4j.Logger;


/**
* Controller for the Heater control card.
*  Handles heater state (ON/OFF) and target temperature settings.

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
  private String nodeId;
  private final String actuatorKey = "heater";

  private volatile boolean suppressSend = false;
  private Integer lastSentTemp = null;


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

    if (!suppressSend && cmdHandler != null && nodeId != null) {
      if (lastSentTemp != null && lastSentTemp.equals(temperature)) {
        log.debug("Skipping duplicate heater temperature send ({}°C)", temperature);
        return;
      }
      sendHeaterCommandAsync(temperature);
    }
  }

  /**
  * Turns off the heater and updates UI.
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

    if (!suppressSend && cmdHandler != null && nodeId != null) {
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
  * @param temperature target temperature (0 = OFF)
  */
  private void sendHeaterCommandAsync(int temperature) {
    new Thread(() -> {
      try {
        log.debug("Attempting to send heater command nodeId={} temp={}°C", nodeId, temperature);
        cmdHandler.setValue(nodeId, actuatorKey, temperature);
        lastSentTemp = temperature;
        log.info("Heater command sent successfully nodeId={} temp={}°C", nodeId, temperature);
      } catch (IOException e) {
        log.error("Failed to send heater command nodeId={} temp={}°C", nodeId, temperature, e);
      }
    }, "heater-cmd-send").start();
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
      new Thread(() -> {
        try {
          Thread.sleep(100);
        } catch (InterruptedException ignored) {}
        suppressSend = false;
      }).start();
    });
  }

  /**
  * Determines the source of the temperature setting.
  *
  * @param temperature the temperature value
  * @return description of the source (Preset name or Manual)
  */
  private String getTemperatureSource(int temperature) {
    if (temperature == TEMP_COOL) {
      return "Preset Cool";
    } else if (temperature == TEMP_MODERATE) {
      return "Preset Moderate";
    } else if (temperature == TEMP_WARM) {
      return "Preset Warm";
    } else {
      return "Manual";
    }
  }

  /**
  * Gets the current heater state.
  *
  * @return true if heater is ON, false if OFF
  */
  public boolean isHeaterOn() {
    return isOn;
  }

  /**
  * Gets the current target temperature.
  *
  * @return target temperature in Celsius, or null if heater is OFF
  */
  public Integer getCurrentTargetTemp() {
    return currentTargetTemp;
  }

  /**
  * Injects required dependencies for this heater card controller.
  *
  * @param cmdHandler the command input handler
  * @param nodeId the node ID this controller manages
  */
  public void setDependencies(CommandInputHandler cmdHandler, String nodeId) {
    this.cmdHandler = Objects.requireNonNull(cmdHandler, "cmdHandler");
    this.nodeId = Objects.requireNonNull(nodeId, "nodeId");
    log.debug("HeaterCardController dependencies injected (nodeId={})", nodeId);
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
