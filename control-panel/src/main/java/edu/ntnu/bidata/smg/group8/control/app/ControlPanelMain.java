package edu.ntnu.bidata.smg.group8.control.app;

import edu.ntnu.bidata.smg.group8.common.util.AppLogger;
import edu.ntnu.bidata.smg.group8.control.console.ConsoleInputLoop;
import edu.ntnu.bidata.smg.group8.control.console.DisplayManager;
import edu.ntnu.bidata.smg.group8.control.infra.network.PanelAgent;
import edu.ntnu.bidata.smg.group8.control.logic.command.CommandInputHandler;
import edu.ntnu.bidata.smg.group8.control.logic.state.StateStore;
import edu.ntnu.bidata.smg.group8.control.ui.controller.ControlPanelController;
import edu.ntnu.bidata.smg.group8.control.ui.controller.DashboardController;
import edu.ntnu.bidata.smg.group8.control.ui.controller.SceneManager;
import edu.ntnu.bidata.smg.group8.control.ui.view.ControlPanelView;
import edu.ntnu.bidata.smg.group8.control.ui.view.DashboardView;
import edu.ntnu.bidata.smg.group8.control.util.UiExecutors;
import java.io.IOException;
import java.util.Objects;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.slf4j.Logger;

/**
* Entry point for the Smart Greenhouse application.
*
* @author Andrea Sandnes
* @version 16.10.2025
*/
public final class ControlPanelMain extends Application {
  private static final Logger log = AppLogger.get(ControlPanelMain.class);

  private PanelAgent agent;
  private ControlPanelController controller;
  private DashboardController dashboardController;
  private SceneManager sceneManager;

  private DisplayManager consoleDisplay;

  private Thread consoleInputThread;
  private ConsoleInputLoop consoleInput;

  private Thread dynamicDataThread;

  /**
  * Initializes and launches the Control Panel user interface.
  *
  * @param stage the primary stage provided by the JavaFX runtime
  */
  @Override
  public void start(Stage stage) {
    log.info("Starting application (host={} port={} panelId={})",
            BROKER_HOST(), BROKER_PORT(), PANEL_ID());

    try {
      StateStore stateStore = new StateStore();

      try {
        agent = new PanelAgent(BROKER_HOST(), BROKER_PORT(), PANEL_ID(), stateStore);
        agent.start();
        log.info("PanelAgent connected to broker at {}:{}", BROKER_HOST(), BROKER_PORT());
      } catch (IOException e) {
        log.warn("Failed to connect to broker at {}:{}. Using test data only.",
                BROKER_HOST(), BROKER_PORT(), e);
        // Continue without broker - will use test data
      }


      CommandInputHandler cmdHandler = new CommandInputHandler(agent);

      ControlPanelView controlPanelView = new ControlPanelView();
      DashboardView dashboardView = new DashboardView();

      controller = new ControlPanelController(controlPanelView, cmdHandler, stateStore);

      if (agent != null) {
        agent.addNodeListListener(controller::updateAvailableNodes);
      }

      sceneManager = new SceneManager();
      sceneManager.registerView("dashboard", dashboardView.getRootNode());
      sceneManager.registerView("control-panel", controlPanelView.getRootNode());
      log.info("Views registered with SceneManager");

      dashboardController = new DashboardController(dashboardView, sceneManager);

      controller.start();
      dashboardController.start();

      controlPanelView.getReturnButton().setOnAction(e -> {
        log.info("Return button clicked");
        sceneManager.showView("dashboard");
      });

      sceneManager.showView("dashboard");
      log.info("DashboardController and ControlPanelController initialized and started");

      boolean enableConsoleDisplay = Boolean.parseBoolean(
              System.getProperty("console.display", "false"));
      boolean enableConsoleInput = Boolean.parseBoolean(
              System.getProperty("console.input", "false"));
      boolean clearConsole = Boolean.parseBoolean(
              System.getProperty("console.clear", "false"));

      if (enableConsoleDisplay) {
        consoleDisplay = new DisplayManager(stateStore);
        consoleDisplay.setClearScreen(clearConsole);
        consoleDisplay.start();
      }

      if (enableConsoleInput) {
        consoleInput = new ConsoleInputLoop(cmdHandler, controller, consoleDisplay, stateStore);
        consoleInputThread = new Thread(consoleInput, "console-input");
        consoleInputThread.setDaemon(true);
        consoleInputThread.start();
      }

      log.debug("Creating Scene with dimensions 1000x700");
      Scene scene = new Scene(sceneManager.getContainer(), 1000, 700);

      String cssPath = "/css/styleSheet.css";
      log.debug("Loading CSS from: {}", cssPath);
      scene.getStylesheets().add(
              Objects.requireNonNull(ControlPanelMain.class.getResource(cssPath),
                      "stylesheet not found: " + cssPath).toExternalForm());
      log.debug("CSS stylesheet loaded successfully");

      stage.setTitle("Smart Farm - Dashboard @ " + BROKER_HOST() + ":" + BROKER_PORT());
      stage.setScene(scene);

      stage.setOnCloseRequest(e -> shutdown());

      log.info("Showing application window");
      stage.show();
    } catch (Exception e) {
      log.error("Failed to start Control Panel application", e);
      shutdown();
      throw e;
    }
  }

