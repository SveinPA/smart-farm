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
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import org.slf4j.Logger;


/**
* Factory class for creating custom styled UI components.
* This factory provides utility methods for creating and enhancing JavaFX
* controls with custom graphics, animations, and predefined styles. It centralizes
* UI component creation to ensure consistency across the application.
*/
public class ButtonFactory {
  private static final Logger log = AppLogger.get(ButtonFactory.class);
  private static final String DANGER_STYLE =
          "-fx-background-color: #da6655; -fx-text-fill: white;";
  private static final String PRIMARY_STYLE =
          "-fx-background-color: #2196f3; -fx-text-fill: white;";
  private static final String SUCCESS_STYLE =
          "-fx-background-color: #4caf50; -fx-text-fill: white;";
  private static final String WARNING_STYLE =
          "-fx-background-color: #ff9800; -fx-text-fill: white;";

  /**
  * Attaches a custom animated switch graphic to a ToggleButton.
  * The switch displays "ON" and "OFF" Labels with a sliding thumb
  * animation that transitions smoothly when the button state changes.

  * @param button The ToggleButton to enhance with a switch graphic
  */
  public static void attachSwitch(ToggleButton button) {
    log.debug("Attaching ON/OFF switch to ToggleButton");

    button.setContentDisplay((ContentDisplay.RIGHT));
    button.setGraphicTextGap(14);
    button.setAlignment(Pos.CENTER);
    button.setGraphic(createSwitchGraphic(button));

    log.trace("ON/OFF switch attached successfully");

  }

  /**
  * Attaches a custom animated switch graphic to a ToggleButton for window controls.
  * The switch displays "OPEN" and "CLOSE" Labels with a sliding thumb
  * animation that transitions smoothly when the button state changes.

  * @param button The ToggleButton to enhance with a switch graphic
  */
  public static void attachWindowSwitch(ToggleButton button) {
    log.debug("Attaching OPEN/CLOSE switch to ToggleButton");

    button.setContentDisplay((ContentDisplay.RIGHT));
    button.setGraphicTextGap(14);
    button.setAlignment(Pos.CENTER);
    button.setGraphic(createWindowSwitchGraphic(button));

    log.trace("OPEN/CLOSE switch attached successfully");
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
  * Creates a custom window switch graphic node with animated sliding thumb.
  * The switch consists of:
  * - A background track
  * - "OPEN" and "CLOSE" text Labels
  * - A circular thumb that slides between positions
  * Smooth transition animation

  * @param owner The ToggleButton that will own this witch graphic
  * @return A node containing the complete switch graphic with animations
  */
  private static Node createWindowSwitchGraphic(ToggleButton owner) {
    log.trace("Creating OPEN/CLOSE switch graphic");

    double trackW = 85;
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

    // OPEN/CLOSE Labels
    Label openLabel = new Label("OPEN");
    Label closeLabel = new Label("CLOSE");
    openLabel.getStyleClass().addAll("switch-label", "switch-open-label");
    closeLabel.getStyleClass().addAll("switch-label", "switch-close-label");

    // Sliding thumb
    Region thumb = new Region();
    thumb.getStyleClass().add("switch-thumb");
    thumb.setMinSize(thumbD, thumbD);
    thumb.setPrefSize(thumbD, thumbD);
    thumb.setMaxSize(thumbD, thumbD);

    // Assembling components in a container
    StackPane container = new StackPane(track, openLabel, closeLabel, thumb);
    container.getStyleClass().add("switch");
    container.setPadding(new Insets(pad));
    container.setMouseTransparent(true);

    // Positioning the elements
    StackPane.setAlignment(thumb, Pos.CENTER_LEFT);
    StackPane.setAlignment(openLabel, Pos.CENTER_LEFT);
    StackPane.setAlignment(closeLabel, Pos.CENTER_RIGHT);
    StackPane.setMargin(openLabel, new Insets(0, pad + 6, 0, pad + 6));
    StackPane.setMargin(closeLabel, new Insets(0, pad + 6, 0, pad + 6));

    // Initial thumb position
    thumb.setTranslateX(owner.isSelected() ? shift : 0);

    // Label visibility based on initial position
    updateOppositeTextVisibility(openLabel, closeLabel, thumb.getTranslateX(), shift);

    // State change animation
    owner.selectedProperty().addListener((o, was, isNow) -> {
      log.debug("Window switch state changed to: {}", isNow ? "OPEN" : "CLOSE");
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
      updateOppositeTextVisibility(openLabel, closeLabel, thumb.getTranslateX(), shift);
    });

    owner.sceneProperty().addListener((o, oldS, newS) -> {
      thumb.setTranslateX(owner.isSelected() ? shift : 0);
      updateOppositeTextVisibility(openLabel, closeLabel, thumb.getTranslateX(), shift);
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
  * Creates a schedule button with special styling.

  * @param text the button text
  * @return a styled schedule button
  */
  public static Button createScheduleButton(String text) {
    log.debug("Creating schedule button: {}", text);

    Button button = new Button(text);
    button.getStyleClass().add("schedule-button");
    return button;
  }

  public static Button createHistoryButton(String text) {
    log.debug("Creating history button: {}", text);

    Button historyButton = new Button(text);
    historyButton.setId("history-button");
    return historyButton;
  }


  /**
  * Creates a standard button with default styling.
  *
  * @param text the button text
  * @return a new Button instance
  */
  public static Button createButton(String text) {
    log.trace("Creating button: {}", text);

    Button button = new Button(text);
    return button;
  }

  /**
  * Creates a standard button with specified width.
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
}
