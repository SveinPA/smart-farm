package edu.ntnu.bidata.smg.group8.control.ui.view.cards;

import edu.ntnu.bidata.smg.group8.control.ui.factory.ButtonFactory;
import edu.ntnu.bidata.smg.group8.control.ui.view.ControlCard;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Separator;
import javafx.scene.control.Slider;
import javafx.scene.control.Spinner;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
* Builder for the fan control card.
* This builder creates a control card for managing the greenhouse fans
*
*
*/
public class FanCardBuilder implements CardBuilder {
  private final ControlCard card;
  private Label speedLabel;
  private RadioButton manualMode;
  private RadioButton autoMode;
  private ToggleGroup modeGroup;

  // Manual mode controls
  private Button lowButton;
  private Button mediumButton;
  private Button highButton;
  private Button fullButton;
  private Button offButton;
  private Slider speedSlider;
  private Label sliderLabel;
  private VBox manualBox;

  // Auto mode controls
  private Spinner<Integer> tempSpinner;
  private Spinner<Integer> humiditySpinner;
  private Label autoStatusLabel;
  private VBox autoBox;

  private Button scheduleButton;

  /**
  * Constructs a new fan card builder.
  */
  public FanCardBuilder() {
    this.card = new ControlCard("Fan");
    card.setValueText("OFF");
  }

  /**
  * Builds and returns the complete fan control card.

  * @return the fully constructed ControlCard ready for display
  */
  @Override
  public ControlCard build() {
    createSpeedLabel();
    createModeControls();
    createManualControls();
    createAutoControls();
    createFooter();

    card.addContent(
            speedLabel,
            new Separator(),
            createModeRow(),
            manualBox,
            autoBox
    );

    setupBindings();

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
  * Creates the speed percentage label.
  */
  private void createSpeedLabel() {
    speedLabel = new Label("Speed: 0%");
    speedLabel.getStyleClass().add("card-subtle");
  }

  /**
  * Creates the mode control radio buttons (Manual/Auto).
  */
  private void createModeControls() {
    modeGroup = new ToggleGroup();
    manualMode = new RadioButton("Manual");
    autoMode = new RadioButton("Auto");
    manualMode.setToggleGroup(modeGroup);
    autoMode.setToggleGroup(modeGroup);
    manualMode.setSelected(true);
  }

  /**
  * Creates the mode row with radio buttons.
  *
  * @return HBox containing mode controls
  */
  private HBox createModeRow() {
    HBox modeRow = new HBox(12, new Label("Mode:"), manualMode, autoMode);
    modeRow.setAlignment(Pos.CENTER_LEFT);
    return modeRow;
  }

  /**
  * Creates all manual mode controls.
  */
  private void createManualControls() {
    // Preset buttons
    lowButton = ButtonFactory.createPresetButton("Low", 60);
    mediumButton = ButtonFactory.createPresetButton("Medium", 60);
    highButton = ButtonFactory.createPresetButton("High", 60);
    fullButton = ButtonFactory.createPresetButton("Full", 60);

    // OFF button
    offButton = ButtonFactory.createFullWidthDangerButton("Turn OFF");

    // Add event handlers for presets
    lowButton.setOnAction(e -> setFanSpeed(25));
    mediumButton.setOnAction(e -> setFanSpeed(50));
    highButton.setOnAction(e -> setFanSpeed(75));
    fullButton.setOnAction(e -> setFanSpeed(100));
    offButton.setOnAction(e -> setFanSpeed(0));

    // Slider
    sliderLabel = new Label("Custom: 0%");
    speedSlider = new Slider(0, 100, 0);
    speedSlider.setShowTickLabels(false);
    speedSlider.setShowTickMarks(true);
    speedSlider.setMajorTickUnit(25);
    speedSlider.setMaxWidth(Double.MAX_VALUE);

    // Update label when slider changes
    speedSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
      int value = newVal.intValue();
      sliderLabel.setText("Custom: " + value + "%");
      updateCardValue(value);
    });

    // Create layout
    HBox presetsRow1 = new HBox(6, lowButton, mediumButton, highButton, fullButton);
    presetsRow1.setAlignment(Pos.CENTER_LEFT);

    VBox sliderBox = new VBox(6, sliderLabel, speedSlider);
    sliderBox.setAlignment(Pos.CENTER_LEFT);

