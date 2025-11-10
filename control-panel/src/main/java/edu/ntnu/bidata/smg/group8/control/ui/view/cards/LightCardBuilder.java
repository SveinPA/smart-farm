package edu.ntnu.bidata.smg.group8.control.ui.view.cards;

import edu.ntnu.bidata.smg.group8.common.util.AppLogger;
import edu.ntnu.bidata.smg.group8.control.ui.controller.cardcontrollers.LightCardController;
import edu.ntnu.bidata.smg.group8.control.ui.factory.ButtonFactory;
import edu.ntnu.bidata.smg.group8.control.ui.view.ControlCard;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;

/**
* Builder for the lights control card.
*
* <p>This builder constructs and configures a ControlCard component
* dedicated to controlling greenhouse lighting with ON/OFF state
* and intensity adjustment.</p>

* @author Andrea Sandnes & Mona Amundsen
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
    card.setValueText("-- lx");
  }

  /**
  * Builds and returns the complete lights control card.
  *
  * @return the fully constructed ControlCard ready for display
  */
  @Override
  public ControlCard build() {
    log.info("Building Light control card");

    // Visual representation of ambient light level
    ProgressBar ambientBar = new ProgressBar(0);
    ambientBar.setMaxWidth(Double.MAX_VALUE);
    ambientBar.setPrefHeight(20);
    ambientBar.getStyleClass().addAll("light-bar", "light-very-low");
    Tooltip.install(ambientBar, new Tooltip("Current ambient light level in greenhouse"));

    // Minimum ambient light level the last 24h
    Label minLabel = new Label("Min: -- lx");
    minLabel.getStyleClass().addAll("card-subtle", "light-ambient-label");
    minLabel.setMaxWidth(Double.MAX_VALUE);
    minLabel.setAlignment(Pos.CENTER);

    // Max ambient light level the last 24h
    Label maxLabel = new Label("Max: -- lx");
    maxLabel.getStyleClass().addAll("card-subtle", "light-ambient-label");
    maxLabel.setMaxWidth(Double.MAX_VALUE);
    maxLabel.setAlignment(Pos.CENTER);

    // Average ambient light level the last 24h
    Label avgLabel = new Label("Avg: -- lx");
    avgLabel.getStyleClass().addAll("card-subtle", "light-ambient-label");
    avgLabel.setMaxWidth(Double.MAX_VALUE);
    avgLabel.setAlignment(Pos.CENTER);

    // Label for ambient light the last 24h
    Label statsTitle = new Label("24h Statistics:");
    statsTitle.getStyleClass().addAll("card-subtle", "ambient-stats-title");
    statsTitle.setMaxWidth(Double.MAX_VALUE);
    statsTitle.setAlignment(Pos.CENTER);

    VBox statsBox = new VBox(6, statsTitle, new Separator(),minLabel, avgLabel, maxLabel);
    statsBox.setAlignment(Pos.CENTER);
    statsBox.getStyleClass().add("light-stats-box");

    Button historyButton = ButtonFactory.createHistoryButton("History");
    card.getFooter().getChildren().add(historyButton);

    card.addContent(
            new Separator(),
            ambientBar,
            statsBox
    );

    var controller = new LightCardController(
            card,
            ambientBar,
            minLabel,
            maxLabel,
            avgLabel,
            historyButton
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