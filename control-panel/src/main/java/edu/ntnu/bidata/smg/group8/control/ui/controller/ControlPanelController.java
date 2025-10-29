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
* Main UI Controller for the control panel.
* This controller coordinates all card specific controllers and manages the
* interaction between ControlPanelView and the underlying card builders.

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
  private WindowsCardController windowController;
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
  * Initializes all card-specific controllers.
  */
  private void initializeControllers() {
    fanController = new FanCardController(view.getFanBuilder());
    fertilizerController = new FertilizerCardController(view.getFertilizerBuilder());
    heaterController = new HeaterCardController(view.getHeaterBuilder());
    humidityController = new HumidityCardController(view.getHumidityBuilder());
    lightController = new LightCardController(view.getLightsBuilder());
    pHController = new PHCardController(view.getPhBuilder());
    temperatureController = new TemperatureCardController(view.getTemperatureBuilder());
    valveController = new ValveCardController(view.getValveBuilder());
    windowController = new WindowsCardController(view.getWindowsBuilder());
    windSpeedController = new WindSpeedCardController(view.getWindSpeedBuilder());

    log.debug("All card controllers initialized successfully");
  }

  /**
  * Starts all card controllers.
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
    valveController.start();
    windowController.start();
    windSpeedController.start();

    log.info("ControlPanelController started successfully");
  }

  /**
  * Stops all card controllers and cleans up resources.
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
    valveController.stop();
    windowController.stop();
    windSpeedController.stop();

    log.info("ControlPanelController stopped successfully");
  }
}
