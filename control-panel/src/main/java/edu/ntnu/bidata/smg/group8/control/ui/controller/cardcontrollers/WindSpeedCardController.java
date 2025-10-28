package edu.ntnu.bidata.smg.group8.control.ui.controller.cardcontrollers;

import edu.ntnu.bidata.smg.group8.common.util.AppLogger;
import edu.ntnu.bidata.smg.group8.control.ui.view.cards.WindSpeedCardBuilder;
import org.slf4j.Logger;

/**
* Controller for the Wind Speed control card.
* This controller coordinates the interaction between the WindSpeedCardBuilder UI
* and the underlying logic it is responsible for.

* @author Andrea Sandnes
* @version 28.10.2025
*/
public class WindSpeedCardController {
  private static final Logger log = AppLogger.get(WindSpeedCardController.class);

  WindSpeedCardBuilder builder;

  /**
  * Creates new WindSpeedCardController for the given builder.

  * @param builder the Wind Speed card builder instance to control.
  */
  public WindSpeedCardController(WindSpeedCardBuilder builder) {
    this.builder = builder;
    log.debug("WindSpeedCardController created for builder: {}", builder);
  }

  /**
  * Initializes event handlers and starts any listeners required by this controller.
  */
  public void start() {
    log.info("Starting WindSpeedCardController");
    // TODO: Add initialization logic here
    log.debug("WindSpeedCardController started successfully");
  }

  /**
  * Stops this controller and cleans up resources/listeners.
  */
  public void stop() {
    log.info("Stopping WindSpeedCardController");
    // TODO: Add cleanup logic here
    log.debug("WindSpeedCardController stopped successfully");
  }
}
