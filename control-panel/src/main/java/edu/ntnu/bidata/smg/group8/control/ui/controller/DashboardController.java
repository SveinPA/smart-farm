package edu.ntnu.bidata.smg.group8.control.ui.controller;

import edu.ntnu.bidata.smg.group8.common.util.AppLogger;
import edu.ntnu.bidata.smg.group8.control.logic.command.CommandInputHandler;
import edu.ntnu.bidata.smg.group8.control.logic.state.ActuatorReading;
import edu.ntnu.bidata.smg.group8.control.logic.state.SensorReading;
import edu.ntnu.bidata.smg.group8.control.logic.state.StateStore;
import edu.ntnu.bidata.smg.group8.control.ui.view.DashboardView;
import java.util.List;
import java.util.function.Consumer;
import javafx.application.Platform;
import org.slf4j.Logger;

/**
 * Controller for the dashboard view. Which is responsible for
 * displaying real-time sensor data and managing user interactions.
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
  private Consumer<ActuatorReading> valveActuatorSink;
  private Integer lastWindowPosition;
  private Integer lastValvePosition;

  /**
   * Constructor for DashboardController.
   *
   * @param view the DashboardView instance
   * @param sceneManager the SceneManager which handles view navigation
   * @param stateStore the StateStore for accessing sensor and actuator data
   * @param cmdHandler the CommandInputHandler for sending actuator commands
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
   * Starts the controller, setting up event handlers and listeners.
   *
   * <p>This method initializes the dashboard by configuring
   * button actions and registering listeners for sensor
   * and actuator updates from the StateStore.</p>
   *
   * <p>AI has been used as guidance for this method in order to improve
   * relevancy/redundancy and code quality. The code as been reviewed and considered
   * before use, which improved learning.</p>
   */
  public void start() {
    logger.info("Starting DashboardController");

    view.getControlPanelButton().setOnAction(event -> {
      logger.info("Control Panel button clicked. Navigating to Control Panel.");
      sceneManager.showView("control-panel");
    });

    // AI helped by suggesting methods for setting up listeners and handlers
    setUpSensorListeners(); // reduced the redundancy by extracting method
    setUpActuatorListeners();
    setUpToggleButtonHandlers();

    logger.debug("DashboardController started successfully.");
  }

  /**
   * Sets up action handlers for the toggle buttons.
   *
   * <p>These handlers are called when the user clicks
   * the toggle buttons in the dashboard view.</p>
   *
   * <p>AI suggested to extract this method to improve code organization
   * and readability.</p>
   */
  private void setUpToggleButtonHandlers() {
    view.getWindowsToggleButton().setOnAction(event -> {
      handleWindowToggle();
    });

    view.getValveTogglebutton().setOnAction(event -> {
      handleValveToggle();
    });
  }

  /**
   * Sets up listeners for sensors updates from the StateStore.
   *
   * <p>These listeners update the dashboard view
   * when sensor readings change in the system.</p>
   *
   * <p>AI suggested to extract this method to improve code organization
   * and readability.</p>
   */
  private void setUpSensorListeners() {
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
                view.updateLightStatus(String.format("%.1f lx", light)));
      } catch (NumberFormatException e) {
        logger.warn("Invalid light value '{}' for nodeId={}", sr.value(), sr.nodeId());
      }
    };
    stateStore.addSensorSink(lightSink);
  }

  /**
   * Sets up listeners for actuator state updates from
   * the StateStore.
   *
   * <p>These listeners update the dashboard view
   * when actuator states change in the system.</p>
   *
   * <p>AI suggested to extract this method to improve code organization
   * and readability.</p>
   */
  private void setUpActuatorListeners() {
    windowActuatorSink = ar -> {
      if (!"window".equalsIgnoreCase(ar.type())) {
        return;
      }
      updateWindowSate(ar);
    };
    stateStore.addActuatorSink(windowActuatorSink);

    valveActuatorSink = ar -> {
      if (!"valve".equalsIgnoreCase(ar.type())) {
        return;
      }
      updateValveState(ar);
    };
    stateStore.addActuatorSink(valveActuatorSink);
  }

  /**
   * Updates the window state in the dashboard view.
   *
   * <p>The updated state should reflect the latest position of the window actuator
   * in the system.</p>
   *
   * <p>This method updates the window toggle button state
   * based on the latest actuator reading.</p>
   *
   *
   * @param ar the ActuatorReading containing the new state
   */
  private void updateWindowSate(ActuatorReading ar) {
    try {
      int position = Integer.parseInt(ar.state().trim());
      lastWindowPosition = position;

      Platform.runLater(() -> {
        boolean shouldBeSelected = position > 0;
        if (view.getWindowsToggleButton().isSelected() != shouldBeSelected) {
          view.getWindowsToggleButton().setSelected(shouldBeSelected);
        }
      });
      logger.debug("Window position updated to {}%", position);
    } catch (NumberFormatException e) {
      logger.warn("Invalid window actuator state '{}' for nodeId={}", ar.state(), ar.nodeId());
    }
  }

  /**
   * Updates the valve state in the dashboard view.
   *
   * <p>The updated state should reflect the latest position of the valve actuator
   * in the system.</p>
   *
   * <p>This method updates the valve toggle button state
   * based on the latest actuator reading.</p>
   *
   * @param ar the ActuatorReading containing the new state
   */
  private void updateValveState(ActuatorReading ar) {
    try {
      int position = Integer.parseInt(ar.state().trim());
      lastValvePosition = position;

      Platform.runLater(() -> {
        boolean shouldBeSelected = position > 0;
        if (view.getValveTogglebutton().isSelected() != shouldBeSelected) {
          view.getValveTogglebutton().setSelected(shouldBeSelected);
        }
      });
      logger.debug("Valve position updated to {}%", position);
    } catch (NumberFormatException e) {
      logger.warn("Invalid valve actuator state '{}' for nodeId={}", ar.state(), ar.nodeId());
    }
  }

  /**
   * Handles the window toggle button action in the dashboard view.
   *
   * <p>This window should be in sync with the value displayed on the
   * valve control card in the control panel view.</p>
   *
   * <p>This method determines the current state of the window
   * and sends a command to toggle its position between open and closed.
   * The state should match the last known position of the window in the system.</p>
   */
  private void handleWindowToggle() {
    boolean currentlyOpen = lastWindowPosition != null && lastWindowPosition > 0;
    int newPosition = currentlyOpen ? 0 : 50;

    logger.info("Dashboard: user requested window position {}%", newPosition);

    String nodeId = resolveTargetNodeId();
    // If no node is available, revert the toggle button state
    if (nodeId == null) {
      Platform.runLater(() ->
              view.getWindowsToggleButton().setSelected(currentlyOpen));
      return;
    }
    try {
      cmdHandler.sendActuatorCommand(nodeId, "window", newPosition);
    } catch (Exception e) {
      logger.warn("Failed to send window command from dashboard", e);
      Platform.runLater(() ->
              view.getWindowsToggleButton().setSelected(currentlyOpen));
    }
  }

  /**
   * Handles the valve toggle button action in the dashboard view.
   *
   * <p>This value should be in sync with the value displayed on the
   * valve control card in the control panel view.</p>
   *
   * <p>This method determines the current state of the valve
   * and sends a command to toggle its position between open and closed.
   * The state should match the last known position of the valve in the system.</p>
   */
  private void handleValveToggle() {
    boolean currentlyOpen = lastValvePosition != null && lastValvePosition > 0;
    int newPosition = currentlyOpen ? 0 : 50;

    logger.info("Dashboard: user requested valve position {}%", newPosition);

    String nodeId = resolveTargetNodeId();
    // If no node is available, revert the toggle button state
    if (nodeId == null) {
      Platform.runLater(() ->
              view.getValveTogglebutton().setSelected(currentlyOpen));
      return;
    }

    try {
      cmdHandler.sendActuatorCommand(nodeId, "valve", newPosition);
    } catch (Exception e) {
      logger.warn("Failed to send valve command from dashboard", e);
      Platform.runLater(() ->
              view.getValveTogglebutton().setSelected(currentlyOpen));
    }
  }


  private String resolveTargetNodeId() {
    List<String> nodes = stateStore.nodeIds();
    if (nodes == null || nodes.isEmpty()) {
      logger.warn("Dashboard: no nodes available for actuator commands");
      return null;
    }

    return nodes.get(0);
  }

  /**
   * Stops the controller, and clean up resources.
   *
   * <p>This method removes all registered listeners
   * and handlers to prevent memory leaks and unintended behavior.</p>
   */
  public void stop() {
    logger.info("Stopping DashboardController");

    view.getControlPanelButton().setOnAction(null);
    view.getWindowsToggleButton().setOnAction(null);
    view.getValveTogglebutton().setOnAction(null);

    // Remove sensor sinks
    if (temperatureSink != null) {
      stateStore.removeSensorSink(temperatureSink);
      temperatureSink = null;
    }
    if (humiditySink != null) {
      stateStore.removeSensorSink(humiditySink);
      humiditySink = null;
    }
    if (lightSink != null) {
      stateStore.removeSensorSink(lightSink);
      lightSink = null;
    }

    // Remove actuator sinks
    if (windowActuatorSink != null) {
      stateStore.removeActuatorSink(windowActuatorSink);
      windowActuatorSink = null;
    }
    if (valveActuatorSink != null) {
      stateStore.removeActuatorSink(valveActuatorSink);
      valveActuatorSink = null;
    }
    logger.debug("DashboardController stopped.");
  }
}
