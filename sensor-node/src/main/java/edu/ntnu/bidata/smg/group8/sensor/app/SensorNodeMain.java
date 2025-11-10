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
import edu.ntnu.bidata.smg.group8.sensor.logic.DeviceCatalog;
import edu.ntnu.bidata.smg.group8.sensor.logic.NodeAgent;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * <h3>Sensor Node Main - Entry Point for Sensor & Actuator Nodes</h3>
 *
 * <p>This class is the <b>glue</b> that ties together all components of a sensor node:
 * including device catalog, node agent, sensors, and actuators.</p>
 *
 * <p><b>Features:</b></p>
 * <ol>
 *   <li><b>Unique identifier:</b> Auto-generated UUID suffix or manual via node ID parameter</li>
 *   <li><b>Multiple sensors:</b> Supports up to 6 sensor types per node (temp, hum, light, fert, ph, wind)</li>
 *   <li><b>Different sensors:</b> Configurable via sensorConfig parameter (e.g., "temp,hum" vs "light")</li>
 *   <li><b>Actuator node:</b> Every node includes 4 actuator types (heater, fan, window, valve)</li>
 * </ol>
 *
 * <p><b>Usage Examples:</b></p>
 * <pre>
 * # Minimal configuration (all sensors, auto-generated ID)
 * mvn exec:java -pl sensor-node
 * → Node ID: sensor-node-a3f2, Broker: localhost:23048, Sensors: all
 *
 * # Custom nodes with specific sensors
 * mvn exec:java -pl sensor-node -Dexec.args="localhost 23048 greenhouse-1 temp,hum"
 * → Only temperature and humidity sensors
 *
 * mvn exec:java -pl sensor-node -Dexec.args="localhost 23048 outdoor-light light"
 * → Only light sensor
 *
 * # If dependency errors occur:
 * mvn clean install
 * </pre>
 *
 * <p><b>Important Notes:</b></p>
 * <ul>
 *   <li>Broker must be running <b>before</b> starting sensor nodes</li>
 *   <li>Each node requires a unique node ID (auto-generated if not provided)</li>
 *   <li>Sensor data is sent every 12 seconds as 5-sample rolling average</li>
 *   <li>Heartbeats maintain connection every 30 seconds</li>
 * </ul>
 *
 * <p><b>Note:</b> This class has been heavily been structured and assisted by
 * AI, specifically a combination of <b>Claude Sonnet 4.5 and GPT-5</b>. This is due
 * to the heavy amounts of research that had to be done in this project,
 * and this class was among the most complex to develop. Comments have been
 * added for the team's own understanding.</p>
 *
 * @author Ida Soldal
 * @version 2.11.2025
 * @see DeviceCatalog
 * @see NodeAgent
 */
public final class SensorNodeMain {

  private static final Logger log = AppLogger.get(SensorNodeMain.class);
  private static final Map<String, Deque<Double>> recentReadings = new HashMap<>();

  // ============================================================
  // CONFIGURATION CONSTANTS

  // Default broker hostname if not specified via command line
  private static final String DEFAULT_BROKER_HOST = "localhost";

  // Default broker port if not specified via command line
  private static final int DEFAULT_BROKER_PORT = 23048;

  // Default node ID if not specified via command line
  private static final String DEFAULT_NODE_ID = "sensor-node";

  // Interval for sending sensor data to the broker (how often)
  private static final int SENSOR_DATA_INTERVAL_SECONDS = 12; // in seconds

  // Number of samples to average for sensor data (how much data)
  private static final int AVERAGE_SAMPLE_COUNT = 5;

