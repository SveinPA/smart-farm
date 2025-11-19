package edu.ntnu.bidata.smg.group8.control.ui.view.cards;

import edu.ntnu.bidata.smg.group8.common.util.AppLogger;
import edu.ntnu.bidata.smg.group8.control.ui.controller.cardcontrollers.ValveCardController;
import edu.ntnu.bidata.smg.group8.control.ui.view.ControlCard;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Separator;
import javafx.scene.control.Slider;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;

/**
 * Builder for the valve control card.
 *
 * <p>This builder constructs and configures a ControlCard component
 * dedicated to displaying and controlling greenhouse valve state and
 * flow rate.</p>
 *
 * <h2>The valve control card includes the following features:</h2>
 * <ul>
 *     <li>Real-time valve state display (OPEN/CLOSED).</li>
 *     <li>Flow rate indicator with a progress bar.</li>
 *     <li>Slider to customize valve opening percentage.</li>
 *     <li>Buttons to open or close the valve.</li>
 * </ul>
 *
 * @author Andrea Sandnes
 * @version 27.10.2025
 */
public class ValveCardBuilder implements CardBuilder {
  private static final Logger log = AppLogger.get(ValveCardBuilder.class);

  private final ControlCard card;

  /**
   * Constructs a new valve card builder.
   */
  public ValveCardBuilder() {
    this.card = new ControlCard("Valve");
    card.setValueText("CLOSED");
    log.debug("ValveCardBuilder initialized with default state: CLOSED");
    card.getStyleClass().add("actuator-card");
  }

  /**
   * Builds and returns the complete valve control card.
   *
   * <p>This method sets up all UI components including labels,
   * progress bar that adjust the valve state and flow rate, and buttons
   * to open/close the valve.</p>
   *
   * @return the fully constructed ControlCard ready for display
   */
  @Override
  public ControlCard build() {
    log.info("Building valve control card");

    // Shows the flow in text
    Label flowLabel = new Label("Flow: 0 L/min");
    flowLabel.getStyleClass().addAll("card-subtle", "valve-flow");

    // Shows the flow visually
    ProgressBar flowIndicator = new ProgressBar(0);
    flowIndicator.setMaxWidth(Double.MAX_VALUE);
    flowIndicator.setPrefHeight(15);
    flowIndicator.getStyleClass().add("valve-flow-indicator");

    // Slider for customizing flow
    Label sliderLabel = new Label("Custom: 0%");
    Slider openingSlider = new Slider(0, 100, 0);
    openingSlider.setShowTickMarks(true);
    openingSlider.setShowTickLabels(false);
    openingSlider.setMajorTickUnit(25);
    openingSlider.setMaxWidth(Double.MAX_VALUE);

    VBox sliderBox = new VBox(6, sliderLabel, openingSlider);
    sliderBox.setAlignment(Pos.CENTER);
    sliderBox.setPadding(new Insets(8, 0, 8, 0));

    // Open button
    Button openButton = new Button("OPEN");
    openButton.getStyleClass().add("open-valve-button");

    // Close button
    Button closeButton = new Button("CLOSED");
    closeButton.getStyleClass().add("close-valve-button");

    HBox buttonRow = new HBox(10, openButton, closeButton);
    buttonRow.setAlignment(Pos.CENTER);

    VBox buttonBox = new VBox(6, buttonRow);
    buttonBox.setAlignment(Pos.CENTER);

    card.addContent(
            flowLabel,
            flowIndicator,
            new Separator(),
            sliderBox,
            buttonBox
    );

    var controller = new ValveCardController(
        card,
        flowLabel,
        flowIndicator,
        openingSlider,
        sliderLabel,
        openButton,
        closeButton
    );
    card.setUserData(controller);

    log.debug("Valve control card built successfully");

    return card;
  }

  /**
   * Creates the control card instance.
   *
   * @return the ControlCard instance
   */
  @Override
  public ControlCard getCard() {
    return card;
  }
}

