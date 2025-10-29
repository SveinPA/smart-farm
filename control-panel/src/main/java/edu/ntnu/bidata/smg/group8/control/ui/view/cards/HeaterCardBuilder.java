package edu.ntnu.bidata.smg.group8.control.ui.view.cards;

import edu.ntnu.bidata.smg.group8.common.util.AppLogger;
import edu.ntnu.bidata.smg.group8.control.ui.factory.ButtonFactory;
import edu.ntnu.bidata.smg.group8.control.ui.view.ControlCard;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.Spinner;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;

/**
* Builder for the Heater control card.
* This builder creates a control card for managing the heat in the greenhouse.
*/
public class HeaterCardBuilder implements CardBuilder {
  private static final Logger log = AppLogger.get(HeaterCardBuilder.class);

  private final ControlCard card;
  private Label targetLabel;
  private Spinner<Integer> tempSpinner;
  private Button applyButton;
  private Button coolButton;
  private Button moderateButton;
  private Button warmButton;
  private Button offButton;
  private Button scheduleButton;

  private static final int TEMP_COOL = 18;
  private static final int TEMP_MODERATE = 22;
  private static final int TEMP_WARM = 26;

  private static final int TEMP_MIN = 0;
  private static final int TEMP_MAX = 40;
  private static final int TEMP_DEFAULT = 20;

  private static final int TEMP_VERY_LOW = 10;
  private static final int TEMP_LOW = 15;
  private static final int TEMP_HIGH = 30;
  private static final int TEMP_VERY_HIGH = 35;

  private boolean isOn = false;
  private Integer currentTargetTemp = null;


  /**
  * Constructs a new heater card builder.
  */
  public HeaterCardBuilder() {
    this.card = new ControlCard("Heater");
    card.setValueText("OFF");
    log.debug("HeaterCardBuilder initialized - State: OFF, Range: [{}°C - {}°C]",
            TEMP_MIN, TEMP_MAX);
  }

