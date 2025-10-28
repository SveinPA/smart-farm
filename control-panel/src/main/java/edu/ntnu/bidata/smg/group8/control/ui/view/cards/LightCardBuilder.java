package edu.ntnu.bidata.smg.group8.control.ui.view.cards;

import edu.ntnu.bidata.smg.group8.control.ui.factory.ButtonFactory;
import edu.ntnu.bidata.smg.group8.control.ui.view.ControlCard;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Separator;
import javafx.scene.control.Slider;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
* Builder for the lights control card.
* This builder creates a control card for managing greenhouse lighting,
* including ON/OFF state toggle and intensity control.

* @author Andrea Sandnes
* @version 28.10.2025
*/
public class LightsCardBuilder implements CardBuilder {
  private final ControlCard card;
  private Label ambientLabel;
  private RadioButton onButton;
  private RadioButton offButton;
  private ToggleGroup stateGroup;
  private Slider intensitySlider;
  private Label intensityLabel;
  private VBox intensityBox;
  private Button scheduleButton;

  /**
  * Constructs a new lights card builder.
  */
  public LightsCardBuilder() {
    this.card = new ControlCard("Lights");
    card.setValueText("OFF");
  }

  /**
  * Builds and returns the complete lights control card.

  * @return the fully constructed ControlCard ready for display
  */
  @Override
  public ControlCard build() {
    createAmbientLabel();
    createStateControls();
    createIntensityControls();
    createFooter();

    card.addContent(
            ambientLabel,
            createStateRow(),
            new Separator(),
            intensityBox
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
  * Creates the ambient light label.
  */
  private void createAmbientLabel() {
    ambientLabel = new Label("Ambient: -- lx");
    ambientLabel.getStyleClass().add("card-subtle");
  }

  /**
  * Creates the state control radio buttons (ON/OFF).
  */
  private void createStateControls() {
    stateGroup = new ToggleGroup();
    onButton = new RadioButton("ON");
    offButton = new RadioButton("OFF");
    onButton.setToggleGroup(stateGroup);
    offButton.setToggleGroup(stateGroup);
    offButton.setSelected(true);

    // Listen to state changes to update card value
    stateGroup.selectedToggleProperty().addListener((obs, oldT, newT) -> {
      boolean isOn = newT == onButton;
      card.setValueText(isOn ? "ON" : "OFF");
    });
  }

  /**
  * Creates the state row with label and radio buttons.
  *
  * @return HBox containing the state controls
  */
  private HBox createStateRow() {
    HBox stateRow = new HBox(12, new Label("State:"), onButton, offButton);
    stateRow.setAlignment(Pos.CENTER_LEFT);
    return stateRow;
  }

  /**
  * Creates the intensity slider and label.
  */
  private void createIntensityControls() {
    intensitySlider = new Slider(0, 100, 60);
    intensitySlider.setShowTickLabels(false);
    intensitySlider.setShowTickMarks(false);
    intensitySlider.setMaxWidth(Double.MAX_VALUE);

    intensityLabel = new Label("Intensity: 60%");
    intensitySlider.valueProperty().addListener((o, ov, nv) ->
            intensityLabel.setText("Intensity: " + nv.intValue() + "%"));

    intensityBox = new VBox(8, intensityLabel, intensitySlider);
    intensityBox.setAlignment(Pos.CENTER_LEFT);

    // Only show intensity controls when lights are ON
    intensityBox.visibleProperty().bind(onButton.selectedProperty());
    intensityBox.managedProperty().bind(intensityBox.visibleProperty());
    intensitySlider.disableProperty().bind(onButton.selectedProperty().not());
  }

  /**
  * Creates the footer with schedule button.
  */
  private void createFooter() {
    scheduleButton = ButtonFactory.createScheduleButton("Schedule...");
    card.getFooter().getChildren().add(scheduleButton);
  }

  // Getters for controller access

  /**
  * Gets the ON radio button.
  *
  * @return the ON button
  */
  public RadioButton getOnButton() {
    return onButton;
  }

  /**
  * Gets the OFF radio button.
  *
  * @return the OFF button
  */
  public RadioButton getOffButton() {
    return offButton;
  }

  /**
  * Gets the state toggle group.
  *
  * @return the toggle group
  */
  public ToggleGroup getStateGroup() {
    return stateGroup;
  }

  /**
  * Gets the intensity slider.
  *
  * @return the intensity slider
  */
  public Slider getIntensitySlider() {
    return intensitySlider;
  }

  /**
  * Gets the intensity label.
  *
  * @return the intensity label
  */
  public Label getIntensityLabel() {
    return intensityLabel;
  }

  /**
  * Gets the ambient light label.
  *
  * @return the ambient label
  */
  public Label getAmbientLabel() {
    return ambientLabel;
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