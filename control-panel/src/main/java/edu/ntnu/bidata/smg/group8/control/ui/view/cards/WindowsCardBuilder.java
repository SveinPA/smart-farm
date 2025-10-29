package edu.ntnu.bidata.smg.group8.control.ui.view.cards;

import edu.ntnu.bidata.smg.group8.common.util.AppLogger;
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
import org.slf4j.Logger;

/**
* Builder for the windows control card.
* This builder creates a control card for managing greenhouse windows,
*

* @author Andrea Sandnes
* @version 27.10.2025
*/
public class WindowsCardBuilder implements CardBuilder {
  private static final Logger log = AppLogger.get(WindowsCardBuilder.class);


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

  private static final int POSITION_CLOSED = 0;
  private static final int POSITION_SLIGHT = 25;
  private static final int POSITION_HALF = 50;
  private static final int POSITION_MOSTLY = 75;
  private static final int POSITION_OPEN = 100;

  private String currentMode = "MANUAL";
  private int currentPosition = 0;


  /**
  * Constructs a new windows card builder.
  */
  public WindowsCardBuilder() {
    this.card = new ControlCard("Windows");
    card.setValueText("CLOSED");
    log.debug("WindowsCardBuilder initialized with default state: CLOSED, Mode: Manual");
  }

  /**
  * Builds and returns the complete windows control card.

  * @return the fully constructed ControlCard ready for display
  */
  @Override
  public ControlCard build() {
    log.info("Building Windows control card");

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

    log.debug("Windows control card built successfully");

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
    log.trace("Opening label created");
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

    modeGroup.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
      String newMode = newToggle == manualMode ? "MANUAL" : "AUTO";
      log.info("Window control mode changed: {} -> {}", currentMode, newMode);
      currentMode = newMode;

      if (newMode.equals("AUTO")) {
        log.debug("Auto mode activated - Temp threshold: {}°C, Wind limit: {} m/s",
                tempSpinner.getValue(), windSpinner.getValue());
      }
    });

    log.trace("Mode controls (Manual/Auto) created with default: MANUAL");
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
    closedButton.setOnAction(e -> setWindowPosition(POSITION_CLOSED));
    slightButton.setOnAction(e -> setWindowPosition(POSITION_SLIGHT));
    halfButton.setOnAction(e -> setWindowPosition(POSITION_HALF));
    mostlyButton.setOnAction(e -> setWindowPosition(POSITION_MOSTLY));
    openButton.setOnAction(e -> setWindowPosition(POSITION_OPEN));

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
      int oldValue = oldVal.intValue();
      sliderLabel.setText("Custom: " + value + "%");
      updateCardValue(value);

      if (Math.abs(value - oldValue) >= 10 || value == 0 || value == 100) {
        log.debug("Window position adjusted via slider: {}% -> {}%", oldValue, value);
      }
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

    log.trace("Manual controls created with presets and slider");
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

    tempSpinner.valueProperty().addListener((obs, oldVal, newVal) -> {
      log.info("Auto mode temperature threshold changed: {}°C -> {}°C", oldVal, newVal);
    });

    HBox tempBox = new HBox(8, tempLabel, tempSpinner, tempUnit);
    tempBox.setAlignment(Pos.CENTER_LEFT);

    // Wind speed limit
    Label windLabel = new Label("Close at:");
    windSpinner = new Spinner<>(5, 20, 10, 1);
    windSpinner.setEditable(false);
    windSpinner.setPrefWidth(75);
    Label windUnit = new Label("m/s");

    windSpinner.valueProperty().addListener((obs, oldVal, newVal) -> {
      log.info("Auto mode wind speed limit changed: {} m/s -> {} m/s", oldVal, newVal);
    });

    HBox windBox = new HBox(8, windLabel, windSpinner, windUnit);
    windBox.setAlignment(Pos.CENTER_LEFT);

    autoStatusLabel = new Label("Auto-control: Active");
    autoStatusLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #4caf50;");

    autoBox = new VBox(8, autoInfoLabel, tempBox, windBox, autoStatusLabel);
    autoBox.setAlignment(Pos.CENTER_LEFT);

    log.trace("Auto controls created - Default temp: 25°C, wind: 10 m/s");

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
  * Sets up visibility bindings between mode and control boxes.
  */
  private void setupBindings() {
    // Show manual controls only in manual mode
    manualBox.visibleProperty().bind(manualMode.selectedProperty());
    manualBox.managedProperty().bind(manualBox.visibleProperty());

    // Show auto controls only in auto mode
    autoBox.visibleProperty().bind(autoMode.selectedProperty());
    autoBox.managedProperty().bind(autoBox.visibleProperty());

    log.trace("Mode visibility bindings configured");
  }

  /**
  * Sets the window position and updates all related UI elements.
  *
  * @param position the position percentage (0-100)
  */
  private void setWindowPosition(int position) {
    log.info("Window position preset selected: {}% ({})",
            position, getPositionDescription(position));

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

    openingLabel.setText("Opening: " + position + "%");

    card.setValueText(statusText);

    if (position != currentPosition) {
      int diff = Math.abs(position - currentPosition);
      if (diff >= 10 || position == 0 || position == 100) {
        log.debug("Window status updated: {} ({}% -> {}%)",
                statusText, currentPosition, position);
      }
      currentPosition = position;
    }
  }

  /**
   * Updates the auto control status.
   *
   * @param isActive true if auto control is actively adjusting windows
   * @param reason the reason for the current state (e.g., "High temperature", "Strong wind")
   */
  public void updateAutoStatus(boolean isActive, String reason) {
    log.debug("Auto control status update - Active: {}, Reason: {}", isActive, reason);

    Runnable ui = () -> {
      if (autoStatusLabel == null) {
        log.warn("updateAutoStatus called before build() - skipping UI update");
        return;
      }

      if (isActive) {
        autoStatusLabel.setText("Auto-control: " + reason);
        autoStatusLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #4caf50;");
      } else {
        autoStatusLabel.setText("Auto-control: Standby");
        autoStatusLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #888;");
      }
    };

    if (javafx.application.Platform.isFxApplicationThread()) {
      ui.run();
    } else {
      javafx.application.Platform.runLater(ui);
    }
  }

  /**
   * Gets a description of the window position.
   *
   * @param position the position percentage
   * @return description string
   */
  private String getPositionDescription(int position) {
    if (position == POSITION_CLOSED) return "Closed";
    if (position == POSITION_SLIGHT) return "Slight";
    if (position == POSITION_HALF) return "Half";
    if (position == POSITION_MOSTLY) return "Mostly";
    if (position == POSITION_OPEN) return "Open";
    return "Custom";
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
