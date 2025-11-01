package edu.ntnu.bidata.smg.group8.control.ui.controller;

import edu.ntnu.bidata.smg.group8.common.util.AppLogger;
import edu.ntnu.bidata.smg.group8.control.logic.command.CommandInputHandler;
import edu.ntnu.bidata.smg.group8.control.logic.state.ActuatorReading;
import edu.ntnu.bidata.smg.group8.control.logic.state.StateStore;
import edu.ntnu.bidata.smg.group8.control.ui.controller.cardcontrollers.FanCardController;
import edu.ntnu.bidata.smg.group8.control.ui.controller.cardcontrollers.FertilizerCardController;
import edu.ntnu.bidata.smg.group8.control.ui.controller.cardcontrollers.HeaterCardController;
import edu.ntnu.bidata.smg.group8.control.ui.controller.cardcontrollers.HumidityCardController;
import edu.ntnu.bidata.smg.group8.control.ui.controller.cardcontrollers.LightCardController;
import edu.ntnu.bidata.smg.group8.control.ui.controller.cardcontrollers.PHCardController;
import edu.ntnu.bidata.smg.group8.control.ui.controller.cardcontrollers.TemperatureCardController;
import edu.ntnu.bidata.smg.group8.control.ui.controller.cardcontrollers.ValveCardController;
import edu.ntnu.bidata.smg.group8.control.ui.controller.cardcontrollers.WindSpeedCardController;
import edu.ntnu.bidata.smg.group8.control.ui.controller.cardcontrollers.WindowsCardController;
import edu.ntnu.bidata.smg.group8.control.ui.view.ControlPanelView;
import java.util.Objects;
import java.util.function.Consumer;
import org.slf4j.Logger;

/**
* Main controller responsible for managing all control card controllers
* within the greenhouse control panel interface.
*
* <p>The ControlPanelController serves as the central hub for coordinating
*     all individual subsystem controllers such as temperature, humidity,
*     light, and wind speed. It initializes, starts, and stops each subsystem
*     controller in a consistent and safe manner.
*
*     This design ensures that all UI cards are synchronized and can be
*     controlled from a single entry point.
* </p>

* @author Andrea Sandnes
* @version 28.10.2025
*/
public class ControlPanelController {
  private static final Logger log = AppLogger.get(ControlPanelController.class);

  private final ControlPanelView view;
  private final CommandInputHandler cmdHandler;
  private final StateStore stateStore;
  private final String nodeId;

  private FanCardController fanController;
  private FertilizerCardController fertilizerController;
  private HeaterCardController heaterController;
  private HumidityCardController humidityController;
  private LightCardController lightController;
  private PHCardController pHController;
  private TemperatureCardController temperatureController;
  private ValveCardController valveController;
  private WindowsCardController windowsController;
  private WindSpeedCardController windSpeedController;

  private Consumer<ActuatorReading> fanSink;

  /**
  * Creates a new ControlPanelController for the given view.
   *
  * @param view the ControlPanelView instance to control
  * @param cmdHandler handler for sending actuator commands
  * @param stateStore state store for subscribing to backend updates
  * @param nodeId the node ID this panel controls
  */
  public ControlPanelController(ControlPanelView view, CommandInputHandler cmdHandler,
                                StateStore stateStore, String nodeId) {
    this.view = view;
    this.cmdHandler = Objects.requireNonNull(cmdHandler, "cmdHandler");
    this.stateStore = Objects.requireNonNull(stateStore, "stateStore");
    this.nodeId = Objects.requireNonNull(nodeId, "nodeId");

    log.debug("ControlPanelController created for view: {} nodeId: {}", view, nodeId);
    initializeControllers();
  }

