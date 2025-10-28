package edu.ntnu.bidata.smg.group8.control.ui.controller.cardcontrollers;

import edu.ntnu.bidata.smg.group8.control.ui.view.cards.FanCardBuilder;

/**
* Controller for the Fan control card.
* This controller coordinates the interaction between the FanCardBuilder UI
* and the underlying logic it is responsible for.

* @author Andrea Sandnes
* @version 28.10.2025
*/
public class FanCardController {
  FanCardBuilder builder;

  /**
  * Creates new FanCardController for the given builder.

  * @param builder the Fan card builder instance to control.
  */
  public FanCardController(FanCardBuilder builder) {
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
