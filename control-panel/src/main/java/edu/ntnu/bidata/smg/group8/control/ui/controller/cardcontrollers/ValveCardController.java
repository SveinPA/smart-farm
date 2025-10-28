package edu.ntnu.bidata.smg.group8.control.ui.controller.cardcontrollers;

import edu.ntnu.bidata.smg.group8.control.ui.view.cards.ValveCardBuilder;

/**
* Controller for the Valve control card.
* This controller coordinates the interaction between the ValveCardBuilder UI
* and the underlying actuator logic it is responsible for.

* @author Andrea Sandnes
* @version 28.10.2025
*/
public class ValveCardController {
  ValveCardBuilder builder;

  /**
  * Creates new ValveCardController for the given builder.

  * @param builder the Valve card builder instance to control.
  */
  public ValveCardController(ValveCardBuilder builder) {
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
