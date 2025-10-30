package edu.ntnu.bidata.smg.group8.control.ui.view.cards;

import edu.ntnu.bidata.smg.group8.common.util.AppLogger;
import edu.ntnu.bidata.smg.group8.control.ui.controller.cardcontrollers.ValveCardController;
import edu.ntnu.bidata.smg.group8.control.ui.factory.ButtonFactory;
import edu.ntnu.bidata.smg.group8.control.ui.view.ControlCard;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Separator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;


/**
* Builder for the valve control card.
*
* <p>This builder constructs and configures a ControlCard component
* dedicated to displaying and controlling greenhouse valve state and
* flow rate.</p>
*

* @author Andrea Sandnes
* @version 27.10.2025
*/
public class ValveCardBuilder implements CardBuilder {
  private static final Logger log = AppLogger.get(ValveCardBuilder.class);

  private final ControlCard card;

  /**
   * Constructs a new valve card builder.
   */
  public ValveCardBuilder() {
    this.card = new ControlCard("Valve");
    card.setValueText("CLOSED");
    log.debug("ValveCardBuilder initialized with default state: CLOSED");
  }

  /**
   * Builds and returns the complete valve control card.
   *
   * @return the fully constructed ControlCard ready for display
   */
  @Override
  public ControlCard build() {
    log.info("Building valve control card");

    Label stateLabel = new Label("CLOSED");
    stateLabel.getStyleClass().addAll("card-subtle", "valve-state");

    Label flowLabel = new Label("Flow: 0 L/min");
    flowLabel.getStyleClass().addAll("card-subtle", "valve-flow");

    ProgressBar flowIndicator = new ProgressBar(0);
    flowIndicator.setMaxWidth(Double.MAX_VALUE);
    flowIndicator.setPrefHeight(15);
    flowIndicator.getStyleClass().add("valve-flow-indicator");

    Button openButton = new Button("OPEN");
    openButton.getStyleClass().add("open-valve-button");

    Button closeButton = new Button("CLOSED");
    closeButton.getStyleClass().add("close-valve-button");

    Label actionLabel = new Label("Quick actions:");
    actionLabel.getStyleClass().add("valve-action-label");

    HBox buttonRow = new HBox(10, openButton, closeButton);
    buttonRow.setAlignment(Pos.CENTER);

    VBox buttonBox = new VBox(6, actionLabel, buttonRow);
    buttonBox.setAlignment(Pos.CENTER);

    Button scheduleButton = ButtonFactory.createScheduleButton("Schedule...");
    card.getFooter().getChildren().add(scheduleButton);

    card.addContent(
            stateLabel,
            flowLabel,
            flowIndicator,
            new Separator(),
            buttonBox
    );

    var controller = new ValveCardController(
        card,
        stateLabel,
        flowLabel,
        flowIndicator,
        openButton,
        closeButton,
        scheduleButton
    );
    card.setUserData(controller);

    log.debug("Valve control card built successfully");

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

