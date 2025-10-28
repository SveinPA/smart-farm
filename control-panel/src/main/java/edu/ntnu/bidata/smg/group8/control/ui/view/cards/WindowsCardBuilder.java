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
* Builder for the windows control card.
* This builder creates a control card for managing greenhouse windows,
*

* @author Andrea Sandnes
* @version 27.10.2025
*/
public class WindowsCardBuilder implements CardBuilder {
  private final ControlCard card;
  private Label openingLabel;
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
  private VBox manualBox;

  // Auto mode controls
  private Spinner<Integer> tempSpinner;
  private Spinner<Integer> windSpinner;
  private Label autoStatusLabel;
  private VBox autoBox;

  private Button scheduleButton;

  /**
  * Constructs a new windows card builder.
  */
  public WindowsCardBuilder() {
    this.card = new ControlCard("Windows");
    card.setValueText("CLOSED");
  }

  /**
  * Builds and returns the complete windows control card.

  * @return the fully constructed ControlCard ready for display
  */
  @Override
  public ControlCard build() {
    createOpeningLabel();
    createModeControls();
    createManualControls();
    createAutoControls();
    createFooter();

    card.addContent(
            openingLabel,
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
  * Creates the opening percentage label.
  */
  private void createOpeningLabel() {
    openingLabel = new Label("Opening: 0%");
    openingLabel.getStyleClass().add("card-subtle");
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
    closedButton = ButtonFactory.createPresetButton("Closed", 65);
    slightButton = ButtonFactory.createPresetButton("Slight", 65);
    halfButton = ButtonFactory.createPresetButton("Half", 65);
    mostlyButton = ButtonFactory.createPresetButton("Mostly", 65);
    openButton = ButtonFactory.createPresetButton("Open", 65);

    // Add event handlers for presets
    closedButton.setOnAction(e -> setWindowPosition(0));
    slightButton.setOnAction(e -> setWindowPosition(25));
    halfButton.setOnAction(e -> setWindowPosition(50));
    mostlyButton.setOnAction(e -> setWindowPosition(75));
    openButton.setOnAction(e -> setWindowPosition(100));

    // Slider
    sliderLabel = new Label("Custom: 0%");
    openingSlider = new Slider(0, 100, 0);
    openingSlider.setShowTickLabels(false);
    openingSlider.setShowTickMarks(true);
    openingSlider.setMajorTickUnit(25);
    openingSlider.setMaxWidth(Double.MAX_VALUE);

    // Update label when slider changes
    openingSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
      int value = newVal.intValue();
      sliderLabel.setText("Custom: " + value + "%");
      updateCardValue(value);
    });

    // Create layout
    HBox presetsRow1 = new HBox(6, closedButton, slightButton, halfButton);
    HBox presetsRow2 = new HBox(6, mostlyButton, openButton);
    presetsRow1.setAlignment(Pos.CENTER_LEFT);
    presetsRow2.setAlignment(Pos.CENTER_LEFT);

    VBox sliderBox = new VBox(6, sliderLabel, openingSlider);
    sliderBox.setAlignment(Pos.CENTER_LEFT);

    manualBox = new VBox(8, presetsRow1, presetsRow2, sliderBox);
    manualBox.setAlignment(Pos.CENTER_LEFT);
  }

  /**
  * Creates all automatic mode controls.
  */
  private void createAutoControls() {
    Label autoInfoLabel = new Label("Linked to: Temperature & Wind Speed");
    autoInfoLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #888;");

    // Temperature threshold
    Label tempLabel = new Label("Open at:");
    tempSpinner = new Spinner<>(20, 35, 25, 1);
    tempSpinner.setEditable(false);
    tempSpinner.setPrefWidth(75);
    Label tempUnit = new Label("°C");

    HBox tempBox = new HBox(8, tempLabel, tempSpinner, tempUnit);
    tempBox.setAlignment(Pos.CENTER_LEFT);

    // Wind speed limit
    Label windLabel = new Label("Close at:");
    windSpinner = new Spinner<>(5, 20, 10, 1);
    windSpinner.setEditable(false);
    windSpinner.setPrefWidth(75);
    Label windUnit = new Label("m/s");

    HBox windBox = new HBox(8, windLabel, windSpinner, windUnit);
    windBox.setAlignment(Pos.CENTER_LEFT);

    autoStatusLabel = new Label("Auto-control: Active");
    autoStatusLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #4caf50;");

    autoBox = new VBox(8, autoInfoLabel, tempBox, windBox, autoStatusLabel);
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
  * Sets the window position and updates all related UI elements.
  *
  * @param position the position percentage (0-100)
  */
  private void setWindowPosition(int position) {
    openingSlider.setValue(position);
    sliderLabel.setText("Custom: " + position + "%");
    updateCardValue(position);
  }

  /**
  * Updates the card's main value text based on position.
  *
  * @param position the position percentage (0-100)
  */
  private void updateCardValue(int position) {
    openingLabel.setText("Opening: " + position + "%");

    String statusText;
    if (position == 0) {
      statusText = "CLOSED";
    } else if (position < 30) {
      statusText = "SLIGHTLY OPEN";
    } else if (position < 60) {
      statusText = "HALF OPEN";
    } else if (position < 90) {
      statusText = "MOSTLY OPEN";
    } else {
      statusText = "FULLY OPEN";
    }

    card.setValueText(statusText);
  }

  // Getters for controller access

  /**
  * Gets the opening percentage label.
  *
  * @return the opening label
  */
  public Label getOpeningLabel() {
    return openingLabel;
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
  * Gets the closed preset button.
  *
  * @return the closed button
  */
  public Button getClosedButton() {
    return closedButton;
  }

  /**
  * Gets the slight preset button.
  *
  * @return the slight button
  */
  public Button getSlightButton() {
    return slightButton;
  }

  /**
  * Gets the half preset button.
  *
  * @return the half button
  */
  public Button getHalfButton() {
    return halfButton;
  }

  /**
  * Gets the mostly preset button.
  *
  * @return the mostly button
  */
  public Button getMostlyButton() {
    return mostlyButton;
  }

  /**
  * Gets the open preset button.
  *
  * @return the open button
  */
  public Button getOpenButton() {
    return openButton;
  }

  /**
  * Gets the opening slider.
  *
  * @return the opening slider
  */
  public Slider getOpeningSlider() {
    return openingSlider;
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
  * Gets the wind speed limit spinner.
  *
  * @return the wind speed spinner
  */
  public Spinner<Integer> getWindSpinner() {
    return windSpinner;
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
