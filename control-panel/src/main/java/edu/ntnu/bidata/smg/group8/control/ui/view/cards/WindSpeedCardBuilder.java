package edu.ntnu.bidata.smg.group8.control.ui.view.cards;

import edu.ntnu.bidata.smg.group8.control.ui.controller.cardcontrollers.WindSpeedCardController;
import edu.ntnu.bidata.smg.group8.control.ui.factory.ButtonFactory;
import edu.ntnu.bidata.smg.group8.control.ui.view.ControlCard;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Separator;
import javafx.scene.layout.VBox;

/**
* Builder class for creating a wind speed control card.
* This builder constructs and configures a ControlCard component
* dedicated to displaying real-time and statistical wind speed data.
*
* <p>The card includes:
* <ul>
*     <li>Current wind speed</li>
*     <li>Wind status</li>
*     <li>Gust speed</li>
*     <li>A progress bar visualizing wind intensity</li>
*     <li>24-hour minimum, maximum, and average statistics</li>
*     <li>A button to view historical wind speed data</li>
* </ul>
* </p>

* @author Andrea Sandnes
* @version 27.10.2025
*/
public class WindSpeedCardBuilder implements CardBuilder {
  private final ControlCard card;

  /**
  * Constructs a new wind speed card builder.
  * Initializes the base ControlCard instance with title
  * and text.
  */
  public WindSpeedCardBuilder() {
    this.card = new ControlCard("Wind Speed");
    card.setValueText("-- m/s");
  }

  /**
  * Builds and returns the complete wind speed control card.
  * this method sets up labels, progress  bar, statistics section,
  * and the history button, and binds them to a WindSpeedCardController
  * instance.

  * @return the fully constructed ControlCard ready for display
  */
  @Override
  public ControlCard build() {

    // Status label (breeze, moderate etc.)
    Label statusLabel = new Label("Status: --");
    statusLabel.getStyleClass().addAll("card-subtle", "wind-speed-status");
    statusLabel.setMaxWidth(Double.MAX_VALUE);
    statusLabel.setAlignment(Pos.CENTER);

    // Label for showing gust
    Label gustLabel = new Label("Gust: -- m/s");
    gustLabel.getStyleClass().addAll("card-subtle", "wind-speed-gust");
    gustLabel.setMaxWidth(Double.MAX_VALUE);
    gustLabel.setAlignment(Pos.CENTER);

    // ProgressBar for visualizing the wind-intensity now
    ProgressBar windBar = new ProgressBar(0);
    windBar.setMaxWidth(Double.MAX_VALUE);
    windBar.getStyleClass().add("wind-speed-bar");

    // Header for min, avg and max labels
    Label statsTitle = new Label("24h Statistics:");
    statsTitle.getStyleClass().add("wind-speed-stats-title");

    // The lowest wind-speed the last 24 hours.
    Label minLabel = new Label("Min: -- m/s");
    minLabel.getStyleClass().add("wind-speed-stat");
    minLabel.setAlignment(Pos.CENTER);

    // The average wind-speed the last 24 hours.
    Label avgLabel = new Label("Avg: -- m/s");
    avgLabel.getStyleClass().add("wind-speed-stat");
    avgLabel.setAlignment(Pos.CENTER);

    // The highest wind-speed the last 24 hours.
    Label maxLabel = new Label("Max: -- m/s");
    maxLabel.getStyleClass().add("wind-speed-stat");
    maxLabel.setAlignment(Pos.CENTER);

    VBox currentStatsBox = new VBox(6, statusLabel, gustLabel);
    currentStatsBox.setFillWidth(true);

    VBox statsBox = new VBox(6, statsTitle, new Separator() , minLabel, avgLabel, maxLabel);
    statsBox.setAlignment(Pos.CENTER);

    Button historyButton = ButtonFactory.createHistoryButton("History");
    card.getFooter().getChildren().add(historyButton);

    // Add content to the card
    card.addContent(currentStatsBox,
            new Separator(), windBar, statsBox);

    // Connecting UI-components to the controller which updates them with live-data
    var controller = new WindSpeedCardController(card, statusLabel,
            gustLabel, windBar, minLabel, maxLabel, avgLabel,historyButton);

    // Saving controller in the card for later access
    card.setUserData(controller);

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