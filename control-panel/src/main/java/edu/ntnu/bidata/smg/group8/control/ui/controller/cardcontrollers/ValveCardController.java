package edu.ntnu.bidata.smg.group8.control.ui.controller.cardcontrollers;

import edu.ntnu.bidata.smg.group8.common.util.AppLogger;
import edu.ntnu.bidata.smg.group8.control.ui.view.cards.ValveCardBuilder;
import org.slf4j.Logger;

/**
* Controller for the Valve control card.
* This controller coordinates the interaction between the ValveCardBuilder UI
* and the underlying actuator logic it is responsible for.

* @author Andrea Sandnes
* @version 28.10.2025
*/
public class ValveCardController {
  private static final Logger log = AppLogger.get(ValveCardController.class);

  ValveCardBuilder builder;

  /**
  * Creates new ValveCardController for the given builder.

  * @param builder the Valve card builder instance to control.
  */
  public ValveCardController(ValveCardBuilder builder) {
    this.builder = builder;
    log.debug("ValveCardController created for builder: {}", builder);

  }

  /**
  * Initializes event handlers and starts any listeners required by this controller.
  */
  public void start() {
    log.info("Starting ValveCardController");
    // TODO: Add initialization logic here
    log.debug("ValveCardController started successfully");
  }

  /**
  * Stops this controller and cleans up resources/listeners.
  */
  public void stop() {
    log.info("Stopping ValveCardController");
    // TODO: Add cleanup logic here
    log.debug("ValveCardController stopped successfully");
  }
}
