package edu.ntnu.bidata.smg.group8.control.ui.view.cards;

import edu.ntnu.bidata.smg.group8.common.util.AppLogger;
import edu.ntnu.bidata.smg.group8.control.ui.controller.cardcontrollers.TemperatureCardController;
import edu.ntnu.bidata.smg.group8.control.ui.factory.ButtonFactory;
import edu.ntnu.bidata.smg.group8.control.ui.view.ControlCard;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Separator;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;

/**
* Builder for the temperature control card.
*
* <p>This builder constructs and configures a ControlCard component
* dedicated to displaying real-time temperature readings and 24h statistics</p>

* @author Andrea Sandnes & Mona Amundsen
* @version 27.10.2025
*/
public class TemperatureCardBuilder implements CardBuilder {
  private static final Logger log = AppLogger.get(TemperatureCardBuilder.class);

  private final ControlCard card;

  /**
  * Constructs a new temperature card builder.
  */
  public TemperatureCardBuilder() {
    this.card = new ControlCard("Temperature");
    card.setValueText("--°C");

    log.debug("TemperatureCardBuilder initialized");
    card.getStyleClass().add("sensor-card");
  }

  /**
  * Builds and returns the complete temperature control card.

  * @return the fully constructed ControlCard ready for display
  */
  @Override
  public ControlCard build() {
    log.info("Building Temperature control card");

    // Visual representation of temperature
    ProgressBar temperatureBar = new ProgressBar(0);
    temperatureBar.setMaxWidth(Double.MAX_VALUE);
    temperatureBar.setPrefHeight(20);
    temperatureBar.getStyleClass().addAll("temp-bar", "temp-cool");
    Tooltip.install(temperatureBar, new Tooltip("Current temperature status"));

    // Minimum temperature the last 24h
    Label minLabel = new Label("Min: --°C");
    minLabel.getStyleClass().addAll("card-subtle", "temp-stat");
    minLabel.setMaxWidth(Double.MAX_VALUE);
    minLabel.setAlignment(Pos.CENTER);

    // Maximum temperature the last 24h
    Label maxLabel = new Label("Max: --°C");
    maxLabel.getStyleClass().addAll("card-subtle", "temp-stat");
    maxLabel.setMaxWidth(Double.MAX_VALUE);
    maxLabel.setAlignment(Pos.CENTER);

    // Average temperature the last 24h
    Label avgLabel = new Label("Avg: --°C");
    avgLabel.getStyleClass().addAll("card-subtle", "temp-stat");
    avgLabel.setMaxWidth(Double.MAX_VALUE);
    avgLabel.setAlignment(Pos.CENTER);

    // Label for temperature the last 24h
    Label statsTitle = new Label("24h Statistics:");
    statsTitle.getStyleClass().add("temp-stats-title");
    statsTitle.setMaxWidth(Double.MAX_VALUE);
    statsTitle.setAlignment(Pos.CENTER);

    VBox statsBox = new VBox(6, statsTitle, new Separator(), minLabel, avgLabel, maxLabel);
    statsBox.setAlignment(Pos.CENTER);
    statsBox.getStyleClass().add("temp-stats-box");

    Button historyButton = ButtonFactory.createHistoryButton("History");
    card.getFooter().getChildren().add(historyButton);

    card.addContent(
            new Separator(),
            temperatureBar,
            statsBox
    );

    var controller = new TemperatureCardController(
            card,
            temperatureBar,
            minLabel,
            maxLabel,
            avgLabel,
            historyButton
    );
    card.setUserData(controller);

    log.debug("Temperature control card built successfully");
    return card;
  }

  /**
  * Gets the control card instance.

  * @return the ControlCard instance
  */
  @Override
  public ControlCard getCard() {
    return card;
  }
}