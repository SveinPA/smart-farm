package edu.ntnu.bidata.smg.group8.control.ui.factory;

import edu.ntnu.bidata.smg.group8.common.util.AppLogger;
import javafx.animation.TranslateTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import org.slf4j.Logger;

/**
 * Factory class for creating custom styled UI components.
 * This factory provides utility methods for creating and enhancing JavaFX
 * controls with custom graphics, animations, and predefined styles. It centralizes
 * UI component creation to ensure consistency across the application.
 *
 * <h2>The Button Factory provides the following features:</h2>
 * <ul>
 *     <li>Attaching animated ON/OFF switch graphics to ToggleButtons.</li>
 *     <li>Creating buttons with specific styles such as danger and primary action buttons.</li>
 *     <li>Graphic enhancements for buttons, such as the toggle witch.</li>
 *     <li>History buttons with specific styling for accessing historical data views.</li>
 *     <li>Return buttons with navigation icons.</li>
 * </ul>
 *
 * @author Andrea Sandnes
 * @version 17.11.2025 (last updated)
 */
public class ButtonFactory {
  private static final Logger log = AppLogger.get(ButtonFactory.class);
  private static final String DANGER_STYLE =
          "-fx-background-color: #da6655; -fx-text-fill: white;";
  private static final String PRIMARY_STYLE =
          "-fx-background-color: #2196f3; -fx-text-fill: white;";

  /**
  * Attaches a custom animated switch graphic to a ToggleButton.
  * The switch displays "ON" and "OFF" Labels with a sliding thumb
  * animation that transitions smoothly when the button state changes.

  * @param button The ToggleButton to enhance with a switch graphic
  */
  public static void attachSwitch(ToggleButton button) {
    if (Boolean.TRUE.equals(button.getProperties().get("switchAttached"))) {
      return;
    }
    button.getProperties().put("switchAttached", true);

    log.debug("Attaching ON/OFF switch to ToggleButton");

    button.setContentDisplay((ContentDisplay.RIGHT));
    button.setGraphicTextGap(14);
    button.setAlignment(Pos.CENTER);
    button.setGraphic(createSwitchGraphic(button));

    log.trace("ON/OFF switch attached successfully");
  }

  /**
  * Creates a custom switch graphic node with animated sliding thumb.
  * The switch consists of:
  * - A background track
  * - "ON" and "OFF" text Labels
  * - A circular thumb that slides between positions
  * Smooth transition animation

  * @param owner The ToggleButton that will own this witch graphic
  * @return A node containing the complete switch graphic with animations
  */
  private static Node createSwitchGraphic(ToggleButton owner) {
    log.trace("Creating ON/OFF switch graphic");

    double trackW = 64;
    double trackH = 28;
    double pad = 2;
    double thumbD = trackH - 2 * pad;
    double shift = trackW - thumbD - 2 * pad;

    // Background track
    Region track = new Region();
    track.getStyleClass().add("switch-track");
    track.setMinSize(trackW, trackH);
    track.setPrefSize(trackW, trackH);
    track.setMaxSize(trackW, trackH);
    track.relocate(0, 0);

    // ON/OFF Labels
    Label onLabel = new Label("ON");
    Label offLabel = new Label("OFF");
    onLabel.getStyleClass().addAll("switch-label", "switch-on-label");
    offLabel.getStyleClass().addAll("switch-label", "switch-off-label");

    // Sliding thumb
    Region thumb = new Region();
    thumb.getStyleClass().add("switch-thumb");
    thumb.setMinSize(thumbD, thumbD);
    thumb.setPrefSize(thumbD, thumbD);
    thumb.setMaxSize(thumbD, thumbD);

    // Assembling components in a container
    StackPane container = new StackPane(track, onLabel, offLabel, thumb);
    container.getStyleClass().add("switch");
    container.setPadding(new Insets(pad));
    container.setMouseTransparent(true);

    // Positioning the elements
    StackPane.setAlignment(thumb, Pos.CENTER_LEFT);
    StackPane.setAlignment(onLabel, Pos.CENTER_LEFT);
    StackPane.setAlignment(offLabel, Pos.CENTER_RIGHT);
    StackPane.setMargin(onLabel, new Insets(0, pad + 6, 0, pad + 6));
    StackPane.setMargin(offLabel, new Insets(0, pad + 6, 0, pad + 6));

    // Initial thumb position
    thumb.setTranslateX(owner.isSelected() ? shift : 0);

    // Label visibility based on initial position
    updateOppositeTextVisibility(onLabel, offLabel, thumb.getTranslateX(), shift);

    // State change animation
    owner.selectedProperty().addListener((o, was, isNow) -> {
      log.debug("Switch state changed to: {}", isNow ? "ON" : "OFF");
      double end = isNow ? shift : 0;

      TranslateTransition prev = (TranslateTransition) thumb.getProperties().get("tt");
      if (prev != null) {
        prev.stop();
      }
      TranslateTransition tt = new TranslateTransition(Duration.millis(140), thumb);
      tt.setFromX(thumb.getTranslateX());
      tt.setToX(end);
      thumb.getProperties().put("tt", tt);
      tt.playFromStart();
    });


    // Update Label visibility during animation
    thumb.translateXProperty().addListener((obs, oldX, newX) -> {
      updateOppositeTextVisibility(onLabel, offLabel, thumb.getTranslateX(), shift);
    });

    owner.sceneProperty().addListener((o, oldS, newS) -> {
      thumb.setTranslateX(owner.isSelected() ? shift : 0);
      updateOppositeTextVisibility(onLabel, offLabel, thumb.getTranslateX(), shift);
    });
    return container;
  }


