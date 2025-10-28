package edu.ntnu.bidata.smg.group8.control.ui.controller.cardcontrollers;

import edu.ntnu.bidata.smg.group8.control.ui.view.cards.WindowsCardBuilder;

/**
* Controller for the Windows control card.
* This controller coordinates the interaction between the WindowsCardBuilder UI
* and the underlying logic it is responsible for.

* @author Andrea Sandnes
* @version 28.10.2025
*/
public class WindowsCardController {
  WindowsCardBuilder builder;

  /**
  * Creates new WindowsCardController for the given builder.

  * @param builder the Windows card builder instance to control.
  */
  public WindowsCardController(WindowsCardBuilder builder) {
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
