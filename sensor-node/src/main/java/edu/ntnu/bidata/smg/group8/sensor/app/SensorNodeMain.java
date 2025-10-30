package edu.ntnu.bidata.smg.group8.sensor.app;

import edu.ntnu.bidata.smg.group8.common.sensor.Sensor;
import edu.ntnu.bidata.smg.group8.common.util.AppLogger;
import edu.ntnu.bidata.smg.group8.sensor.entity.actuators.FanActuator;
import edu.ntnu.bidata.smg.group8.sensor.entity.actuators.HeaterActuator;
import edu.ntnu.bidata.smg.group8.sensor.entity.actuators.ValveActuator;
import edu.ntnu.bidata.smg.group8.sensor.entity.actuators.WindowActuator;
import edu.ntnu.bidata.smg.group8.sensor.entity.sensors.FertilizerSensor;
import edu.ntnu.bidata.smg.group8.sensor.entity.sensors.HumiditySensor;
import edu.ntnu.bidata.smg.group8.sensor.entity.sensors.LightSensor;
import edu.ntnu.bidata.smg.group8.sensor.entity.sensors.PhSensor;
import edu.ntnu.bidata.smg.group8.sensor.entity.sensors.TemperatureSensor;
import edu.ntnu.bidata.smg.group8.sensor.entity.sensors.WindSpeedSensor;
import edu.ntnu.bidata.smg.group8.sensor.infra.SensorDataPacket;
import edu.ntnu.bidata.smg.group8.sensor.logic.DeviceCatalog;
import edu.ntnu.bidata.smg.group8.sensor.logic.NodeAgent;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * <h3>Sensor Node Main - Entry Point for Sensor/Actuator Nodes</h3>
 *
 * <p>This is the <b>"glue" class</b> that orchestrates all components of a sensor node.
 * It brings together the device catalog, network communication, and periodic tasks
 * into a complete working sensor/actuator node application.</p>
 *
 * <p><b>What This Class Does:</b></p>
 * <ul>
 *   <li><b>Initialization:</b> Creates and configures all sensors and actuators</li>
 *   <li><b>Connection:</b> Establishes TCP connection to the broker via {@link NodeAgent}</li>
 *   <li><b>Data Broadcasting:</b> Periodically sends sensor readings to the broker</li>
 *   <li><b>Heartbeat:</b> Keeps the connection alive with periodic health checks</li>
 *   <li><b>Lifecycle Management:</b> Handles startup, running, and graceful shutdown</li>
 * </ul>
 *
 * <p><b>Architecture:</b> This class coordinates three main components:</p>
 * <ul>
 *   <li>{@link DeviceCatalog}: Manages the collection of sensors and actuators</li>
 *   <li>{@link NodeAgent}: Handles network communication with the broker</li>
 *   <li>{@link ScheduledExecutorService}: Manages periodic tasks (data + heartbeat)</li>
 * </ul>
 *
 * <p><b>Usage Examples:</b></p>
 * <pre>
 * # Run with default settings (localhost:23048, nodeId=node-1)
 * java SensorNodeMain
 *
 * # Multiple nodes can run simultaneously with different IDs
 * java SensorNodeMain localhost 23048 greenhouse-1  # Terminal 1
 * java SensorNodeMain localhost 23048 greenhouse-2  # Terminal 2
 * </pre>
 *
 * <p><b>Execution:</b></p>
 * <ol>
 *   <li>Parse command line arguments (broker address, port, node ID)</li>
 *   <li>Create {@link DeviceCatalog} and populate with sensors/actuators</li>
 *   <li>Create {@link NodeAgent} and connect to broker</li>
 *   <li>Start periodic tasks (sensor data every 5s, heartbeat every 30s)</li>
 *   <li>Run until user presses ENTER or sends termination signal</li>
 *   <li>Perform graceful shutdown (stop tasks, disconnect from broker)</li>
 * </ol>
 *
 * <p><b>Important Notes:</b></p>
 * <ul>
 *   <li>The broker must be running before starting sensor nodes</li>
 *   <li>Each node must have a unique node ID for proper identification</li>
 *   <li>Graceful shutdown is handled via shutdown hook (CTRL+C) or ENTER key</li>
 *   <li>Connection failures will cause the application to exit with status code 1</li>
 * </ul>
 *
 * @author Ida Soldal
 * @version 29.10.2025
 * @see DeviceCatalog
 * @see NodeAgent
 * @see SensorDataPacket
 */
