package edu.ntnu.bidata.smg.group8.control.ui.view;

import edu.ntnu.bidata.smg.group8.common.util.AppLogger;
import edu.ntnu.bidata.smg.group8.control.ui.factory.ButtonFactory;
import edu.ntnu.bidata.smg.group8.control.ui.view.cards.FanCardBuilder;
import edu.ntnu.bidata.smg.group8.control.ui.view.cards.FertilizerCardBuilder;
import edu.ntnu.bidata.smg.group8.control.ui.view.cards.HeaterCardBuilder;
import edu.ntnu.bidata.smg.group8.control.ui.view.cards.HumidityCardBuilder;
import edu.ntnu.bidata.smg.group8.control.ui.view.cards.LightCardBuilder;
import edu.ntnu.bidata.smg.group8.control.ui.view.cards.PHCardBuilder;
import edu.ntnu.bidata.smg.group8.control.ui.view.cards.TemperatureCardBuilder;
import edu.ntnu.bidata.smg.group8.control.ui.view.cards.ValveCardBuilder;
import edu.ntnu.bidata.smg.group8.control.ui.view.cards.WindSpeedCardBuilder;
import edu.ntnu.bidata.smg.group8.control.ui.view.cards.WindowsCardBuilder;
import java.util.ArrayList;
import java.util.List;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import org.slf4j.Logger;


/**
 * This class represents the Control Panel display of the greenhouse monitoring
 * and control application. It provides a graphical user interface for monitoring
 * environmental sensors and controlling various actuators in real-time.
 * The control panel is organized as a grid of interactive cards, each representing
 * a specific sensor or actuator in the greenhouse system. Users can monitor current
 * values and adjust settings through intuitive controls.
 *
 * @author Andrea Sandnes
 * @version 16.10.2025
 */
public class ControlPanelView {
  private static final Logger log = AppLogger.get(ControlPanelView.class);

  private final BorderPane rootNode;
  private Button returnButton;

  private GridPane cardGrid;
  private List<StackPane> cards;

  private ControlCard temperatureCard;
  private ControlCard humidityCard;
  private ControlCard phCard;
  private ControlCard windSpeedCard;
  private ControlCard lightCard;
  private ControlCard windowsCard;
  private ControlCard fanCard;
  private ControlCard heaterCard;
  private ControlCard valveCard;
  private ControlCard fertilizerCard;


  /**
   * Constructs a new Control Panel View with all sensor and actuator cards.
   *
   * <p>This constructor initializes the complete user interface, including:</p>
   * <ul>
   *     <li>Header with application title</li>
   *     <li>Grid layout with three columns</li>
   *     <li>All sensor and actuator control cards</li>
   *     <li>Scrollable surface for overflow content</li>
   * </ul>
   *
   * <p>The cards are automatically arranged in a three-column grid layout,
   * with responsive sizing to accommodate different screen dimensions.</p>
   */
  public ControlPanelView() {
    log.info("Initializing ControlPanelView");
    this.rootNode = new BorderPane();

    //----------------------------- Upper Border Section ----------------------------//
    returnButton = ButtonFactory.createReturnButton("Return");
    returnButton.setId("return-button");

    Text title = new Text("Control Panel");
    title.setId("upper-border-title");

    StackPane header = new StackPane();
    header.setPadding(new Insets(35, 50, 35, 40));

    StackPane.setAlignment(title, Pos.CENTER);
    StackPane.setAlignment(returnButton, Pos.CENTER_RIGHT);
    header.getChildren().addAll(title, returnButton);
    rootNode.setTop(header);

    //----------------------------- Grid Layout ----------------------------//
    GridPane grid = new GridPane();
    grid.setAlignment(Pos.CENTER);
    grid.setHgap(24);
    grid.setVgap(24);
    grid.setPadding(new Insets(30, 40, 40, 40));
    grid.setMaxWidth(Double.MAX_VALUE);

    ColumnConstraints c1 = new ColumnConstraints();
    ColumnConstraints c2 = new ColumnConstraints();
    ColumnConstraints c3 = new ColumnConstraints();
    c1.setPercentWidth(33.333);
    c2.setPercentWidth(33.333);
    c3.setPercentWidth(33.333);
    c1.setHgrow(Priority.ALWAYS);
    c2.setHgrow(Priority.ALWAYS);
    c3.setHgrow(Priority.ALWAYS);
    grid.getColumnConstraints().addAll(c1, c2, c3);

    log.debug("Grid layout configured with 3 columns");

    this.cardGrid = grid;
    this.cards = new ArrayList<>();

    //----------------------------- Scrollable Surface ----------------------------//
    StackPane surface = new StackPane(grid);
    surface.getStyleClass().add("control-surface");
    surface.setPadding(new Insets(32));
    log.debug("Creating control cards(surface)");

    ScrollPane scroll = new ScrollPane(surface);
    scroll.setFitToWidth(true);
    scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
    scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
    scroll.setPannable(true);
    scroll.getStyleClass().add("transparent-scroll");

    scroll.maxWidthProperty().bind(rootNode.widthProperty().multiply(0.82));
    surface.maxWidthProperty().bind(scroll.widthProperty().subtract(24));

    StackPane centerWrapper = new StackPane(scroll);
    centerWrapper.setAlignment(Pos.TOP_CENTER);
    centerWrapper.setPadding(new Insets(24, 24, 40, 24));
    BorderPane.setMargin(centerWrapper, new Insets(0, 0, 0, 0));

    rootNode.setCenter(centerWrapper);

    log.debug("Scrollable surface configured");
    log.info("ControlPanelView initialization completed successfully");
  }
  //----------------------------- CREATE CARDS ----------------------------//

