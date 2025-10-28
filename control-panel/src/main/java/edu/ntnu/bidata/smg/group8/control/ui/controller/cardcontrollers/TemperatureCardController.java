package edu.ntnu.bidata.smg.group8.control.ui.controller.cardcontrollers;

import edu.ntnu.bidata.smg.group8.control.ui.view.cards.TemperatureCardBuilder;


/**
* Controller for the Temperature control card.
* This controller coordinates the interaction between the TemperatureCardBuilder UI
* and the underlying logic it is responsible for.

* @author Andrea Sandnes
* @version 28.10.2025
*/
public class TemperatureCardController {
  TemperatureCardBuilder builder;

  /**
  * Creates new TemperatureCardController for the given builder.

  * @param builder the Temperature card builder instance to control.
  */
  public TemperatureCardController(TemperatureCardBuilder builder) {
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
