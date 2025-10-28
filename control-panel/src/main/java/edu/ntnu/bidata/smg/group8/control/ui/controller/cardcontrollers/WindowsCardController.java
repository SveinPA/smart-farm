package edu.ntnu.bidata.smg.group8.control.ui.controller.cardcontrollers;

import edu.ntnu.bidata.smg.group8.common.util.AppLogger;
import edu.ntnu.bidata.smg.group8.control.ui.view.cards.WindowsCardBuilder;
import org.slf4j.Logger;

/**
* Controller for the Windows control card.
* This controller coordinates the interaction between the WindowsCardBuilder UI
* and the underlying logic it is responsible for.

* @author Andrea Sandnes
* @version 28.10.2025
*/
public class WindowsCardController {
  private static final Logger log = AppLogger.get(WindowsCardController.class);

  WindowsCardBuilder builder;


  /**
  * Creates new WindowsCardController for the given builder.

  * @param builder the Windows card builder instance to control.
  */
  public WindowsCardController(WindowsCardBuilder builder) {
    this.builder = builder;
    log.debug("WindowsCardController created for builder: {}", builder);
  }

  /**
  * Initializes event handlers and starts any listeners required by this controller.
  */
  public void start() {
    log.info("Starting WindowsCardController");
    // TODO: Add initialization logic here
    log.debug("WindowsCardController started successfully");
  }

  /**
  * Stops this controller and cleans up resources/listeners.
  */
  public void stop() {
    log.info("Stopping WindowsCardController");
    // TODO: Add cleanup logic here
    log.debug("WindowsCardController stopped successfully");
  }
}
