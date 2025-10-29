package edu.ntnu.bidata.smg.group8.control.ui.view.cards;

import edu.ntnu.bidata.smg.group8.common.util.AppLogger;
import edu.ntnu.bidata.smg.group8.control.ui.factory.ButtonFactory;
import edu.ntnu.bidata.smg.group8.control.ui.view.ControlCard;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Separator;
import javafx.scene.control.Spinner;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;

/**
* Builder for the fertilizer dispenser control card.
* This builder creates a control card for managing greenhouse fertilizer
* dispensing. It provides both manual dosing controls and tracks fertilizer
* usage and reservoir levels.
*/
public class FertilizerCardBuilder implements CardBuilder {
  private static final Logger log = AppLogger.get(FertilizerCardBuilder.class);

  private final ControlCard card;
  private Label statusLabel;
  private Label usageLabel;
  private Spinner<Integer> doseSpinner;
  private Button dispenseButton;
  private Button smallButton;
  private Button mediumButton;
  private Button largeButton;
  private ProgressBar reservoirBar;
  private Label reservoirLabel;
  private Button scheduleButton;



  /**
  * Constructs a new fertilizer card builder.
  */
  public FertilizerCardBuilder() {
    this.card = new ControlCard("Fertilizer");
    card.setValueText("IDLE");
    log.debug("FertilizerCardBuilder initialized");
  }

