package edu.ntnu.bidata.smg.group8.control.ui.view.cards;

import edu.ntnu.bidata.smg.group8.common.util.AppLogger;
import edu.ntnu.bidata.smg.group8.control.ui.controller.cardcontrollers.PHCardController;
import edu.ntnu.bidata.smg.group8.control.ui.factory.ButtonFactory;
import edu.ntnu.bidata.smg.group8.control.ui.view.ControlCard;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Separator;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;

/**
* Builder for the pH control card.
*
* <p>This builder constructs and configures a ControlCard component
* dedicated to displaying real-time pH readings and 24h statistics.</p>
*

* @author Andrea Sandnes
* @version 28.10.2025
*/
public class PHCardBuilder implements CardBuilder {
  private static final Logger log = AppLogger.get(PHCardBuilder.class);

  private final ControlCard card;


  /**
  * Constructs a new pH card builder.
  */
  public PHCardBuilder() {
    this.card = new ControlCard("pH Level");
    card.setValueText("--");
    log.debug("PHCardBuilder initialized");
    card.getStyleClass().add("sensor-card");
  }

  /**
  * Builds and returns the complete pH control card.

  * @return the fully constructed ControlCard ready for display
  */
  @Override
  public ControlCard build() {
    log.info("Building pH control card");

    // Status Label
    Label statusLabel = new Label("Status: --");
    statusLabel.getStyleClass().addAll("card-subtle", "ph-status");
    statusLabel.setAlignment(Pos.CENTER);

    // Visual representation of the ph value
    ProgressBar phBar = new ProgressBar(0.5);
    phBar.setMaxWidth(Double.MAX_VALUE);
    phBar.setPrefHeight(20);
    phBar.getStyleClass().add("ph-bar");

    // Minimum temperature the last 24h
    Label minLabel = new Label("Min: -- ");
    minLabel.getStyleClass().addAll("card-subtle", "ph-stat");
    minLabel.setAlignment(Pos.CENTER);

    // Maximum temperature the last 24h
    Label maxLabel = new Label("Max: -- ");
    maxLabel.getStyleClass().addAll("card-subtle", "ph-stat");
    maxLabel.setAlignment(Pos.CENTER);

    // Average temperature the last 24h
    Label avgLabel = new Label("Avg: -- ");
    avgLabel.getStyleClass().addAll("card-subtle", "ph-stat");
    avgLabel.setAlignment(Pos.CENTER);

    Label statsTitle = new Label("24h Statistics:");
    statsTitle.getStyleClass().add("ph-stats-title");

    VBox statsBox = new VBox(4, statsTitle, new Separator(), minLabel, avgLabel, maxLabel);
    statsBox.setAlignment(Pos.CENTER);

    Button historyButton = ButtonFactory.createHistoryButton("History");
    card.getFooter().getChildren().add(historyButton);

    card.addContent(
            statusLabel,
            new Separator(),
            phBar,
            statsBox
    );

    var controller = new PHCardController(
            card,
            statusLabel,
            phBar,
            minLabel,
            maxLabel,
            avgLabel,
            historyButton
    );
    card.setUserData(controller);

    log.debug("pH control card built successfully");

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
}