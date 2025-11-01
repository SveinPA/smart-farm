package edu.ntnu.bidata.smg.group8.control.ui.controller.cardcontrollers;

import edu.ntnu.bidata.smg.group8.common.util.AppLogger;
import edu.ntnu.bidata.smg.group8.control.logic.command.CommandInputHandler;
import edu.ntnu.bidata.smg.group8.control.ui.view.ControlCard;
import java.io.IOException;
import java.util.Objects;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import org.slf4j.Logger;

/**
* Controller for the Valve control card.
* This controller coordinates the interaction between the ValveCardBuilder UI
* and the underlying actuator logic it is responsible for.

* @author Andrea Sandnes
* @version 28.10.2025
*/
public class ValveCardController {
  private static final Logger log = AppLogger.get(ValveCardController.class);

  private static final double MAX_FLOW_RATE = 20.0;
  private static final double OPEN_FLOW_RATE = 15.0;
  private static final double FLOW_WARNING_THRESHOLD = 18.0;

  private boolean currentState = false;

  private final ControlCard card;
  private Label stateLabel;
  private Label flowLabel;
  private Button openButton;
  private Button closeButton;
  private ProgressBar flowIndicator;
  private Button scheduleButton;

  private CommandInputHandler cmdHandler;
  private String nodeId;
  private final String actuatorKey = "valve";

  private volatile boolean suppressSend = false;
  private Boolean lastSentState = null;


  /**
   * Creates a new ValveCardController with the specified UI components.
   * This constructor wires together all UI elements that the controller
   * will manage throughout its lifecycle. The components are typically created
   * by the ValveCardBuilder and passed to this controller for coordinated
   * updates and event handling.
   *
   * @param card           the main card container
   * @param stateLabel     label displaying Valve state (OPEN/CLOSED)
   * @param flowLabel      label displaying current flow rate
   * @param openButton     button to open the valve
   * @param closeButton    button to close the valve
   * @param flowIndicator  progress bar visualizing flow rate
   * @param scheduleButton button for accessing schedule configuration
   */
  public ValveCardController(ControlCard card, Label stateLabel, Label flowLabel,
                             ProgressBar flowIndicator, Button openButton,
                             Button closeButton, Button scheduleButton) {
    this.card = card;
    this.stateLabel = stateLabel;
    this.flowLabel = flowLabel;
    this.openButton = openButton;
    this.closeButton = closeButton;
    this.flowIndicator = flowIndicator;
    this.scheduleButton = scheduleButton;


    log.debug("ValveCardController wired");

  }

  /**
   * Initializes event handlers and starts any listeners required by this controller.
   */
  public void start() {
    log.info("Starting ValveCardController");

    openButton.setOnAction(e -> openValve());
    closeButton.setOnAction(e -> closeValve());

    scheduleButton.setOnAction(e -> {
      log.info("Schedule button clicked");
      //TODO: Implement actions
    });
    fx(() -> {
      closeButton.setDisable(true);
      openButton.setDisable(false);
    });

    log.debug("ValveCardController started successfully");
  }

  /**
   * Stops this controller and cleans up resources/listeners.
   */
  public void stop() {
    log.info("Stopping ValveCardController");

    openButton.setOnAction(null);
    closeButton.setOnAction(null);
    scheduleButton.setOnAction(null);

    log.debug("ValveCardController stopped successfully");
  }

  /**
  * Opens the valve and updates UI.
  */
  private void openValve() {
    if (!suppressSend) {
      log.info("Valve OPEN command triggered");
    }

    fx(() -> {
      card.setValueText("OPEN");
      stateLabel.setText("Status: OPEN");
      stateLabel.getStyleClass().removeAll("valve-closed", "valve-open");
      stateLabel.getStyleClass().add("valve-open");

      flowIndicator.setProgress(1.0);
      flowIndicator.getStyleClass().removeAll("valve-flow-closed", "valve-flow-open");
      flowIndicator.getStyleClass().add("valve-flow-open");

      flowLabel.setText(String.format("Flow: %.1f L/min", OPEN_FLOW_RATE));

      openButton.setDisable(true);
      closeButton.setDisable(false);

      if (!suppressSend) {
        log.debug("Valve opened - Flow rate set to {} L/min",
                String.format("%.1f", OPEN_FLOW_RATE));
      }
    });

    if (!suppressSend && cmdHandler != null && nodeId != null) {
      if (lastSentState != null && lastSentState) {
        log.debug("Skipping duplicate valve OPEN send");
        return;
      }
      sendValveCommandAsync(true);
    }
  }

