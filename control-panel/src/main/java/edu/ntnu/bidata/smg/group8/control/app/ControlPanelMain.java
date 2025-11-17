package edu.ntnu.bidata.smg.group8.control.app;

import edu.ntnu.bidata.smg.group8.common.util.AppLogger;
import edu.ntnu.bidata.smg.group8.control.console.ConsoleInputLoop;
import edu.ntnu.bidata.smg.group8.control.console.DisplayManager;
import edu.ntnu.bidata.smg.group8.control.infra.network.PanelAgent;
import edu.ntnu.bidata.smg.group8.control.logic.command.CommandInputHandler;
import edu.ntnu.bidata.smg.group8.control.logic.history.HistoricalDataStore;
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
* <h3>Control Panel Main - Entry Point for the Smart Greenhouse GUI.</h3>
*
* <p>This class binds the entire application by initializing the
* JavaFX GUI establishing connection to the message broker,
* setting up state management and coordinating all the major
* components like sensors, actuators and historical data tracking.</p>
*
* <p><b>Core Components:</b></p>
* <ul>
*     <li><b>Network layer:</b> Connects to message broker via PanelAgent for live sensor data</li>
*     <li><b>State management:</b> Centralized StateStore that all components observe</li>
*     <li><b>Historical tracking:</b> Records 24-hour statistics for trend analysis</li>
*     <li><b>Dual UI:</b> Dashboard overview + detailed control panel views</li>
*     <li><b>Optional console:</b> Text-based display and input for debugging</li>
* </ul>
*
* <p><b>Important Notes:</b></p>
* <ul>
*     <li>Application continues running even if broker connection fails</li>
*     <li>All sensor readings are automatically stored for historical analysis</li>
*     <li>Window close triggers proper shutdown of all threads and connections</li>
*     <li>CSS styling is loaded from {@code /css/styleSheet.css} in resources</li>
* </ul>
*
* @author Andrea Sandnes
* @version 1.0
* @since 16.10.2025
* @see PanelAgent
* @see StateStore
* @see HistoricalDataStore
* @see DashboardController
* @see ControlPanelController
* @see SceneManager
*/
public final class ControlPanelMain extends Application {
  private static final Logger log = AppLogger.get(ControlPanelMain.class);

  private PanelAgent agent;
  private ControlPanelController controller;
  private DashboardController dashboardController;
  private SceneManager sceneManager;
  private HistoricalDataStore historicalDataStore;

  private DisplayManager consoleDisplay;

  private Thread consoleInputThread;
  private ConsoleInputLoop consoleInput;

  private Thread dynamicDataThread;

  /**
  * Sets up and launches the main application window.
  *
  * <p>This method handles all the heavy lifting: connecting to
  * broker, initializing the state management, creating UI views,
  * and wiring everything together. If the broker connection fails,
  * the app will continue running but without live data.</p>
  *
  * @param stage the primary stage provided by the JavaFX runtime
  * @throws Exception if critical initialization fails
  */
  @Override
  public void start(Stage stage) {
    log.info("Starting application (host={} port={} panelId={})",
            BROKER_HOST(), BROKER_PORT(), PANEL_ID());

    try {
      StateStore stateStore = new StateStore();

      // Create historical data store for 24-hour statistics
      this.historicalDataStore = new HistoricalDataStore();

      // Register historical data store to receive all sensor readings
      stateStore.addSensorSink(reading -> {
        try {
          double value = Double.parseDouble(reading.value());
          historicalDataStore.addReading(reading.type(), value, reading.ts());
        } catch (NumberFormatException e) {
          log.debug("Skipping non-numeric sensor value: {} = {}", reading.type(), reading.value());
        }
      });

      // Connect to message broker (optional - app works without it)
      try {
        agent = new PanelAgent(BROKER_HOST(), BROKER_PORT(), PANEL_ID(), stateStore);
        agent.start();
        log.info("PanelAgent connected to broker at {}:{}", BROKER_HOST(), BROKER_PORT());
      } catch (IOException e) {
        log.warn("Failed to connect to broker at {}:{}. Using test data only.",
                BROKER_HOST(), BROKER_PORT(), e);
      }

      CommandInputHandler cmdHandler = new CommandInputHandler(agent);

      // Create the main UI views
      ControlPanelView controlPanelView = new ControlPanelView();
      DashboardView dashboardView = new DashboardView();

      // Wiring up controllers with their dependencies
      controller = new ControlPanelController(controlPanelView,
              cmdHandler, stateStore, this.historicalDataStore);

      if (agent != null) {
        agent.addNodeListListener(controller::updateAvailableNodes);
      }

      // Register views with scene manager for navigation
      sceneManager = new SceneManager();
      sceneManager.registerView("dashboard", dashboardView.getRootNode());
      sceneManager.registerView("control-panel", controlPanelView.getRootNode());
      log.info("Views registered with SceneManager");

      dashboardController = new DashboardController(dashboardView,
              sceneManager, stateStore, cmdHandler);

      controller.start();
      dashboardController.start();

      // Setting up navigation between views
      controlPanelView.getReturnButton().setOnAction(e -> {
        log.info("Return button clicked");
        sceneManager.showView("dashboard");
      });

      sceneManager.showView("dashboard");
      log.info("DashboardController and ControlPanelController initialized and started");

      // Optional console interface (enabled via system properties)
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

      // Building and configuring the main window
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
  * Called by JavaFX when the application window is closed.
  * Delegates to {@link #shutdown()} for cleanup.
  */
  @Override
  public void stop() {
    shutdown();
  }

  /**
  * Performs a clean shutdown of all application components.
  *
  * <p>Stops controllers, closes  network connections, terminates
  * background threads, and releases resources in the correct order
  * to avoid issues. Each component is shut down in its own try-catch
  * block so that one failure doesn't prevent the rest from being
  * cleaned up.</p>
  */
  private void shutdown() {
    log.info("Shutting down Control Panel");

    // Stopping UI controllers first since they depend on other components
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

    // Shutting down the shared thread pool used by UI components
    try {
      log.info("Shutting down UiExecutors thread pool");
      UiExecutors.shutDown();
      log.debug("UiExecutors shutdown complete");
    } catch (Exception e) {
      log.error("Error shutting down UiExecutors", e);
    }

    // Stopping console I/O threads
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

    // Giving the dynamic data thread some time to finish gracefully
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

    // Closing network connection last since other components might still need it
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
   * Displays a simple error dialog to the user.
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
   * Application entry point. Launches the JavaFx application.

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
   * Returns the broker hostname from system properties.
   * Defaults to "localhost" if not specified.
   *
   * @return the broker hostname
   */
  private static String BROKER_HOST() {
    return System.getProperty("broker.host", "localhost");
  }

  /**
   * Returns the broker port from system properties.
   * Defaults to 23048 if not specified or if the value is invalid.
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
   * Returns the Panel ID from system properties.
   * Defaults to "panel-1" if not specified.
   *
   * @return the panel ID
   */
  private static String PANEL_ID() {
    return System.getProperty("panel.id", "panel-1");
  }

}
