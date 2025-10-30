package edu.ntnu.bidata.smg.group8.control.ui.controller.cardcontrollers;

import edu.ntnu.bidata.smg.group8.common.util.AppLogger;
import edu.ntnu.bidata.smg.group8.control.ui.view.ControlCard;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import org.slf4j.Logger;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
* Controller for the Fertilizer control card.
* Handles fertilizer dosing operations and status updates.

* @author Andrea Sandnes
* @version 28.10.2025
*/
public class FertilizerCardController {
  private static final Logger log = AppLogger.get(FertilizerCardController.class);

  private static final int DOSE_QUICK_SMALL = 50;
  private static final int DOSE_QUICK_MEDIUM = 100;
  private static final int DOSE_QUICK_LARGE = 200;

  private static final int DOSE_LARGE = 150;
  private static final int DOSE_VERY_LARGE = 300;

  private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

  private final ControlCard card;
  private final Label statusLabel;
  private final Label lastDoseLabel;
  private final Spinner<Integer> doseSpinner;
  private final Button applyButton;
  private final Button quickDose50Button;
  private final Button quickDose100Button;
  private final Button quickDose200Button;
  private final Button scheduleButton;

  private LocalDateTime lastDoseTime = null;
  private int lastDoseAmount = 0;

  private ChangeListener<Integer> spinnerChangeListener;
  private EventHandler<ActionEvent> applyHandler;
  private EventHandler<ActionEvent> quickDose50Handler;
  private EventHandler<ActionEvent> quickDose100Handler;
  private EventHandler<ActionEvent> quickDose200Handler;


  /**
  * Creates a new FertilizerCardController with the specified UI components.
  *
  * @param card the main card container
  * @param statusLabel label displaying fertilizer system status
  * @param lastDoseLabel label displaying last dose information
  * @param doseSpinner spinner for setting custom dose amount
  * @param applyButton button to apply custom dose
  * @param quickDose50Button button for quick 50ml dose
  * @param quickDose100Button button for quick 100ml dose
  * @param quickDose200Button button for quick 200ml dose
  * @param scheduleButton button to access scheduling configuration
  */
  public FertilizerCardController(ControlCard card, Label statusLabel,
                                  Label lastDoseLabel, Spinner<Integer> doseSpinner,
                                  Button applyButton, Button quickDose50Button,
                                  Button quickDose100Button, Button quickDose200Button,
                                  Button scheduleButton) {
    this.card = card;
    this.statusLabel = statusLabel;
    this.lastDoseLabel = lastDoseLabel;
    this.doseSpinner = doseSpinner;
    this.applyButton = applyButton;
    this.quickDose50Button = quickDose50Button;
    this.quickDose100Button = quickDose100Button;
    this.quickDose200Button = quickDose200Button;
    this.scheduleButton = scheduleButton;

    log.debug("FertilizerCardController wired");
  }

  /**
  * Initializes event handlers and starts any listeners required by this controller.
  */
  public void start() {
    log.info("Starting FertilizerCardController");
    spinnerChangeListener = (obs, oldVal, newVal) -> {
      if (oldVal != null && newVal != null && !oldVal.equals(newVal)) {
        log.trace("Dose spinner value changed: {} ml -> {} ml", oldVal, newVal);
      }
    };
    doseSpinner.valueProperty().addListener(spinnerChangeListener);

    applyHandler = e -> {
      int dose = doseSpinner.getValue();
      applyDose(dose, "Manual");
    };
    applyButton.setOnAction(applyHandler);

    quickDose50Handler = e -> applyDose(DOSE_QUICK_SMALL, "Quick Dose");
    quickDose50Button.setOnAction(quickDose50Handler);

    quickDose100Handler = e -> applyDose(DOSE_QUICK_MEDIUM, "Quick Dose");
    quickDose100Button.setOnAction(quickDose100Handler);

    quickDose200Handler = e -> applyDose(DOSE_QUICK_LARGE, "Quick Dose");
    quickDose200Button.setOnAction(quickDose200Handler);

    scheduleButton.setOnAction(e -> {
      log.info("Schedule button clicked (not implemented)");
      // TODO: Open scheduling dialog
    });

    log.debug("FertilizerCardController started successfully");
  }

