package edu.ntnu.bidata.smg.group8.control.ui.view.cards;


import edu.ntnu.bidata.smg.group8.control.ui.controller.cardcontrollers.WindSpeedCardController;
import edu.ntnu.bidata.smg.group8.control.ui.factory.ButtonFactory;
import edu.ntnu.bidata.smg.group8.control.ui.view.ControlCard;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Separator;
import javafx.scene.layout.HBox;
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
    Label currentLabel = new Label("Current: -- m/s");
    currentLabel.getStyleClass().addAll("card-subtle", "current-wind-speed");

    Label statusLabel = new Label("Status: --");
    statusLabel.getStyleClass().addAll("card-subtle", "wind-speed-status");

    Label gustLabel = new Label("Gust: -- m/s");
    gustLabel.getStyleClass().addAll("card-subtle", "wind-speed-gust");

    ProgressBar windBar = new ProgressBar(0);
    windBar.setMaxWidth(Double.MAX_VALUE);
    windBar.getStyleClass().add("wind-speed-bar");

    Label statsTitle = new Label("24h Statistics:");
    statsTitle.getStyleClass().add("wind-speed-stats-title");

    Label minLabel = new Label("Min: -- m/s");
    Label maxLabel = new Label("Max: -- m/s");
    Label avgLabel = new Label("Avg: -- m/s");
    minLabel.getStyleClass().add("wind-speed-stat");
    maxLabel.getStyleClass().add("wind-speed-stat");
    avgLabel.getStyleClass().add("wind-speed-stat");

    HBox statsRow1 = new HBox(15, minLabel, maxLabel);
    statsRow1.setAlignment(Pos.CENTER);

    VBox statsBox = new VBox(4, statsTitle, statsRow1, avgLabel);
    statsBox.setAlignment(Pos.CENTER);

    Button historyButton = ButtonFactory.createButton("History...");
    card.getFooter().getChildren().add(historyButton);

    card.addContent(currentLabel, statusLabel, gustLabel,
            new Separator(), windBar, statsBox);

    var controller = new WindSpeedCardController(card, currentLabel, statusLabel,
            gustLabel, windBar, minLabel, maxLabel, avgLabel);

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