  /**
   * Adds a sensor control card to the grid based on the specified sensor type.
   *
   * <p>This method is used to build/create and add sensor control cards to the control panel
   * grid layout. Each sensor type corresponds to a specific control card
   * that displays real-time data and statistics for that sensor.</p>
   *
   * <p>The method supports the following sensor types:
   * "temp" (Temperature), "hum" (Humidity), "ph" (pH Level), "wind" (Wind Speed),
   * "light" (Light Intensity), "fert" (Fertilizer Level). If an
   * unknown sensor type is provided, a warning is logged.</p>
   *
   * @param sensorType the type of sensor for which to create and add a control card
   */
  public void addSensorCard(String sensorType) {
    ControlCard card = null;

    switch (sensorType.toLowerCase()) {
      case "temp":
        TemperatureCardBuilder temperatureBuilder = new TemperatureCardBuilder();
        temperatureCard = temperatureBuilder.build();
        card = temperatureCard;
        log.info("Temperature card created and added");
        break;

      case "hum":
        HumidityCardBuilder humidityBuilder = new HumidityCardBuilder();
        humidityCard = humidityBuilder.build();
        card = humidityCard;
        log.info("Humidity card created and added");
        break;

      case "ph":
        PHCardBuilder phBuilder = new PHCardBuilder();
        phCard = phBuilder.build();
        card = phCard;
        log.info("pH card created and added");
        break;

      case "wind":
        WindSpeedCardBuilder windSpeedBuilder = new WindSpeedCardBuilder();
        windSpeedCard = windSpeedBuilder.build();
        card = windSpeedCard;
        log.info("Wind Speed card created and added");
        break;

      case "light":
        LightCardBuilder lightBuilder = new LightCardBuilder();
        lightCard = lightBuilder.build();
        card = lightCard;
        log.info("Light card created and added");
        break;

      case "fert":
        FertilizerCardBuilder fertilizerBuilder = new FertilizerCardBuilder();
        fertilizerCard = fertilizerBuilder.build();
        card = fertilizerCard;
        log.info("Fertilizer card created and added");
        break;

      default:
        log.warn("Unknown sensor type: {}", sensorType);
        return;
    }
    if (card != null) {
      addCardToGrid(card);
    }
  }

  /**
   * Adds an actuator control card to the grid based on the specified actuator type.
   *
   * <p>This method is used to build/create and add actuator control cards to the control panel
   * grid layout. Each actuator type corresponds to a specific control card
   * that allows users to interact with and manage the actuator's settings.</p>
   *
   * <p>The method supports the following actuator types:
   * "windows" (Window Control), "fan" (Fan Control), "heater" (Heater Control),
   * "valve" (Valve Control). If an unknown actuator type is provided, a warning is logged.</p>
   *
   * @param actuatorType the type of actuator for which to create and add a control card
   */
  public void addActuatorCard(String actuatorType) {
    ControlCard card = null;

    switch (actuatorType.toLowerCase()) {
      case "windows":
        WindowsCardBuilder windowsBuilder = new WindowsCardBuilder();
        windowsCard = windowsBuilder.build();
        card = windowsCard;
        log.info("Windows card created and added");
        break;

      case "fan":
        FanCardBuilder fanBuilder = new FanCardBuilder();
        fanCard = fanBuilder.build();
        card = fanCard;
        log.info("Fan card created and added");
        break;

      case "heater":
        HeaterCardBuilder heaterBuilder = new HeaterCardBuilder();
        heaterCard = heaterBuilder.build();
        card = heaterCard;
        log.info("Heater card created and added");
        break;

      case "valve":
        ValveCardBuilder valveBuilder = new ValveCardBuilder();
        valveCard = valveBuilder.build();
        card = valveCard;
        log.info("Valve card created and added");
        break;

      default:
        log.warn("Unknown actuator type: {}", actuatorType);
        return;
    }
    if (card != null) {
      addCardToGrid(card);
    }
  }

