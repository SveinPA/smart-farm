package edu.ntnu.bidata.smg.group8.control.app;

import edu.ntnu.bidata.smg.group8.control.ui.view.ControlPanelView;
import edu.ntnu.bidata.smg.group8.control.ui.view.DashboardView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.Objects;

public final class ControlPanelMain extends Application {

  @Override
  public void start(Stage stage) {

    // For trying out the Dashboard styling
   // DashboardView view = new DashboardView();
    ControlPanelView view = new ControlPanelView();
    Scene scene = new Scene(view.getRootNode(), 1000, 700);

    String cssPath = "/css/styleSheet.css";

    scene.getStylesheets().add(Objects.requireNonNull(ControlPanelMain.class.getResource(cssPath),
            "app.css not found" + cssPath).toExternalForm());

   // stage.setTitle("Smart-Greenhouse");
    stage.setTitle("Control Panel");
    stage.setScene(scene);
    stage.show();
  }
  public static void main(String[] args) {
    launch(args);
  }
}

