package edu.ntnu.bidata.smg.group8.control.ui.controller.cardcontrollers;

import edu.ntnu.bidata.smg.group8.control.ui.view.cards.LightCardBuilder;

/**
* Controller for the Light control card.
* This controller coordinates the interaction between the LightCardBuilder UI
* and the underlying logic it is responsible for.

* @author Andrea Sandnes
* @version 28.10.2025
*/
public class LightCardController {
  LightCardBuilder builder;

  /**
  * Creates new LightCardController for the given builder.

  * @param builder the Light card builder instance to control.
  */
  public LightCardController(LightCardBuilder builder) {
    this.builder = builder;
  }

  /**
  * Initializes event handlers and starts any listeners required by this controller.
  */
  public void start() {
    if (builder.getOnButton() != null) {
      builder.getOnButton().setOnAction(e -> builder.getCard().setValueText("ON"));
    }
    if (builder.getOffButton() != null) {
      builder.getOffButton().setOnAction(e -> builder.getCard().setValueText("OFF"));
    }
    if (builder.getIntensitySlider() != null) {
      builder.getIntensitySlider().valueProperty().addListener((o, ov, nv) ->
        builder.getCard().setValueText("ON"));
    }
  }

  /**
  * Stops this controller and cleans up resources/listeners.
  */
  public void stop() {

  }
}