  /**
  * Initializes all subsystem card controllers using the builders
  * provided by the view. This method ensures that each UI card has
  * an active controller linked to its visual component.
  */
  private void initializeControllers() {
    fanController = getControllerFromCard(
            view.getFanCard(), FanCardController.class);
    fertilizerController = getControllerFromCard(
            view.getFertilizerCard(), FertilizerCardController.class);
    heaterController = getControllerFromCard(
            view.getHeaterCard(), HeaterCardController.class);
    humidityController = getControllerFromCard(
            view.getHumidityCard(), HumidityCardController.class);
    lightController = getControllerFromCard(
            view.getLightCard(), LightCardController.class);
    pHController = getControllerFromCard(
            view.getPHCard(), PHCardController.class);
    temperatureController = getControllerFromCard(
            view.getTemperatureCard(), TemperatureCardController.class);
    valveController = getControllerFromCard(
            view.getValveCard(), ValveCardController.class);
    windowsController = getControllerFromCard(
            view.getWindowsCard(), WindowsCardController.class);
    windSpeedController = getControllerFromCard(
            view.getWindSpeedCard(), WindSpeedCardController.class);

    log.debug("All card controllers initialized successfully");
  }

  /**
  * Safely extracts the controller associated with a given card, if available.
  *
  * @param card the ControlCard instance
  * @param expectedType the expected controller class type
  * @param <T> the controller type
  * @return the controller instance, or null if not found/wrong type
  */
  private <T> T getControllerFromCard(Object card, Class<T> expectedType) {
    if (card == null) {
      log.warn("Card is null, cannot extract controller for type: "
              + "{}", expectedType.getSimpleName());
      return null;
    }

    Object userData = null;
    try {
      var method = card.getClass().getMethod("getUserData");
      userData = method.invoke(card);
    } catch (Exception e) {
      log.error("Failed to get userData from card: {}", card.getClass().getSimpleName(), e);
      return null;
    }

    if (expectedType.isInstance(userData)) {
      log.debug("Controller {} successfully extracted from card", expectedType.getSimpleName());
      return expectedType.cast(userData);
    } else {
      log.warn("userData is not of expected type {}. Found: {}",
              expectedType.getSimpleName(),
              userData != null ? userData.getClass().getSimpleName() : "null");
      return null;
    }
  }

  /**
  * Starts all card subsystem controllers managed by this control panel.
  */
  public void start() {
    log.info("Starting ControlPanelController and all card controllers");

    injectDependencies();

    fanSink = ar -> {
      if (!nodeId.equals(ar.nodeId())) {
        return;
      }
      if (!"fan".equalsIgnoreCase(ar.type())) {
        return;
      }
      try {
        int speed = Integer.parseInt(ar.state());
        if (fanController != null) {
          fanController.updateFanSpeed(speed);
        }
      } catch (NumberFormatException e) {
        log.warn("Invalid fan state '{}' for nodeId={}", ar.state(), ar.nodeId());
      }
    };
    stateStore.addActuatorSink(fanSink);

    safeStart(fanController, "FanCardController");
    safeStart(fertilizerController, "FertilizerCardController");
    safeStart(heaterController, "HeaterCardController");
    safeStart(humidityController, "HumidityCardController");
    safeStart(lightController, "LightCardController");
    safeStart(pHController, "PHCardController");
    safeStart(temperatureController, "TemperatureCardController");
    safeStart(valveController, "ValveCardController");
    safeStart(windowsController, "WindowsCardController");
    safeStart(windSpeedController, "WindSpeedCardController");

    log.info("ControlPanelController started successfully");
  }

  /**
  * Safely invokes the start() method on a controller, if it exists.
  *
  * @param controller the controller instance to start
  * @param name the descriptive name of the controller for logging
  */
  private void safeStart(Object controller, String name) {
    if (controller != null) {
      try {
        controller.getClass().getMethod("start").invoke(controller);
      } catch (Exception e) {
        log.error("Failed to start {}", name, e);
      }
    } else {
      log.warn("{} is null, skipping start", name);
    }
  }

