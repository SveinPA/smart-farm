package edu.ntnu.bidata.smg.group8.control.ui.view.cards;

import edu.ntnu.bidata.smg.group8.control.ui.factory.ButtonFactory;
import edu.ntnu.bidata.smg.group8.control.ui.view.ControlCard;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.Spinner;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
* Builder for the Heater control card.
* This builder creates a control card for managing the heat in the greenhouse.
*/
public class HeaterCardBuilder implements CardBuilder {
  private final ControlCard card;
  private Label targetLabel;
  private Spinner<Integer> tempSpinner;
  private Button applyButton;
  private Button coolButton;
  private Button moderateButton;
  private Button warmButton;
  private Button offButton;
  private Button scheduleButton;

  /**
  * Constructs a new heater card builder.
  */
  public HeaterCardBuilder() {
    this.card = new ControlCard("Heater");
    card.setValueText("OFF");
  }

  /**
  * Builds and returns the complete heater control card.

  * @return the fully constructed ControlCard ready for display
  */
  @Override
  public ControlCard build() {
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
  }

  /**
  * Creates the temperature spinner and apply button.
  */
  private void createTemperatureSpinner() {
    tempSpinner = new Spinner<>(0, 40, 20, 1);
    tempSpinner.setEditable(true);
    tempSpinner.setPrefWidth(80);

    applyButton = ButtonFactory.createPrimaryButton("Apply");
    applyButton.setOnAction(e -> setTargetTemperature(tempSpinner.getValue()));
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
    coolButton = ButtonFactory.createFullWidthButton("Cool (18°C)");
    coolButton.setOnAction(e -> setTargetTemperature(18));

    moderateButton = ButtonFactory.createFullWidthButton("Moderate (22°C)");
    moderateButton.setOnAction(e -> setTargetTemperature(22));

    warmButton = ButtonFactory.createFullWidthButton("Warm (26°C)");
    warmButton.setOnAction(e -> setTargetTemperature(26));

    offButton = ButtonFactory.createFullWidthDangerButton("Turn OFF");
    offButton.setOnAction(e -> turnOff());
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
  }

  /**
  * Sets the target temperature and updates UI.
  *
  * @param temperature the target temperature in Celsius
  */
  private void setTargetTemperature(int temperature) {
    tempSpinner.getValueFactory().setValue(temperature);
    targetLabel.setText("Target: " + temperature + "°C");
    card.setValueText("ON (" + temperature + "°C)");
  }

  /**
  * Turns off the heater and updates UI.
  */
  private void turnOff() {
    targetLabel.setText("Target: --°C");
    card.setValueText("OFF");
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