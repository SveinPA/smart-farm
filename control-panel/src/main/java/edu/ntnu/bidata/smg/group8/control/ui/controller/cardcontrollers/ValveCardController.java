package edu.ntnu.bidata.smg.group8.control.ui.controller.cardcontrollers;

import edu.ntnu.bidata.smg.group8.common.util.AppLogger;
import edu.ntnu.bidata.smg.group8.control.logic.command.CommandInputHandler;
import edu.ntnu.bidata.smg.group8.control.ui.controller.ControlPanelController;
import edu.ntnu.bidata.smg.group8.control.ui.view.ControlCard;
import edu.ntnu.bidata.smg.group8.control.util.UiExecutors;
import java.io.IOException;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Slider;
import javafx.scene.input.MouseEvent;
import org.slf4j.Logger;


/**
 * Controller for the Valve control card.
 * This controller coordinates the interaction between the ValveCardBuilder UI
 * and the underlying actuator logic it is responsible for - making this controller
 * responsible for connecting the UI with backend operations.
 *
 * <p>The ValveCardController manages valve opening and closing operations,
 * user interactions, and synchronization of valve state updates with the backend system.
 * It provides real-time feedback on valve position and flow rate through the UI components.</p>

 * @author Andrea Sandnes
 * @version 28.10.2025
 */
public class ValveCardController {
  private static final Logger log = AppLogger.get(ValveCardController.class);

  private static final double MAX_FLOW_RATE = 20.0;

  private int currentOpeningPercent = 0;

  private final ControlCard card;
  private final Label flowLabel;
  private final Slider openingSlider;
  private final Label sliderLabel;
  private final Button openButton;
  private final Button closeButton;
  private final ProgressBar flowIndicator;

  private CommandInputHandler cmdHandler;
  private ControlPanelController controller;
  private final String actuatorKey = "valve";

  private volatile boolean suppressSend = false;
  private volatile Integer lastSentOpening = null;

  private ChangeListener<Number> sliderValueListener;
  private EventHandler<MouseEvent> sliderMouseReleasedHandler;
  private ChangeListener<Boolean> sliderValueChangingListener;

  /**
   * Creates a new ValveCardController with the specified UI components.
   * This constructor wires together all UI elements that the controller
   * will manage throughout its lifecycle. The components are typically created
   * by the ValveCardBuilder and passed to this controller for coordinated
   * updates and event handling.
   *
   * @param card           the main card container
   * @param flowLabel      label displaying current flow rate
   * @param openButton     button to open the valve
   * @param closeButton    button to close the valve
   * @param flowIndicator  progress bar visualizing flow rate
   */
  public ValveCardController(ControlCard card, Label flowLabel,
                             ProgressBar flowIndicator, Slider openingSlider, Label sliderLabel,
                             Button openButton, Button closeButton) {
    this.card = card;
    this.flowLabel = flowLabel;
    this.openingSlider = openingSlider;
    this.sliderLabel = sliderLabel;
    this.openButton = openButton;
    this.closeButton = closeButton;
    this.flowIndicator = flowIndicator;


    log.debug("ValveCardController wired");

  }

  /**
   * Initializes event handlers and starts any listeners required by this controller.
   *
   * <p>This method sets up the necessary event listeners for user interactions
   * with the valve control UI components. It ensures that the UI reflects
   * the current state of the valve and that user actions are properly handled
   * to update both the UI and backend state as needed.</p>
   */
  public void start() {
    log.info("Starting ValveCardController");

    sliderValueListener = (obs, ov, nv) -> {
      int p = clampPercent(nv.intValue());
      sliderLabel.setText("Custom: " + p + "%");
      double flow = percentToFlow(p);
      flowLabel.setText(String.format(Locale.US, "Flow: %.1f L/min", flow));
      flowIndicator.setProgress(Math.min(1.0, flow / MAX_FLOW_RATE));
    };
    openingSlider.valueProperty().addListener(sliderValueListener);

    sliderMouseReleasedHandler = e -> {
      int p = clampPercent((int) openingSlider.getValue());

      if (currentOpeningPercent > 0) {
        applyOpeningUi(p);
        sendValveCommandIfNeeded(p);
      } else {
        if (p > 0) {
          openValveToSlider();
        } else {
          applyOpeningUi(0);
        }
      }
    };
    openingSlider.setOnMouseReleased(sliderMouseReleasedHandler);

    sliderValueChangingListener = (obs, wasChanging, isChanging) -> {
      if (!isChanging) {
        int p = clampPercent((int) openingSlider.getValue());
        if (currentOpeningPercent > 0) {
          applyOpeningUi(p);
          sendValveCommandIfNeeded(p);
        } else if (p > 0) {
          openValveToSlider();
        } else {
          applyOpeningUi(0);
        }
      }
    };
    openingSlider.valueChangingProperty().addListener(sliderValueChangingListener);

    openButton.setOnAction(e -> openValveToSlider());
    closeButton.setOnAction(e -> closeValve());

    fx(() -> {
      applyOpeningUi(0);
      closeButton.setDisable(true);
      openButton.setDisable(false);
    });

    log.debug("ValveCardController started successfully");
  }