  /**
  * Updates the visibility of ON/OFF Labels based on thumb position.
  * Shows "ON" when the thumb is on the right side, "OFF" when on the left.
  * This creates a clean visual effect where only the appropriate Label is visible.

  * @param onLabel The Label displaying "ON" text
  * @param offLabel The Label displaying "OFF" text
  * @param thumbTX The current X translation of the thumb
  * @param shift The maximum translation distance (right position)
  */
  private static void updateOppositeTextVisibility(Label onLabel, Label offLabel,
                                                   double thumbTX, double shift) {
    boolean thumbOnRight = thumbTX > (shift / 2.0);

    onLabel.setVisible(thumbOnRight);
    offLabel.setVisible(!thumbOnRight);
  }

  /**
   * Creates a styled history button with the given label text.
   *
   * <p>This button is specifically styled for accessing history views
   * within the application.</p>

   * @param text the label to display on the button
   * @return a new Button
   */
  public static Button createHistoryButton(String text) {
    log.debug("Creating history button: {}", text);

    Button historyButton = new Button(text);
    historyButton.setId("history-button");
    return historyButton;
  }


  /**
   * Creates a standard button with default styling.
   *
   * <p>Use this method to create buttons with default appearance
   * across the application.</p>
   *
   * @param text the button text
   * @return a new Button configured as a history button
   */
  public static Button createButton(String text) {
    log.trace("Creating button: {}", text);

    Button button = new Button(text);
    return button;
  }

  /**
   * Creates a standard button with specified width.
   *
   * <p>Use this method to create buttons with custom widths
   * while maintaining default styling.</p>
   *
   * @param text the button text
   * @param width the preferred width in pixels
   * @return a new Button instance
   */
  public static Button createButton(String text, double width) {
    log.trace("Creating button with width {}: {}", width, text);

    Button button = createButton(text);
    button.setPrefWidth(width);
    return button;
  }

  /**
   * Creates a preset button with custom width.
   *
   * <p>Use this method to create buttons with a consistent preset width
   * across the application.</p>
   *
   * @param text the button text
   * @param width the preferred width in pixels
   * @return a preset button with specified width
   */
  public static Button createPresetButton(String text, double width) {
    log.debug("Creating preset button with width {}: {}", width, text);

    return createButton(text, width);
  }

  /**
   * Creates a full-width button.
   *
   * <p>This button will expand to fill the maximum width
   * of its container.</p>
   *
   * @param text the button text
   * @return a button with max width set
   */
  public static Button createFullWidthButton(String text) {
    log.trace("Creating full-width button: {}", text);

    Button button = createButton(text);
    button.setMaxWidth(Double.MAX_VALUE);
    return button;
  }


  /**
  * Creates a danger button (red) for destructive actions.
  *
  * <p>Use this for actions like "Turn OFF", "Stop", or "Delete"
  * that require user caution.</p>
  *
  * @param text the button text
  * @return a styled danger button
  */
  public static Button createDangerButton(String text) {
    log.debug("Creating danger button: {}", text);

    Button button = createButton(text);
    button.setStyle(DANGER_STYLE);
    return button;
  }

  /**
   * Creates a full-width danger button.
   *
   * <p>Use this for destructive actions that need to span the full width
   * of their container.</p>
   *
   * @param text the button text
   * @return a full-width danger button
   */
  public static Button createFullWidthDangerButton(String text) {
    log.debug("Creating full-width danger button: {}", text);

    Button button = createDangerButton(text);
    button.setMaxWidth(Double.MAX_VALUE);
    return button;
  }

  /**
   * Creates a primary action button (blue).
   *
   * <p>Use this for main actions like "Submit", "Save", or "Start".</p>
   *
   * @param text the button text
   * @return a styled primary button
   */
  public static Button createPrimaryButton(String text) {
    log.debug("Creating primary button: {}", text);

    Button button = createButton(text);
    button.setStyle(PRIMARY_STYLE);
    return button;
  }

  /**
   * Creates a return button styled as a primary button.
   *
   * <p>This button/method is used for navigation
   * actions to return to a previous view.</p>
   *
   * @param text the button text
   * @return a styled return button
   */
  public static Button createReturnButton(String text) {
    log.debug("Creating return button: {}", text);

    Button button = createButton(text);

    Image returnIcon = new Image(ButtonFactory.class.getResourceAsStream("/images/returnIcon.png"));
    ImageView iconView = new ImageView(returnIcon);

    iconView.setFitWidth(16);
    iconView.setFitHeight(16);

    button.setGraphic(iconView);

    button.setPrefWidth(105);
    button.setPrefHeight(50);

    return button;
  }
}
