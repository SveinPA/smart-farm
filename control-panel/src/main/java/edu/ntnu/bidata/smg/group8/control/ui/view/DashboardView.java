
package edu.ntnu.bidata.smg.group8.control.ui.view;

import edu.ntnu.bidata.smg.group8.common.util.AppLogger;
import edu.ntnu.bidata.smg.group8.control.ui.factory.ButtonFactory;
import java.util.Objects;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import org.slf4j.Logger;

/**
 * This class represents the dashboard display of the application.
 * It provides a graphical user interface for monitoring greenhouse sensors
 * and controlling various actuators such as valve and window.
 *
 * @author Andrea Sandnes & Mona Amundsen
 * @version 17.11.2025 (last updated)
 */
public class DashboardView {
  private static final Logger log = AppLogger.get(DashboardView.class);

  private final BorderPane rootNode;
  private Button controlPanelButton;
  private ToggleButton valveToggleButton;
  private ToggleButton windowToggleButton;
  private Label humidityValueLabel;
  private Label temperatureValueLabel;
  private Label lightValueLabel;

  /**
   * Constructs a new DashboardView with all UI components initialized.
   * The dashboard is divided into three main sections:
   * - Left: Status cards displaying sensor readings
   * - Center: Application Logo
   * - Right: Control buttons for actuators
   */
  public DashboardView() {
    log.info("Initializing DashboardView");

    this.rootNode = new BorderPane();

    //----------------------------- Upper Border Section ----------------------------//

    // The header bar with application title

    HBox upperBorder = new HBox();

    Text upperBorderTitle = new Text("Smart-Greenhouse");
    upperBorderTitle.setId("upper-border-title");

    upperBorder.getChildren().add(upperBorderTitle);
    upperBorder.setAlignment(Pos.CENTER);
    upperBorder.setPadding(new Insets(70, 0, 0, 0));
    upperBorder.setId("upper-border");
    rootNode.setTop(upperBorder);

    log.debug("Header section created");

    //----------------------------- Left Side - Sensor Display Section --------------//

    // Status cards with current sensor readings from the greenhouse

    VBox display = new VBox(25);
    display.setAlignment(Pos.CENTER_LEFT);
    display.setPadding(new Insets(0));

    StackPane humidityStatus = createStatusCard("Humidity:", "--", true);
    StackPane temperatureStatus = createStatusCard("Temperature:", "--", true);
    StackPane lightStatus = createStatusCard("Lights:", "OFF", true);

    display.getChildren().addAll(humidityStatus, temperatureStatus, lightStatus);

    log.debug("Sensor display section created with {} status cards", 3);

    //---------------------------- Center Section - Logo Display --------------------//

    // Application logo

    VBox centralWindow = new VBox(12);
    centralWindow.setAlignment(Pos.CENTER);
    centralWindow.setPadding(new Insets(0));

    String logoPath = "/images/smart-greenhouse-logo.png";
    ImageView smartFarmLogo;

    try {
      var logoUrl = Objects.requireNonNull(getClass().getResource(logoPath),
              "Logo image not found at " + logoPath);
      smartFarmLogo = new ImageView(new Image(logoUrl.toExternalForm(), true));
      smartFarmLogo.setPreserveRatio(true);

      // Binding the logo size to window for responsive scaling
      smartFarmLogo.fitWidthProperty().bind(rootNode.widthProperty().multiply(0.6));
      smartFarmLogo.fitHeightProperty().bind(rootNode.heightProperty().multiply(0.9));

      log.debug("Logo loaded successfully from: {}", logoPath);

    } catch (Exception e) {
      log.error("Failed to load logo from {}: {}", logoPath, e.getMessage());
      smartFarmLogo = new ImageView();
    }

    centralWindow.getChildren().add(smartFarmLogo);

    //------------------------ Right Side - Control Buttons Section ------------------//

    // Toggle buttons and control panel access for greenhouse actuators

    VBox optionsButtons = new VBox(20);
    optionsButtons.setAlignment(Pos.CENTER_RIGHT);
    optionsButtons.setPadding(new Insets(0));

    valveToggleButton = new ToggleButton("VALVE");
    valveToggleButton.setId("valve-toggle-button");
    valveToggleButton.setPrefWidth(230);
    valveToggleButton.setPrefHeight(75);
    valveToggleButton.setTextAlignment(TextAlignment.CENTER);

    ButtonFactory.attachSwitch(valveToggleButton);
    log.debug("Valve toggle button created and configured");


    // Window - Toggle button
    windowToggleButton = new ToggleButton("WINDOWS");
    windowToggleButton.setPrefWidth(230);
    windowToggleButton.setPrefHeight(75);
    windowToggleButton.setTextAlignment(TextAlignment.CENTER);
    windowToggleButton.setId("window-toggle-button");

    // Switch animation
    ButtonFactory.attachSwitch(windowToggleButton);
    log.debug("Window toggle button created and configured");

    // Control Panel Button
    controlPanelButton = new Button("CONTROL PANEL");
    controlPanelButton.setPrefWidth(230);
    controlPanelButton.setPrefHeight(75);
    controlPanelButton.setId("control-panel-button");

    controlPanelButton.setOnAction(event -> {
      log.info("User clicked Control Panel button");
    });

    log.debug("Control Panel button created");

    optionsButtons.getChildren().addAll(valveToggleButton, windowToggleButton, controlPanelButton);

    //------------------------------- Main Layout Assembly ----------------------------//

    // Combining all three sections into one main row.

    HBox centerRow = new HBox();
    centerRow.setAlignment(Pos.CENTER);
    centerRow.setSpacing(0);
    centerRow.setPadding(new Insets(0, 10, 0, 10));

    BorderPane.setMargin(centerRow, new Insets(-40, 0, 0, 0));

    VBox.setVgrow(smartFarmLogo, Priority.NEVER);

    HBox.setMargin(optionsButtons, new Insets(0, 0, 0, -120));
    HBox.setMargin(display, new Insets(0, -120, 0, 0));

    centerRow.getChildren().addAll(display, centralWindow, optionsButtons);

    rootNode.setCenter(centerRow);

    log.info("DashboardView initialization completed successfully");
  }

