package edu.ntnu.bidata.smg.group8.control.ui.view.cards;

import edu.ntnu.bidata.smg.group8.common.util.AppLogger;
import edu.ntnu.bidata.smg.group8.control.ui.controller.cardcontrollers.HumidityCardController;
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
* Builder for the humidity control card.
*
* <p>This builder constructs and configures a ControlCard component
* dedicated to displaying real-time humidity readings and 24h statistics.</p>
*
* @author Andrea Sandnes & Mona Amundsen
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
    VBox statsTitleBox = new VBox(statsTitle);
    statsTitleBox.setAlignment(Pos.CENTER);
    VBox.setMargin(statsTitleBox, new Insets(0, 0, 6, 0));

    VBox statsBox = new VBox(6, minLabel, maxLabel, avgLabel);
    statsBox.setAlignment(Pos.CENTER);
    VBox.setMargin(statsBox, new Insets(6, 0, 0, 0));

    Button historyButton = ButtonFactory.createHistoryButton("History...");
    card.getFooter().getChildren().add(historyButton);

    card.addContent(
            new Separator(),
            humidityBar,
            statsTitleBox,
            new Separator(),
            statsBox
    );

    var controller = new HumidityCardController(
            card,
            humidityBar,
            minLabel,
            maxLabel,
            avgLabel,
            historyButton,
            statsTitle
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