package edu.ntnu.bidata.smg.group8.control.ui.controller;

import edu.ntnu.bidata.smg.group8.common.util.AppLogger;
import java.util.Map;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;
import org.slf4j.Logger;

/**
 * Manages scene navigation between different views in the application.
 *
 * <p>The SceneManager allows registering multiple views identified by unique names
 * and provides functionality to switch between these views seamlessly.
 * It maintains a stack of views and ensures that only the currently active view is visible
 * at any given time.</p>
 *
 * <p>AI has helped in the design and implementation of this class to evaluate relevant
 * approaches and best practices (especially WHY we need this class
 * and WHAT its responsibility is). The code has been reviewed and considered
 * before use, which helps with understanding and learning.</p>
 *
 * @author Mona Amundsen
 * @version 12.11.2025
 */
public class SceneManager {
  private static final Logger log = AppLogger.get(SceneManager.class);

  private final StackPane container;
  private final Map<String, Node> views;
  private String currentViewName;

  /**
   * Constructor for SceneManager.
   *
   * <p>Initializes the SceneManager with an empty container and view registry.</p>
   *
   * <p>The container is a StackPane that holds all registered views,
   * allowing for easy switching between them.</p>
   */
  public SceneManager() {
    this.container = new StackPane();
    this.views = new java.util.HashMap<>();
    this.currentViewName = null;
    log.info("SceneManager initialized.");
  }

  /**
   * Registers a view with a given name.
   *
   * <p>The view is added to the container and made visible.
   * If a view with the same name already exists, an exception is thrown.</p>
   *
   * @param name the name of the view
   * @param view the Node representing the view
   */
  public void registerView(String name, Node view) {
    if (views.containsKey(name)) {
      throw new IllegalArgumentException("View with name " + name + " is already registered.");
    }
    views.put(name, view);
    container.getChildren().add(view);
    view.setVisible(true);
    log.info("View registered: '{}'", name);
  }

  /**
   * Shows the view associated with the given name.
   *
   * <p>Hides all other views and brings the specified view to the front.
   * Logs the navigation event.</p>
   *
   * @param name the name of the view to show
   * @throws IllegalArgumentException if no view is registered with the given name
   */
  public void showView(String name) {
    Node view = views.get(name);
    if (view == null) {
      throw new IllegalArgumentException("No view registered with name: " + name);
    }
    String previousView = currentViewName;

    for (Node v : views.values()) {
      v.setVisible(false);
    }
    view.setVisible(true);
    view.toFront();
    currentViewName = name;

    if (previousView != null) {
      log.info("Navigation: '{}' -> '{}'", previousView, name);
    } else {
      log.info("Initial view: '{}'", name);
    }
  }

  /**
   * Gets the container StackPane.
   *
   * @return the container StackPane
   */
  public StackPane getContainer() {
    return container;
  }
}
