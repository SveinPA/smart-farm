package edu.ntnu.bidata.smg.group8.control.ui.view.cards;

import edu.ntnu.bidata.smg.group8.common.util.AppLogger;
import edu.ntnu.bidata.smg.group8.control.ui.controller.cardcontrollers.WindowsCardController;
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
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import org.slf4j.Logger;

/**
 * Builder for the Windows Control Card.
 *
 * <p>This builder constructs and configures a comprehensive ControlCard component
 * dedicated to displaying real-time and status</p>
 *
 * <h2>The windows control card includes the following features:</h2>
 * <ul>
 *     <li>Mode selection between Manual and Auto.</li>
 *     <li>Manual controls with preset opening buttons
 *     (Closed, Slight, Half, Mostly, Open) and a custom opening slider.</li>
 *     <li>Auto controls allowing configuration based on temperature and wind speed thresholds.</li>
 *     <li>Status display indicating whether auto-control is active.</li>
 * </ul>

 * @author Andrea Sandnes
 * @version 27.10.2025
 */
public class WindowsCardBuilder implements CardBuilder {
  private static final Logger log = AppLogger.get(WindowsCardBuilder.class);

  private final ControlCard card;

  /**
   * Constructs a new windows card builder.
   */
  public WindowsCardBuilder() {
    this.card = new ControlCard("Windows");
    card.setValueText("CLOSED");
    log.debug("WindowsCardBuilder initialized with default state: CLOSED, Mode: Manual");
    card.getStyleClass().add("actuator-card");
  }

  /**
   * Builds and returns the complete windows control card.
   *
   * <p>This method sets up all UI components, such as: mode selection
   * manual and auto controls, and status display indicating whether
   * auto-control is active. It also has buttons for opening and closing
   * the window.</p>
   *
   * @return the fully constructed ControlCard ready for display
   */
  @Override
  public ControlCard build() {
    log.info("Building Windows control card");

    // ToggleGroup - Manual/Auto mode
    ToggleGroup modeGroup = new ToggleGroup();
    RadioButton manualMode = new RadioButton("Manual");
    RadioButton autoMode = new RadioButton("Auto");
    manualMode.setToggleGroup(modeGroup);
    autoMode.setToggleGroup(modeGroup);
    manualMode.setSelected(true);

    HBox modeRow = new HBox(12, new Label("Mode:"), manualMode, autoMode);
    modeRow.setAlignment(Pos.CENTER);
    modeRow.setPadding(new Insets(4, 0, 4, 0));

    // Preset Button for window opening
    Button closedButton = ButtonFactory.createPresetButton("Closed", 65);
    Button slightButton = ButtonFactory.createPresetButton("Slight", 65);
    Button halfButton = ButtonFactory.createPresetButton("Half", 65);
    Button mostlyButton = ButtonFactory.createPresetButton("Mostly", 65);
    Button openButton = ButtonFactory.createPresetButton("Open", 65);

    // Label for Custom-slider
    Label sliderLabel = new Label("Custom: 0%");
    Slider openingSlider = new Slider(0, 100, 0);
    openingSlider.setShowTickLabels(false);
    openingSlider.setShowTickMarks(true);
    openingSlider.setMajorTickUnit(25);
    openingSlider.setMaxWidth(Double.MAX_VALUE);

    // Containers for Preset Buttons
    HBox presetsRow1 = new HBox(6, closedButton, slightButton, halfButton);
    HBox presetsRow2 = new HBox(6, mostlyButton, openButton);
    presetsRow1.setAlignment(Pos.CENTER);
    presetsRow2.setAlignment(Pos.CENTER);
    presetsRow2.setPadding(new Insets(0, 0, 6, 0));

    // Container for custom slider
    VBox sliderBox = new VBox(6, sliderLabel, openingSlider);
    sliderBox.setAlignment(Pos.BOTTOM_CENTER);
    sliderBox.setPadding(new Insets(4, 0, 4, 0));

    VBox manualBox = new VBox(8, presetsRow1, presetsRow2, new Separator(), sliderBox);
    manualBox.setAlignment(Pos.CENTER);
    manualBox.setPadding(new Insets(12, 0, 12, 0));

    // Label showing that Windows are linked to temperature and wind
    Label autoInfoLabel = new Label("Linked to: Temperature & Wind Speed");
    autoInfoLabel.getStyleClass().add("window-auto-info");

    // Label and Spinner for changing temp threshold
    Label tempLabel = new Label("Open at:");
    Spinner<Integer> tempSpinner = new Spinner<>(20, 35, 25, 1);
    tempSpinner.setEditable(false);
    tempSpinner.setPrefWidth(75);
    Label tempUnit = new Label("°C");
    HBox tempBox = new HBox(8, tempLabel, tempSpinner, tempUnit);
    tempBox.setAlignment(Pos.CENTER_LEFT);

    // Label and Spinner for changing wind threshold
    Label windLabel = new Label("Close at:");
    Spinner<Integer> windSpinner = new Spinner<>(5, 20, 10, 1);
    windSpinner.setEditable(false);
    windSpinner.setPrefWidth(75);
    Label windUnit = new Label("m/s");
    HBox windBox = new HBox(8, windLabel, windSpinner, windUnit);
    windBox.setAlignment(Pos.CENTER_LEFT);

    // auto status label
    Label autoStatusLabel = new Label("Auto-control: Active");
    autoStatusLabel.getStyleClass().add("window-auto-status");
    autoStatusLabel.setWrapText(true);
    autoStatusLabel.setMaxWidth(Double.MAX_VALUE);
    autoStatusLabel.setTextAlignment(TextAlignment.CENTER);
    autoStatusLabel.setAlignment(Pos.CENTER);

    // creates space in design
    Region spacerTop = new Region();
    Region spacerBottom = new Region();
    VBox.setVgrow(spacerTop, Priority.ALWAYS);
    VBox.setVgrow(spacerBottom, Priority.ALWAYS);

    VBox autoBox = new VBox(8, autoInfoLabel, tempBox,
             windBox, spacerTop, autoStatusLabel, spacerBottom);
    autoBox.setAlignment(Pos.CENTER_LEFT);
    autoBox.setFillWidth(true);

    card.addContent(
            modeRow,
            new Separator(),
            manualBox,
            autoBox
    );

    var controller = new WindowsCardController(
            card,
            manualMode,
            autoMode,
            modeGroup,
            closedButton,
            slightButton,
            halfButton,
            mostlyButton,
            openButton,
            openingSlider,
            sliderLabel,
            tempSpinner,
            windSpinner,
            autoStatusLabel,
            manualBox,
            autoBox
    );
    card.setUserData(controller);

    log.debug("Windows control card built successfully");
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

