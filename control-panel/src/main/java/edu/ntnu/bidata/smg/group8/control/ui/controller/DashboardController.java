package edu.ntnu.bidata.smg.group8.control.ui.controller;

import edu.ntnu.bidata.smg.group8.common.util.AppLogger;
import edu.ntnu.bidata.smg.group8.control.logic.command.CommandInputHandler;
import edu.ntnu.bidata.smg.group8.control.logic.state.ActuatorReading;
import edu.ntnu.bidata.smg.group8.control.logic.state.SensorReading;
import edu.ntnu.bidata.smg.group8.control.logic.state.StateStore;
import edu.ntnu.bidata.smg.group8.control.ui.view.DashboardView;
import java.util.function.Consumer;
import javafx.application.Platform;
import org.slf4j.Logger;


/**
 * Controller for the dashboard view.
 *
 * <p>Manages Dashboard-specific logic and user interactions.</p>
 *
 * <p>This class handles updating the dashboard display with real-time
 * sensor data such as temperature, humidity, and light levels.
 * It also manages navigation to other views like the control panel.</p>
 *
 * <p>AI has been used as guidance for this class in order to improve relevancy
 * and code quality. The code as been reviewed and considered before use, which
 * helps learning.</p>
 *
 * @author Mona Amundsen
 * @version 12.11.2025
 */
public class DashboardController {
  private static final Logger logger = AppLogger.get(DashboardController.class);

  private final DashboardView view;
  private final SceneManager sceneManager;
  private final StateStore stateStore;
  private Consumer<SensorReading> temperatureSink;
  private Consumer<SensorReading> humiditySink;
  private Consumer<SensorReading> lightSink;
  private final CommandInputHandler cmdHandler;
  private Consumer<ActuatorReading> windowActuatorSink;
  private Integer lastWindowPosition;

  /**
   * Constructor for DashboardController.
   *
   * @param view the DashboardView instance
   * @param sceneManager the SceneManager which handles view navigation
   */
  public DashboardController(DashboardView view, SceneManager sceneManager,
                             StateStore stateStore, CommandInputHandler cmdHandler) {
    this.view = view;
    this.sceneManager = sceneManager;
    this.stateStore = stateStore;
    this.cmdHandler = cmdHandler;
    logger.info("DashboardController initialized.");
  }

  /**
   * Starts the controller, setting up event handlers.
   */
  public void start() {
    logger.info("Starting DashboardController");
    
    // Navigate to control panel when button clicked
    view.getControlPanelButton().setOnAction(event -> {
      logger.info("Control Panel button clicked. Navigating to Control Panel.");
      sceneManager.showView("control-panel");
    });

    temperatureSink = sr -> {
      if (!"temp".equalsIgnoreCase(sr.type())) {
        return;
      }
      try {
        double temp = Double.parseDouble(sr.value());
        Platform.runLater(() ->
                view.updateTemperatureDisplay(String.format("%.1f °C", temp)));
      } catch (NumberFormatException e) {
        logger.warn("Invalid temperature value '{}' for nodeId={}", sr.value(), sr.nodeId());
      }
    };
    stateStore.addSensorSink(temperatureSink);

    humiditySink = sr -> {
      if (!"hum".equalsIgnoreCase(sr.type())) {
        return;
      }
      try {
        double hum = Double.parseDouble(sr.value());
        Platform.runLater(() ->
                view.updateHumidityDisplay(String.format("%.1f %%", hum)));
      } catch (NumberFormatException e) {
        logger.warn("Invalid humidity value '{}' for nodeId={}", sr.value(), sr.nodeId());
      }
    };
    stateStore.addSensorSink(humiditySink);

    lightSink = sr -> {
      if (!"light".equalsIgnoreCase(sr.type())) {
        return;
      }
      try {
        double light = Double.parseDouble(sr.value());
        Platform.runLater(() ->
                view.updateLightStatus(String.format("%.1f m/s", light)));
      } catch (NumberFormatException e) {
        logger.warn("Invalid wind value '{}' for nodeId={}", sr.value(), sr.nodeId());
      }
    };
    stateStore.addSensorSink(lightSink);

    view.getWindowsToggleButton().setOnAction(event -> {
      boolean currentlyOpen = lastWindowPosition != null && lastWindowPosition > 0;
      int newPosition = currentlyOpen ? 0 : 50;

      logger.info("Dashboard: user requested window position {}%", newPosition);

      try {
        cmdHandler.sendActuatorCommand("window", newPosition);
      } catch (Exception e) {
        logger.warn("Failed to send window command from dashboard", e);
      }
    });

    view.getValveTogglebutton().setOnAction(event -> {
      boolean currentlyOpen = lastWindowPosition != null && lastWindowPosition > 0;
      int newPosition = currentlyOpen ? 0 : 50;

      logger.info("Dashboard: user requested valve position {}%", newPosition);

      try {
        cmdHandler.sendActuatorCommand("valve", newPosition);
      } catch (Exception e) {
        logger.warn("Failed to send valve command from dashboard", e);
      }
    });

    logger.debug("Dashboard Controller started successfully.");
  }

  /**
   * Stops the controller, and clean up resources.
   */
  public void stop() {
    logger.info("Stopping DashboardController");
    view.getControlPanelButton().setOnAction(null);
    logger.debug("DashboardController stopped.");
  }
}
