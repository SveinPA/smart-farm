package edu.ntnu.bidata.smg.group8.control.ui.view;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/**
*
*/
public class ControlCard extends StackPane {

  private static final double CARD_ASPECT = 0.62;

  private final VBox root;
  private final Label title;
  private final Label value;
  private final VBox contentBox;
  private final HBox footer;

  /**
  *

  * @param titleText
  */
  public ControlCard(String titleText) {
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
  * Creates the footer.

  * @return
  */
  public HBox getFooter() {
    return footer;
  }

  /**
  *

  * @param nodes
  * @return
  */
  public ControlCard addContent(Node... nodes) {
    contentBox.getChildren().addAll(nodes);
    return this;
  }

  /**
  *

  * @param text
  * @return
  */
  public ControlCard setValueText(String text) {
    value.setText(text);
    return this;
  }

}
