package edu.ntnu.bidata.smg.group8.control.ui.controller.cardcontrollers;

import edu.ntnu.bidata.smg.group8.common.util.AppLogger;
import edu.ntnu.bidata.smg.group8.control.ui.view.cards.FertilizerCardBuilder;
import org.slf4j.Logger;

/**
* Controller for the Fertilizer control card.
* This controller coordinates the interaction between the FertilizerCardBuilder UI
* and the underlying logic it is responsible for.

* @author Andrea Sandnes
* @version 28.10.2025
*/
public class FertilizerCardController {
  private static final Logger log = AppLogger.get(FertilizerCardController.class);

  FertilizerCardBuilder builder;

  /**
  * Creates new FertilizerCardController for the given builder.

  * @param builder the Fertilizer card builder instance to control.
  */
  public FertilizerCardController(FertilizerCardBuilder builder) {
    this.builder = builder;
    log.debug("FertilizerCardController created for builder: {}", builder);
  }

  /**
  * Initializes event handlers and starts any listeners required by this controller.
  */
  public void start() {
    log.info("Starting FertilizerCardController");
    // TODO: Add initialization logic here
    log.debug("FertilizerCardController started successfully");
  }

  /**
  * Stops this controller and cleans up resources/listeners.
  */
  public void stop() {
    log.info("Stopping FertilizerCardController");
    // TODO: Add cleanup logic here
    log.debug("FertilizerCardController stopped successfully");
  }
}
