package edu.ntnu.bidata.smg.group8.control.ui.controller.cardcontrollers;

import edu.ntnu.bidata.smg.group8.common.util.AppLogger;
import edu.ntnu.bidata.smg.group8.control.logic.command.CommandInputHandler;
import edu.ntnu.bidata.smg.group8.control.ui.view.ControlCard;
import edu.ntnu.bidata.smg.group8.control.util.UiExecutors;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Spinner;
import javafx.scene.layout.Region;
import org.slf4j.Logger;


/**
* Controller for the Fertilizer control card.
*
* <p>This class handles fertilizer dosing operations, user interactions
* and synchronization of status updates with the backend system.
* It also provides real-time nitrogen level monitoring and warnings
* for potentially harmful dose levels</p>
*
* @author Andrea Sandnes & Mona Amundsen
* @version 28.10.2025
*/
public class FertilizerCardController {
  private static final Logger log = AppLogger.get(FertilizerCardController.class);
  private static final List<String> FERT_ZONE_CLASSES = List.of(
      "fertilizer-zone-very-low",
      "fertilizer-zone-low",
      "fertilizer-zone-optimal",
      "fertilizer-zone-high",
      "fertilizer-zone-very-high"
  );

  private static final int DOSE_QUICK_SMALL = 50;
  private static final int DOSE_QUICK_MEDIUM = 100;
  private static final int DOSE_QUICK_LARGE = 200;

  private static final int DOSE_LARGE = 150;
  private static final int DOSE_VERY_LARGE = 300;

  private static final DateTimeFormatter TIME_FORMAT =
      DateTimeFormatter.ofPattern("HH:mm");

  private final ControlCard card;
  private final Label statusLabel;
  private final Label lastDoseLabel;
  private final Spinner<Integer> doseSpinner;
  private final Button applyButton;
  private final Button quickDose50Button;
  private final Button quickDose100Button;
  private final Button quickDose200Button;
  private final Button historyButton;
  private final ProgressBar nitrogenBar;
  private final List<String> changeHistory = new ArrayList<>();

  private LocalDateTime lastDoseTime = null;
  private int lastDoseAmount = 0;

  private ChangeListener<Integer> spinnerChangeListener;
  private EventHandler<ActionEvent> applyHandler;
  private EventHandler<ActionEvent> quickDose50Handler;
  private EventHandler<ActionEvent> quickDose100Handler;
  private EventHandler<ActionEvent> quickDose200Handler;

  private CommandInputHandler cmdHandler;
  private String nodeId;
  private static final String ACTUATOR_KEY = "fertilizer";

