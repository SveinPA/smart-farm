package edu.ntnu.bidata.smg.group8.control.ui.view.cards;

import edu.ntnu.bidata.smg.group8.common.util.AppLogger;
import edu.ntnu.bidata.smg.group8.control.ui.controller.cardcontrollers.HumidityCardController;
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
* Builder for the humidity control card.
*
* <p>This builder constructs and configures a ControlCard component
* dedicated to displaying real-time humidity readings and 24h statistics.</p>
*
* @author Andrea Sandnes
* @version 28.10.2025
*/
public class HumidityCardBuilder implements CardBuilder {
  private static final Logger log = AppLogger.get(HumidityCardBuilder.class);

  private final ControlCard card;


  /**
  * Constructs a new humidity card builder.
  */
  public HumidityCardBuilder() {
    this.card = new ControlCard("Humidity");
    card.setValueText("--%");
    log.debug("HumidityCardBuilder initialized with range [0% - 100%]");
  }

  /**
  * Builds and returns the complete humidity control card.
  *
  * @return the fully constructed ControlCard ready for display
  */
  @Override
  public ControlCard build() {
    log.info("Building Humidity control card");

    Label currentLabel = new Label("Current: --%");
    currentLabel.getStyleClass().addAll("card-subtle", "humidity-current");

    ProgressBar humidityBar = new ProgressBar(0);
    humidityBar.setMaxWidth(Double.MAX_VALUE);
    humidityBar.setPrefHeight(20);
    humidityBar.getStyleClass().add("humidity-bar");

    Label minLabel = new Label("Min: --%");
    minLabel.getStyleClass().addAll("card-subtle", "humidity-stat");

    Label maxLabel = new Label("Max: --%");
    maxLabel.getStyleClass().addAll("card-subtle", "humidity-stat");

    Label avgLabel = new Label("Avg: --%");
    avgLabel.getStyleClass().addAll("card-subtle", "humidity-stat");

    Label statsTitle = new Label("24h Statistics:");
    statsTitle.getStyleClass().add("humidity-stats-title");

    HBox statsRow1 = new HBox(15, minLabel, maxLabel);
    statsRow1.setAlignment(Pos.CENTER);

    VBox statsBox = new VBox(4, statsTitle, statsRow1, avgLabel);
    statsBox.setAlignment(Pos.CENTER);

    Button historyButton = ButtonFactory.createButton("History...");
    card.getFooter().getChildren().add(historyButton);

    card.addContent(
            currentLabel,
            new Separator(),
            humidityBar,
            new Separator(),
            statsBox
    );

    var controller = new HumidityCardController(
            card,
            currentLabel,
            humidityBar,
            minLabel,
            maxLabel,
            avgLabel,
            historyButton
    );
    card.setUserData(controller);

    log.debug("Humidity control card built successfully");

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