  // Interval for sending heartbeat messages to the broker (in seconds)
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
   * <p><b>Lifecycle:</b></p>
   * <ol>
   *   <li>Parse command line arguments (with defaults)</li>
   *   <li>Create device catalog with sensors/actuators</li>
   *   <li>Connect NodeAgent to broker</li>
   *   <li>Start periodic tasks (sensor data + heartbeat)</li>
   *   <li>Wait for user termination (ENTER key)</li>
   *   <li>Clean shutdown</li>
   * </ol>
   *
   * <p><b>Usage:</b> {@code java SensorNodeMain [brokerHost] [brokerPort] [nodeId]}</p>
   *
   * @param args Command line arguments: [brokerHost] [brokerPort] [nodeId]
   */
  public static void main(String[] args) {
    log.info("=== Smart Farm Sensor Node Starting ===");

    // Step 1-2: Parse arguments and log configuration
    NodeConfiguration cfg = parseArguments(args);
    logStartupInfo(cfg);

    // Step 3-6: Initialize components, start tasks, and wait for shutdown
    try {
        DeviceCatalog catalog = createDeviceCatalog(cfg.sensorConfig); // Step 3: Create catalog
        NodeAgent agent = initializeAgent(cfg, catalog); // Step 4: Connect agent
        ScheduledExecutorService scheduler = startScheduledTasks(agent, catalog); // Step 5: Start tasks

        setupShutdownHook(agent, scheduler); // Step 6: Setup shutdown hook
        waitForUserInput(); // Wait for user to press ENTER
        shutdown(agent, scheduler); // Clean shutdown

    } catch (Exception e) { // Step 3-6 error handling
        handleFatalError(e);
    }
  }

  /**
   * Parses command line arguments and builds a configuration object.
   *
   * <p>Expected order: {@code [brokerHost] [brokerPort] [nodeId] [sensorConfig]}.
   * If any argument is missing, defaults are applied automatically.</p>
   *
   * @param args Command line arguments array
   * @return Populated {@link NodeConfiguration} with resolved defaults
   */
  private static NodeConfiguration parseArguments(String[] args) {
    String brokerHost = args.length > 0 ? args[0] : DEFAULT_BROKER_HOST;
    int brokerPort = args.length > 1 ? Integer.parseInt(args[1]) : DEFAULT_BROKER_PORT;
    String nodeId = args.length > 2
        ? args[2]
        : DEFAULT_NODE_ID + "-" + UUID.randomUUID().toString().substring(0, 4);
        // Generate unique node ID if not provided - default name + random 4-char suffix
        // Example: sensor-node-1234
    String sensorConfig = args.length > 3 ? args[3] : null;
    // Optional sensor configuration (comma-separated types), or null for all sensors (default)

    return new NodeConfiguration(brokerHost, brokerPort, nodeId, sensorConfig);
  }

  /**
   * Logs the startup configuration in a readable format.
   *
   * @param cfg The configuration object containing node parameters
   */
  private static void logStartupInfo(NodeConfiguration cfg) {
    log.info("Configuration:");
    log.info("  Node ID: {}", cfg.nodeId);
    log.info("  Broker: {}:{}", cfg.brokerHost, cfg.brokerPort);
    log.info("  Sensors: {}", cfg.sensorConfig != null ? cfg.sensorConfig : "all (default)");
  }

  /**
   * Initializes the NodeAgent and establishes connection to the broker.
   *
   * <p>This method encapsulates connection setup and error handling for clarity.</p>
   *
   * @param cfg     Node configuration object
   * @param catalog The DeviceCatalog to associate with the agent
   * @return Initialized and connected {@link NodeAgent}
   * @throws IOException if connection fails
   */
  private static NodeAgent initializeAgent(NodeConfiguration cfg, DeviceCatalog catalog) throws IOException {
    NodeAgent agent = new NodeAgent(cfg.nodeId, cfg.brokerHost, cfg.brokerPort);
    agent.setCatalog(catalog);
    log.info("Connecting to broker at {}:{}...", cfg.brokerHost, cfg.brokerPort);
    agent.connect();
    log.info("Successfully connected to broker!");
    return agent;
  }

