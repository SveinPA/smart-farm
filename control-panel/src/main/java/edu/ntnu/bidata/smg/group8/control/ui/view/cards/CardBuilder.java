package edu.ntnu.bidata.smg.group8.control.ui.view.cards;

import edu.ntnu.bidata.smg.group8.control.ui.view.ControlCard;
import javafx.scene.layout.StackPane;

/**
* Base interface for building control cards.
* This interface defines the contract for creating specialized
* control cards. Each card type should have its own builder implementation
* that handles the specific UI components and Layout for that card.
*/
public interface CardBuilder {
  /**
  * Builds and returns the complete control card.

  * @return a StackPane containing the fully configured card.
  */
  StackPane build();

  /**
  * Gets the card instance for direct manipulation.

  * @return the ControlCard instance
  */
  ControlCard getCard();
}