  /**
  * Builds and returns the complete heater control card.

  * @return the fully constructed ControlCard ready for display
  */
  @Override
  public ControlCard build() {
    log.info("Building Heater control card");

    createTargetLabel();
    createTemperatureSpinner();
    createPresetButtons();
    createFooter();

    card.addContent(
            targetLabel,
            new Separator(),
            createSpinnerBox(),
            new Separator(),
            createPresetsBox()
    );

    log.debug("Heater control card built successfully");

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
  * Creates the target temperature label.
  */
  private void createTargetLabel() {
    targetLabel = new Label("Target: --°C");
    targetLabel.getStyleClass().add("card-subtle");
    log.trace("Target temperature label created");
  }

  /**
  * Creates the temperature spinner and apply button.
  */
  private void createTemperatureSpinner() {
    tempSpinner = new Spinner<>(TEMP_MIN, TEMP_MAX,TEMP_DEFAULT, 1);
    tempSpinner.setEditable(true);
    tempSpinner.setPrefWidth(80);

    tempSpinner.valueProperty().addListener((obs, oldVal, newVal) -> {
      if (oldVal != null && newVal != null && !oldVal.equals(newVal)) {
        log.trace("Temperature spinne value changed: {}°C -> {}°C", oldVal, newVal);
      }
    });

    applyButton = ButtonFactory.createPrimaryButton("Apply");
    applyButton.setOnAction(e -> setTargetTemperature(tempSpinner.getValue()));

    log.trace("Temperature spinner created with range [{}°C - {}°C], default: {}°C",
            TEMP_MIN, TEMP_MAX, TEMP_DEFAULT);
  }

  /**
  * Creates the spinner box with label, spinner, unit, and apply button.
  *
  * @return HBox containing spinner controls
  */
  private HBox createSpinnerBox() {
    Label setLabel = new Label("Set target:");
    Label unitLabel = new Label("°C");

    HBox spinnerBox = new HBox(8, setLabel, tempSpinner, unitLabel, applyButton);
    spinnerBox.setAlignment(Pos.CENTER_LEFT);
    return spinnerBox;
  }

  /**
  * Creates all preset temperature buttons.
  */
  private void createPresetButtons() {
    // Preset buttons with event handlers
    coolButton = ButtonFactory.createFullWidthButton("Cool (" + TEMP_COOL + "°C)");
    coolButton.setOnAction(e -> setTargetTemperature(TEMP_COOL));

    moderateButton = ButtonFactory.createFullWidthButton("Moderate (" + TEMP_MODERATE + "°C)");
    moderateButton.setOnAction(e -> setTargetTemperature(TEMP_MODERATE));

    warmButton = ButtonFactory.createFullWidthButton("Warm (" + TEMP_WARM + "°C)");
    warmButton.setOnAction(e -> setTargetTemperature(TEMP_WARM));

    offButton = ButtonFactory.createFullWidthDangerButton("Turn OFF");
    offButton.setOnAction(e -> turnOff());

    log.trace("Preset buttons created - Cool: {}°C, Moderate: {}°C, Warm: {}°C",
            TEMP_COOL, TEMP_MODERATE, TEMP_WARM);
  }

  /**
  * Creates the presets box with all preset buttons.
  *
  * @return VBox containing preset buttons
  */
  private VBox createPresetsBox() {
    Label presetsLabel = new Label("Quick presets:");
    presetsLabel.setStyle("-fx-font-size: 11px; -fx-font-weight: bold;");

    VBox presetsBox = new VBox(6, presetsLabel, coolButton, moderateButton, warmButton, offButton);
    presetsBox.setAlignment(Pos.CENTER_LEFT);
    return presetsBox;
  }

  /**
  * Creates the footer with schedule button.
  */
  private void createFooter() {
    scheduleButton = ButtonFactory.createButton("Schedule...");
    card.getFooter().getChildren().add(scheduleButton);
    log.trace("Footer with schedule button created");
  }

  /**
  * Sets the target temperature and updates UI.
  *
  * @param temperature the target temperature in Celsius
  */
  private void setTargetTemperature(int temperature) {
    String source = getTemperatureSource(temperature);
    log.info("Heater target temperature set to {}°C (Source: {})", temperature, source);

    Runnable ui = () -> {
      if (targetLabel == null || tempSpinner == null) {
        log.warn("setTargetTemperature called before build() - skipping UI update");
        return;
      }

      tempSpinner.getValueFactory().setValue(temperature);
      targetLabel.setText("Target: " + temperature + "°C");
      card.setValueText("ON (" + temperature + "°C)");

      boolean wasOff = !isOn;
      isOn = true;
      currentTargetTemp = temperature;

      if (wasOff) {
        log.info("Heater turned ON - Target: {}°C", temperature);
      } else {
        log.debug("Heater target adjusted: {}°C", temperature);
      }

      checkTemperatureWarnings(temperature);
    };

    if (javafx.application.Platform.isFxApplicationThread()) {
      ui.run();
    } else {
      javafx.application.Platform.runLater(ui);
    }
  }


  /**
  * Turns off the heater and updates UI.
  */
  private void turnOff() {
    log.info("Heater turned OFF");

    Runnable ui = () -> {
      if (targetLabel == null) {
        log.warn("turnOff called before build() - skipping UI update");
        return;
      }

      targetLabel.setText("Target: --°C");
      card.setValueText("OFF");

      if (isOn) {
        log.debug("Heater deactivated - Previous target: {}°C",
                currentTargetTemp != null ? currentTargetTemp : "N/A");
      }

      isOn = false;
      currentTargetTemp = null;
    };

    if (javafx.application.Platform.isFxApplicationThread()) {
      ui.run();
    } else {
      javafx.application.Platform.runLater(ui);
    }
  }

  /**
   * Checks for temperature warnings and logs them.
   *
   * @param temperature the target temperature
   */
  private void checkTemperatureWarnings(int temperature) {
    if (temperature <= TEMP_VERY_LOW) {
      log.warn("CAUTION: Very low target temperature ({}°C) - Risk of plant damage from cold stress",
              temperature);
    } else if (temperature < TEMP_LOW) {
      log.info("NOTICE: Low target temperature ({}°C) - Suitable for cool-season crops only",
              temperature);
    } else if (temperature >= TEMP_VERY_HIGH) {
      log.warn("CAUTION: Very high target temperature ({}°C) - Risk of plant stress and excessive energy use",
              temperature);
    } else if (temperature > TEMP_HIGH) {
      log.info("NOTICE: High target temperature ({}°C) - Monitor plant conditions closely",
              temperature);
    }
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
   * Updates the heater state externally (e.g., from controller).
   *
   * @param on true if heater should be on, false if off
   * @param targetTemp target temperature (null if off)
   */
  public void updateHeaterState(boolean on, Integer targetTemp) {
    log.debug("External heater state update - On: {}, Target: {}°C",
            on, targetTemp != null ? targetTemp : "N/A");

    if (on && targetTemp != null) {
      setTargetTemperature(targetTemp);
    } else {
      turnOff();
    }
  }

  // Getters for controller access

  /**
  * Gets the target temperature label.
  *
  * @return the target label
  */
  public Label getTargetLabel() {
    return targetLabel;
  }

  /**
  * Gets the temperature spinner.
  *
  * @return the temperature spinner
  */
  public Spinner<Integer> getTempSpinner() {
    return tempSpinner;
  }

  /**
  * Gets the apply button.
  *
  * @return the apply button
  */
  public Button getApplyButton() {
    return applyButton;
  }

  /**
  * Gets the cool preset button.
  *
  * @return the cool button
  */
  public Button getCoolButton() {
    return coolButton;
  }

  /**
  * Gets the moderate preset button.
  *
  * @return the moderate button
  */
  public Button getModerateButton() {
    return moderateButton;
  }

  /**
  * Gets the warm preset button.
  *
  * @return the warm button
  */
  public Button getWarmButton() {
    return warmButton;
  }

  /**
  * Gets the OFF button.
  *
  * @return the OFF button
  */
  public Button getOffButton() {
    return offButton;
  }

  /**
  * Gets the schedule button.
  *
  * @return the schedule button
  */
  public Button getScheduleButton() {
    return scheduleButton;
  }
}