  /**
   * Starts periodic background tasks for sensor data updates and heartbeats.
   *
   * <p><b>Tasks Started:</b></p>
   * <ul>
   *  <li><b>Sensor Data:</b> Sends averaged sensor readings every
   *   {@value #SENSOR_DATA_INTERVAL_SECONDS} seconds</li>
   * <li><b>Actuator Status:</b> Sends current actuator states every
   * {@value #SENSOR_DATA_INTERVAL_SECONDS} seconds</li>
   * <li><b>Heartbeat:</b> Sends heartbeat messages every
   * {@value #HEARTBEAT_INTERVAL_SECONDS} seconds</li>
   * </ul>
   *
   * @param agent   Connected {@link NodeAgent}
   * @param catalog Device catalog for reading sensors
   * @return Scheduled executor service managing background tasks
   */
  private static ScheduledExecutorService startScheduledTasks(NodeAgent agent, DeviceCatalog catalog) {
      ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(3);
      // 3 means we can run 3 tasks concurrently

      // Task 1: Send averaged sensor data
      scheduler.scheduleAtFixedRate(() ->
          sendAllSensorData(agent, catalog),
          0, SENSOR_DATA_INTERVAL_SECONDS, TimeUnit.SECONDS
      );

      // Task 2: Send actuator status
      scheduler.scheduleAtFixedRate(() ->
          sendAllActuatorStatus(agent, catalog),
          0, SENSOR_DATA_INTERVAL_SECONDS, TimeUnit.SECONDS
      );

      // Task 3: Send heartbeat messages
      scheduler.scheduleAtFixedRate(() -> {
          try {
              if (!agent.isConnected()) {
                  log.error("Lost connection during heartbeat check! Exiting...");
                  System.exit(1);
              }
              agent.sendHeartbeat();
          } catch (Exception e) {
              log.error("Heartbeat failed: {}", e.getMessage());
          }
      }, HEARTBEAT_INTERVAL_SECONDS, HEARTBEAT_INTERVAL_SECONDS, TimeUnit.SECONDS);

      log.info("Periodic tasks started (sensor data + actuator status every {}s, heartbeat every {}s)",
          SENSOR_DATA_INTERVAL_SECONDS, HEARTBEAT_INTERVAL_SECONDS);
      log.info("Averaging enabled: keeping {} samples per sensor (~1 minute window).",
          AVERAGE_SAMPLE_COUNT);
      return scheduler;
  }

