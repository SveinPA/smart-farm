package edu.ntnu.bidata.smg.group8.control.ui.factory;

import javafx.animation.TranslateTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;


/**
* Factory class for creating custom styled UI components.
* Provides utility methods for enhancing JavaFX controls with custom
* graphics and animations.
*/
public class ButtonFactory {

  /**
  * Attaches a custom animated switch graphic to a ToggleButton.
  * The switch displays "ON" and "OFF" Labels with a sliding thumb
  * animation that transitions smoothly when the button state changes.

  * @param button The ToggleButton to enhance with a switch graphic
  */
  public static void attachSwitch(ToggleButton button) {
    button.setContentDisplay((ContentDisplay.RIGHT));
    button.setGraphicTextGap(14);
    button.setAlignment(Pos.CENTER);
    button.setGraphic(createSwitchGraphic(button));
  }

  /**
  * Attaches a custom animated switch graphic to a ToggleButton for window controls.
  * The switch displays "OPEN" and "CLOSE" Labels with a sliding thumb
  * animation that transitions smoothly when the button state changes.

  * @param button The ToggleButton to enhance with a switch graphic
  */
  public static void attachWindowSwitch(ToggleButton button) {
    button.setContentDisplay((ContentDisplay.RIGHT));
    button.setGraphicTextGap(14);
    button.setAlignment(Pos.CENTER);
    button.setGraphic(createWindowSwitchGraphic(button));
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
    StackPane.setMargin(onLabel, new Insets(0, pad + 6, 0 , pad + 6));
    StackPane.setMargin(offLabel, new Insets(0, pad + 6, 0, pad + 6));

    // Initial thumb position
    thumb.setTranslateX(owner.isSelected() ? shift : 0);

    // Label visibility based on initial position
    updateOppositeTextVisibility(onLabel, offLabel, thumb.getTranslateX(), shift);

    // State change animation
    owner.selectedProperty().addListener((o, was, isNow) -> {
      double end = isNow ? shift : 0;

      TranslateTransition prev = (TranslateTransition)thumb.getProperties().get("tt");
      if (prev != null) prev.stop();
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
        StackPane.setMargin(openLabel, new Insets(0, pad + 6, 0 , pad + 6));
        StackPane.setMargin(closeLabel, new Insets(0, pad + 6, 0, pad + 6));

        // Initial thumb position
        thumb.setTranslateX(owner.isSelected() ? shift : 0);

        // Label visibility based on initial position
        updateOppositeTextVisibility(openLabel, closeLabel, thumb.getTranslateX(), shift);

        // State change animation
        owner.selectedProperty().addListener((o, was, isNow) -> {
            double end = isNow ? shift : 0;

            TranslateTransition prev = (TranslateTransition)thumb.getProperties().get("tt");
            if (prev != null) prev.stop();
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
  private static void updateOppositeTextVisibility(Label onLabel, Label offLabel, double thumbTX, double shift) {
    boolean thumbOnRight = thumbTX > (shift / 2.0);

    onLabel.setVisible(thumbOnRight);
    offLabel.setVisible(!thumbOnRight);
  }
}
