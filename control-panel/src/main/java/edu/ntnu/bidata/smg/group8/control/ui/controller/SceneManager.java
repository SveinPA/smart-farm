package edu.ntnu.bidata.smg.group8.control.ui.controller;

import edu.ntnu.bidata.smg.group8.common.util.AppLogger;
import java.util.Map;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;
import org.slf4j.Logger;

/**
 * Manages scene navigation between different views in the application.
 *
 * <p>AI has helped in the design and implementation of this class to evaluate relevant
 * approaches and best practices. The code has been reviewed and considered before use,
 * which helps with understanding and learning.</p>
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
   * @param name the name of the view to show
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

  /**
   * Gets the name of the currently displayed view.
   *
   * @return the current view name
   */
  public String getCurrentViewName() {
    return currentViewName;
  }
}