  /**
  * Stops the JavaFx application gracefully.
  */
  @Override
  public void stop() {
    shutdown();
  }

  /**
  * Gracefully shuts down the control panel, and all
   * its components.
  */
  private void shutdown() {
    log.info("Shutting down Control Panel");

    if (dashboardController != null) {
      try {
        dashboardController.stop();
        log.debug("DashboardController stopped");
      } catch (Exception e) {
        log.error("Error stopping dashboard controller", e);
      } finally {
        dashboardController = null;
      }
    }

    if (controller != null) {
      try {
        controller.stop();
        log.debug("ControlPanelController stopped");
      } catch (Exception e) {
        log.error("Error stopping controller", e);
      } finally {
        controller = null;
      }
    }

    try {
      log.info("Shutting down UiExecutors thread pool");
      UiExecutors.shutDown();
      log.debug("UiExecutors shutdown complete");
    } catch (Exception e) {
      log.error("Error shutting down UiExecutors", e);
    }

    if (consoleInput != null) {
      try {
        consoleInput.stop();
      } catch (Exception e) {
        log.warn("Error stopping console input loop", e);
      } finally {
        consoleInput = null;
      }
    }

    if (consoleInputThread != null && consoleInputThread.isAlive()) {
      try {
        consoleInputThread.join(500);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        log.warn("Interrupted while waiting for console input thread to finish", e);
      } finally {
        consoleInputThread = null;
      }
    }

    if (dynamicDataThread != null) {
      try {
        dynamicDataThread.interrupt();
        dynamicDataThread.join(2500);
        if (dynamicDataThread.isAlive()) {
          log.warn("Dynamic data thread did not stop within timeout");
        } else {
          log.debug("Dynamic data thread stopped");
        }
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        log.warn("Interrupted while waiting for dynamic data thread to finish", e);
      } finally {
        dynamicDataThread = null;
      }
    }

    if (consoleDisplay != null) {
      try {
        consoleDisplay.stop();
        log.debug("DisplayManager stopped");
      } catch (Exception e) {
        log.warn("Error stopping console display", e);
      } finally {
        consoleDisplay = null;
      }
    }

    if (agent != null) {
      try {
        agent.close();
        log.debug("PanelAgent closed");
      } catch (Exception e) {
        log.error("Error closing agent", e);
      } finally {
        agent = null;
      }
    }
    log.info("Control Panel shutdown complete");
  }


  /**
  * Displays error dialog with a title and message.
  *
  * @param title the title text of the dialog window
  * @param message the message to display inside the dialogue
  */
  private void showErrorDialog(String title, String message) {
    javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
            javafx.scene.control.Alert.AlertType.ERROR);
    alert.setTitle(title);
    alert.setHeaderText(null);
    alert.setContentText(message);
    alert.showAndWait();
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

  /**
  * Gets the hostname or IP address of the broker.
  *
  * @return the broker hostname
  */
  private static String BROKER_HOST() {
    return System.getProperty("broker.host", "localhost");
  }

  /**
  * Gets the TCP port number used to connect to the broker.
  *
  * @return the broker port number
  */
  private static int BROKER_PORT() {
    String portStr = System.getProperty("broker.port", "23048");
    try {
      return Integer.parseInt(portStr);
    } catch (NumberFormatException e) {
      log.error("Invalid broker.port value '{}', falling back to"
              + "default port 23048", portStr, e);
      return 23048;
    }
  }

  /**
  * Gets the unique identifier for this control panel instance.
  *
  * @return the panel ID
  */
  private static String PANEL_ID() {
    return System.getProperty("panel.id", "panel-1");
  }

}
