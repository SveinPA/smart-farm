package edu.ntnu.bidata.smg.group8.control.console;

import edu.ntnu.bidata.smg.group8.common.util.AppLogger;
import edu.ntnu.bidata.smg.group8.control.logic.command.CommandInputHandler;
import edu.ntnu.bidata.smg.group8.control.logic.state.StateStore;
import edu.ntnu.bidata.smg.group8.control.ui.controller.ControlPanelController;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.Instant;
import org.slf4j.Logger;

/**
* Handles user input from the console in a continuous loop.
*
* <p>This class listens for commands typed in the console and forwards
* them to CommandInputHandler for execution. It is typically used to
* send control commands (e.g "heater 22") to a specific node in a
* distributed system or smart grid.</p>
*
* @author Andrea Sandnes & Ida Soldal
* @version 1.0
* @since 01.11.2025
* @see CommandInputHandler
* @see DisplayManager
* @see ControlPanelController
* @see StateStore
*/
public class ConsoleInputLoop implements Runnable {
  private static final Logger log = AppLogger.get(ConsoleInputLoop.class);

  private enum Mode {
    INPUT,
    VIEW
  }

  private final CommandInputHandler cmdHandler;
  private final DisplayManager display;
  private final StateStore stateStore;
  private final ControlPanelController controller;


  private volatile boolean running = true;
  private volatile Mode mode = Mode.INPUT;

  /**
  * Creates a new console input loop instance.
  *
  * @param cmdHandler the handler responsible for executing parsed commands
  * @param controller the controller for accessing selected node
  * @param display the display manager
  * @param stateStore the state store
  */
  public ConsoleInputLoop(CommandInputHandler cmdHandler, ControlPanelController controller,
                          DisplayManager display, StateStore stateStore) {
    this.cmdHandler = cmdHandler;
    this.controller = controller;
    this.display = display;
    this.stateStore = stateStore;
  }

