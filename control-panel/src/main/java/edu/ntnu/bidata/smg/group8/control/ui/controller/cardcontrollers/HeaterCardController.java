package edu.ntnu.bidata.smg.group8.control.ui.controller.cardcontrollers;

import edu.ntnu.bidata.smg.group8.control.ui.view.cards.HeaterCardBuilder;

/**
* Controller for the Heater control card.
* This controller coordinates the interaction between the HeaterCardBuilder UI
* and the underlying logic it is responsible for.

* @author Andrea Sandnes
* @version 28.10.2025
*/
public class HeaterCardController {
  HeaterCardBuilder builder;

  /**
  * Creates new HeaterCardController for the given builder.

  * @param builder the Heater card builder instance to control.
  */
  public HeaterCardController(HeaterCardBuilder builder) {
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