  /**
  * Builds and returns the complete fertilizer control card.

  * @return the fully constructed ControlCard ready for display
  */
  @Override
  public ControlCard build() {
    log.info("Building Fertilizer control card");

    createStatusLabel();
    createUsageLabel();
    createReservoirIndicator();
    createDoseSpinner();
    createPresetButtons();
    createFooter();

    card.addContent(
            statusLabel,
            usageLabel,
            new Separator(),
            createReservoirBox(),
            new Separator(),
            createDoseBox(),
            createPresetsBox()
    );

    log.debug("Fertilizer control card built successfully");
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
  * Creates the status label.
  */
  private void createStatusLabel() {
    statusLabel = new Label("Status: Ready");
    statusLabel.getStyleClass().add("card-subtle");
    statusLabel.setStyle("-fx-font-weight: bold;");
    log.trace("Status label created");
  }

  /**
  * Creates the usage label.
  */
  private void createUsageLabel() {
    usageLabel = new Label("Today: 0 ml");
    usageLabel.getStyleClass().add("card-subtle");
    usageLabel.setStyle("-fx-font-size: 11px;");
    log.trace("Usage label created");
  }

  /**
  * Creates the reservoir level indicator.
  */
  private void createReservoirIndicator() {
    reservoirBar = new ProgressBar(1.0); // Start at 100%
    reservoirBar.setMaxWidth(Double.MAX_VALUE);
    reservoirBar.setPrefHeight(15);
    reservoirBar.setStyle("-fx-accent: #4caf50;");

    reservoirLabel = new Label("Reservoir: 100%");
    reservoirLabel.getStyleClass().add("card-subtle");
    reservoirLabel.setStyle("-fx-font-size: 11px;");

    log.trace("Reservoir indicator created");
  }

  /**
  * Creates the reservoir box with bar and label.
  *
  * @return VBox containing reservoir indicator
  */
  private VBox createReservoirBox() {
    VBox reservoirBox = new VBox(4, reservoirLabel, reservoirBar);
    reservoirBox.setAlignment(Pos.CENTER_LEFT);
    return reservoirBox;
  }

  /**
  * Creates the dose spinner and dispense button.
  */
  private void createDoseSpinner() {
    doseSpinner = new Spinner<>(10, 500, 50, 10);
    doseSpinner.setEditable(true);
    doseSpinner.setPrefWidth(80);

    dispenseButton = ButtonFactory.createPrimaryButton("Dispense");
    dispenseButton.setOnAction(e -> dispenseFertilizer(doseSpinner.getValue()));

    log.trace("Dose spinner created");
  }

  /**
  * Creates the dose box with spinner and button.
  *
  * @return HBox containing dose controls
  */
  private HBox createDoseBox() {
    Label doseLabel = new Label("Custom dose:");
    Label unitLabel = new Label("ml");

    HBox doseBox = new HBox(8, doseLabel, doseSpinner, unitLabel, dispenseButton);
    doseBox.setAlignment(Pos.CENTER_LEFT);
    return doseBox;
  }

  /**
  * Creates the preset dose buttons.
  */
  private void createPresetButtons() {
    smallButton = ButtonFactory.createFullWidthButton("Small (50 ml)");
    smallButton.setOnAction(e -> dispenseFertilizer(50));

    mediumButton = ButtonFactory.createFullWidthButton("Medium (100 ml)");
    mediumButton.setOnAction(e -> dispenseFertilizer(100));

    largeButton = ButtonFactory.createFullWidthButton("Large (200 ml)");
    largeButton.setOnAction(e -> dispenseFertilizer(200));

    log.trace("Preset buttons created");
  }

  /**
  * Creates the presets box with all preset buttons.
  *
  * @return VBox containing preset buttons
  */
  private VBox createPresetsBox() {
    Label presetsLabel = new Label("Quick doses:");
    presetsLabel.setStyle("-fx-font-size: 11px; -fx-font-weight: bold;");

    VBox presetsBox = new VBox(6, presetsLabel, smallButton, mediumButton, largeButton);
    presetsBox.setAlignment(Pos.CENTER_LEFT);
    return presetsBox;
  }

  /**
  * Creates the footer with schedule button.
  */
  private void createFooter() {
    scheduleButton = ButtonFactory.createButton("Schedule...");
    card.getFooter().getChildren().add(scheduleButton);
    log.trace("Footer created");
  }

  /**
  * Dispenses fertilizer and updates UI.
  *
  * @param amount the amount in milliliters
  */
  private void dispenseFertilizer(int amount) {
    log.info("Dispensing fertilizer: {} ml", amount);

    card.setValueText("DISPENSING");
    statusLabel.setText("Status: Dispensing " + amount + " ml...");
    statusLabel.setStyle("-fx-text-fill: #2196f3; -fx-font-weight: bold;");

    // Disable buttons during dispensing
    setButtonsEnabled(false);
    log.debug("Dispenser controls disabled during operation");

  }

  /**
  * Updates the status to ready after dispensing.
  */
  public void setReady() {
    log.info("Fertilizer dispenser ready");

    card.setValueText("IDLE");
    statusLabel.setText("Status: Ready");
    statusLabel.setStyle("-fx-text-fill: #4caf50; -fx-font-weight: bold;");
    setButtonsEnabled(true);

    log.debug("Dispenser controls re-enabled");
  }

  /**
  * Updates the daily usage display.
  *
  * @param totalML the total milliliters used today
  */
  public void updateUsage(int totalML) {
    log.debug("Updating fertilizer usage: {} ml today", totalML);

    usageLabel.setText("Today: " + totalML + " ml");
  }

  /**
  * Updates the reservoir level display.
  *
  * @param percentage the reservoir fill percentage (0-100)
  */
  public void updateReservoir(double percentage) {
    log.debug("Updating reservoir level: {:.1f}%", percentage);

    double progress = percentage / 100.0;
    reservoirBar.setProgress(progress);
    reservoirLabel.setText(String.format("Reservoir: %.0f%%", percentage));

    // Update color based on level
    String color;
    if (percentage > 50) {
      color = "#4caf50"; // Green
    } else if (percentage > 20) {
      color = "#ff9800"; // Orange
      log.warn("Reservoir level low: {:.1f}%", percentage);
    } else {
      color = "#f44336"; // Red
      log.warn("Reservoir level critical: {:.1f}%", percentage);
    }
    reservoirBar.setStyle("-fx-accent: " + color + ";");

    // Disable buttons if reservoir is too low
    if (percentage < 5) {
      setButtonsEnabled(false);
      statusLabel.setText("Status: Reservoir Empty!");
      statusLabel.setStyle("-fx-text-fill: #f44336; -fx-font-weight: bold;");
      log.error("Reservoir empty - Dispenser disabled");
    }
  }

  /**
  * Enables or disables all control buttons.
  *
  * @param enabled true to enable, false to disable
  */
  private void setButtonsEnabled(boolean enabled) {
    dispenseButton.setDisable(!enabled);
    smallButton.setDisable(!enabled);
    mediumButton.setDisable(!enabled);
    largeButton.setDisable(!enabled);
    doseSpinner.setDisable(!enabled);

    log.trace("Fertilizer controls {}", enabled ? "enabled" : "disabled");
  }

  // Getters for controller access

  /**
  * Gets the status label.
  *
  * @return the status label
  */
  public Label getStatusLabel() {
    return statusLabel;
  }

  /**
  * Gets the usage label.
  *
  * @return the usage label
  */
  public Label getUsageLabel() {
    return usageLabel;
  }

  /**
  * Gets the dose spinner.
  *
  * @return the dose spinner
  */
  public Spinner<Integer> getDoseSpinner() {
    return doseSpinner;
  }

  /**
  * Gets the dispense button.
  *
  * @return the dispense button
  */
  public Button getDispenseButton() {
    return dispenseButton;
  }

  /**
  * Gets the small dose preset button.
  *
  * @return the small button
  */
  public Button getSmallButton() {
    return smallButton;
  }

  /**
  * Gets the medium dose preset button.
  *
  * @return the medium button
  */
  public Button getMediumButton() {
    return mediumButton;
  }

  /**
  * Gets the large dose preset button.
  *
  * @return the large button
  */
  public Button getLargeButton() {
    return largeButton;
  }

  /**
  * Gets the reservoir progress bar.
  *
  * @return the reservoir bar
  */
  public ProgressBar getReservoirBar() {
    return reservoirBar;
  }

  /**
  * Gets the reservoir label.
  *
  * @return the reservoir label
  */
  public Label getReservoirLabel() {
    return reservoirLabel;
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