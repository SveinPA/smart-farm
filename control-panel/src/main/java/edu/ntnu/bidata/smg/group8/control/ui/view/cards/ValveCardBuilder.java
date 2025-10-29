package edu.ntnu.bidata.smg.group8.control.ui.view.cards;

import edu.ntnu.bidata.smg.group8.common.util.AppLogger;
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
* Builder for the valve control card.
* This builder creates a control card for managing greenhouse valves,
*

* @author Andrea Sandnes
* @version 27.10.2025
*/
public class ValveCardBuilder implements CardBuilder {
  private static final Logger log = AppLogger.get(ValveCardBuilder.class);

  private final ControlCard card;
  private Label stateLabel;
  private Label flowLabel;
  private Button openButton;
  private Button closeButton;
  private ProgressBar flowIndicator;
  private Button scheduleButton;

  private static final double MAX_FLOW_RATE = 20.0;
  private static final double OPEN_FLOW_RATE = 15.0;
  private static final double FLOW_WARNING_THRESHOLD = 18.0;

  private boolean currentState = false;

  /**
  * Constructs a new valve card builder.
  */
  public ValveCardBuilder() {
    this.card = new ControlCard("Valve");
    card.setValueText("CLOSED");
    log.debug("ValveCardBuilder initialized with default state: CLOSED");
  }

