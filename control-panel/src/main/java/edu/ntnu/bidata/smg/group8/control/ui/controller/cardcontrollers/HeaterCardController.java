package edu.ntnu.bidata.smg.group8.control.ui.controller.cardcontrollers;

import edu.ntnu.bidata.smg.group8.common.util.AppLogger;
import edu.ntnu.bidata.smg.group8.control.ui.view.cards.HeaterCardBuilder;
import org.slf4j.Logger;

/**
* Controller for the Heater control card.
* This controller coordinates the interaction between the HeaterCardBuilder UI
* and the underlying logic it is responsible for.

* @author Andrea Sandnes
* @version 28.10.2025
*/
public class HeaterCardController {
  private static final Logger log = AppLogger.get(HeaterCardController.class);

  HeaterCardBuilder builder;

  /**
  * Creates new HeaterCardController for the given builder.

  * @param builder the Heater card builder instance to control.
  */
  public HeaterCardController(HeaterCardBuilder builder) {
    this.builder = builder;
    log.debug("HeaterCardController created for builder: {}", builder);
  }

  /**
  * Initializes event handlers and starts any listeners required by this controller.
  */
  public void start() {
    log.info("Starting HeaterCardController");
    // TODO: Add initialization logic here
    log.debug("HeaterCardController started successfully");
  }

  /**
  * Stops this controller and cleans up resources/listeners.
  */
  public void stop() {
    log.info("Stopping HeaterCardController");
    // TODO: Add cleanup logic here
    log.debug("HeaterCardController stopped successfully");
  }
}
