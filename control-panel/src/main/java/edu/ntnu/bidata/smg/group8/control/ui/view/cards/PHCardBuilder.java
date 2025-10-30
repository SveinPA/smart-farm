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
import javafx.scene.layout.HBox;
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
  }

  /**
  * Builds and returns the complete pH control card.

  * @return the fully constructed ControlCard ready for display
  */
  @Override
  public ControlCard build() {
    log.info("Building pH control card");

    Label currentLabel = new Label("Current: --");
    currentLabel.getStyleClass().addAll("card-subtle", "ph-current");

    Label statusLabel = new Label("Status: --");
    statusLabel.getStyleClass().addAll("card-subtle", "ph-status");

    ProgressBar phBar = new ProgressBar(0.5);
    phBar.setMaxWidth(Double.MAX_VALUE);
    phBar.setPrefHeight(20);
    phBar.getStyleClass().add("ph-bar");

    Label minLabel = new Label("Min: -- m/s");
    minLabel.getStyleClass().addAll("card-subtle", "ph-stat");

    Label maxLabel = new Label("Max: -- m/s");
    maxLabel.getStyleClass().addAll("card-subtle", "ph-stat");

    Label avgLabel = new Label("Avg: -- m/s");
    avgLabel.getStyleClass().addAll("card-subtle", "ph-stat");

    Label statsTitle = new Label("24h Statistics:");
    statsTitle.getStyleClass().add("ph-stats-title");

    HBox statsRow = new HBox(15, minLabel, maxLabel);
    statsRow.setAlignment(Pos.CENTER);

    VBox statsBox = new VBox(4, statsTitle, statsRow, avgLabel);
    statsBox.setAlignment(Pos.CENTER);

    Button historyButton = ButtonFactory.createButton("History...");
    card.getFooter().getChildren().add(historyButton);

    card.addContent(
            currentLabel,
            statusLabel,
            new Separator(),
            phBar,
            statsBox
    );

    var controller = new PHCardController(
            card,
            currentLabel,
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