  /**
   * Stops this controller and cleans up resources/listeners.
   *
   * <p>This method removes all event listeners and handlers that were
   * set up during the start() method. It ensures that no lingering references
   * remain, preventing potential memory leaks or unintended behavior
   * after the controller is no longer in use.</p>
   */
  public void stop() {
    log.info("Stopping ValveCardController");

    if (sliderValueListener != null) {
      openingSlider.valueProperty().removeListener(sliderValueListener);
      sliderValueListener = null;
    }
    if (sliderMouseReleasedHandler != null) {
      openingSlider.setOnMouseReleased(null);
      sliderMouseReleasedHandler = null;
    }
    if (sliderValueChangingListener != null) {
      openingSlider.valueChangingProperty().removeListener(sliderValueChangingListener);
      sliderValueChangingListener = null;
    }

    openButton.setOnAction(null);
    closeButton.setOnAction(null);

    log.debug("ValveCardController stopped successfully");
  }

  /**
   * Opens the valve to the percentage set by the slider and
   * sends the command to backend.
   *
   */
  public void openValveToSlider() {
    int p = clampPercent((int) openingSlider.getValue());
    log.info("Valve OPEN command to {}%", p);
    fx(() -> {
      applyOpeningUi(p);
      openButton.setDisable(true);
      closeButton.setDisable(false);
    });

    if (!suppressSend && cmdHandler != null && controller != null) {
      sendValveCommandIfNeeded(p);
    }
  }

  /**
   * Updates the valve opening percentage based on external input from the backend.
   *
   * <p>This method is used to synchronize the UI with the actual valve position
   * reported by the backend system. It updates the slider, labels, and flow indicator
   * to reflect the current valve opening percentage without triggering
   * a new command send, preventing echo effects.</p>
   *
   * @param percent valve opening percentage (0-100)
   */
  public void updateValvePositionExternal(int percent) {
    int p = clampPercent(percent);
    log.info("External valve opening update: {}%", p);
    Platform.runLater(() -> {
      suppressSend = true;
      applyOpeningUi(p);

      UiExecutors.schedule(() -> {
        suppressSend = false;
      }, 100, TimeUnit.MILLISECONDS);
    });
  }

  /**
   * Closes the valve and updates UI.
   *
   * <p>This method sets the valve to a closed state (0% opening),
   * updates the UI components accordingly, and sends the close command
   * to the backend if not suppressed.</p>
   */
  private void closeValve() {
    if (!suppressSend) {
      log.info("Valve CLOSE command triggered");
    }

    fx(() -> {
      applyOpeningUi(0);
      openButton.setDisable(false);
      closeButton.setDisable(true);
    });

    if (!suppressSend && cmdHandler != null && controller != null) {
      sendValveCommandIfNeeded(0);
    }
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
      int p = open ? Math.max(1, clampPercent((int) openingSlider.getValue())) : 0;
      applyOpeningUi(p);

      UiExecutors.schedule(() -> {
        suppressSend = false;
      }, 100, TimeUnit.MILLISECONDS);
    });
  }

  /**
   * Applies the specified opening percentage to all UI components.
   * This method serves as the central UI update point, ensuring that
   * all visual elements are synchronized to reflect the current
   * valve opening.

   * @param percent the valve opening percentage
   */
  private void applyOpeningUi(int percent) {
    currentOpeningPercent = percent;
    openingSlider.setValue(percent);

    if (percent == 0) {
      card.setValueText("CLOSED");
    } else {
      card.setValueText("OPEN " + percent + "%");
    }

    double flow = percentToFlow(percent);
    flowLabel.setText(String.format(Locale.US, "Flow: %.1f L/min", flow));
    flowIndicator.getStyleClass().removeAll("valve-flow-open", "valve-flow-closed");
    flowIndicator.getStyleClass().add(percent > 0 ? "valve-flow-open" : "valve-flow-closed");
    flowIndicator.setProgress(Math.min(1.0, flow / MAX_FLOW_RATE));
    log.debug("Valve UI applied: {}% (flow ~ {} L/min)", percent,
            String.format(Locale.US, "%.1f", flow));
  }


  /**
   * Sends a valve command to the backend if necessary,
   * avoiding duplicate sends.

   * @param percent the valve opening percentage to send
   */
  private void sendValveCommandIfNeeded(int percent) {
    if (suppressSend || cmdHandler == null || controller == null) {
      return;
    }

    if (lastSentOpening != null && lastSentOpening == percent) {
      log.debug("Skipping duplicate valve send ({}%)", percent);
      return;
    }
    sendValveCommandAsync(percent);
  }

  /**
   * Asynchronously sends a valve opening command to the backend.

   * @param value the valve opening percentage to send
   */
  private void sendValveCommandAsync(int value) {
    UiExecutors.execute(() -> {
      try {
        String nodeId = controller != null ? controller.getSelectedNodeId() : null;
        if (nodeId == null) {
          log.warn("Cannot send valve command: no node selected");
          return;
        }

        log.debug("Attempting to send valve command nodeId={} opening={}%", nodeId, value);
        cmdHandler.setValue(nodeId, actuatorKey, value);
        lastSentOpening = value;
        log.info("Valve command sent successfully nodeId={} opening={}%", nodeId, value);
      } catch (IOException e) {
        log.error("Failed to send valve command opening={}%", value, e);
      }
    });
  }

  /**
   * Clamps an integer value to the valid percentage range of 0-100.

   * @param v the value to clamp
   * @return the clamped value
   */
  private static int clampPercent(int v) {
    return Math.max(0, Math.min(100, v));
  }

  /**
   * Calculates the estimated flow rate for a given opening percentage.

   * @param percent the valve opening percentage
   * @return the estimated flow rate in liters per minute
   */
  private static double percentToFlow(int percent) {
    return (percent / 100.0) * MAX_FLOW_RATE;
  }

  /**
   * Ensures the given runnable executes on the JavaFX Application Thread.
   *
   * <p>If already on the FX thread, the runnable is executed immediately.
   * Otherwise, it is scheduled to run later on the FX thread.</p>
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



