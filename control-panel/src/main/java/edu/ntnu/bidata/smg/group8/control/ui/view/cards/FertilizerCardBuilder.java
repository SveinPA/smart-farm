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
import javafx.scene.layout.VBox;
import org.slf4j.Logger;

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

  private final ControlCard card;

  /**
  * Constructs a new fertilizer card builder.
  */
  public FertilizerCardBuilder() {
    this.card = new ControlCard("Fertilizer");
    card.setValueText("-- ppm");
    log.debug("FertilizerCardBuilder initialized");
    card.getStyleClass().add("sensor-card");
  }

  /**
  * Builds and returns the complete fertilizer control card.
  *
  * @return the fully constructed ControlCard ready for display
  */
  @Override
  public ControlCard build() {
    log.info("Building Fertilizer control card");

    Label statusLabel = new Label("Status: --");
    statusLabel.getStyleClass().addAll("card-subtle", "fertilizer-status");
    statusLabel.setMaxWidth(Double.MAX_VALUE);
    statusLabel.setAlignment(Pos.CENTER_LEFT);

    Label lastDoseLabel = new Label("Last dose: --");
    lastDoseLabel.getStyleClass().addAll("card-subtle", "fertilizer-last-dose");

    ProgressBar nitrogenBar = new ProgressBar(0);
    nitrogenBar.setMaxWidth(Double.MAX_VALUE);
    nitrogenBar.setPrefHeight(8);
    nitrogenBar.getStyleClass().addAll("fertilizer-bar", "fertilizer-very-low");

    // Minimum nitrogen level in the last 24h
    Label minLabel = new Label("Min: -- ppm");
    minLabel.getStyleClass().addAll("card-subtle", "fertilizer-stat");
    minLabel.setMaxWidth(Double.MAX_VALUE);
    minLabel.setAlignment(Pos.CENTER);

    // Maximum nitrogen level in the last 24h
    Label maxLabel = new Label("Max: -- ppm");
    maxLabel.getStyleClass().addAll("card-subtle", "fertilizer-stat");
    maxLabel.setMaxWidth(Double.MAX_VALUE);
    maxLabel.setAlignment(Pos.CENTER);

    // Average nitrogen level in the last 24h
    Label avgLabel = new Label("Avg: -- ppm");
    avgLabel.getStyleClass().addAll("card-subtle", "fertilizer-stat");
    avgLabel.setMaxWidth(Double.MAX_VALUE);
    avgLabel.setAlignment(Pos.CENTER);

    // Label for nitrogen statistics in the last 24h
    Label statsTitle = new Label("24h Statistics:");
    statsTitle.getStyleClass().add("fertilizer-stats-title");
    statsTitle.setMaxWidth(Double.MAX_VALUE);
    statsTitle.setAlignment(Pos.CENTER);

    VBox statsBox = new VBox(6, statsTitle, new Separator(), minLabel, avgLabel, maxLabel);
    statsBox.setAlignment(Pos.CENTER);
    statsBox.getStyleClass().add("fertilizer-stats-box");

    Button historyButton = ButtonFactory.createHistoryButton("History");
    card.getFooter().getChildren().add(historyButton);

    card.addContent(
            statusLabel,
            new Separator(),
            nitrogenBar,
            statsBox
    );

    var controller = new FertilizerCardController(
            card,
            statusLabel,
            minLabel,
            maxLabel,
            avgLabel,
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