  /**
  * Stops this controller and cleans up resources/listeners.
  */
  public void stop() {
    log.info("Stopping FertilizerCardController");
    if (spinnerChangeListener != null) {
      doseSpinner.valueProperty().removeListener(spinnerChangeListener);
      spinnerChangeListener = null;
    }

    if (applyHandler != null) {
      applyButton.setOnAction(null);
      applyHandler = null;
    }

    if (quickDose50Handler != null) {
      quickDose50Button.setOnAction(null);
      quickDose50Handler = null;
    }

    if (quickDose100Handler != null) {
      quickDose100Button.setOnAction(null);
      quickDose100Handler = null;
    }

    if (quickDose200Handler != null) {
      quickDose200Button.setOnAction(null);
      quickDose200Handler = null;
    }

    scheduleButton.setOnAction(null);

    log.debug("FertilizerCardController stopped successfully");
  }

  /*** Applies a fertilizer dose and updates UI.
  *
  * @param amount the dose amount in milliliters
  * @param source description of the source (e.g., "Manual", "Quick Dose")
  */
  private void applyDose(int amount, String source) {
    log.info("Applying fertilizer dose: {} ml (Source: {})", amount, source);

    fx(() -> {
      // Update status to "Dosing..."
      statusLabel.setText("Status: Dosing...");
      card.setValueText("DOSING");

      // Simulate dosing operation
      Platform.runLater(() -> {
        try {
          // Simulate delay for dosing operation
          Thread.sleep(2000);

          fx(() -> {
            lastDoseTime = LocalDateTime.now();
            lastDoseAmount = amount;

            String timeStr = lastDoseTime.format(TIME_FORMAT);
            lastDoseLabel.setText(String.format("Last dose: %d ml at %s", amount, timeStr));
            statusLabel.setText("Status: Ready");
            card.setValueText("READY");

            log.info("Fertilizer dose applied successfully: {} ml at {}", amount, timeStr);
            checkDoseWarnings(amount);
          });
        } catch (InterruptedException e) {
          log.error("Dosing operation interrupted", e);
          Thread.currentThread().interrupt();
        }
      });
    });

    // TODO: Send command to backend
  }

  /**
  * Checks for dose warnings and logs them.
  *
  * @param amount the dose amount in milliliters
  */
  private void checkDoseWarnings(int amount) {
    if (amount >= DOSE_VERY_LARGE) {
      log.warn("CAUTION: Very large fertilizer dose ({} ml) - Risk of nutrient burn and runoff",
              amount);
    } else if (amount >= DOSE_LARGE) {
      log.info("NOTICE: Large fertilizer dose ({} ml) - Monitor plant response closely",
              amount);
    }
  }

  /**
  * Updates the fertilizer status externally (e.g., from backend).
  *
  * @param status the status message to display
  */
  public void updateStatus(String status) {
    log.debug("External status update: {}", status);

    fx(() -> {
      statusLabel.setText("Status: " + status);
      card.setValueText(status.toUpperCase());
    });
  }

  /**
  * Updates the last dose information externally (e.g., from backend).
  *
  * @param amount the dose amount in milliliters
  * @param time the time of the dose
  */
  public void updateLastDose(int amount, LocalDateTime time) {
    log.debug("External last dose update: {} ml at {}", amount, time);

    lastDoseAmount = amount;
    lastDoseTime = time;

    fx(() -> {
      String timeStr = time.format(TIME_FORMAT);
      lastDoseLabel.setText(String.format("Last dose: %d ml at %s", amount, timeStr));
    });
  }

  /**
  * Gets the last dose amount.
  *
  * @return the last dose amount in milliliters, or 0 if no dose has been applied
  */
  public int getLastDoseAmount() {
    return lastDoseAmount;
  }

  /**
  * Gets the time of the last dose.
  *
  * @return the time of the last dose, or null if no dose has been applied
  */
  public LocalDateTime getLastDoseTime() {
    return lastDoseTime;
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
