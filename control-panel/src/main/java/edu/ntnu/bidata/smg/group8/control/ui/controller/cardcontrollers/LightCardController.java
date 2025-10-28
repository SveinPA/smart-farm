package edu.ntnu.bidata.smg.group8.control.ui.controller.cardcontrollers;

import edu.ntnu.bidata.smg.group8.common.util.AppLogger;
import edu.ntnu.bidata.smg.group8.control.ui.view.cards.LightCardBuilder;
import org.slf4j.Logger;

/**
* Controller for the Light control card.
* This controller coordinates the interaction between the LightCardBuilder UI
* and the underlying logic it is responsible for.

* @author Andrea Sandnes
* @version 28.10.2025
*/
public class LightCardController {
  private static final Logger log = AppLogger.get(LightCardController.class);

  LightCardBuilder builder;

  /**
  * Creates new LightCardController for the given builder.

  * @param builder the Light card builder instance to control.
  */
  public LightCardController(LightCardBuilder builder) {
    this.builder = builder;
    log.debug("LightCardController created for builder: {}", builder);
  }

  /**
  * Initializes event handlers and starts any listeners required by this controller.
  */
  public void start() {
    log.info("Starting LightCardController");

    if (builder.getOnButton() != null) {
      builder.getOnButton().setOnAction(e -> {
        builder.getCard().setValueText("ON");
        log.debug("Light turned ON");
    });
    log.debug("ON button event handler registered");
    } else {
      log.warn("ON button is null, skipping event handler registration");
    }

    if (builder.getOffButton() != null) {
      builder.getOffButton().setOnAction(e -> {
        builder.getCard().setValueText("OFF");
        log.debug("Light turned OFF");
      });
      log.debug("OFF button event handler registered");
    } else {
      log.warn("OFF button is null, skipping event handler registration");
    }

    if (builder.getIntensitySlider() != null) {
      builder.getIntensitySlider().valueProperty().addListener((o, ov, nv) -> {
        builder.getCard().setValueText("ON");
        log.debug("Light intensity changed to: {}", nv);
      });
      log.debug("Intensity slider listener registered");
    } else {
      log.warn("Intensity slider is null, skipping listener registration");
    }

    log.debug("LightCardController started successfully");
  }


  /**
  * Stops this controller and cleans up resources/listeners.
  */
  public void stop() {
    log.info("Stopping LightCardController");
    // TODO: Add cleanup logic here
    log.debug("LightCardController stopped successfully");
  }
}
