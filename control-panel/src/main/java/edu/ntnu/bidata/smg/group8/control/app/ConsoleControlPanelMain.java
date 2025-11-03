package edu.ntnu.bidata.smg.group8.control.app;

import edu.ntnu.bidata.smg.group8.common.util.AppLogger;
import edu.ntnu.bidata.smg.group8.control.console.ConsoleInputLoop;
import edu.ntnu.bidata.smg.group8.control.console.DisplayManager;
import edu.ntnu.bidata.smg.group8.control.console.MockFeeder;
import edu.ntnu.bidata.smg.group8.control.infra.network.PanelAgent;
import edu.ntnu.bidata.smg.group8.control.logic.command.CommandInputHandler;
import edu.ntnu.bidata.smg.group8.control.logic.state.StateStore;
import org.slf4j.Logger;


/**
* Entry point for the Console-based version of the Control Panel.
* This class provides a simple, text-only interface for interacting with
* connected nodes through the PanelAgent network component. It manages
* user input, system display, and communication with a remote message broker.
*
* @author Andrea Sandnes
* @version 02.11.2025
*/
public class ConsoleControlPanelMain {
  private static final Logger log = AppLogger.get(ConsoleControlPanelMain.class);

  private PanelAgent agent;
  private DisplayManager display;
  private ConsoleInputLoop inputLoop;
  private Thread inputThread;
  private Thread mockThread;


  /**
  * Launches the console control panel.
  *
  * @param args command line arguments
  */
  public static void main(String[] args) {
    new ConsoleControlPanelMain().run();
  }

  /**
  * Initializes and starts the main components of the console control panel.
  * This method sets up network communication, the display system, and the
  * console input loop. It also registers a JVM shutdown hook to ensure that
  * all components are stopped cleanly when the application exits.
  */
  private void run() {
    String host = System.getProperty("broker.host", "localhost");
    int port = Integer.parseInt(System.getProperty("broker.port", "23048"));
    String panelId = System.getProperty("panel.id", "panel-1");
    String nodeId = System.getProperty("node.id", "node-1");

    String mockFile = System.getProperty("mock.file");
    long mockDelay = Long.parseLong(System.getProperty("mock.interval.ms", "500"));
    boolean mockOnly = Boolean.parseBoolean(System.getProperty("mock.only", "false"));

    log.info(
            "Starting Console Control Panel (host={} port={} "
                    + "panelId={} nodeId={} mock.file={} mock.only={})",
            host, port, panelId, nodeId, mockFile, mockOnly);

    StateStore stateStore = new StateStore();

    // Optional: start mock feeder if a file is provided
    if (mockFile != null && !mockFile.isBlank()) {
      MockFeeder feeder = new MockFeeder(stateStore, mockFile, mockDelay);
      mockThread = new Thread(feeder, "mock-feeder");
      mockThread.setDaemon(true);
      mockThread.start();
    }

    // Start network agent unless mock-only
    if (!mockOnly) {
      try {
        agent = new PanelAgent(host, port, panelId, stateStore);
        agent.start();
        log.info("PanelAgent connected to broker at {}:{}", host, port);
      } catch (Exception e) {
        log.error("Failed to start PanelAgent ({}:{})", host, port, e);
        safeShutdown();
        return;
      }
    } else {
      log.info("Mock-only mode: skipping broker connection");
    }

    // Start display (always)
    display = new DisplayManager(stateStore);
    display.setClearScreen(false);
    display.start();

    // Start input loop only when agent is available (i.e., not mock-only)
    if (agent != null) {
      CommandInputHandler cmdHandler = new CommandInputHandler(agent);
      inputLoop = new ConsoleInputLoop(cmdHandler, nodeId, display, stateStore);
      inputThread = new Thread(inputLoop, "console-input");
      inputThread.setDaemon(false);
      inputThread.start();
    } else {
      log.info("Mock-only mode: ConsoleInputLoop not started (no broker)");
    }

    Runtime.getRuntime().addShutdownHook(new Thread(this::safeShutdown, "console-shutdown"));

    try {
      if (inputThread != null) {
        inputThread.join();
      } else if (mockThread != null) {
        mockThread.join();
      } else {
        Thread.sleep(Long.MAX_VALUE);
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }

    safeShutdown();
    log.info("Console Control Panel stopped");
  }

  /**
  * Gracefully shuts down all console-related components.
  * This method ensures that the input loop, display manager, and
  * network agent are stopped and cleaned up properly, even if
  * error occurs during shutdown.
  */
  private void safeShutdown() {
    log.info("Shutting down console components");

    // Stop input loop
    if (inputLoop != null) {
      try {
        inputLoop.stop();
      } catch (Exception e) {
        log.warn("Failed to stop ConsoleInputLoop cleanly", e);
      } finally {
        inputLoop = null;
      }
    }

    // Wait a short moment for the input thread to finish
    if (inputThread != null) {
      try {
        if (inputThread.isAlive()) {
          inputThread.join(500);
        }
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        log.warn("Interrupted while waiting for console-input thread to finish", e);
      } finally {
        inputThread = null;
      }
    }

    // Stop display
    if (display != null) {
      try {
        display.stop();
      } catch (Exception e) {
        log.warn("Failed to stop DisplayManager cleanly", e);
      } finally {
        display = null;
      }
    }

    // Close network agent
    if (agent != null) {
      try {
        agent.close();
      } catch (Exception e) {
        log.warn("Failed to close PanelAgent cleanly", e);
      } finally {
        agent = null;
      }
    }

    if (mockThread != null) {
      try {
        mockThread.join(200);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      } finally {
        mockThread = null;
      }
    }

    log.info("Shutdown sequence completed");
  }
}