public final class SensorNodeMain {

  private static final Logger log = AppLogger.get(SensorNodeMain.class);

  // ============================================================
  // CONFIGURATION CONSTANTS

  // Default broker hostname if not specified via command line.
  private static final String DEFAULT_BROKER_HOST = "localhost";

  // Default broker port if not specified via command line.
  private static final int DEFAULT_BROKER_PORT = 23048;

  // Default node ID if not specified via command line.
  private static final String DEFAULT_NODE_ID = "node-1";

  // Interval for sending sensor data to the broker (in seconds).
  private static final int SENSOR_DATA_INTERVAL_SECONDS = 5;

  // Interval for sending heartbeat messages to the broker (in seconds).
  private static final int HEARTBEAT_INTERVAL_SECONDS = 30;

  /**
   * Private constructor to prevent instantiation.
   *
   * <p>This is a utility class with only static methods and should not be instantiated.
   * The main entry point is {@link #main(String[])}.</p>
   */
  private SensorNodeMain() {
    // Utility class - no instances allowed
  }

  // ============================================================
  // MAIN ENTRY POINT

  /**
   * Main entry point for the sensor node application.
   *
   * <p>This method orchestrates the entire lifecycle of a sensor node:</p>
   * <ol>
   *   <li>Parse command line arguments</li>
   *   <li>Initialize device catalog with sensors and actuators</li>
   *   <li>Create and connect NodeAgent to broker</li>
   *   <li>Start periodic tasks for data transmission and heartbeat</li>
   *   <li>Wait for user termination signal</li>
   *   <li>Perform clean shutdown</li>
   * </ol>
   *
   * <p><b>Command Line Arguments:</b></p>
   * <ul>
   *   <li><code>args[0]</code> - Broker hostname (default: localhost)</li>
   *   <li><code>args[1]</code> - Broker port (default: 23048)</li>
   *   <li><code>args[2]</code> - Node ID (default: node-1)</li>
   * </ul>
   *
   * <p><b>Exit Codes:</b></p>
   * <ul>
   *   <li><code>0</code> - Normal shutdown</li>
   *   <li><code>1</code> - Connection error or fatal exception</li>
   * </ul>
   *
   * @param args Command line arguments: [brokerHost] [brokerPort] [nodeId]
   */
  public static void main(String[] args) {
    log.info("=== Smart Farm Sensor Node Starting ===");

    // Parse command line arguments with defaults
    String brokerHost = args.length > 0 ? args[0] : DEFAULT_BROKER_HOST;
    int brokerPort = args.length > 1 ? Integer.parseInt(args[1]) : DEFAULT_BROKER_PORT;
    String nodeId = args.length > 2 ? args[2] : DEFAULT_NODE_ID;

    log.info("Configuration:");
    log.info("  Node ID: {}", nodeId);
    log.info("  Broker: {}:{}", brokerHost, brokerPort);

    ScheduledExecutorService scheduler = null;
    NodeAgent agent = null;

    try {
      // Step 1: Create and populate device catalog
      DeviceCatalog catalog = createDeviceCatalog();
      log.info("Device catalog created: {}", catalog.summary());

      // Step 2: Create node agent for communication
      agent = new NodeAgent(nodeId, brokerHost, brokerPort);
      agent.setCatalog(catalog); // Link catalog to agent
      log.info("NodeAgent created for node '{}'", nodeId);

      // Step 3: Connect to broker
      log.info("Connecting to broker at {}:{}...", brokerHost, brokerPort);
      agent.connect();
      log.info("Successfully connected to broker!");

      // Step 4: Start periodic tasks
      scheduler = Executors.newScheduledThreadPool(2);

      // Task 1: Send sensor data periodically
      final NodeAgent finalAgent = agent;
      scheduler.scheduleAtFixedRate(() -> {
        sendAllSensorData(finalAgent, catalog);
      }, 0, SENSOR_DATA_INTERVAL_SECONDS, TimeUnit.SECONDS);

      // Task 2: Send heartbeat periodically with error detection
      scheduler.scheduleAtFixedRate(() -> {
        try {
          // Check connection before sending heartbeat
          if (!finalAgent.isConnected()) {
            log.error("Lost connection during heartbeat check! Exiting...");
            System.exit(1);
          }
          finalAgent.sendHeartbeat();
        } catch (Exception e) {
          log.error("Heartbeat failed: {}", e.getMessage());
          // If connection error, exit
          String errorMsg = e.getMessage();
          if (errorMsg != null && errorMsg.contains("Broken pipe")) {
            log.error("Connection lost! Exiting...");
            System.exit(1);
          }
        }
      }, HEARTBEAT_INTERVAL_SECONDS, HEARTBEAT_INTERVAL_SECONDS, TimeUnit.SECONDS);

      log.info("Periodic tasks started (sensor data every {}s, heartbeat every {}s)",
          SENSOR_DATA_INTERVAL_SECONDS, HEARTBEAT_INTERVAL_SECONDS);

      // Step 5: Set up graceful shutdown
      final NodeAgent shutdownAgent = agent;
      final ScheduledExecutorService shutdownScheduler = scheduler;
      setupShutdownHook(shutdownAgent, shutdownScheduler);

      // Step 6: Keep running and wait for user to stop
      log.info("Sensor node '{}' is now running. Press ENTER to stop.", nodeId);
      waitForUserInput();

      // Step 7: Clean shutdown
      log.info("Shutting down sensor node '{}'...", nodeId);
      scheduler.shutdown();
      if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
        scheduler.shutdownNow();
      }
      agent.disconnect();
      log.info("Sensor node stopped successfully.");

    } catch (IOException e) {
      log.error("Connection error: {}", e.getMessage(), e);
      System.exit(1);
    } catch (Exception e) {
      log.error("Fatal error in sensor node: {}", e.getMessage(), e);
      System.exit(1);
    } finally {
      // Cleanup in case of unexpected errors
      if (scheduler != null && !scheduler.isShutdown()) {
        scheduler.shutdownNow();
      }
      if (agent != null && agent.isConnected()) {
        agent.disconnect();
      }
    }
  }

  // ============================================================
  // PERIODIC TASK METHODS

  /**
   * Sends all sensor data from the catalog to the broker.
   *
   * <p>This method is called periodically by the scheduler (every 5 seconds by default)
   * to broadcast current sensor readings to the broker. Each sensor is read individually,
   * and its data is sent via the {@link NodeAgent}.</p>
   *
   * <p><b>Error Handling:</b> If a connection error is detected ("Broken pipe"),
   * the application exits immediately to prevent error spam. Other errors are logged
   * but allow continued operation for remaining sensors.</p>
   *
   * @param agent   The NodeAgent to send data through
   * @param catalog The DeviceCatalog containing all sensors
   */
  private static void sendAllSensorData(NodeAgent agent, DeviceCatalog catalog) {
    try {
      // Check if still connected before sending
      if (!agent.isConnected()) {
        log.error("Connection to broker lost! Shutting down...");
        System.exit(1);
      }

        for (Sensor sensor : catalog.getAllSensors()) {
            try { // Try-catch per sensor to continue on individual failures
                agent.sendSensorData(sensor);
            } catch (Exception e) {
                log.warn("Failed to send data for sensor {}: {}", sensor.getKey(), e.getMessage());
                // Continue with remaining sensors
            }
        }
      log.debug("Sent data for {} sensors", catalog.getSensorCount());
    } catch (Exception e) {
      log.error("Error sending sensor data: {}", e.getMessage());

      // If it's a connection error, exit immediately
      String errorMsg = e.getMessage();
      if (errorMsg != null && (errorMsg.contains("Broken pipe")
          || errorMsg.contains("Connection reset")
          || errorMsg.contains("Socket closed"))) {
        log.error("Fatal connection error! Broker connection lost. Exiting...");
        System.exit(1);
      }
    }
  }

  // ============================================================
  // INITIALIZATION METHODS

  /**
   * Creates and populates the device catalog with sensors and actuators.
   *
   * <p><b>Purpose:</b> This method allows different nodes to have different sensor configurations.
   * By modifying this method, you can create specialized node types
   * (e.g., temperature-only nodes, humidity-focused nodes, etc.).</p>
   *
   * <p><b>Current Configuration:</b> This creates a "full-featured" node with:</p>
   * <ul>
   *   <li><b>6 Sensors:</b> Temperature, Humidity, Light, Fertilizer, pH, Wind Speed</li>
   *   <li><b>4 Actuators:</b> Heater, Fan, Window, Valve</li>
   * </ul>
   *
   * <p><b>Sensor-Actuator Integration:</b> Temperature, humidity, and wind speed sensors
   * are linked to the catalog so they can respond to actuator states (e.g., heater affects
   * temperature readings).</p>
   *
   * @return A fully populated DeviceCatalog ready for use
   */
  private static DeviceCatalog createDeviceCatalog() {
      DeviceCatalog catalog = new DeviceCatalog();
  
      // Add all sensor types
      log.info("Adding sensors to catalog...");
      TemperatureSensor tempSensor = new TemperatureSensor();
      HumiditySensor humSensor = new HumiditySensor();
      WindSpeedSensor windSensor = new WindSpeedSensor();
      
      catalog.addSensor(tempSensor);
      catalog.addSensor(humSensor);
      catalog.addSensor(new LightSensor());
      catalog.addSensor(new FertilizerSensor());
      catalog.addSensor(new PhSensor());
      catalog.addSensor(windSensor);
  
      // Add all actuator types
      log.info("Adding actuators to catalog...");
      catalog.addActuator(new HeaterActuator());
      catalog.addActuator(new FanActuator());
      catalog.addActuator(new WindowActuator());
      catalog.addActuator(new ValveActuator());
      
      // Link sensors to catalog so they can check actuator states
      log.info("Linking sensors to actuators for realistic readings...");
      tempSensor.setCatalog(catalog);
      log.debug("✓ Temperature sensor linked (affected by: heater, fan, window)");

      humSensor.setCatalog(catalog);
      log.debug("✓ Humidity sensor linked (affected by: valve, window)");
      
      windSensor.setCatalog(catalog);
      log.debug("✓ Wind speed sensor linked (affected by: window state)");
  
      return catalog;
  }

  // ============================================================
  // SHUTDOWN MANAGEMENT

  /**
   * Sets up a shutdown hook to ensure clean shutdown on CTRL+C or kill signal.
   *
   * <p><b>Purpose:</b> When the user presses CTRL+C or the JVM receives a termination
   * signal, this hook ensures that the node properly disconnects from the broker
   * and cleans up resources (threads, sockets, etc.).</p>
   *
   * <p><b>What It Does:</b></p>
   * <ol>
   *   <li>Stops the scheduled executor (no more periodic tasks)</li>
   *   <li>Disconnects the NodeAgent from the broker</li>
   *   <li>Logs the cleanup process</li>
   * </ol>
   *
   * @param agent     The NodeAgent to disconnect
   * @param scheduler The ScheduledExecutorService to shut down
   */
  private static void setupShutdownHook(NodeAgent agent, ScheduledExecutorService scheduler) {
    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
      log.info("Shutdown signal received. Cleaning up...");
      if (scheduler != null) {
        scheduler.shutdownNow();
      }
      if (agent != null) {
        agent.disconnect();
      }
      log.info("Cleanup complete.");
    }, "shutdown-hook"));
  }

  /**
   * Waits for user input (ENTER key) to trigger shutdown.
   *
   * <p><b>Purpose:</b> This keeps the main thread alive while the NodeAgent runs
   * its communication tasks in background threads. Without this, the main method
   * would exit immediately after starting the background tasks.</p>
   *
   * <p>The user can stop the node at any time by pressing
   * ENTER in the terminal, which triggers a graceful shutdown sequence.</p>
   *
   */
  private static void waitForUserInput() {
    try (Scanner scanner = new Scanner(System.in)) {
      scanner.nextLine();
    } catch (Exception e) {
      log.warn("Error reading user input: {}", e.getMessage());
    }
  }
}