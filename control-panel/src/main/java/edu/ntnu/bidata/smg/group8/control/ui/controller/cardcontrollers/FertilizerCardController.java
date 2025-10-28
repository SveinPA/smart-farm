package edu.ntnu.bidata.smg.group8.control.ui.controller.cardcontrollers;

import edu.ntnu.bidata.smg.group8.control.ui.view.cards.FertilizerCardBuilder;

/**
* Controller for the Fertilizer control card.
* This controller coordinates the interaction between the FertilizerCardBuilder UI
* and the underlying logic it is responsible for.

* @author Andrea Sandnes
* @version 28.10.2025
*/
public class FertilizerCardController {
  FertilizerCardBuilder builder;

  /**
  * Creates new FertilizerCardController for the given builder.

  * @param builder the Fertilizer card builder instance to control.
  */
  public FertilizerCardController(FertilizerCardBuilder builder) {
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