  /**
  * Closes the valve and updates UI.
  */
  private void closeValve() {
    if (!suppressSend) {
      log.info("Valve CLOSE command triggered");
    }

    fx(() -> {
      card.setValueText("CLOSE");
      stateLabel.setText("Status: CLOSE");
      stateLabel.getStyleClass().removeAll("valve-closed", "valve-open");
      stateLabel.getStyleClass().add("valve-closed");

      flowIndicator.setProgress(1.0);
      flowIndicator.getStyleClass().removeAll("valve-flow-closed", "valve-flow-open");
      flowIndicator.getStyleClass().add("valve-flow-closed");

      flowLabel.setText(String.format("Flow: 0 L/min"));

      openButton.setDisable(false);
      closeButton.setDisable(true);

      currentState = false;

      if (!suppressSend) {
        log.debug("Valve closed - Flow rate set to 0 L/min");
      }
    });

    if (!suppressSend && cmdHandler != null && nodeId != null) {
      if (lastSentState != null && !lastSentState) {
        log.debug("Skipping duplicate valve CLOSE send");
        return;
      }
      sendValveCommandAsync(false);
    }
  }

  /**
  * Sends valve command asynchronously to avoid blocking UI thread.
  *
  * @param open true to open valve, false to close
  */
  private void sendValveCommandAsync(boolean open) {
    new Thread(() -> {
      try {
        String state = open ? "1" : "0"; // 1 = OPEN, 0 = CLOSED
        log.debug("Attempting to send valve command nodeId={} "
                + "state={}", nodeId, open ? "OPEN" : "CLOSED");
        cmdHandler.setValue(nodeId, actuatorKey, Integer.parseInt(state));
        lastSentState = open;
        log.info("Valve command sent successfully nodeId={} "
                + "state={}", nodeId, open ? "OPEN" : "CLOSED");
      } catch (IOException e) {
        log.error("Failed to send valve command nodeId={} "
                + "state={}", nodeId, open ? "OPEN" : "CLOSED", e);
      }
    }, "valve-cmd-send").start();
  }

  /**
  * Updates the valve state externally (e.g., from backend).
  * Does not trigger a new command send (prevents echo).
  *
  * @param open true if valve should be open, false if closed
  */
  public void updateValveState(boolean open) {
    log.info("External valve state update: {}", open ? "OPEN" : "CLOSED");
    Platform.runLater(() -> {
      suppressSend = true;
      setValveState(open);
      new Thread(() -> {
        try {
          Thread.sleep(100);
        } catch (InterruptedException ignored) {}
        suppressSend = false;
      }).start();
    });
  }

  /**
  * Updates the flow rate display and checks for anomalies.
  *
  * @param flowRate the flow rate in liters per minute
  */
  public void updateFlowRate(double flowRate) {
    log.debug("Updating flow rate to: {} L/min", String.format("%.2f", flowRate));

    fx(() -> {
      flowLabel.setText(String.format("Flow: %.1f L/min", flowRate));

      double progress = Math.min(flowRate / MAX_FLOW_RATE, 1.0);
      flowIndicator.setProgress(progress);

      log.trace("Flow indicator updated: {} progress ({}/{} L/min)",
              String.format("%.2f", progress),
              String.format("%.1f", flowRate),
              String.format("%.1f", MAX_FLOW_RATE));

      if (flowRate > FLOW_WARNING_THRESHOLD) {
        log.warn("High flow rate detected: {} L/min (threshold: {} L/min)",
                String.format("%.1f", flowRate),
                String.format("%.1f", FLOW_WARNING_THRESHOLD));
      }

      if (currentState && flowRate < 1.0) {
        log.warn("ANOMALY: Valve is OPEN but flow rate is very low ({} L/min)"
                        + " - Possible blockage or leak",
                String.format("%.1f", flowRate));
      } else if (!currentState && flowRate > 1.0) {
        log.warn("ANOMALY: Valve is CLOSED but flow detected ({} L/min)"
                        + " - Possible valve malfunction",
                String.format("%.1f", flowRate));
      }
    });
  }

  /**
  * Sets the valve state and updates UI accordingly.
  *
  * @param isOpen true if valve is open, false if closed
  */
  public void setValveState(boolean isOpen) {
    if (!suppressSend) {
      log.info("Setting valve state to: {}", isOpen ? "OPEN" : "CLOSED");
    }

    if (isOpen) {
      openValve();
    } else {
      closeValve();
    }
  }

  /**
  * Gets the current valve state.
  *
  * @return true if valve is open, false if closed
  */
  public boolean isOpen() {
    return currentState;
  }

  /**
  * Injects required dependencies for this valve card controller.
  *
  * @param cmdHandler the command input handler
  * @param nodeId the node ID this controller manages
  */
  public void setDependencies(CommandInputHandler cmdHandler, String nodeId) {
    this.cmdHandler = Objects.requireNonNull(cmdHandler, "cmdHandler");
    this.nodeId = Objects.requireNonNull(nodeId, "nodeId");
    log.debug("ValveCardController dependencies injected (nodeId={})", nodeId);
  }

  /**
   * Ensures the given runnable executes on the JavaFX Application Thread.
   *
   * @param r the runnable to execute on the FX thread
   */
  private static void fx(Runnable r) {
    if (Platform.isFxApplicationThread()) {
      r.run();
    } else {
      Platform.runLater(r);
    }
  }
}



