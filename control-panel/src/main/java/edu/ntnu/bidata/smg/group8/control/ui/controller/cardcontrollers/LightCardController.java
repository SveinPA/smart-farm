package edu.ntnu.bidata.smg.group8.control.ui.controller.cardcontrollers;

import edu.ntnu.bidata.smg.group8.common.util.AppLogger;
import edu.ntnu.bidata.smg.group8.control.logic.command.CommandInputHandler;
import edu.ntnu.bidata.smg.group8.control.ui.view.ControlCard;
import java.io.IOException;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Slider;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;



/**
* Controller for the Light control card.
* Handles light state (ON/OFF), intensity adjustment, and ambient light monitoring.

* @author Andrea Sandnes
* @version 28.10.2025
*/
public class LightCardController {
  private static final Logger log = AppLogger.get(LightCardController.class);

  private final ControlCard card;

  private Label ambientLabel;
  private RadioButton onButton;
  private RadioButton offButton;
  private ToggleGroup stateGroup;
  private Slider intensitySlider;
  private Label intensityLabel;
  private VBox intensityBox;
  private Button scheduleButton;

  private EventHandler<ActionEvent> onButtonHandler;
  private EventHandler<javafx.event.ActionEvent> offButtonHandler;
  private ChangeListener<Toggle> stateChangeListener;
  private ChangeListener<Number> intensityChangeListener;

  private CommandInputHandler cmdHandler;
  private String nodeId;


  /**
  * Creates new LightCardController with the specified UI components.
  *
  * @param card the main card container
  * @param ambientLabel label displaying ambient light level in lux
  * @param onButton radio button to turn lights ON
  * @param offButton radio button to turn lights OFF
  * @param stateGroup toggle group for ON/OFF state
  * @param intensitySlider slider controlling light intensity (0-100%)
  * @param intensityLabel label displaying current intensity percentage
  * @param intensityBox container for intensity controls
  * @param scheduleButton button to access scheduling configuration
  */
  public LightCardController(ControlCard card, Label ambientLabel, RadioButton onButton,
                             RadioButton offButton, ToggleGroup stateGroup, Slider intensitySlider,
                             Label intensityLabel, VBox intensityBox, Button scheduleButton) {
    this.card = card;
    this.ambientLabel = ambientLabel;
    this.onButton = onButton;
    this.offButton = offButton;
    this.stateGroup = stateGroup;
    this.intensitySlider = intensitySlider;
    this.intensityLabel = intensityLabel;
    this.intensityBox = intensityBox;
    this.scheduleButton = scheduleButton;

    log.debug("LightCardController wired");
  }

  /**
  * Injects backend dependencies into this controller.
  *
  * @param cmdHandler the command handler for sending commands
  * @param nodeId the node ID this controller manages
  */
  public void setDependencies(CommandInputHandler cmdHandler, String nodeId) {
    this.cmdHandler = cmdHandler;
    this.nodeId = nodeId;
    log.debug("LightCardController dependencies injected (nodeId={})", nodeId);
  }

