package edu.ntnu.bidata.smg.group8.control.ui.controller.cardcontrollers;

import edu.ntnu.bidata.smg.group8.common.util.AppLogger;
import edu.ntnu.bidata.smg.group8.control.ui.view.cards.FanCardBuilder;
import javafx.application.Platform;
import org.slf4j.Logger;

/**
* Controller for the Fan control card.
* This controller coordinates the interaction between the FanCardBuilder UI
* and the underlying logic it is responsible for.

* @author Andrea Sandnes
* @version 28.10.2025
*/
public class FanCardController {
  private static final Logger log = AppLogger.get(FanCardController.class);

  FanCardBuilder builder;

  /**
  * Creates new FanCardController for the given builder.

  * @param builder the Fan card builder instance to control.
  */
  public FanCardController(FanCardBuilder builder) {
    this.builder = builder;
    log.debug("FanCardController created for builder: {}", builder);
  }

  /**
  * Initializes event handlers and starts any listeners required by this controller.
  */
  public void start() {
    log.info("Starting FanCardController");
    // TODO: Add initialization logic here
    log.debug("FanCardController started successfully");
  }

  /**
  * Stops this controller and cleans up resources/listeners.
  */
  public void stop() {
    log.info("Stopping FanCardController");
    // TODO: Add cleanup logic here
    log.debug("FanCardController stopped successfully");
  }

  /**
   * Ensures the given runnable executes on the JavaFX Application Thread.
   *
   * @param r the runnable to execute on the FX thread
   */
  private static void fx(Runnable r) {
    if (Platform.isFxApplicationThread()) {
      r.run();
    } else {
      Platform.runLater(r);
    }
  }
}
