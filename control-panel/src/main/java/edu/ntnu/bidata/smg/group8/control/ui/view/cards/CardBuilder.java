package edu.ntnu.bidata.smg.group8.control.ui.view.cards;

import edu.ntnu.bidata.smg.group8.control.ui.view.ControlCard;
import javafx.scene.layout.StackPane;

/**
 * Base interface for building control cards.
 *
 * <p>This interface defines the contract for creating specialized
 * control cards. Each card type should have its own builder implementation
 * that handles the specific UI components and Layout for that card.</p>
 */
public interface CardBuilder {
  /**
   * Builds and returns the complete control card.
   * This method should construct the card with all its UI components,
   * styling and event handlers fully configured.

   * @return a StackPane containing the fully configured card.
   */
  StackPane build();

  /**
   * Gets the card instance for direct manipulation.
   * This method provides access to the underlying ControlCard instance,
   * allowing controllers to interact with the card after it has been built.

   * @return the ControlCard instance
   */
  ControlCard getCard();
}
