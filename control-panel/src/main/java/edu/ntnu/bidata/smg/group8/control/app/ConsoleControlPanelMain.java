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
* <h3>Console Control Panel Main - Entry Point for Text-Based Control Interface</h3>
*
* <p>This class is the <b>orchestrator</b> that ties together all components of the
* console-based control panel: network communication, state management, user input,
* and system display. </p>
*
* <p><b>Features:</b></p>
* <ol>
*     <li><b>Network communication:</b> Connects to message broker via PanelAgent</li>
*     <li><b>Real-time display:</b> Shows current state of connected nodes</li>
*     <li><b>Command interface:</b> Accepts user commands for controlling actuators</li>
*     <li><b>Mock mode:</b> Supports offline testing with simulated sensor data</li>
* </ol>
*
* <p><b>Usage Examples:</b></p>
* <pre>
*     # Default configuration (connects to localhost:23058)
*     mvn exec:java -pl control-panel
*     -> Panel ID: panel-1, Node ID: node-1, Broker: localhost:23048
*
*     # Custom broker connection
*     mvn exec:java -pl control-panel "-Dbroker.host=localhost" "-Dbroker.port=23048"
*     -> Connects to remote broker
*
*     # Mock mode for offline testing
*     mvn exec:java -pl control-panel "-Dmock.file=test-data.json -Dmock.only=true"
*     -> Runs without broker connection, uses simulated data
* </pre>
*
* <p><b>Important Notes:</b></p>
* <ul>
*     <li>Broker must be running <b>before</b> starting the panel (unless mock-only mode)</li>
*     <li>Display updates automatically when new sensor data arrives</li>
*     <li>Commands are sent immediately to connected nodes via the broker</li>
*     <li>Graceful shutdown ensures all connections are properly closed</li>
* </ul>
*
* @author Andrea Sandnes
* @version 1.0
* @see PanelAgent
* @see StateStore
* @see DisplayManager
* @see ConsoleInputLoop
*/
public class ConsoleControlPanelMain {
  private static final Logger log = AppLogger.get(ConsoleControlPanelMain.class);

  // -------------------------- Component references ------------------------------//

  // Network agent for broker communication
  private PanelAgent agent;

  // Display manager for rendering system state
  private DisplayManager display;

  // Console input handler for user commands
  private ConsoleInputLoop inputLoop;

  // Thread running the input loop
  private Thread inputThread;

  // Thread running the mock data feeder
  private Thread mockThread;

  /**
  * Main entry point for the console control panel application.
  *
  * <p>Creates a new instance and delegates to {@link #run()} for initialization
  * and execution.</p>
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
    int port;

    try {
      port = Integer.parseInt(System.getProperty("broker.port", "23048"));
    } catch (NumberFormatException e) {
      log.warn("Invalid broker.port value: '{}'. Falling back to default 23048.",
              System.getProperty("broker.port"));
      port = 23048;
    }

    String panelId = System.getProperty("panel.id", "panel-1");
    String nodeId = System.getProperty("node.id", "node-1");

    String mockFile = System.getProperty("mock.file");

    long mockDelay;
    try {
      mockDelay = Long.parseLong(System.getProperty("mock.intervals.ms", "500"));
    } catch (NumberFormatException e) {
      log.error("Invalid value for mock.intervals.ms: '{}', using default 500ms",
              System.getProperty("mock.intervals.ms"));
      mockDelay = 500L;
    }

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

    // Start display
    display = new DisplayManager(stateStore);
    display.setClearScreen(false);
    display.pause();

    // Start input loop only when agent is available (i.e., not mock-only)
    if (agent != null) {
      CommandInputHandler cmdHandler = new CommandInputHandler(agent);
      inputLoop = new ConsoleInputLoop(cmdHandler, null, display, stateStore);
      inputThread = new Thread(inputLoop, "console-input");
      inputThread.setDaemon(false);
      inputThread.start();

    try {
      Thread.sleep(100);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }
  display.start();

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
        mockThread.interrupt();
        mockThread.join(1000);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      } finally {
        mockThread = null;
      }
    }

    log.info("Shutdown sequence completed");
  }
}
