package edu.ntnu.bidata.smg.group8.control.ui.controller.cardcontrollers;

import edu.ntnu.bidata.smg.group8.control.ui.view.cards.HumidityCardBuilder;

/**
* Controller for the Humidity control card.
* This controller coordinates the interaction between the HumidityCardBuilder UI
* and the underlying logic it is responsible for.

* @author Andrea Sandnes
* @version 28.10.2025
*/
public class HumidityCardController {
  HumidityCardBuilder builder;

  /**
  * Creates new HumidityCardController for the given builder.

  * @param builder the Humidity card builder instance to control.
  */
  public HumidityCardController(HumidityCardBuilder builder) {
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
