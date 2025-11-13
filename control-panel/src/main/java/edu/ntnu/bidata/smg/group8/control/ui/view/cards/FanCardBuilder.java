package edu.ntnu.bidata.smg.group8.control.ui.view.cards;

import edu.ntnu.bidata.smg.group8.common.util.AppLogger;
import edu.ntnu.bidata.smg.group8.control.ui.controller.cardcontrollers.FanCardController;
import edu.ntnu.bidata.smg.group8.control.ui.factory.ButtonFactory;
import edu.ntnu.bidata.smg.group8.control.ui.view.ControlCard;
import javafx.geometry.Insets;
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
* Builder for the Fan Control Card.
*
* <p>This builder constructs and configures a ControlCard component
* dedicated to controlling greenhouse fans with both manual and automatic modes.</p>
*
* @author Andrea Sandnes & Mona Amundsen
* @version 28.10.2025
*/
public class FanCardBuilder implements CardBuilder {
  private static final Logger log = AppLogger.get(FanCardBuilder.class);

  private final ControlCard card;


  /**
   * Constructs a new fan card builder.
   */
  public FanCardBuilder() {
    this.card = new ControlCard("Fan");
    card.setValueText("OFF");
    log.debug("FanCardBuilder initialized");
  }

  /**
   * Builds and returns the complete fan control card.
   *
   * @return the fully constructed ControlCard ready for display
   */
  @Override
  public ControlCard build() {
    log.info("Building Fan control card");

    ToggleGroup modeGroup = new ToggleGroup();
    RadioButton manualMode = new RadioButton("Manual");
    RadioButton autoMode = new RadioButton("Auto");
    manualMode.setToggleGroup(modeGroup);
    autoMode.setToggleGroup(modeGroup);
    manualMode.setSelected(true);

    HBox modeRow = new HBox(12, new Label("Mode:"), manualMode, autoMode);
    modeRow.setAlignment(Pos.CENTER_LEFT);
    //Create some spacing above the mode row
    VBox.setMargin(modeRow, new Insets(13,0,10,0));

    // Manual controls
    Button lowButton = ButtonFactory.createPresetButton("Low", 60);
    Button mediumButton = ButtonFactory.createPresetButton("Medium", 60);
    Button highButton = ButtonFactory.createPresetButton("High", 60);
    Button fullButton = ButtonFactory.createPresetButton("Full", 60);
    Button offButton = ButtonFactory.createFullWidthDangerButton("Turn OFF");

    double presetWidth = 80;
    lowButton.setMinWidth(presetWidth);
    mediumButton.setMinWidth(presetWidth);
    highButton.setMinWidth(presetWidth);
    fullButton.setMinWidth(presetWidth);

    HBox presetsRow1 = new HBox(8, lowButton, mediumButton);
    HBox presetsRow2 = new HBox(8, highButton, fullButton);
    presetsRow1.setAlignment(Pos.CENTER);
    presetsRow2.setAlignment(Pos.CENTER);
    VBox presetsBox = new VBox(6, presetsRow1, presetsRow2);
    presetsBox.setAlignment(Pos.CENTER);

    Label sliderLabel = new Label("Custom: 0%");
    sliderLabel.getStyleClass().add("fan-slider-label");

    Slider speedSlider = new Slider(0, 100, 0);
    speedSlider.setShowTickLabels(false);
    speedSlider.setShowTickMarks(true);
    speedSlider.setMajorTickUnit(25);
    speedSlider.setMaxWidth(Double.MAX_VALUE);
    speedSlider.getStyleClass().add("fan-slider");

    VBox sliderBox = new VBox(6, sliderLabel, speedSlider);
    sliderBox.setAlignment(Pos.CENTER_LEFT);

    VBox manualBox = new VBox(8, presetsBox, sliderBox, offButton);
    VBox.setMargin(offButton, new Insets(0, 0, 20, 0));
    manualBox.setAlignment(Pos.CENTER_LEFT);

    // Auto controls
    Label autoInfoLabel = new Label("Linked to: Temperature & Humidity");
    autoInfoLabel.getStyleClass().add("fan-auto-info");
    VBox.setMargin(autoInfoLabel, new Insets(10,0,5,0));

    Label tempLabel = new Label("Activate at: ");
    Spinner<Integer> tempSpinner = new Spinner<>(22, 35, 26, 1);
    tempSpinner.setEditable(false);
    tempSpinner.setPrefWidth(75);
    Label tempUnit = new Label("°C");
    HBox tempRow = new HBox(8,tempLabel, tempSpinner, tempUnit);
    tempRow.setAlignment(Pos.CENTER);
    tempSpinner.getStyleClass().add("fan-auto-spinner");

    Label humidityLabel = new Label("Or at: ");
    Spinner<Integer> humiditySpinner = new Spinner<>(60, 90, 75, 5);
    humiditySpinner.setEditable(false);
    humiditySpinner.setPrefWidth(75);
    humiditySpinner.getStyleClass().add("fan-auto-spinner");
    Label humidityUnit = new Label("% RH");
    HBox humidityRow = new HBox(8, humidityLabel, humiditySpinner, humidityUnit);
    humidityRow.setAlignment(Pos.CENTER);

    Label autoIntensityLabel = new Label("Fan intensity: 0%");
    VBox.setMargin(autoIntensityLabel, new Insets(10,0,0,0));
    autoIntensityLabel.getStyleClass().add("fan-slider-label");

    Slider autoIntensitySlider = new Slider(0, 100, 0);
    autoIntensitySlider.setShowTickLabels(false);
    autoIntensitySlider.setShowTickMarks(true);
    autoIntensitySlider.setMajorTickUnit(25);
    autoIntensitySlider.setMaxWidth(Double.MAX_VALUE);
    autoIntensitySlider.getStyleClass().add("fan-slider");

    VBox autoFanSlider = new VBox(6, autoIntensityLabel, autoIntensitySlider);
    autoFanSlider.setAlignment(Pos.CENTER_LEFT);

    Label autoStatusLabel = new Label("Auto-control: Active");
    autoStatusLabel.getStyleClass().add("fan-auto-status");

    Label noteLabel = new Label("Speed adjusts based on current values");
    noteLabel.getStyleClass().add("fan-auto-note");

    VBox autoBox = new VBox(
            8,
            autoInfoLabel,
            tempRow,
            humidityRow,
            autoFanSlider,
            autoStatusLabel,
            noteLabel);
    autoBox.setAlignment(Pos.CENTER_LEFT);

    // Setup visibility bindings
    manualBox.visibleProperty().bind(manualMode.selectedProperty());
    manualBox.managedProperty().bind(manualBox.visibleProperty());
    autoBox.visibleProperty().bind(autoMode.selectedProperty());
    autoBox.managedProperty().bind(autoBox.visibleProperty());

    card.addContent(
            new Separator(),
            modeRow,
            manualBox,
            autoBox
    );

    var controller = new FanCardController(
            card,
            manualMode,
            autoMode,
            modeGroup,
            lowButton,
            mediumButton,
            highButton,
            fullButton,
            offButton,
            speedSlider,
            sliderLabel,
            tempSpinner,
            humiditySpinner,
            autoStatusLabel,
            autoIntensitySlider,
            autoIntensityLabel
    );
    card.setUserData(controller);

    log.debug("Fan control card built successfully");

    return card;
  }

  /**
   * Creates the control card instance.
   *
   * @return the ControlCard instance
   */
  @Override
  public ControlCard getCard() {
    return card;
  }

}