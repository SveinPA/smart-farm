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

    // Parse command line arguments with defaults
    String brokerHost = args.length > 0 ? args[0] : DEFAULT_BROKER_HOST;
    int brokerPort = args.length > 1 ? Integer.parseInt(args[1]) : DEFAULT_BROKER_PORT;
    String nodeId = args.length > 2 // Check if node ID provided, length has to be > 2
        ? args[2] // Use provided node ID, 2nd argument
        : DEFAULT_NODE_ID + "-" + java.util.UUID.randomUUID().toString().substring(0, 4);
        // Generate unique node ID if not provided - default name + random 4-char suffix
        // Example: sensor-node-1234
    String sensorConfig = args.length > 3 ? args[3] : null;
    // Optional sensor configuration (comma-separated types), or null for all sensors (default)

    log.info("Configuration:");
    log.info("  Node ID: {}", nodeId);
    log.info("  Broker: {}:{}", brokerHost, brokerPort);
    if (sensorConfig != null) { // Guard Condition:
      log.info("  Sensors: {}", sensorConfig);
    } else {
      log.info("  Sensors: all (default)");
    }

    ScheduledExecutorService scheduler = null;
    NodeAgent agent = null;

    try {
      // Step 1: Create and populate device catalog
      DeviceCatalog catalog = createDeviceCatalog(sensorConfig);
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
      log.info("Averaging enabled: keeping {} samples per sensor (~1 minute window).",
          AVERAGE_SAMPLE_COUNT);

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