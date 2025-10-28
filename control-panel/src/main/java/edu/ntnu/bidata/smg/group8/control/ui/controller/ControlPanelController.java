package edu.ntnu.bidata.smg.group8.control.ui.controller;

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

/**
* Main UI Controller for the control panel.
* This controller coordinates all card specific controllers and manages the
* interaction between ControlPanelView and the underlying card builders.

* @author Andrea Sandnes
* @version 28.10.2025
*/
public class ControlPanelController {
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
  *
  * @param view
  */
  public ControlPanelController(ControlPanelView view) {
    this.view = view;
    initializeControllers();
  }

  /**
  *
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
  }

  /**
  *
  */
  public void start() {
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

  }

  /**
  *
  */
  public void stop() {
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
  }
}
