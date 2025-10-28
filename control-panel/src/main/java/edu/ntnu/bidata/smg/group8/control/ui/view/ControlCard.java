package edu.ntnu.bidata.smg.group8.control.ui.view;

import edu.ntnu.bidata.smg.group8.common.util.AppLogger;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;

/**
* A reusable control card component for the greenhouse control panel.
* This card provides a consistent layout for displaying sensor values
* and actuator controls with a title, value display, content area and footer.
*/
public class ControlCard extends StackPane {
  private static final Logger log = AppLogger.get(ControlCard.class);

  private static final double CARD_ASPECT = 0.62;

  private final VBox root;
  private final Label title;
  private final Label value;
  private final VBox contentBox;
  private final HBox footer;

  /**
  * Creates a new ControlCard with the specified title.

  * @param titleText the title text to display on the card
  */
  public ControlCard(String titleText) {
    log.debug("Creating ControlCard: {}", titleText);

    this.root = new VBox(12);
    this.title = new Label();
    this.value = new Label();
    this.contentBox = new VBox();
    this.footer = new HBox();

    // Title
    title.setText(titleText);
    title.getStyleClass().add("control-card-title");

    // Value (placeholder)
    value.setText("--");
    value.getStyleClass().add("control-card-value");

    // layout
    root.setAlignment(Pos.TOP_CENTER);
    footer.setAlignment(Pos.BOTTOM_CENTER);

    // Structure
    Region spacer = new Region();
    VBox.setVgrow(spacer, Priority.ALWAYS);
    root.getChildren().addAll(title, value, contentBox, spacer, footer);

    getChildren().add(root);

    getStyleClass().add("control-card");

    setMaxWidth(Double.MAX_VALUE);
    setPrefWidth(280);

    minHeightProperty().bind(widthProperty().multiply((CARD_ASPECT)));

    log.trace("ControlCard '{}' initialized with default value '--'", titleText);

  }

  /**
  * Gets the title label.

  * @return the title label
  */
  public Label getTitleLabel() {
    return title;
  }

  /**
  * Gets the value label.

  * @return the value label
  */
  public Label getValueLabel() {
    return value;
  }

  /**
  * Gets the content box.

  * @return the VBox containing main content
  */
  public VBox getContentBox() {
    return contentBox;
  }

  /**
  * Gets the footer.

  * @return the HBox containing footer content
  */
  public HBox getFooter() {
    return footer;
  }

  /**
  * Adds content nodes to the card's content area.

  * @param nodes the nodes to add to the content area
  * @return this ControlCard instance for method chaining
  */
  public ControlCard addContent(Node... nodes) {
    log.trace("Adding {} content node(s) to card '{}'", nodes.length, title.getText());

    contentBox.getChildren().addAll(nodes);
    return this;
  }

  /**
  * Sets the value text displayed on the card.

  * @param text the value text to display
  * @return this ControlCard instance for method chaining
  */
  public ControlCard setValueText(String text) {
    log.debug("Updating card '{}' value: {} -> {}", title.getText(), value.getText(), text);

    value.setText(text);
    return this;
  }

}