  /**
   * Adds a ControlCard to the grid layout in the next available position.
   *
   * <p>The grid is organized in three columns, and cards are added
   * row by row. This method calculates the appropriate column
   * and row indices based on the current number of cards.</p>
   *
   * @param card the ControlCard to add to the grid
   */
  private void addCardToGrid(ControlCard card) {
    int position = cards.size();
    int col = position % 3;
    int row = position / 3;

    GridPane.setHgrow(card, Priority.ALWAYS);
    GridPane.setVgrow(card, Priority.NEVER);
    cardGrid.add(card, col, row);
    cards.add(card);
    log.debug("Card added at [{}, {}], total cards: {}", col, row, cards.size());
  }

  /**
   * Re-arranges the visible cards in the grid layout.
   *
   * <p>This method iterates through all cards and updates their
   * positions based on their visibility status. Only managed
   * (cards that are visible) are considered for layout.</p>
   *
   * <p>This method helps maintain a compact grid layout when cards
   * are shown or hidden dynamically.</p>
   */
  public void reLayoutVisibleCards() {
    int position = 0;
    
    for (StackPane card : cards) {
      if (!card.isManaged()) {
        continue;
      }

      int col = position % 3;
      int row = position / 3;

      GridPane.setColumnIndex(card, col);
      GridPane.setRowIndex(card, row);

      position++;
    }
  }

  /**
   * Gets the root node of the control panel view.
   *
   * <p>This method provides access to the main BorderPane container
   * that holds all UI components. The root node can be added to a
   * Scene for display in a JavaFX application.</p>
   *
   * @return the root BorderPane node containing the complete control panel interface
   */
  public BorderPane getRootNode() {
    return rootNode;
  }

  //----------------------------- Card getters ----------------------------//

  /**
   * Gets the wind speed card.
   *
   * @return the wind speed ControlCard instance
   */
  public ControlCard getWindSpeedCard() {
    return windSpeedCard;
  }

  /**
   * Gets the windows card.
   *
   * @return the windows ControlCard instance
   */
  public ControlCard getWindowsCard() {
    return windowsCard;
  }

  /**
   * Gets the valve card.
   *
   * @return the valve ControlCard instance
   */
  public ControlCard getValveCard() {
    return valveCard;
  }

  /**
   * Gets the temperature card.
   *
   * @return the temperature ControlCard instance
   */
  public ControlCard getTemperatureCard() {
    return temperatureCard;
  }

  /**
   * Gets the pH card.
   *
   * @return the pH ControlCard instance
   */
  public ControlCard getPHCard() {
    return phCard;
  }

  /**
   * Gets the light card.
   *
   * @return the light ControlCard instance
   */
  public ControlCard getLightCard() {
    return lightCard;
  }

  /**
   * Gets the humidity card.
   *
   * @return the humidity ControlCard instance
   */
  public ControlCard getHumidityCard() {
    return humidityCard;
  }

  /**
   * Gets the heater card.
   *
   * @return the heater ControlCard instance
   */
  public ControlCard getHeaterCard() {
    return heaterCard;
  }

  /**
   * Gets the fertilizer card.
   *
   * @return the heater ControlCard instance
   */
  public ControlCard getFertilizerCard() {
    return fertilizerCard;
  }

  /**
   * Gets the fan card.
   *
   * @return the fan ControlCard instance
   */
  public ControlCard getFanCard() {
    return fanCard;
  }

  /**
   * Gets the return button from the header.
   *
   * @return the return Button instance
   */
  public Button getReturnButton() {
    return returnButton;
  }
}

