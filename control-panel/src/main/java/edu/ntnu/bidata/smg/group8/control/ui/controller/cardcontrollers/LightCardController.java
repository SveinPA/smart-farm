package edu.ntnu.bidata.smg.group8.control.ui.controller.cardcontrollers;

import edu.ntnu.bidata.smg.group8.common.util.AppLogger;
import edu.ntnu.bidata.smg.group8.control.ui.view.ControlCard;
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


  /**
  * Creates new LightCardController with the specified UI components.
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
  * Initializes event handlers and starts any listeners required by this controller.
  */
  public void start() {
    log.info("Starting LightCardController");

    onButtonHandler = e -> {
      fx(() -> {
        card.setValueText("ON");
        log.debug("Light turned ON");
      });
      //TODO! Send command to backend
    };
    onButton.setOnAction(onButtonHandler);

    offButtonHandler = e -> {
      fx(() -> {
        card.setValueText("OFF");
        log.debug("Light turned OFF");
      });
      //TODO! Send command to backend
    };
    offButton.setOnAction(offButtonHandler);

    stateChangeListener = (obs, oldToggle, newToggle) -> {
      if (newToggle == onButton) {
        fx(() ->
                card.setValueText("ON"));
      } else if (newToggle == offButton) {
        fx(() ->
                card.setValueText("OFF"));
      }
    };
    stateGroup.selectedToggleProperty().addListener(stateChangeListener);
    log.debug("LightCardController started successfully");

    intensityChangeListener = (obs, oldVal, newVal) -> {
      int newIntensity = newVal.intValue();
      int oldIntensity = oldVal.intValue();

      fx(() ->
              intensityLabel.setText("Intensity: " + newIntensity + "%"));

      if (Math.abs(newIntensity - oldIntensity) >= 5 || newIntensity == 0 || newIntensity == 100) {
        log.debug("Light intensity adjusted: {}% -> {}%", oldIntensity, newIntensity);
      }
      // TODO: Send intensity command to backend
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
    log.debug("Updating ambient light to: {} lx", String.format("%.0f", lux));

    fx(() ->
            ambientLabel.setText(String.format("Ambient: %.0f lx", lux)));
  }

  /**
  * Sets the light state programmatically.
  *
  * @param isOn true to turn lights ON, false to turn OFF
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
  * Sets the light intensity programmatically.
  *
  * @param intensity the intensity percentage (0-100)
  */
  public void setIntensity(int intensity) {
    int clamped = Math.max(0, Math.min(100, intensity));
    log.info("Setting light intensity to: {}%", clamped);

    fx(() -> intensitySlider.setValue(clamped));
  }

  /**
  * Gets the current light state.
  *
  * @return true if lights are ON, false if OFF
  */
  public boolean isLightOn() {
    return onButton.isSelected();
  }

  /**
  * Gets the current intensity percentage.
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
