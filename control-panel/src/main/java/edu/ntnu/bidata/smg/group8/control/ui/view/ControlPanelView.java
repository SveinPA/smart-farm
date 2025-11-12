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
import javafx.scene.layout.HBox;
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

  private TemperatureCardBuilder temperatureBuilder;
  private HumidityCardBuilder humidityBuilder;
  private PHCardBuilder phBuilder;
  private WindSpeedCardBuilder windSpeedBuilder;
  private LightCardBuilder lightBuilder;
  private WindowsCardBuilder windowsBuilder;
  private FanCardBuilder fanBuilder;
  private HeaterCardBuilder heaterBuilder;
  private ValveCardBuilder valveBuilder;
  private FertilizerCardBuilder fertilizerBuilder;

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

    //----------------------------- Create Cards ----------------------------//
    log.debug("Creating control cards");

    List<StackPane> cards = new ArrayList<>();

    temperatureBuilder = new TemperatureCardBuilder();
    temperatureCard = temperatureBuilder.build();
    cards.add(temperatureCard);
    log.trace("Temperature card created");

    humidityBuilder = new HumidityCardBuilder();
    humidityCard = humidityBuilder.build();
    cards.add(humidityCard);
    log.trace("Humidity card created");

    phBuilder = new PHCardBuilder();
    phCard = phBuilder.build();
    cards.add(phCard);
    log.trace("pH card created");

    windSpeedBuilder = new WindSpeedCardBuilder();
    windSpeedCard = windSpeedBuilder.build();
    cards.add(windSpeedCard);
    log.trace("Wind speed card created");

    lightBuilder = new LightCardBuilder();
    lightCard = lightBuilder.build();
    cards.add(lightCard);
    log.trace("Lights card created");

    windowsBuilder = new WindowsCardBuilder();
    windowsCard = windowsBuilder.build();
    cards.add(windowsCard);
    log.trace("Windows card created");

    fanBuilder = new FanCardBuilder();
    fanCard = fanBuilder.build();
    cards.add(fanCard);
    log.trace("Fan card created");

    heaterBuilder = new HeaterCardBuilder();
    heaterCard = heaterBuilder.build();
    cards.add(heaterCard);
    log.trace("Heater card created");

    valveBuilder = new ValveCardBuilder();
    valveCard = valveBuilder.build();
    cards.add(valveCard);
    log.trace("Valve card created");

    fertilizerBuilder = new FertilizerCardBuilder();
    fertilizerCard = fertilizerBuilder.build();
    cards.add(fertilizerCard);
    log.trace("Fertilizer card created");

    log.debug("All {} control cards created successfully", cards.size());

    for (StackPane card : cards) {
      GridPane.setHgrow(card, Priority.ALWAYS);
      GridPane.setVgrow(card, Priority.NEVER);
    }

    // Add cards to grid
    addCardsToGrid(grid, cards, 3);

    //----------------------------- Scrollable Surface ----------------------------//

    StackPane surface = new StackPane(grid);
    surface.getStyleClass().add("control-surface");
    surface.setPadding(new Insets(32));

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

  /**
   * Adds a list of control cards to a grid pane.
   *
   * <p>This method distributes the cards evenly across the specified number
   * of columns, calculating the appropriate row and column position for each card.</p>
   *
   * @param grid  the grid pane to add cards to
   * @param cards the list of cards to add
   * @param cols  the number of columns in the grid
   */
  private void addCardsToGrid(GridPane grid, List<StackPane> cards, int cols) {
    log.debug("Adding {} cards to grid with {} columns", cards.size(), cols);

    for (int i = 0; i < cards.size(); i++) {
      int col = i % cols;
      int row = i / cols;
      grid.add(cards.get(i), col, row);
    }

    log.trace("Cards arranged in {} rows", (cards.size() + cols - 1) / cols);
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