  /**
   * Injects dependencies (cmdHandler, nodeId) into all card controllers
   * that support setDependencies().
   */
  private void injectDependencies() {
    log.debug("Injecting dependencies into card controllers (nodeId={})", nodeId);

    safeInject(fanController, "FanCardController");
    safeInject(fertilizerController, "FertilizerCardController");
    safeInject(heaterController, "HeaterCardController");
    safeInject(humidityController, "HumidityCardController");
    safeInject(lightController, "LightCardController");
    safeInject(pHController, "PHCardController");
    safeInject(temperatureController, "TemperatureCardController");
    safeInject(valveController, "ValveCardController");
    safeInject(windowsController, "WindowsCardController");
    safeInject(windSpeedController, "WindSpeedCardController");

    log.debug("Dependency injection completed");
  }

  /**
   * Safely invokes setDependencies(cmdHandler, nodeId) on a controller if it exists.
   *
   * @param controller the controller instance
   * @param name the descriptive name for logging
   */
  private void safeInject(Object controller, String name) {
    if (controller != null) {
      try {
        controller.getClass()
                .getMethod("setDependencies", CommandInputHandler.class, String.class)
                .invoke(controller, cmdHandler, nodeId);
        log.debug("{} dependencies injected", name);
      } catch (NoSuchMethodException e) {

        log.trace("{} does not have setDependencies method (read-only)", name);
      } catch (Exception e) {
        log.error("Failed to inject dependencies into {}", name, e);
      }
    } else {
      log.warn("{} is null, skipping dependency injection", name);
    }
  }

  /**
  * Stops all subsystem controllers and release associated resources.
  * Should be called when the control panel is being closed or refreshed.
  */
  public void stop() {
    log.info("Stopping ControlPanelController and all card controllers");

    if (fanSink != null) {
      stateStore.removeActuatorSink(fanSink);
      fanSink = null;
    }

    safeStop(fanController, "FanCardController");
    safeStop(fertilizerController, "FertilizerCardController");
    safeStop(heaterController, "HeaterCardController");
    safeStop(humidityController, "HumidityCardController");
    safeStop(lightController, "LightCardController");
    safeStop(pHController, "PHCardController");
    safeStop(temperatureController, "TemperatureCardController");
    safeStop(valveController, "ValveCardController");
    safeStop(windowsController, "WindowsCardController");
    safeStop(windSpeedController, "WindSpeedCardController");

    log.info("ControlPanelController stopped successfully");
  }

  /**
  * Safely invokes the method on a controller, if it exists.
  */
  private void safeStop(Object controller, String name) {
    if (controller != null) {
      try {
        controller.getClass().getMethod("stop").invoke(controller);
      } catch (Exception e) {
        log.error("Failed to stop {}", name, e);
      }
    } else {
      log.warn("{} is null, skipping stop", name);
    }
  }

  /**
  * Returns the instance managed by this controller.

  * @return the wind speed controller
  */
  public WindSpeedCardController getWindSpeedController() {
    return windSpeedController;
  }

  /**
   * Returns the instance managed by this controller.

   * @return the windows controller
   */
  public WindowsCardController getWindowsController() {
    return windowsController;
  }

  /**
   * Returns the instance managed by this controller.

   * @return the valve controller
   */
  public ValveCardController getValveController() {
    return valveController;
  }

  /**
   * Returns the instance managed by this controller.

   * @return the temperature controller
   */
  public TemperatureCardController getTemperatureController() {
    return temperatureController;
  }

  /**
   * Returns the instance managed by this controller.

   * @return the pH controller
   */
  public PHCardController getPHController() {
    return pHController;
  }

  /**
   * Returns the instance managed by this controller.

   * @return the light controller
   */
  public LightCardController getLightController() {
    return lightController;
  }

  /**
   * Returns the instance managed by this controller.

   * @return the humidity controller
   */
  public HumidityCardController getHumidityController() {
    return humidityController;
  }

  /**
   * Returns the instance managed by this controller.

   * @return the heater controller
   */
  public HeaterCardController getHeaterController() {
    return heaterController;
  }

  /**
   * Returns the instance managed by this controller.

   * @return the fertilizer controller
   */
  public FertilizerCardController getFertilizerController() {
    return fertilizerController;
  }

  /**
   * Returns the instance managed by this controller.

   * @return the fan controller
   */
  public FanCardController getFanController() {
    return fanController;
  }



}