  /**
   * Shuts down scheduler and disconnects agent cleanly.
   *
   * <p>This method ensures that all resources are released properly
   * before the application exits.</p>
   *
   * @param agent     The NodeAgent
   * @param scheduler The task scheduler
   */
  private static void shutdown(NodeAgent agent, ScheduledExecutorService scheduler) {
    try {
        log.info("Shutting down sensor node...");
        scheduler.shutdown();
        if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
            scheduler.shutdownNow();
        }
        agent.disconnect();
        log.info("Sensor node stopped successfully.");
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        log.warn("Shutdown interrupted: {}", e.getMessage());
    }
  }

  /**
   * Handles fatal exceptions that occur during startup or runtime.
   *
   * <p>This method logs the error and exits the application.</p>
   *
   * @param e Exception that caused failure
   */
  private static void handleFatalError(Exception e) {
    log.error("Fatal error in sensor node: {}", e.getMessage(), e);
    System.exit(1);
  }

  /**
   * Immutable configuration holder for node startup parameters.
   *
   * <p>Holds:</p>
   * <ul>
   *  <li><b>Broker hostname</b></li>
   * <li><b>Broker port</b></li>
   * <li><b>Node identifier</b></li>
   * <li><b>Sensor configuration string</b></li>
   * </ul>
   *
   * <p>A record is used for simplicity and immutability.</p>
   */
  private record NodeConfiguration(String brokerHost, int brokerPort, String nodeId, String sensorConfig){}


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
      if (!agent.isConnected()) { // Pre-check connection
        log.error("Connection to broker lost! Shutting down...");
        System.exit(1);
      }

      for (Sensor sensor : catalog.getAllSensors()) {
        double value = sensor.getReading();
        // Keep 5 most recent readings for averaging
        recentReadings
            .computeIfAbsent(sensor.getKey(), k -> new java.util.ArrayDeque<>())
            .add(value);

        if (recentReadings.get(sensor.getKey()).size() > AVERAGE_SAMPLE_COUNT) {
          recentReadings.get(sensor.getKey()).removeFirst();
        }
        double avg = recentReadings.get(sensor.getKey())
            .stream()
            .mapToDouble(Double::doubleValue)
            .average()
            .orElse(value);

        // Send the average reading
        agent.sendAveragedSensorData(sensor, avg);
      }
      log.debug("Sent averaged data for {} sensors", catalog.getSensorCount());
    } catch (Exception e) {
      log.error("Error sending sensor data: {}", e.getMessage());
    }
  }

  /**
   * Sends status of all actuators to the broker.
   *
   * <p>Reports current state (ON/OFF) of each actuator based on current value.
   * Values > 0.5 are considered ON, <= 0.5 are considered OFF.</p>
   *
   * @param agent   The NodeAgent to send data through
   * @param catalog The DeviceCatalog containing all actuators
   */
  private static void sendAllActuatorStatus(NodeAgent agent, DeviceCatalog catalog) {
      try {
          if (!agent.isConnected()) {
              log.error("Connection to broker lost! Shutting down...");
              System.exit(1);
          }

          for (var actuator : catalog.getAllActuators()) {
              agent.sendActuatorStatus(actuator);
          }
          
          log.debug("Sent status for {} actuators", catalog.getActuatorCount());
      } catch (Exception e) {
          log.error("Error sending actuator status: {}", e.getMessage());
      }
  }


  // ============================================================
  // INITIALIZATION METHODS

  /**
   * Creates and populates the device catalog based on configuration.
   *
   * <p><b>Sensor Configuration:</b> Pass a comma-separated list of sensor types
   * to create custom node configurations. If no config is provided, creates a
   * full-featured node with all sensor types.</p>
   *
   * <p><b>Available Sensor Types:</b></p>
   * <ul>
   *   <li><b>temp</b> - Temperature sensor</li>
   *   <li><b>hum</b> - Humidity sensor</li>
   *   <li><b>light</b> - Light sensor</li>
   *   <li><b>fert</b> - Fertilizer sensor</li>
   *   <li><b>ph</b> - pH sensor</li>
   *   <li><b>wind</b> - Wind speed sensor</li>
   * </ul>
   *
   * @param sensorConfig Comma-separated sensor types, or null for all sensors
   * @return A DeviceCatalog with the specified sensors and all actuators
   */
  private static DeviceCatalog createDeviceCatalog(String sensorConfig) {
    DeviceCatalog catalog = new DeviceCatalog();
    
    // Parse and add sensors
    SensorConfiguration config = parseSensorConfig(sensorConfig);
    addConfiguredSensors(catalog, config);
    
    // Always add all actuators (they control environment regardless of which sensors are present)
    addActuators(catalog);
    
    // Link sensors to catalog for realistic readings
    linkSensorsToActuators(catalog, config);
    
    return catalog;
  }

  /**
   * Parses sensor configuration string into a structured configuration object.
   *
   * @param sensorConfig Comma-separated sensor types, or null for all
   * @return SensorConfiguration indicating which sensors to include
   */
  private static SensorConfiguration parseSensorConfig(String sensorConfig) {
    SensorConfiguration config = new SensorConfiguration();
    
    if (sensorConfig == null || sensorConfig.isEmpty()) {
      log.info("No sensor config provided - adding all sensor types");
      config.includeAll();
      return config;
    }
    
    log.info("Custom sensor config: {}", sensorConfig);
    String[] types = sensorConfig.toLowerCase().split(",");
    
    for (String type : types) {
      // Switch-case to check which sensors user wants
      // Use easy-to-write input (despite fert sounding funny)
      type = type.trim();
      switch (type) {
        case "temp":
          config.includeTemp = true;
          break;
        case "hum":
          config.includeHum = true;
          break;
        case "light":
          config.includeLight = true;
          break;
        case "fert":
          config.includeFert = true;
          break;
        case "ph":
          config.includePh = true;
          break;
        case "wind":
          config.includeWind = true;
          break;
        default:
          log.warn("Unknown sensor type '{}' - ignoring", type);
      }
    }
    return config;
  }

  /**
   * Adds sensors to the catalog based on configuration.
   *
   * @param catalog The catalog to add sensors to
   * @param config  The configuration specifying which sensors to include
   */
  private static void addConfiguredSensors(DeviceCatalog catalog, SensorConfiguration config) {
    log.info("Adding sensors to catalog...");
    
    if (config.includeTemp) { // Add temperature sensor
      config.tempSensor = new TemperatureSensor();
      catalog.addSensor(config.tempSensor);
      log.debug("✓ Temperature sensor added");
    }
    
    if (config.includeHum) { // Add humidity sensor
      config.humSensor = new HumiditySensor();
      catalog.addSensor(config.humSensor);
      log.debug("✓ Humidity sensor added");
    }
    
    if (config.includeLight) { // Add light sensor
      catalog.addSensor(new LightSensor());
      log.debug("✓ Light sensor added");
    }
    
    if (config.includeFert) { // Add fertilizer sensor
      catalog.addSensor(new FertilizerSensor());
      log.debug("✓ Fertilizer sensor added");
    }
    
    if (config.includePh) { // Add pH sensor
      catalog.addSensor(new PhSensor());
      log.debug("✓ pH sensor added");
    }
    
    if (config.includeWind) { // Add wind speed sensor
      config.windSensor = new WindSpeedSensor();
      catalog.addSensor(config.windSensor);
      log.debug("✓ Wind speed sensor added");
    }
  }

  /**
   * Adds all actuators to the catalog.
   * Actuators are always added regardless of sensor configuration.
   *
   * @param catalog The catalog to add actuators to
   */
  private static void addActuators(DeviceCatalog catalog) {
    log.info("Adding actuators to catalog...");
    catalog.addActuator(new HeaterActuator());
    catalog.addActuator(new FanActuator());
    catalog.addActuator(new WindowActuator());
    catalog.addActuator(new ValveActuator());
    log.debug("✓ All actuators added");
  }

  /**
   * Links sensors to the catalog for realistic actuator-aware readings.
   * Only sensors that were added need to be linked.
   *
   * @param catalog The catalog containing actuators
   * @param config  The configuration with sensor references
   */
  private static void linkSensorsToActuators(DeviceCatalog catalog, SensorConfiguration config) {
    if (config.tempSensor != null) {
      config.tempSensor.setCatalog(catalog);
      log.debug("✓ Temperature sensor linked to actuators");
    }
    
    if (config.humSensor != null) {
      config.humSensor.setCatalog(catalog);
      log.debug("✓ Humidity sensor linked to actuators");
    }
    
    if (config.windSensor != null) {
      config.windSensor.setCatalog(catalog);
      log.debug("✓ Wind speed sensor linked to actuators");
    }
  }

  /**
   * Internal configuration object to track which sensors to include.
   * Keeps references to sensors that need actuator linking.
   */
  private static class SensorConfiguration {
    boolean includeTemp = false;
    boolean includeHum = false;
    boolean includeLight = false;
    boolean includeFert = false;
    boolean includePh = false;
    boolean includeWind = false;
    
    // Keep references for sensors that need catalog linking
    TemperatureSensor tempSensor = null;
    HumiditySensor humSensor = null;
    WindSpeedSensor windSensor = null;

    // Inner class to help with sensor configuration
    void includeAll() { // Call to include all sensors
      includeTemp = includeHum = includeLight = includeFert = includePh = includeWind = true;
    }
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