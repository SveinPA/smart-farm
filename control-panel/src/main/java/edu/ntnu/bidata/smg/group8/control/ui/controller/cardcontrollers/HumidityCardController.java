package edu.ntnu.bidata.smg.group8.control.ui.controller.cardcontrollers;

import edu.ntnu.bidata.smg.group8.common.util.AppLogger;
import edu.ntnu.bidata.smg.group8.control.ui.view.cards.HumidityCardBuilder;
import org.slf4j.Logger;

/**
* Controller for the Humidity control card.
* This controller coordinates the interaction between the HumidityCardBuilder UI
* and the underlying logic it is responsible for.

* @author Andrea Sandnes
* @version 28.10.2025
*/
public class HumidityCardController {
  private static final Logger log = AppLogger.get(HumidityCardController.class);

  HumidityCardBuilder builder;

  /**
  * Creates new HumidityCardController for the given builder.

  * @param builder the Humidity card builder instance to control.
  */
  public HumidityCardController(HumidityCardBuilder builder) {
    this.builder = builder;
    log.debug("HumidityCardController created for builder: {}", builder);
  }

  /**
  * Initializes event handlers and starts any listeners required by this controller.
  */
  public void start() {
    log.info("Starting HumidityCardController");
    // TODO: Add initialization logic here
    log.debug("HumidityCardController started successfully");
  }

  /**
  * Stops this controller and cleans up resources/listeners.
  */
  public void stop() {
    log.info("Stopping HumidityCardController");
    // TODO: Add cleanup logic here
    log.debug("HumidityCardController stopped successfully");
  }
}
