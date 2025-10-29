package edu.ntnu.bidata.smg.group8.control.ui.controller.cardcontrollers;

import edu.ntnu.bidata.smg.group8.common.util.AppLogger;
import edu.ntnu.bidata.smg.group8.control.ui.view.cards.TemperatureCardBuilder;
import org.slf4j.Logger;


/**
* Controller for the Temperature control card.
* This controller coordinates the interaction between the TemperatureCardBuilder UI
* and the underlying logic it is responsible for.

* @author Andrea Sandnes
* @version 28.10.2025
*/
public class TemperatureCardController {
  private static final Logger log = AppLogger.get(TemperatureCardController.class);

  TemperatureCardBuilder builder;

  /**
  * Creates new TemperatureCardController for the given builder.

  * @param builder the Temperature card builder instance to control.
  */
  public TemperatureCardController(TemperatureCardBuilder builder) {
    this.builder = builder;
    log.debug("TemperatureCardController created for builder: {}", builder);
  }

  /**
  * Initializes event handlers and starts any listeners required by this controller.
  */
  public void start() {
    log.info("Starting TemperatureCardController");
    // TODO: Add initialization logic here
    log.debug("TemperatureCardController started successfully");
  }

  /**
  * Stops this controller and cleans up resources/listeners.
  */
  public void stop() {
    log.info("Stopping TemperatureCardController");
    // TODO: Add cleanup logic here
    log.debug("TemperatureCardController stopped successfully");
  }
}
