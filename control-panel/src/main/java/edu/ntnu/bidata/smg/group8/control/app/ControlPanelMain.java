package edu.ntnu.bidata.smg.group8.control.app;

import edu.ntnu.bidata.smg.group8.common.util.AppLogger;
import edu.ntnu.bidata.smg.group8.control.console.ConsoleInputLoop;
import edu.ntnu.bidata.smg.group8.control.console.DisplayManager;
import edu.ntnu.bidata.smg.group8.control.infra.network.PanelAgent;
import edu.ntnu.bidata.smg.group8.control.logic.command.CommandInputHandler;
import edu.ntnu.bidata.smg.group8.control.logic.state.StateStore;
import edu.ntnu.bidata.smg.group8.control.ui.controller.ControlPanelController;
import edu.ntnu.bidata.smg.group8.control.ui.view.ControlPanelView;
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
    log.info("Starting application (host={} port={} panelId={} nodeId={})",
            BROKER_HOST(), BROKER_PORT(), PANEL_ID(), NODE_ID());

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

      // TEMPORARY: Always inject test data for GUI development
      injectTestData(stateStore);
      dynamicDataThread = startDynamicTestData(stateStore);

      CommandInputHandler cmdHandler = new CommandInputHandler(agent);

      ControlPanelView view = new ControlPanelView();
      controller = new ControlPanelController(view, cmdHandler, stateStore, NODE_ID());
      controller.start();

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
        consoleInput = new ConsoleInputLoop(cmdHandler, NODE_ID(), consoleDisplay, stateStore);
        consoleInputThread = new Thread(consoleInput, "console-input");
        consoleInputThread.setDaemon(true);
        consoleInputThread.start();
      }

      log.debug("Creating Scene with dimensions 1000x700");
      Scene scene = new Scene(view.getRootNode(), 1000, 700);

      String cssPath = "/css/styleSheet.css";
      log.debug("Loading CSS from: {}", cssPath);
      scene.getStylesheets().add(
              Objects.requireNonNull(ControlPanelMain.class.getResource(cssPath),
                      "stylesheet not found: " + cssPath).toExternalForm());
      log.debug("CSS stylesheet loaded successfully");

      stage.setTitle("Control Panel - Node " + NODE_ID()
              + " @ " + BROKER_HOST() + ":" + BROKER_PORT());
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

    if (dynamicDataThread != null && dynamicDataThread.isAlive()) {
      try {
        dynamicDataThread.interrupt();
        dynamicDataThread.join(200);
        log.debug("Dynamic data thread stopped");
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
      } catch (Exception e) {
        log.warn("Error stopping console display", e);
      } finally {
        consoleDisplay = null;
      }
    }

    if (agent != null) {
      try {
        agent.close();
      } catch (Exception e) {
        log.error("Error closing agent", e);
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
   * Injects test data into the StateStore for testing purposes.
   * This is a temporary solution for GUI development without a broker.
   */
  private void injectTestData(StateStore stateStore) {
    log.info("Injecting test sensor data...");

    java.time.Instant now = java.time.Instant.now();

    // Initial sensor readings
    stateStore.applySensor("node-1", "temperature", "28.5", "°C", now);
    stateStore.applySensor("node-1", "wind", "12.3", "m/s", now);
    stateStore.applySensor("node-1", "humidity", "65.0", "%", now);
    stateStore.applySensor("node-1", "pH", "6.8", "", now);

    log.info("Test data injection complete");
  }

  /**
   * Starts a background thread that continuously updates test data.
   * This simulates real sensor readings for testing the auto-mode functionality.
   *
   * @param stateStore the state store to inject data into
   * @return the thread running the dynamic data injection
   */
  private Thread startDynamicTestData(StateStore stateStore) {
    Thread testThread = new Thread(() -> {
      try {
        // Initial values
        double temp = 28.5;
        double wind = 12.3;
        double humidity = 65.0;
        double pH = 6.8;
        double fertilizer= 90.0;
        double light = 40000.0;
         // Actuator states
        int windowPosition = 50;
        boolean fanOn = true;
        boolean heaterOn = false;


        while (!Thread.currentThread().isInterrupted()) {
          // ===== SIMULATE SENSOR CHANGES =====

          // Temperature: ±1°C per update
          temp += (Math.random() - 0.5) * 2;
          temp = Math.max(15.0, Math.min(35.0, temp));

          // Wind: ±0.5 m/s per update
          wind += (Math.random() - 0.5) * 1;
          wind = Math.max(0.0, Math.min(20.0, wind));

          // Humidity: ±2% per update
          humidity += (Math.random() - 0.5) * 4;
          humidity = Math.max(30.0, Math.min(90.0, humidity));

          // pH: ±0.1 per update
          pH += (Math.random() - 0.5) * 0.2;
          pH = Math.max(5.5, Math.min(8.0, pH));

          //Fertilizer: ±2% per update
          fertilizer += (Math.random() - 0.5) * 4;
          fertilizer = Math.max(0.0, Math.min(300.0, fertilizer));

          // Light: ±5000 lx per update (simulates changing daylight/cloud cover)
          light += (Math.random() - 0.5) * 10000;
          light = Math.max(0.0, Math.min(80000.0, light));

          // Update sensors - VIKTIG: Bruk Locale.US for punktum som desimalskilletegn
          java.time.Instant now = java.time.Instant.now();
          stateStore.applySensor("node-1", "temperature",
                  String.format(java.util.Locale.US, "%.1f", temp), "°C", now);
          stateStore.applySensor("node-1", "wind",
                  String.format(java.util.Locale.US, "%.1f", wind), "m/s", now);
          stateStore.applySensor("node-1", "humidity",
                  String.format(java.util.Locale.US, "%.1f", humidity), "%", now);
          stateStore.applySensor("node-1", "pH",
                  String.format(java.util.Locale.US, "%.1f", pH), "", now);
            stateStore.applySensor("node-1", "fertilizer",
                  String.format(java.util.Locale.US, "%.1f", fertilizer), "ppm", now);
          stateStore.applySensor("node-1", "light",
                  String.format(java.util.Locale.US, "%.1f", light), "lx", now);


          // ===== SIMULATE ACTUATOR CHANGES =====

          // Window: adjust based on temperature and wind
          if (temp > 30 && wind < 15) {
            windowPosition = Math.min(100, windowPosition + 10);
          } else if (temp < 20 || wind > 15) {
            windowPosition = Math.max(0, windowPosition - 10);
          }

          // Fan: turn on if temperature is high
          fanOn = temp > 28;

          // Heater: turn on if temperature is low
          heaterOn = temp < 18;

          // Update actuators
          stateStore.applyActuator("node-1", "window",
                  String.valueOf(windowPosition), now);
          stateStore.applyActuator("node-1", "fan",
                  fanOn ? "on" : "off", now);
          stateStore.applyActuator("node-1", "heater",
                  heaterOn ? "on" : "off", now);

          log.debug("Updated test data: temp={}, wind={}, humidity={}, pH={}, " +
                          "window={}%, fan={}, heater={}, fertilizer={}",
                  String.format(java.util.Locale.US, "%.1f", temp),
                  String.format(java.util.Locale.US, "%.1f", wind),
                  String.format(java.util.Locale.US, "%.1f", humidity),
                  String.format(java.util.Locale.US, "%.1f", pH),
                  String.format(java.util.Locale.US, "%.1f",fertilizer),
                  windowPosition,
                  fanOn ? "on" : "off",
                  heaterOn ? "on" : "off");

          Thread.sleep(2000); // Update every 2 seconds
        }
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        log.debug("Dynamic test data thread interrupted");
      }
    }, "dynamic-test-data");

    testThread.setDaemon(true);
    testThread.start();
    log.info("Dynamic test data thread started (updates every 2 seconds)");

    return testThread;
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

  /**
  * Gets the node identifier that this control panel manages.
  *
  * @return the node ID
  */
  private static String NODE_ID() {
    return System.getProperty("node.id", "node-1");
  }
}
