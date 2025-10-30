package edu.ntnu.bidata.smg.group8.control.ui.view.cards;

import edu.ntnu.bidata.smg.group8.common.util.AppLogger;
import edu.ntnu.bidata.smg.group8.control.ui.controller.cardcontrollers.LightCardController;
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
import org.slf4j.Logger;

/**
* Builder for the lights control card.
*
* <p>This builder constructs and configures a ControlCard component
* dedicated to controlling greenhouse lighting with ON/OFF state
* and intensity adjustment.</p>

* @author Andrea Sandnes
* @version 28.10.2025
*/
public class LightCardBuilder implements CardBuilder {
  private static final Logger log = AppLogger.get(LightCardBuilder.class);

  private final ControlCard card;


  /**
  * Constructs a new lights card builder.
  */
  public LightCardBuilder() {
    this.card = new ControlCard("Lights");
    card.setValueText("OFF");
    log.debug("LightCardBuilder initialized with default state: OFF");
  }

  /**
  * Builds and returns the complete lights control card.
  *
  * @return the fully constructed ControlCard ready for display
  */
  @Override
  public ControlCard build() {
    log.info("Building Light control card");

    Label ambientLabel = new Label("Ambient: -- lx");
    ambientLabel.getStyleClass().add("card-subtle");

    ToggleGroup stateGroup = new ToggleGroup();
    RadioButton onButton = new RadioButton("ON");
    RadioButton offButton = new RadioButton("OFF");
    onButton.setToggleGroup(stateGroup);
    offButton.setToggleGroup(stateGroup);
    offButton.setSelected(true);

    HBox stateRow = new HBox(12, new Label("State:"), onButton, offButton);
    stateRow.setAlignment(Pos.CENTER);

    Slider intensitySlider = new Slider(0, 100, 60);
    intensitySlider.setShowTickLabels(false);
    intensitySlider.setShowTickMarks(false);
    intensitySlider.setMaxWidth(Double.MAX_VALUE);
    intensitySlider.getStyleClass().add("light-intensity-slider");

    Label intensityLabel = new Label("Intensity: 60%");
    intensityLabel.getStyleClass().add("light-intensity-label");

    VBox intensityBox = new VBox(8, intensityLabel, intensitySlider);
    intensityBox.setAlignment(Pos.CENTER);

    // Only show intensity controls when lights are ON
    intensityBox.visibleProperty().bind(onButton.selectedProperty());
    intensityBox.managedProperty().bind(intensityBox.visibleProperty());

    Button scheduleButton = ButtonFactory.createButton("Schedule...");
    card.getFooter().getChildren().add(scheduleButton);

    card.addContent(
            ambientLabel,
            stateRow,
            new Separator(),
            intensityBox
    );

    var controller = new LightCardController(
            card,
            ambientLabel,
            onButton,
            offButton,
            stateGroup,
            intensitySlider,
            intensityLabel,
            intensityBox,
            scheduleButton
    );
    card.setUserData(controller);

    log.debug("Light control card built successfully");

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