    manualBox = new VBox(8, presetsRow1, sliderBox, offButton);
    manualBox.setAlignment(Pos.CENTER_LEFT);
  }

  /**
  * Creates all automatic mode controls.
  */
  private void createAutoControls() {
    Label autoInfoLabel = new Label("Linked to: Temperature & Humidity");
    autoInfoLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #888;");

    // Temperature threshold
    Label tempLabel = new Label("Activate at:");
    tempSpinner = new Spinner<>(22, 35, 26, 1);
    tempSpinner.setEditable(false);
    tempSpinner.setPrefWidth(75);
    Label tempUnit = new Label("°C");

    HBox tempBox = new HBox(8, tempLabel, tempSpinner, tempUnit);
    tempBox.setAlignment(Pos.CENTER_LEFT);

    // Humidity threshold
    Label humidityLabel = new Label("Or at:");
    humiditySpinner = new Spinner<>(60, 90, 75, 5);
    humiditySpinner.setEditable(false);
    humiditySpinner.setPrefWidth(75);
    Label humidityUnit = new Label("% RH");

    HBox humidityBox = new HBox(8, humidityLabel, humiditySpinner, humidityUnit);
    humidityBox.setAlignment(Pos.CENTER_LEFT);

    autoStatusLabel = new Label("Auto-control: Active");
    autoStatusLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #4caf50;");

    Label noteLabel = new Label("Speed adjusts based on current values");
    noteLabel.setStyle("-fx-font-size: 10px; -fx-font-style: italic; -fx-text-fill: #888;");

    autoBox = new VBox(8, autoInfoLabel, tempBox, humidityBox, autoStatusLabel, noteLabel);
    autoBox.setAlignment(Pos.CENTER_LEFT);
  }

  /**
  * Creates the footer with schedule button.
  */
  private void createFooter() {
    scheduleButton = ButtonFactory.createButton("Schedule...");
    card.getFooter().getChildren().add(scheduleButton);
  }

  /**
  * Sets up visibility bindings between mode and control boxes.
  */
  private void setupBindings() {
    // Show manual controls only in manual mode
    manualBox.visibleProperty().bind(manualMode.selectedProperty());
    manualBox.managedProperty().bind(manualBox.visibleProperty());

    // Show auto controls only in auto mode
    autoBox.visibleProperty().bind(autoMode.selectedProperty());
    autoBox.managedProperty().bind(autoBox.visibleProperty());
  }

  /**
  * Sets the fan speed and updates all related UI elements.
  *
  * @param speed the speed percentage (0-100)
  */
  private void setFanSpeed(int speed) {
    speedSlider.setValue(speed);
    sliderLabel.setText("Custom: " + speed + "%");
    updateCardValue(speed);
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
  }

  // Getters for controller access

  /**
  * Gets the speed label.
  *
  * @return the speed label
  */
  public Label getSpeedLabel() {
    return speedLabel;
  }

  /**
  * Gets the manual mode radio button.
  *
  * @return the manual mode button
  */
  public RadioButton getManualMode() {
    return manualMode;
  }

  /**
  * Gets the auto mode radio button.
  *
  * @return the auto mode button
  */
  public RadioButton getAutoMode() {
    return autoMode;
  }

  /**
  * Gets the mode toggle group.
  *
  * @return the toggle group
  */
  public ToggleGroup getModeGroup() {
    return modeGroup;
  }

  /**
  * Gets the low speed preset button.
  *
  * @return the low button
  */
  public Button getLowButton() {
    return lowButton;
  }

  /**
  * Gets the medium speed preset button.
  *
  * @return the medium button
  */
  public Button getMediumButton() {
    return mediumButton;
  }

  /**
  * Gets the high speed preset button.
  *
  * @return the high button
  */
  public Button getHighButton() {
    return highButton;
  }

  /**
  * Gets the full speed preset button.
  *
  * @return the full button
  */
  public Button getFullButton() {
    return fullButton;
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
  * Gets the speed slider.
  *
  * @return the speed slider
  */
  public Slider getSpeedSlider() {
    return speedSlider;
  }

  /**
  * Gets the slider label.
  *
  * @return the slider label
  */
  public Label getSliderLabel() {
    return sliderLabel;
  }

  /**
  * Gets the temperature threshold spinner.
  *
  * @return the temperature spinner
  */
  public Spinner<Integer> getTempSpinner() {
    return tempSpinner;
  }

  /**
  * Gets the humidity threshold spinner.
  *
  * @return the humidity spinner
  */
  public Spinner<Integer> getHumiditySpinner() {
    return humiditySpinner;
  }

  /**
  * Gets the auto status label.
  *
  * @return the auto status label
  */
  public Label getAutoStatusLabel() {
    return autoStatusLabel;
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
