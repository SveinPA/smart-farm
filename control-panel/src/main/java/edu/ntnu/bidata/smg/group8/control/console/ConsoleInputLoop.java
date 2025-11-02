package edu.ntnu.bidata.smg.group8.control.console;

import edu.ntnu.bidata.smg.group8.common.util.AppLogger;
import edu.ntnu.bidata.smg.group8.control.logic.command.CommandInputHandler;
import edu.ntnu.bidata.smg.group8.control.logic.state.StateStore;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.Instant;
import org.slf4j.Logger;


/**
* Handles user input from the console in a continuous loop.
*
* <p>This class listens form commands typed in the console and forwards
* them to CommandInputHandler for execution. It is typically used to
* send control commands (e.g "heater 22") to a specific node in a
* distributed system or smart grid.</p>
*
* @author Andrea Sandnes
* @version 01.11.2025
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
  private final String nodeId;


  private volatile boolean running = true;
  private volatile Mode mode = Mode.INPUT;

  /**
  * Creates a new console input loop instance.
  *
  * @param cmdHandler the handler responsible for executing parsed commands.
  * @param nodeId the identifier of the node to which commands should be sent
  */
  public ConsoleInputLoop(CommandInputHandler cmdHandler, String nodeId,
                          DisplayManager display, StateStore stateStore) {
    this.cmdHandler = cmdHandler;
    this.nodeId = nodeId;
    this.display = display;
    this.stateStore = stateStore;
  }

  /**
  * Starts the console input loop.
  * The loop reads user input, processes commands, and forwards
  * valid actuator-value pairs to the CommandInputHandler. The loop
  * continues until the user types "quit" or "exit", or until
  * stop() is called.
  * Invalid or malformed input will trigger a console message with
  * usage help, but will not stop the loop.
  */
  @Override
  public void run() {
    log.info("Console input loop started. Type 'help' for commands,"
            + " 'view' to watch live, Enter to return.");
    try (BufferedReader br = new BufferedReader(new InputStreamReader(System.in))) {
      while (running) {
        if (mode == Mode.INPUT) {
          if (display != null) {
            display.pause();
            try {
              Thread.sleep(120);
            } catch (InterruptedException ie) {
              Thread.currentThread().interrupt();
            }
          }
          System.out.print("> ");
          System.out.flush();

          String line = br.readLine();
          if (line == null) {
            break;
          }
          line = line.trim();
          if (line.isEmpty()) {
            continue;
          }
          if (line.equalsIgnoreCase("quit") || line.equalsIgnoreCase("exit")) {
            log.info("Exiting console input loop...");
            break;
          }
          if (line.equalsIgnoreCase("help")) {
            printHelp();
            continue;
          }
          if (line.equalsIgnoreCase("view") || line.equalsIgnoreCase("display on")) {
            mode = Mode.VIEW;
            if (display != null) {
              display.resume();
            }
            System.out.println("→ VIEW mode. Press Enter to return to INPUT...");
            System.out.flush();
            continue;
          }
          if (line.equalsIgnoreCase("input") || line.equalsIgnoreCase("display off")) {
            mode = Mode.INPUT;
            if (display != null) {
              display.pause();
            }
            System.out.println("→ INPUT mode.");
            continue;
          }

          String[] parts = line.split("\\s+");
          if (parts.length != 2) {
            System.out.println("Use: <actuator> <int value>   e.g., heater 22");
            continue;
          }
          String actuator = parts[0];
          String valueStr = parts[1];

          try {
            int value = Integer.parseInt(valueStr);
            cmdHandler.setValue(nodeId, actuator, value);
            log.info("Command sent: {} {} (nodeId={})", actuator, value, nodeId);

            boolean optimistic = Boolean.parseBoolean(System.getProperty("panel.optimistic",
                    "true"));
            if (stateStore != null && optimistic) {
              stateStore.applyActuator(nodeId, actuator, Integer.toString(value), Instant.now());
            }
          } catch (NumberFormatException nfe) {
            System.out.println("Value must be integer: " + valueStr);
          } catch (IOException ioe) {
            log.error("Failed to send command: {} {}", actuator, valueStr, ioe);
          }
        } else {
          if (display != null) {
            display.resume();
          }
          System.out.print("\n[Viewing live data] Press Enter to return to INPUT... ");
          System.out.flush();

          String ignored = br.readLine();
          if (!running) {
            break;
          }

          mode = Mode.INPUT;
          if (display != null) {
            display.pause();
          }
          System.out.println("\n→ INPUT mode.");
        }
      }
    } catch (IOException e) {
      log.error("Console input error", e);
    } finally {
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
    System.out.println();
    System.out.println("Mode control:");
    System.out.println(" view            - switch to VIEW mode (live updating)");
    System.out.println(" input           - switch to iNPUT mode (typing)");
    System.out.println(" display on      - alias for 'view'");
    System.out.println(" display off     - alias for 'input'");
    System.out.println(" help | quit");
  }
}