  /**
   *  Creates a status card for displaying sensor information.
   *
   *  <p>This method constructs a StackPane containing a VBox with
   *  title and value labels. It also optionally stores a reference
   *  to the value label for later updates.</p>

   * @param title The title/Label of the sensor (e.g., "Humidity")
   * @param valueText The initial value to display
   * @param storeLabelReference If true, stores a reference to the value label
   *                            for later updates
   * @return A StackPane containing the formatted status card
   */
  private StackPane createStatusCard(String title, String valueText, boolean storeLabelReference) {
    log.trace("Creating status card: {}", title);

    VBox vbox = new VBox(5);
    vbox.setAlignment(Pos.CENTER);

    Label titleLabel = new Label(title);
    titleLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 18));

    Label valueLabel = new Label(valueText);
    valueLabel.setFont(Font.font("Arial", FontWeight.BOLD, 26));

    if (storeLabelReference) {
      if (title.equals("Humidity:")) {
        humidityValueLabel = valueLabel;
        log.debug("Humidity label reference stored");
      } else if (title.equals("Temperature:")) {
        temperatureValueLabel = valueLabel;
        log.debug("Temperature label reference stored");
      } else if (title.equals("Lights:")) {
        lightValueLabel = valueLabel;
        log.debug("Lights label reference stored");
      }
    }

    vbox.getChildren().addAll(titleLabel, valueLabel);

    StackPane card = new StackPane(vbox);
    card.setPadding(new Insets(15));
    card.setPrefWidth(200);
    card.setPrefHeight(100);
    card.setId("display-area");

    return card;
  }

  /**
   * Updates the humidity display with a new value from the SensorNode.
   *
   * <p>This method checks if the humidity label reference is valid
   * before updating its text to avoid null pointer exceptions. If not
   * valid, it logs a warning message.</p>

   * @param humidity The humidity value to display
   */
  public void updateHumidityDisplay(String humidity) {
    log.debug("Updating humidity display to: {}", humidity);
    if (humidityValueLabel != null) {
      humidityValueLabel.setText(humidity);
    } else {
      log.warn("Cannot update humidity: label reference is null");
    }
  }

  /**
   * Updates the temperature display with a new value from the SensorNode.
   *
   * <p>This method checks if the temperature label reference is valid
   * before updating its text to avoid null pointer exceptions. If
   * not valid, it logs a warning message.</p>

   * @param temperature The temperature value to display
   */
  public void updateTemperatureDisplay(String temperature) {
    if (temperatureValueLabel != null) {
      log.debug("Updating temperature display to: {}", temperature);
      temperatureValueLabel.setText(temperature);
    } else {
      log.warn("Cannot update temperature: label reference is null");
    }
  }

  /**
   * Updates the light status display.

   * @param status The light status to display
   */
  public void updateLightStatus(String status) {
    if (lightValueLabel != null) {
      log.debug("Updating light status display to: {}", status);
      lightValueLabel.setText(status);
    } else {
      log.warn("Cannot update light status: label reference is null");
    }
  }

  /**
   * Gets the root node of the DashboardView.
   *
   * @return the BorderPane root node
   */
  public BorderPane getRootNode() {
    return rootNode;
  }

  /**
   * Gets the Control Panel button.
   *
   * @return the Control Panel Button
   */
  public Button getControlPanelButton() {
    return controlPanelButton;
  }

  /**
   * Gets the Valve toggle button.
   *
   * @return the Valve ToggleButton
   */
  public ToggleButton getValveTogglebutton() {
    return valveToggleButton;
  }

  /**
   * Gets the Windows toggle button.
   *
   * @return the Windows ToggleButton
   */
  public ToggleButton getWindowsToggleButton() {
    return windowToggleButton;
  }

}