  /**
  * Main loop that reads and processes console input.
  *
  * <p>Continuously reads lines, parses them as commands,
  * and forwards valid commands to the target node. The
  * loop runs until the user types "quit"/"exit" or
  * {@link #stop()} is called</p>
  */
  @Override
  public void run() {

    // pause live display so it doesn't interfere with user input
    if (display != null) {
      display.pause();
    }

    System.out.println();
    System.out.println("--------------------------------------------------------------");
    System.out.println("Smart Greenhouse Console Control Panel");
    System.out.println("--------------------------------------------------------------");
    System.out.println("Type 'help' for commands, 'view' to see current state");
    String nodeId = controller != null ? controller.getSelectedNodeId() : "node-1";
    System.out.println("Target node: " + nodeId);
    System.out.println();

    log.info("Console input loop started.");

    try (BufferedReader br = new BufferedReader(new InputStreamReader(System.in))) {

    while (running) {

        if (mode == Mode.INPUT) {

            System.out.print("> ");
            System.out.flush();

            String line = br.readLine();
            if (line == null) break;
            line = line.trim();
            if (line.isEmpty()) continue;

            // ---------- INTERNAL COMMANDS ----------
            if (line.equalsIgnoreCase("quit") || line.equalsIgnoreCase("exit")) {
                log.info("Exiting console input loop...");
                break;
            }
            if (line.equalsIgnoreCase("help")) {
                printHelp();
                continue;
            }
            if (line.equalsIgnoreCase("status")) {
                if (display != null) display.showOnce();
                continue;
            }
            if (line.equalsIgnoreCase("view") || line.equalsIgnoreCase("display on")) {
                mode = Mode.VIEW;
                if (display != null) display.resume();
                System.out.println("→ VIEW mode. Press Enter to return to INPUT...");
                continue;
            }
            if (line.equalsIgnoreCase("input") || line.equalsIgnoreCase("display off")) {
                mode = Mode.INPUT;
                if (display != null) display.pause();
                System.out.println("→ INPUT mode.");
                continue;
            }

            // ---------- BROADCAST SUPPORT ----------
            String[] parts = line.split("\\s+");
            if (parts.length == 3 && parts[0].equalsIgnoreCase("all")) {

                String actuator = parts[1];
                String valueStr = parts[2];

                try {
                    int value = Integer.parseInt(valueStr);
                    cmdHandler.setValue("ALL", actuator, value);
                    log.info("BROADCAST command sent: {} {} → ALL nodes", actuator, value);
                    System.out.println("Broadcast sent to ALL nodes.");
                } catch (NumberFormatException nfe) {
                    System.out.println("Value must be integer: " + valueStr);
                } catch (IOException ioe) {
                    log.error("Failed to send broadcast command", ioe);
                    System.out.println("Failed to send broadcast: " + ioe.getMessage());
                }
                continue;
            }

            // ---------- NORMAL 2-ARG COMMAND ----------
            if (parts.length != 2) {
                System.out.println("Use:");
                System.out.println("  <actuator> <value>     e.g., fan 60");
                System.out.println("  all <actuator> <value> e.g., all fan 60");
                continue;
            }

            String actuator = parts[0];
            String valueStr = parts[1];

            try {
                int value = Integer.parseInt(valueStr);

                // Normal mode → requires selected node
                String targetNodeId = controller != null ? controller.getSelectedNodeId() : null;

                if (targetNodeId == null) {
                    System.out.println("ERROR: No node selected in GUI.");
                    continue;
                }

                cmdHandler.setValue(targetNodeId, actuator, value);
                log.info("Command sent: {} {} → node {}", actuator, value, targetNodeId);

                // Optimistic update
                if (stateStore != null &&
                    Boolean.parseBoolean(System.getProperty("panel.optimistic", "true"))) {

                    stateStore.applyActuator(targetNodeId, actuator,
                            Integer.toString(value), Instant.now());
                }

            } catch (NumberFormatException nfe) {
                System.out.println("Value must be integer: " + valueStr);
            } catch (IOException ioe) {
                log.error("Failed to send command", ioe);
                System.out.println("Failed to send command: " + ioe.getMessage());
            }

        } else {
            // ---------- VIEW MODE ----------
            if (display != null) display.resume();
            System.out.print("\n[Viewing live data] Press Enter to return to INPUT... ");
            System.out.flush();

            br.readLine(); // Wait for Enter
            if (!running) break;

            mode = Mode.INPUT;
            if (display != null) display.pause();
            System.out.println("\n→ INPUT mode.");
        }
    }
    } catch (IOException e) {
      log.error("Console input error", e);
    } finally {
      // Make sure display resumes when we exit
      if (display != null) {
        display.resume();
      }
      log.info("Console input loop stopped.");
    }
  }

  /**
  * Stops the console input loop gracefully.
  * Once called, the loop will finish its current iteration and exit.
  */
  public void stop() {
    running = false;
  }

  /**
  * Prints a list of available console commands and their usage examples.
  * This method is called when the user types "help".
  */
  private static void printHelp() {
    System.out.println("Commands:");
    System.out.println(" heater <temp>              e.g., heater 22");
    System.out.println(" fan <percent>              e.g., fan 60");
    System.out.println(" valve <0|1>                e.g., valve 1");
    System.out.println(" window <percent>           e.g., window 50");
    System.out.println(" artificial_light <0-100>   e.g., artificial_light 75");
    System.out.println(" all <actuator> <value>     e.g., all fan 100   (broadcast to ALL nodes)");
    System.out.println();
    System.out.println("Mode control:");
    System.out.println(" view            - switch to VIEW mode (live updating)");
    System.out.println(" input           - switch to INPUT mode (typing)");
    System.out.println(" display on      - alias for 'view'");
    System.out.println(" display off     - alias for 'input'");
    System.out.println(" help | quit");
  }
}
