package edu.ntnu.bidata.smg.group8.control.ui.view.cards;

import edu.ntnu.bidata.smg.group8.common.util.AppLogger;
import edu.ntnu.bidata.smg.group8.control.ui.controller.cardcontrollers.HeaterCardController;
import edu.ntnu.bidata.smg.group8.control.ui.factory.ButtonFactory;
import edu.ntnu.bidata.smg.group8.control.ui.view.ControlCard;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.Spinner;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;

/**
* Builder for the Heater Control Card.
*
* <p>This builder constructs and configures a ControlCard component
* dedicated to controlling greenhouse heating with temperature target setting.</p>
*
* @author Andrea Sandnes & Mona Amundsen
* @version 28.10.2025
*/
public class HeaterCardBuilder implements CardBuilder {
  private static final Logger log = AppLogger.get(HeaterCardBuilder.class);

  private static final int TEMP_MIN = 0;
  private static final int TEMP_MAX = 40;
  private static final int TEMP_DEFAULT = 20;

  private final ControlCard card;

  /**
  * Constructs a new heater card builder.
  */
  public HeaterCardBuilder() {
    this.card = new ControlCard("Heater");
    card.setValueText("OFF");
    log.debug("HeaterCardBuilder initialized - State: OFF, Range: [{}°C - {}°C]",
            TEMP_MIN, TEMP_MAX);
    card.getStyleClass().add("actuator-card");
  }

  /**
  * Builds and returns the complete heater control card.

  * @return the fully constructed ControlCard ready for display
  */
  @Override
  public ControlCard build() {
    log.info("Building Heater control card");

    Spinner<Integer> tempSpinner = new Spinner<>(TEMP_MIN, TEMP_MAX, TEMP_DEFAULT, 1);
    tempSpinner.setEditable(true);
    tempSpinner.setPrefWidth(80);
    tempSpinner.getStyleClass().add("heater-spinner");

    Button applyButton = ButtonFactory.createPrimaryButton("Apply");

    Label setLabel = new Label("Set target:");
    Label unitLabel = new Label("°C");

    HBox spinnerBox = new HBox(8, tempSpinner, unitLabel);
    spinnerBox.setAlignment(Pos.CENTER);

    VBox targetBox = new VBox(4, setLabel, spinnerBox);
    targetBox.setAlignment(Pos.CENTER);

    VBox applyBox = new VBox(8,targetBox, applyButton);
    VBox.setMargin(applyBox, new Insets(8, 0, 10, 0));
    applyBox.setAlignment(Pos.CENTER);

    Button coolButton = ButtonFactory.createFullWidthButton("Cool (18°C)");
    Button moderateButton = ButtonFactory.createFullWidthButton("Moderate (22°C)");
    Button warmButton = ButtonFactory.createFullWidthButton("Warm (26°C)");
    Button offButton = ButtonFactory.createFullWidthDangerButton("Turn OFF");

    Label presetsLabel = new Label("Quick presets:");
    presetsLabel.getStyleClass().add("heater-presets-title");

    VBox presetsLabelBox = new VBox(presetsLabel);
    VBox.setMargin(presetsLabelBox, new Insets(8, 0, 0, 0));

    VBox presetsBox = new VBox(6, coolButton, moderateButton, warmButton, offButton);
    presetsBox.setAlignment(Pos.CENTER);

    card.addContent(
            new Separator(),
            applyBox,
            new Separator(),
            presetsLabel,
            presetsLabelBox,
            presetsBox
    );

    var controller = new HeaterCardController(
            card,
            tempSpinner,
            applyButton,
            coolButton,
            moderateButton,
            warmButton,
            offButton
    );
    card.setUserData(controller);

    log.debug("Heater control card built successfully");

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