  private volatile boolean suppressSend = false;
  private double currentNitrogenLevel = 0.0;

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
  * @param historyButton button to access scheduling configuration
  * @param nitrogenBar progress bar showing current nitrogen level
  */
  public FertilizerCardController(ControlCard card, Label statusLabel,
                                  Label lastDoseLabel, Spinner<Integer> doseSpinner,
                                  Button applyButton, Button quickDose50Button,
                                  Button quickDose100Button, Button quickDose200Button,
                                  Button historyButton, ProgressBar nitrogenBar) {
    this.card = card;
    this.statusLabel = statusLabel;
    this.lastDoseLabel = lastDoseLabel;
    this.doseSpinner = doseSpinner;
    this.applyButton = applyButton;
    this.quickDose50Button = quickDose50Button;
    this.quickDose100Button = quickDose100Button;
    this.quickDose200Button = quickDose200Button;
    this.historyButton = historyButton;
    this.nitrogenBar = nitrogenBar;

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

    historyButton.setOnAction(e -> {
      log.info("History button clicked - showing fertilizer history dialog");
      showHistoryDialog();
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

    historyButton.setOnAction(null);

    log.debug("FertilizerCardController stopped successfully");
  }

  /** Applies a fertilizer dose and updates UI.
  *
  * @param amount the dose amount in milliliters
  * @param source description of the source (e.g., "Manual", "Quick Dose")
  */
  private void applyDose(int amount, String source) {
    log.info("Applying fertilizer dose: {} ml (Source: {})", amount, source);

    fx(() -> {
      statusLabel.setText("Status: Dosing...");
      card.setValueText("DOSING");
    });

    UiExecutors.schedule(() -> {
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
    }, 2, TimeUnit.SECONDS);
    sendDoseCommandIfNeeded(amount);
    addHistoryEntry(String.format("Applied %d ml (%s)", amount, source));
    log.info("Fertilizer dose noted into history");
  }

  /**
  * Sends fertilizer dose command if conditions are met.
  *
  * @param amount dose amount in milliliters
  */
  private void sendDoseCommandIfNeeded(int amount) {
    if (!suppressSend && cmdHandler != null && nodeId != null) {
      sendDoseCommandAsync(amount);
    }
  }

  /**
  * Sends a fertilizer dose command asynchronously to the backend system.
  * Runs on a separate thread to avoid blocking the JavaFX application thread.
  *
  * @param amount the dose amount in milliliters to send
  */
  private void sendDoseCommandAsync(int amount) {
    UiExecutors.execute(() -> {
      try {
        log.debug("Attempting to send fertilizer dose command nodeId={} amount={}ml",
                nodeId, amount);
        cmdHandler.setValue(nodeId, ACTUATOR_KEY, amount);
        log.info("Fertilizer dose command sent successfully nodeId={} amount={}ml",
                nodeId, amount);
      } catch (IOException e) {
        log.error("Failed to send fertilizer dose command nodeId={} amount={}ml",
                nodeId, amount, e);

        fx(() -> {
          statusLabel.setText("Status: Error - Command failed");
          card.setValueText("ERROR");
        });
      }
    });
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
  * Updates the current nitrogen level from sensor reading.
  * This is called when sensor data arrives from the backend.
  *
  * @param nitrogenPpm nitrogen level in parts per million
  */
  public void updateNitrogenLevel(double nitrogenPpm) {
    log.info("Nitrogen level updated: {} ppm", String.format("%.1f", nitrogenPpm));
    currentNitrogenLevel = nitrogenPpm;

    fx(() -> {
      double clamped = Math.max(0, Math.min(300, nitrogenPpm));
      card.setValueText(String.format("%.1f ppm", nitrogenPpm));

      if (nitrogenBar != null) {
        double progress = clamped / 300.0;
        nitrogenBar.setProgress(progress);
      }

      String zoneClass;

      if (nitrogenPpm < 50) {
        statusLabel.setText("Status: \n Very Low - Deficiency Risk");
        zoneClass = "fertilizer-very-low";
      } else if (nitrogenPpm <= 100) {
        statusLabel.setText("Status: \n Low - Supplement Recommended");
        zoneClass = "fertilizer-low";
      } else if (nitrogenPpm <= 150) {
        statusLabel.setText("Status: \n Optimal Range");
        zoneClass = "fertilizer-optimal";
      } else if (nitrogenPpm <= 200) {
        statusLabel.setText("Status: \n High - Good for Heavy Feeders");
        zoneClass = "fertilizer-high";
      } else {
        statusLabel.setText("Status: \n Very High - Burn Risk");
        zoneClass = "fertilizer-very-high";
      }

      if (nitrogenBar != null) {
        ObservableList<String> classes = nitrogenBar.getStyleClass();
        classes.removeAll(FERT_ZONE_CLASSES);
        classes.add(zoneClass);
      }
    });
  }

  /**
  * Gets the current nitrogen level from sensor.
  *
  * @return current nitrogen level from sensor
  */
  public double getCurrentNitrogenLevel() {
    return currentNitrogenLevel;
  }

  /**
  * Injects required dependencies for this fertilizer card controller.
  *
  * @param cmdHandler the command input handler
  * @param nodeId the node ID this controller manages
  */
  public void setDependencies(CommandInputHandler cmdHandler, String nodeId) {
    this.cmdHandler = Objects.requireNonNull(cmdHandler, "cmdHandler");
    this.nodeId = Objects.requireNonNull(nodeId, "nodeId");
    log.debug("FertilizerCardController dependencies injected (nodeId={})", nodeId);
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

  /**
   * Adds an entry to the fertilizer change history.
   *
   * <p>This method prepends a timestamped message to the change history list,
   * which can later be displayed in a dialog.</p>
   *
   * @param message the message describing the change
   */
  private void addHistoryEntry(String message) {
    String timestamp = LocalDateTime.now()
            .truncatedTo(ChronoUnit.SECONDS)
            .toString();
    changeHistory.addFirst(timestamp + "- " + message);
  }

  /**
   * Displays a dialog showing the fertilizer change history.
   * This dialog lists all recorded changes made during the current session.
   */
  private void showHistoryDialog() {
    Dialog<Void> dialog = new Dialog<>();
    dialog.setTitle("Fertilizer History");
    dialog.setHeaderText("Changes made during this session:");

    dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

    ListView<String> listView = new ListView<>();
    listView.getItems().setAll(changeHistory);

    int visibleRows = Math.min(changeHistory.size(), 10);
    double rowHeight = 24;

    listView.setPrefHeight(visibleRows * rowHeight);
    listView.setMinHeight(Region.USE_PREF_SIZE);
    listView.setMaxHeight(Region.USE_PREF_SIZE);

    listView.setPrefWidth(500);
    listView.setPrefHeight(300);
    dialog.getDialogPane().setPrefWidth(520);
    dialog.getDialogPane().setPrefHeight(350);

    dialog.getDialogPane().setContent(listView);
    dialog.showAndWait();
  }
}
