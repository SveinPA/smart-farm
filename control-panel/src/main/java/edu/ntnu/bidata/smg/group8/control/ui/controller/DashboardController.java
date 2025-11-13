package edu.ntnu.bidata.smg.group8.control.ui.controller;

import edu.ntnu.bidata.smg.group8.common.util.AppLogger;
import edu.ntnu.bidata.smg.group8.control.ui.view.DashboardView;
import org.slf4j.Logger;

/**
 * Controller for the dashboard view.
 *
 * <p>Manages Dashboard-specific logic and user interactions.</p>
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

  /**
   * Constructor for DashboardController.
   *
   * @param view the DashboardView instance
   * @param sceneManager the SceneManager which handles view navigation
   */
  public DashboardController(DashboardView view, SceneManager sceneManager) {
    this.view = view;
    this.sceneManager = sceneManager;
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
