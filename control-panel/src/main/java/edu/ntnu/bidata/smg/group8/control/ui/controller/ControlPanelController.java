package edu.ntnu.bidata.smg.group8.control.ui.controller;

import edu.ntnu.bidata.smg.group8.common.util.AppLogger;
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


  /**
  * Creates a new ControlPanelController for the given view.

  * @param view the ControlPanelView instance to control
  */
  public ControlPanelController(ControlPanelView view) {
    this.view = view;
    log.debug("ControlPanelController created for view: {}", view);
    initializeControllers();
  }

  /**
  * Initializes all subsystem card controllers using the builders
  * provided by the view. This method ensures that each UI card has
  * an active controller linked to its visual component.
  */
  private void initializeControllers() {
    fanController = new FanCardController(
            view.getFanBuilder());
    fertilizerController = new FertilizerCardController(
            view.getFertilizerBuilder());
    heaterController = new HeaterCardController(
            view.getHeaterBuilder());
    humidityController = new HumidityCardController(
            view.getHumidityBuilder());
    lightController = new LightCardController(
            view.getLightsBuilder());
    pHController = new PHCardController(
            view.getPhBuilder());
    temperatureController = new TemperatureCardController(
            view.getTemperatureBuilder());
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

    fanController.start();
    fertilizerController.start();
    heaterController.start();
    humidityController.start();
    lightController.start();
    pHController.start();
    temperatureController.start();
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
  * Stops all subsystem controllers and release associated resources.
  * Should be called when the control panel is being closed or refreshed.
  */
  public void stop() {
    log.info("Stopping ControlPanelController and all card controllers");

    fanController.stop();
    fertilizerController.stop();
    heaterController.stop();
    humidityController.stop();
    lightController.stop();
    pHController.stop();
    temperatureController.stop();
    safeStop(valveController, "ValveCardController");
    safeStart(windowsController, "WindowsCardController");
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

   * @return the wind speed controller
   */
  public WindowsCardController getWindowsController() {
    return windowsController;
  }

  /**
   * Returns the instance managed by this controller.

   * @return the wind speed controller
   */
  public ValveCardController getValveController() {
    return valveController;
  }
}

