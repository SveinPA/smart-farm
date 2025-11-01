package edu.ntnu.bidata.smg.group8.control.app;
import edu.ntnu.bidata.smg.group8.common.util.AppLogger;
import edu.ntnu.bidata.smg.group8.control.console.DisplayManager;
import edu.ntnu.bidata.smg.group8.control.infra.network.PanelAgent;
import edu.ntnu.bidata.smg.group8.control.logic.command.CommandInputHandler;
import edu.ntnu.bidata.smg.group8.control.logic.state.StateStore;
import edu.ntnu.bidata.smg.group8.control.ui.controller.ControlPanelController;
import edu.ntnu.bidata.smg.group8.control.ui.view.ControlPanelView;
import edu.ntnu.bidata.smg.group8.control.ui.view.DashboardView;
import java.io.IOException;
import java.time.Instant;
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

  private DisplayManager consoleDisplay;


  /**
  * Initializes and launches the Control Panel user interface.
  *
  * @param stage the primary stage provided by the JavaFX runtime
  */
  @Override
  public void start(Stage stage) {
    log.info("Starting application (host={} port={} panelId={} nodeId={})",
            BROKER_HOST(), BROKER_PORT(), PANEL_ID(), NODE_ID());

    try {
      // For trying out the Dashboard styling
      // DashboardView view = new DashboardView();
      StateStore stateStore = new StateStore();

      try {
        agent = new PanelAgent(BROKER_HOST(), BROKER_PORT(), PANEL_ID(), stateStore);
        agent.start();
        log.info("PanelAgent connected to broker at {}:{}", BROKER_HOST(),
            BROKER_PORT());
      } catch (IOException e) {
        log.error("Failed to connect to broker at {}:{}. Is the broker running?",
            BROKER_HOST(), BROKER_PORT(), e);
        showErrorDialog("Connection Failed",
                "Could not connect to broker at " + BROKER_HOST() + ":" + BROKER_PORT()
            + "\n\nPlease ensure the broker is running and try again.");
        return;
      }

      CommandInputHandler cmdHandler = new CommandInputHandler(agent);

      ControlPanelView view = new ControlPanelView();

      controller = new ControlPanelController(view, cmdHandler, stateStore, NODE_ID());

      controller.start();

      consoleDisplay = new edu.ntnu.bidata.smg.group8.control.console.DisplayManager(stateStore);
      consoleDisplay.setClearScreen(true); // eller false hvis ANSI ikke funker i din terminal
      consoleDisplay.start();

      // TEST DATA INJECTION (for development/testing)

      new Thread(() -> {
        try {
          String testNodeId = NODE_ID();

          // Wait for UI to initialize
          Thread.sleep(3000);

          // TEST ACTUATORS
          log.info("🧪 Injecting test actuator readings...");
          Instant now = Instant.now();
          stateStore.applyActuator(testNodeId, "fan", "75", now);
          stateStore.applyActuator(testNodeId, "heater", "22", now);
          stateStore.applyActuator(testNodeId, "valve", "1", now);
          stateStore.applyActuator(testNodeId, "window", "50", now);
          log.info("✅ Test actuator readings injected");

          // TEST SENSORS
          Thread.sleep(2000);
          log.info("🧪 Injecting test sensor readings...");
          stateStore.applySensor(testNodeId, "temperature", "24.5", "°C", Instant.now());
          stateStore.applySensor(testNodeId, "humidity", "65.0", "%", Instant.now());
          stateStore.applySensor(testNodeId, "wind", "3.2", "m/s", Instant.now());
          stateStore.applySensor(testNodeId, "fertilizer", "125.0", "ppm", Instant.now());
          stateStore.applySensor(testNodeId, "light", "850.0", "lux", Instant.now());
          stateStore.applySensor(testNodeId, "ph", "6.5", "", Instant.now());
          log.info("✅ Test sensor readings injected");

          // TEST ARTIFICIAL LIGHTS
          Thread.sleep(2000);
          log.info("🧪 Injecting artificial light commands...");
          stateStore.applyActuator(testNodeId, "artificial_light", "75", Instant.now());
          log.info("✅ Artificial lights set to 75%");

        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
          log.warn("Test data injection interrupted");
        }
      }, "test-data-injector").start();

      log.debug("Creating Scene with dimensions 1000x700");
      Scene scene = new Scene(view.getRootNode(), 1000, 700);

      String cssPath = "/css/styleSheet.css";
      log.debug("Loading CSS from: {}", cssPath);

      scene.getStylesheets().add(Objects.requireNonNull(ControlPanelMain.class.getResource(cssPath),
              "app.css not found" + cssPath).toExternalForm());

      log.debug("CSS stylesheet loaded successfully");

      // stage.setTitle("Smart-Greenhouse");
      //stage.setTitle("Control Panel");
      stage.setTitle("Control Panel - Node " + NODE_ID()
          + " @ " + BROKER_HOST() + ":" + BROKER_PORT());
      stage.setScene(scene);

      stage.setOnCloseRequest(e -> {
        shutdown();
      });

      log.info("Showing application window");
      stage.show();
    }  catch (Exception e) {
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
  * Gracefully shuts down the control panel and all its components.
  */
  private void shutdown() {
    log.info("Shutting down Control Panel");

    if (controller != null) {
      try {
        controller.stop();
      } catch (Exception e) {
        log.error("Error stopping controller", e);
      }
    }
    if (consoleDisplay != null) {
      try { consoleDisplay.stop(); } catch (Exception ignore) {}
      consoleDisplay = null;
    }
    if (agent != null) {
      try {
        agent.close();
      } catch (Exception e) {
        log.error("Error closing agent", e);
      }
    }
    log.info("Control Panel stopped");
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
    return Integer.parseInt(System.getProperty("broker.port", "23048"));
  }

  /**
  * Gets the unique identifier for this control panel instance.
  *
  * @return the panel ID
  */
  private static String PANEL_ID() {
    return System.getProperty("panel.id", "panel-1");
  }

  /**
  * Gets the node identifier that this control panel manages.
  *
  * @return the node ID
  */
  private static String NODE_ID() {
    return System.getProperty("node.id", "node-1");
  }
}
