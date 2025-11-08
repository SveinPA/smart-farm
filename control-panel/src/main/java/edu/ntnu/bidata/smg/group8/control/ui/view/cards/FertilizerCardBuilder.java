package edu.ntnu.bidata.smg.group8.control.ui.view.cards;

import edu.ntnu.bidata.smg.group8.common.util.AppLogger;
import edu.ntnu.bidata.smg.group8.control.ui.controller.cardcontrollers.FertilizerCardController;
import edu.ntnu.bidata.smg.group8.control.ui.factory.ButtonFactory;
import edu.ntnu.bidata.smg.group8.control.ui.view.ControlCard;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Separator;
import javafx.scene.control.Spinner;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import javafx.scene.control.ProgressBar;
import javafx.geometry.Pos;

/**
* Builder for the Fertilizer Control Card.
*
* <p>This builder constructs and configures a ControlCard component
* dedicated to controlling greenhouse fertilizer dosing.</p>
*
* @author Andrea Sandnes & Mona Amundsen
* @version 28.10.2025
*/
public class FertilizerCardBuilder implements CardBuilder {
  private static final Logger log = AppLogger.get(FertilizerCardBuilder.class);

  private static final int DOSE_MIN = 0;
  private static final int DOSE_MAX = 500;
  private static final int DOSE_DEFAULT = 50;

  private final ControlCard card;


  /**
  * Constructs a new fertilizer card builder.
  */
  public FertilizerCardBuilder() {
    this.card = new ControlCard("Fertilizer");
    card.setValueText("IDLE");
    log.debug("FertilizerCardBuilder initialized");
  }

  /**
  * Builds and returns the complete fertilizer control card.
  *
  * @return the fully constructed ControlCard ready for display
  */
  @Override
  public ControlCard build() {
    log.info("Building Fertilizer control card");

    Label statusLabel = new Label("Status: Ready");
    statusLabel.getStyleClass().addAll("card-subtle", "fertilizer-status");

    Label lastDoseLabel = new Label("Last dose: --");
    lastDoseLabel.getStyleClass().addAll("card-subtle", "fertilizer-last-dose");

    Label nitrogenLabel = new Label("Nitrogen level in soil:");
    nitrogenLabel.getStyleClass().add("fertilizer-nitrogen-level");

    ProgressBar nitrogenBar = new ProgressBar(0);
    nitrogenBar.setMaxWidth(Double.MAX_VALUE);
    nitrogenBar.setPrefHeight(8);
    nitrogenBar.getStyleClass().addAll("fertilizer-bar", "fertilizer-very-low");
    VBox nitrogenBox = new VBox(4, nitrogenLabel, nitrogenBar);
    VBox.setMargin(nitrogenBox, new Insets(10, 0, 15, 0));
    nitrogenBox.setAlignment(Pos.CENTER_LEFT);

    Spinner<Integer> doseSpinner = new Spinner<>(DOSE_MIN, DOSE_MAX, DOSE_DEFAULT, 10);
    doseSpinner.setEditable(true);
    doseSpinner.setPrefWidth(100);
    doseSpinner.getStyleClass().add("fertilizer-spinner");

    Button applyButton = ButtonFactory.createPrimaryButton("Apply Dose");

    Label setLabel = new Label("Dose amount:");
    Label unitLabel = new Label("ml");

    HBox doseBox = new HBox(8, setLabel, doseSpinner, unitLabel);
    doseBox.setAlignment(Pos.CENTER);
    doseBox.setMinWidth(200);

    VBox applyBox = new VBox(8, doseBox, applyButton);
    applyBox.setAlignment(Pos.CENTER);

    Button quickDose50Button = ButtonFactory.createFullWidthButton("Quick Dose (50 ml)");
    Button quickDose100Button = ButtonFactory.createFullWidthButton("Quick Dose (100 ml)");
    Button quickDose200Button = ButtonFactory.createFullWidthButton("Quick Dose (200 ml)");

    Label presetsLabel = new Label("Quick doses:");
    presetsLabel.getStyleClass().add("fertilizer-presets-title");

    VBox presetsBox = new VBox(6, presetsLabel, quickDose50Button, quickDose100Button, quickDose200Button);
    presetsBox.setAlignment(Pos.CENTER_LEFT);

    Button historyButton = ButtonFactory.createButton("History");
    card.getFooter().getChildren().add(historyButton);

    card.addContent(
            statusLabel,
            lastDoseLabel,
            new Separator(),
            nitrogenBox,
            applyBox,
            new Separator(),
            presetsBox
    );

    var controller = new FertilizerCardController(
            card,
            statusLabel,
            lastDoseLabel,
            doseSpinner,
            applyButton,
            quickDose50Button,
            quickDose100Button,
            quickDose200Button,
            historyButton,
            nitrogenBar
    );
    card.setUserData(controller);

    log.debug("Fertilizer control card built successfully");
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