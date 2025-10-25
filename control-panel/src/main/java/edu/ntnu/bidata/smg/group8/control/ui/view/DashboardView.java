
package edu.ntnu.bidata.smg.group8.control.ui.view;
import edu.ntnu.bidata.smg.group8.control.ui.factory.ButtonFactory;

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


import java.util.Objects;


/**
* This class represents the dashboard display of the application.
* It provides a graphical user interface for monitoring greenhouse sensors
* and controlling various actuators such as lights and windows.
*/
public class DashboardView {
  private final BorderPane rootNode;

  // Labels for dynamic sensor values
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
    this.rootNode = new BorderPane();

  //----------------------------- Upper Border Section ----------------------------//

    // The header bar with application title

    HBox upperBorder = new HBox();

    Text upperBorderTitle = new Text("Smart-Greenhouse");
    upperBorderTitle.setId("upper-border-title");

    upperBorder.getChildren().add(upperBorderTitle);
    upperBorder.setAlignment(Pos.CENTER);
    upperBorder.setPadding(new Insets(70,0,0,0));
    upperBorder.setId("upper-border");
    rootNode.setTop(upperBorder);

  //----------------------------- Left Side - Sensor Display Section --------------//

    // Status cards with current sensor readings from the greenhouse

    VBox display = new VBox(25);
    display.setAlignment(Pos.CENTER_LEFT);
    display.setPadding(new Insets(0));

    StackPane humidityStatus = createStatusCard("Humidity:", "--", true);
    StackPane temperatureStatus = createStatusCard("Temperature:", "--", true);
    StackPane lightStatus = createStatusCard("Lights:", "OFF", true);

    display.getChildren().addAll(humidityStatus, temperatureStatus, lightStatus);

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

    } catch (Exception e) {
      System.err.println("Feil ved lasting av bilde: " + e.getMessage());
      smartFarmLogo = new ImageView();
    }

    centralWindow.getChildren().add(smartFarmLogo);

  //------------------------ Right Side - Control Buttons Section ------------------//

    // Toggle buttons and control panel access for greenhouse actuators

    VBox optionsButtons = new VBox(20);
    optionsButtons.setAlignment(Pos.CENTER_RIGHT);
    optionsButtons.setPadding(new Insets(0));

    // Light - Toggle Button
    ToggleButton lightButton = new ToggleButton("LIGHTS");
    lightButton.setId("light-toggle-button");

    lightButton.selectedProperty().addListener((obs, oldVal, newVal) -> {
    if (newVal) {
      System.out.println("Lights ON.");
    } else {
      System.out.println("Lights OFF");
    }
    });

    lightButton.setPrefWidth(230);
    lightButton.setPrefHeight(75);
    lightButton.setTextAlignment(TextAlignment.CENTER);

    // Switch animation
    ButtonFactory.attachSwitch(lightButton);

    // Window - Toggle button
    ToggleButton windowButton = new ToggleButton("WINDOWS");
    windowButton.selectedProperty().addListener((obs, oldVal, newVal) -> {
      if (newVal) {
        System.out.println("Windows OPEN.");
      } else {
        System.out.println("Windows CLOSE");
      }
    });

    windowButton.setPrefWidth(230);
    windowButton.setPrefHeight(75);
    windowButton.setTextAlignment(TextAlignment.CENTER);
    windowButton.setId("window-toggle-button");

    // Switch animation
    ButtonFactory.attachWindowSwitch(windowButton);

    // Control Panel Button
    Button controlPanelButton = new Button("CONTROL PANEL");
    controlPanelButton.setPrefWidth(230);
    controlPanelButton.setPrefHeight(75);
    controlPanelButton.setId("control-panel-button");

    controlPanelButton.setOnAction(event -> {
      System.out.println("Opening Control Panel..");
    });

    optionsButtons.getChildren().addAll(lightButton, windowButton, controlPanelButton);

  //------------------------------- Main Layout Assembly ----------------------------//

    // Combining all three sections into one main row.

    HBox centerRow = new HBox();
    centerRow.setAlignment(Pos.CENTER);
    centerRow.setSpacing(0);
    centerRow.setPadding(new Insets(0, 10, 0, 10));

    BorderPane.setMargin(centerRow, new Insets(-40,0,0,0));

    VBox.setVgrow(smartFarmLogo, Priority.NEVER);

    HBox.setMargin(optionsButtons, new Insets(0, 0, 0, -120));
    HBox.setMargin(display, new Insets(0, -120, 0, 0));

    centerRow.getChildren().addAll(display, centralWindow, optionsButtons);

    rootNode.setCenter(centerRow);
  }

  /**
  *  Creates a status card for displaying sensor information.

  * @param title The title/Label of the sensor (e.g., "Humidity")
  * @param valueText The initial value to display
  * @param storeLabelReference If true, stores a reference to the value label
  * for later updates
  * @return A StackPane containing the formatted status card
  */
  private StackPane createStatusCard(String title, String valueText, boolean storeLabelReference) {
    VBox vbox = new VBox(5);
    vbox.setAlignment(Pos.CENTER);

    Label titleLabel = new Label(title);
    titleLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 18));

    Label valueLabel = new Label(valueText);
    valueLabel.setFont(Font.font("Arial", FontWeight.BOLD, 26));



    if (storeLabelReference) {
      if (title.equals("Humidity:")) {
        humidityValueLabel = valueLabel;
      } else if (title.equals("Temperature:")) {
        temperatureValueLabel = valueLabel;
      } else if (title.equals("Lights:")) {
        lightValueLabel = valueLabel;
      }
    }

    vbox.getChildren().addAll(titleLabel, valueLabel);

    StackPane card = new StackPane(vbox);
    card.setPadding(new Insets(15));
    card.setPrefWidth(200);
    card.setPrefHeight(100);
    card.setStyle("-fx-background-color: #F1F6F8; "
            + "-fx-border-color: black; "
            + "-fx-border-radius: 20; "
            + "-fx-background-radius: 20;");

    return card;
  }


  /**
  * Updates the humidity display with a new value from the SensorNode.

  * @param humidity The humidity value to display
  */
  public void updateHumidity(String humidity) {
    if (humidityValueLabel != null) {
      humidityValueLabel.setText(humidity);
    }
  }

  /**
   * Updates the temperature display with a new value from the SensorNode.

   * @param temperature The temperature value to display
   */
  public void updateTemperature(String temperature) {
    if (temperatureValueLabel != null) {
      temperatureValueLabel.setText(temperature);
    }
  }

  /**
   * Updates the light status display.

   * @param status The light status to display
   */
  public void updateLightStatus(String status) {
    if (lightValueLabel != null) {
      lightValueLabel.setText(status);
    }
  }

  public BorderPane getRootNode() {
    return rootNode;
  }
}
