package edu.ntnu.bidata.smg.group8.control.ui.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;


/**
* This class represents the dashboard display of the application.
*/
public class DashboardView {
  private final BorderPane rootNode;


  public DashboardView() {
    this.rootNode = new BorderPane();

      HBox upperBorder = new HBox();
      Text upperBorderTitle = new Text("Smart - Greenhouse");


      upperBorder.getChildren().add(upperBorderTitle);
      upperBorder.setAlignment(Pos.CENTER);
      upperBorder.setPadding(new Insets(25,0,25,0));



  }

  public BorderPane getRootNode() {
    return rootNode;
  }
}