  /**
  * Initializes event handlers and starts any listeners required by this controller.
  */
  public void start() {
    log.info("Starting LightCardController");

    // Artificial light
    onButtonHandler = e -> {
      int intensity = (int) intensitySlider.getValue();

      fx(() -> {
        card.setValueText("On (" + intensity + "%)");
        log.debug("Artificial lights turned ON at {}%", intensity);
      });

      if (cmdHandler != null && nodeId != null) {
        try {
          cmdHandler.setValue(nodeId, "artificial_light", intensity);
          log.info("Artificial light ON command sent (nodeId={}, "
                  + "intensity={}%)", nodeId, intensity);
        } catch (IOException ex) {
          log.error("Failed to send artificial light ON command (nodeId={})", nodeId, ex);
        }
      }
    };
    onButton.setOnAction(onButtonHandler);


    offButtonHandler = e -> {
      fx(() -> {
        card.setValueText("OFF");
        log.debug("Artificial lights turned OFF");
      });
      if (cmdHandler != null && nodeId != null) {
        try {
          cmdHandler.setValue(nodeId, "artificial_light", 0);
          log.info("Artificial light OFF command sent (nodeId={})", nodeId);
        } catch (IOException ex) {
          log.error("Failed to send artificial light OFF command (nodeId={})", nodeId, ex);
        }
      }
    };
    offButton.setOnAction(offButtonHandler);

    stateChangeListener = (obs, oldToggle, newToggle) -> {
      if (newToggle == onButton) {
        int intensity = (int) intensitySlider.getValue();
        fx(() -> card.setValueText("ON (" + intensity + "%)"));
      } else if (newToggle == offButton) {
        fx(() -> card.setValueText("OFF"));
      }
    };
    stateGroup.selectedToggleProperty().addListener(stateChangeListener);

    intensityChangeListener = (obs, oldVal, newVal) -> {
      int newIntensity = newVal.intValue();
      int oldIntensity = oldVal.intValue();

      fx(() -> {
        intensityLabel.setText("Intensity: " + newIntensity + "%");

        // Update card value text if lights are ON
        if (onButton.isSelected()) {
          card.setValueText("ON (" + newIntensity + "%)");
        }
      });

      if (Math.abs(newIntensity - oldIntensity) >= 5 || newIntensity == 0 || newIntensity == 100) {
        log.debug("Artificial light intensity adjusted: {}% -> {}%", oldIntensity, newIntensity);

        if (onButton.isSelected() && cmdHandler != null && nodeId != null) {
          try {
            cmdHandler.setValue(nodeId, "artificial_light", newIntensity);
            log.info("Artificial light intensity command sent (nodeId={}, intensity={}%)",
                    nodeId, newIntensity);
          } catch (IOException ex) {
            log.error("Failed to send artificial light intensity command "
                            + "(nodeId={}, intensity={}%)",
                    nodeId, newIntensity, ex);
          }
        }
      }
    };
    intensitySlider.valueProperty().addListener(intensityChangeListener);


    scheduleButton.setOnAction(e -> {
      log.info("Schedule button clicked (not implemented)");
      // TODO: Open scheduling dialog
    });

    log.debug("LightCardController started successfully");
  }



  /**
  * Stops this controller and cleans up resources/listeners.
  */
  public void stop() {
    log.info("Stopping LightCardController");
    if (onButtonHandler != null) {
      onButton.setOnAction(null);
      onButtonHandler = null;
    }

    if (offButtonHandler != null) {
      offButton.setOnAction(null);
      offButtonHandler = null;
    }

    if (stateChangeListener != null) {
      stateGroup.selectedToggleProperty().removeListener(stateChangeListener);
      stateChangeListener = null;
    }

    if (intensityChangeListener != null) {
      intensitySlider.valueProperty().removeListener(intensityChangeListener);
      intensityChangeListener = null;
    }

    scheduleButton.setOnAction(null);

    log.debug("LightCardController stopped successfully");
  }

  /**
  * Updates the ambient light level display.
  *
  * @param lux the ambient light level in lux
  */
  public void updateAmbientLight(double lux) {
    log.info("Updating ambient light to: {} lx", String.format("%.0f", lux));

    fx(() -> {
      ambientLabel.setText(String.format("Ambient: %.0f lux", lux));

      String lightLevel = getLightLevelDescription(lux);
      log.debug("Ambient light level: {} ({})", lightLevel, String.format("%.0f", lux));
    });
  }

  /**
  * Gets a readable description of light level.
  *
  * @param lux the light level in lux
  * @return description of the light level
  */
  private String getLightLevelDescription(double lux) {
    if (lux < 100) {
      return "Dark";
    }
    if (lux < 300) {
      return "Low";
    }
    if (lux < 1000) {
      return "Medium";
    }
    if (lux < 10000) {
      return "Bright";
    }
    return "Very Bright";
  }

  /**
  * Sets the artificial light state programmatically.
  *
  * @param isOn true to turn artificial lights ON, false to turn OFF
  */
  public void setLightState(boolean isOn) {
    log.info("Setting light state to: {}", isOn ? "ON" : "OFF");

    fx(() -> {
      if (isOn) {
        onButton.setSelected(true);
      } else {
        offButton.setSelected(true);
      }
    });
  }

  /**
  * Sets the artificial light intensity programmatically.
  *
  * @param intensity the  intensity percentage (0-100)
  */
  public void setIntensity(int intensity) {
    int clamped = Math.max(0, Math.min(100, intensity));
    log.info("Setting light intensity to: {}%", clamped);

    fx(() -> intensitySlider.setValue(clamped));
  }

  /**
  * Gets the current artificial light state.
  *
  * @return true if artificial lights are ON, false if OFF
  */
  public boolean isLightOn() {
    return onButton.isSelected();
  }

  /**
  * Gets the current artificial light intensity percentage.
  *
  * @return intensity value (0-100)
  */
  public int getIntensity() {
    return (int) intensitySlider.getValue();
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
