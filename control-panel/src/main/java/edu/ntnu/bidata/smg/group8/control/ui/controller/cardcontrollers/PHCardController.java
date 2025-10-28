package edu.ntnu.bidata.smg.group8.control.ui.controller.cardcontrollers;

import edu.ntnu.bidata.smg.group8.control.ui.view.cards.PHCardBuilder;

/**
* Controller for the pH control card.
* This controller coordinates the interaction between the PHCardBuilder UI
* and the underlying logic it is responsible for.

* @author Andrea Sandnes
* @version 28.10.2025
*/
public class PHCardController {
  PHCardBuilder builder;

  /**
  * Creates new PHCardController for the given builder.

  * @param builder the pH card builder instance to control.
  */
  public PHCardController(PHCardBuilder builder) {
    this.builder = builder;
  }

  /**
  * Initializes event handlers and starts any listeners required by this controller.
  */
  public void start() {

  }

  /**
  * Stops this controller and cleans up resources/listeners.
  */
  public void stop() {

  }
}