  /**
  * Builds and returns the complete valve control card.

  * @return the fully constructed ControlCard ready for display
  */
  @Override
  public ControlCard build() {
    log.info("Building valve control card");
    createStateLabel();
    createFlowLabel();
    createFlowIndicator();
    createControlButtons();
    createFooter();

    card.addContent(
            stateLabel,
            flowLabel,
            flowIndicator,
            new Separator(),
            createButtonBox()
    );

    log.debug("Valve control card built successfully");

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

  /**
  * Creates the state label.
  */
  private void createStateLabel() {
    stateLabel = new Label("Status: CLOSED");
    stateLabel.getStyleClass().add("card-subtle");
    stateLabel.setStyle("-fx-font-weight: bold;");
    log.trace("State label created");
  }

  /**
  * Creates the flow rate label.
  */
  private void createFlowLabel() {
    flowLabel = new Label("Flow: 0 L/min");
    flowLabel.getStyleClass().add("card-subtle");
    flowLabel.setStyle("-fx-font-size: 11px;");
    log.trace("Flow rate label created");
  }

  /**
  * Creates the flow indicator progress bar.
  */
  private void createFlowIndicator() {
    flowIndicator = new ProgressBar(0);
    flowIndicator.setMaxWidth(Double.MAX_VALUE);
    flowIndicator.setPrefHeight(15);
    flowIndicator.setStyle("-fx-accent: #2196f3;");
    log.trace("Flow indicator progress bar created");
  }

  /**
  * Creates the open and close control buttons.
  */
  private void createControlButtons() {
    openButton = ButtonFactory.createButton("Open", 100);
    openButton.setStyle("-fx-background-color: #4caf50; -fx-text-fill: white;");
    openButton.setOnAction(e -> openValve());

    closeButton = ButtonFactory.createButton("Close", 100);
    closeButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");
    closeButton.setOnAction(e -> closeValve());

    log.trace("Control buttons (Open/Close) created");
  }

  /**
  * Creates the button box with open/close buttons.
  *
  * @return VBox containing control buttons
  */
  private VBox createButtonBox() {
    Label actionLabel = new Label("Quick actions:");
    actionLabel.setStyle("-fx-font-size: 11px; -fx-font-weight: bold;");

    HBox buttonRow = new HBox(10, openButton, closeButton);
    buttonRow.setAlignment(Pos.CENTER);

    VBox buttonBox = new VBox(6, actionLabel, buttonRow);
    buttonBox.setAlignment(Pos.CENTER_LEFT);
    return buttonBox;
  }

  /**
  * Creates the footer with schedule button.
  */
  private void createFooter() {
    scheduleButton = ButtonFactory.createButton("Schedule...");
    card.getFooter().getChildren().add(scheduleButton);
    log.trace("Footer with schedule button created");
  }

  /**
  * Opens the valve and updates UI.
  */
  private void openValve() {
    log.info("Valve OPEN command triggered");

    Runnable ui = () -> {
      if (stateLabel == null || flowIndicator == null) {
        log.warn("openValve called before build() - skipping UI update");
        return;
      }

      card.setValueText("OPEN");
      stateLabel.setText("Status: OPEN");
      stateLabel.setStyle("-fx-text-fill: #4caf50; -fx-font-weight: bold;");
      flowIndicator.setProgress(1.0);
      flowIndicator.setStyle("-fx-accent: #4caf50;");
      flowLabel.setText(String.format("Flow: %.1f L/min", OPEN_FLOW_RATE));

      // Visual feedback
      openButton.setDisable(true);
      closeButton.setDisable(false);

      currentState = true;
      log.debug("Valve opened - Flow rate set to {:.1f} L/min", OPEN_FLOW_RATE);
    };

    if (javafx.application.Platform.isFxApplicationThread()) {
      ui.run();
    } else {
      javafx.application.Platform.runLater(ui);
    }
    }

  /**
  * Closes the valve and updates UI.
  */
  private void closeValve() {
    log.info("Valve CLOSE command triggered");

    Runnable ui = () -> {
      if (stateLabel == null || flowIndicator == null) {
        log.warn("closeValve called before build() - skipping UI update");
        return;
      }

      card.setValueText("CLOSED");
      stateLabel.setText("Status: CLOSED");
      stateLabel.setStyle("-fx-text-fill: #888; -fx-font-weight: bold;");
      flowIndicator.setProgress(0);
      flowIndicator.setStyle("-fx-accent: #2196f3;");
      flowLabel.setText("Flow: 0 L/min");

      // Visual feedback
      openButton.setDisable(false);
      closeButton.setDisable(true);

      currentState = false;
      log.debug("Valve closed - Flow rate set to 0 L/min");
    };

    if (javafx.application.Platform.isFxApplicationThread()) {
    ui.run();
    } else {
      javafx.application.Platform.runLater(ui);
    }
  }


  /**
  * Updates the flow rate display.
  *
  * @param flowRate the flow rate in liters per minute
  */
  public void updateFlowRate(double flowRate) {
    log.debug("Updating flow rate to: {:.2f} L/min", flowRate);

    Runnable ui = () -> {
      if (flowLabel == null || flowIndicator == null) {
        log.warn("updateFlowRate called before build() - skipping UI update");
        return;
      }

      flowLabel.setText(String.format("Flow: %.1f L/min", flowRate));

      double progress = Math.min(flowRate / MAX_FLOW_RATE, 1.0);
      flowIndicator.setProgress(progress);

      log.trace("Flow indicator updated: {:.2f} progress ({:.1f}/{:.1f} L/min)",
              progress, flowRate, MAX_FLOW_RATE);

      // Warn about high flow rates
      if (flowRate > FLOW_WARNING_THRESHOLD) {
        log.warn("High flow rate detected: {:.1f} L/min (threshold: {:.1f} L/min)",
                flowRate, FLOW_WARNING_THRESHOLD);
      }

      // Detect anomalies
      if (currentState && flowRate < 1.0) {
        log.warn("ANOMALY: Valve is OPEN but flow rate is very low ({:.1f} L/min) - Possible blockage or leak",
                flowRate);
      } else if (!currentState && flowRate > 1.0) {
        log.warn("ANOMALY: Valve is CLOSED but flow detected ({:.1f} L/min) - Possible valve malfunction",
                flowRate);
      }
    };

    if (javafx.application.Platform.isFxApplicationThread()) {
      ui.run();
    } else {
      javafx.application.Platform.runLater(ui);
    }
    }

  /**
  * Sets the valve state and updates UI accordingly.
  *
  * @param isOpen true if valve is open, false if closed
  */
  public void setValveState(boolean isOpen) {
    log.info("Setting valve state to: {}", isOpen ? "OPEN" : "CLOSED");

    if (isOpen) {
      openValve();
    } else {
      closeValve();
    }
  }

  // Getters for controller access

  /**
  * Gets the state label.
  *
  * @return the state label
  */
  public Label getStateLabel() {
    return stateLabel;
  }

  /**
  * Gets the flow rate label.
  *
  * @return the flow label
  */
  public Label getFlowLabel() {
    return flowLabel;
  }

  /**
  * Gets the open button.
  *
  * @return the open button
  */
  public Button getOpenButton() {
    return openButton;
  }

  /**
  * Gets the close button.
  *
  * @return the close button
  */
  public Button getCloseButton() {
    return closeButton;
  }

  /**
  * Gets the flow indicator progress bar.
  *
  * @return the flow indicator
  */
  public ProgressBar getFlowIndicator() {
    return flowIndicator;
  }

  /**
  * Gets the schedule button.
  *
  * @return the schedule button
  */
  public Button getScheduleButton() {
    return scheduleButton;
  }
}