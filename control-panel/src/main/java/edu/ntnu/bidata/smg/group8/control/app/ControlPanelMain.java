package edu.ntnu.bidata.smg.group8.control.app;

import edu.ntnu.bidata.smg.group8.control.ui.view.DashboardView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public final class ControlPanelMain extends Application {

  @Override
  public void start(Stage stage) {

    DashboardView view = new DashboardView();
    stage.setScene(new Scene(view.getRootNode(), 1000, 700));
    stage.show();
  }
  public static void main(String[] args) {
    launch(args);
  }
}

