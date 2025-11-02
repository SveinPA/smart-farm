package edu.ntnu.bidata.smg.group8.control.console;

import edu.ntnu.bidata.smg.group8.common.util.AppLogger;
import edu.ntnu.bidata.smg.group8.control.logic.command.CommandInputHandler;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
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

  private final CommandInputHandler cmdHandler;
  private final String nodeId;
  private volatile boolean running = true;

  /**
  * Creates a new console input loop instance.
  *
  * @param cmdHandler the handler responsible for executing parsed commands.
  * @param nodeId the identifier of the node to which commands should be sent
  */
  public ConsoleInputLoop(CommandInputHandler cmdHandler, String nodeId) {
    this.cmdHandler = cmdHandler;
    this.nodeId = nodeId;
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
  public void run() {
    log.info("Console input loop started. Typed 'help' or 'quit'.");
    try (BufferedReader br = new BufferedReader(new InputStreamReader(System.in))) {
      String line;
      while (running && (line = br.readLine()) != null) {
        line = line.trim();
        if (line.isEmpty()) {
          continue;
        }
        if (line.equalsIgnoreCase("quit") || line.equalsIgnoreCase(
                "exit")) {
          log.info("Exiting console input loop...");
          break;
        }
        if (line.equalsIgnoreCase("help")) {
          printHelp();
          continue;
        }

        String[] parts = line.split("\\s+");
        if (parts.length != 2) {
          System.out.println("Use: <actuator> <int value>  e.g, heater 22");
          continue;
        }

        String actuator = parts[0];
        String valueStr = parts[1];

        try {
          int value = Integer.parseInt(valueStr);
          cmdHandler.setValue(nodeId, actuator, value);
          log.info("Command sent: {} {} (nodeId={})", actuator, value, nodeId);
        } catch (NumberFormatException nfe) {
          System.out.println("Value must be integer: " + valueStr);
        } catch (IOException ioe) {
          log.error("Failed to send command: {} {}", actuator, valueStr, nodeId);
        }
      }
    } catch (IOException e) {
      log.error("Console input error", e);
    }
    log.info("Console input loop stopped");
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
    System.out.println(" help | quit");
  }
}
