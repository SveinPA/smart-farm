package edu.ntnu.bidata.smg.group8.control.app;

import edu.ntnu.bidata.smg.group8.common.util.AppLogger;
import edu.ntnu.bidata.smg.group8.control.ui.view.ControlPanelView;
import edu.ntnu.bidata.smg.group8.control.ui.view.DashboardView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.slf4j.Logger;

import java.util.Objects;

public final class ControlPanelMain extends Application {
  private static final Logger log = AppLogger.get(ControlPanelMain.class);

  @Override
  public void start(Stage stage) {
    log.info("Starting application");

    try {
      // For trying out the Dashboard styling
      // DashboardView view = new DashboardView();
      ControlPanelView view = new ControlPanelView();

      log.debug("Creating Scene with dimensions 1000x700");
      Scene scene = new Scene(view.getRootNode(), 1000, 700);

      String cssPath = "/css/styleSheet.css";
      log.debug("Loading CSS from: {}", cssPath);

      scene.getStylesheets().add(Objects.requireNonNull(ControlPanelMain.class.getResource(cssPath),
              "app.css not found" + cssPath).toExternalForm());

      log.debug("CSS stylesheet loaded successfully");

      // stage.setTitle("Smart-Greenhouse");
      stage.setTitle("Control Panel");
      stage.setScene(scene);

      log.info("Showing application window");
      stage.show();
    }  catch (Exception e) {
      log.error("Failed to start Control Panel application", e);
      throw e;
    }
  }

  /**
  * Main entry point for the application.

  * @param args command line arguments
  */
  public static void main(String[] args) {
    log.info("Launching Control Panel application");
    try {
      launch(args);
    } catch (Exception e) {
      log.error("Fatal error during application launch", e);
      System.exit(1);
    }
  }
}
