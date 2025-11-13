package edu.ntnu.bidata.smg.group8.sensor.logic;

import edu.ntnu.bidata.smg.group8.common.actuator.AbstractActuator;
import edu.ntnu.bidata.smg.group8.common.actuator.Actuator;
import edu.ntnu.bidata.smg.group8.common.protocol.MessageParser;
import edu.ntnu.bidata.smg.group8.common.protocol.Protocol;
import edu.ntnu.bidata.smg.group8.common.protocol.dto.ActuatorCommandMessage;
import edu.ntnu.bidata.smg.group8.common.sensor.Sensor;
import edu.ntnu.bidata.smg.group8.common.util.AppLogger;
import edu.ntnu.bidata.smg.group8.common.util.JsonBuilder;
import edu.ntnu.bidata.smg.group8.sensor.app.SensorNodeMain;
import edu.ntnu.bidata.smg.group8.sensor.infra.SensorDataPacket;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

/**
 * <h3>Node Agent - The Sensor Node's "Phone" to the Broker</h3>
 *
 * <p><b>Phone Analogy:</b> Think of NodeAgent as a phone that lets the sensor node
 * talk to the broker. Just like we use a phone to call someone and have a conversation,
 * NodeAgent handles all the communication with the broker.</p>
 *
 * <p>This class is responsible for handling network communication
 * between a sensor node and the central broker. It manages the TCP connection,
 * registration handshake, data transmission, and graceful disconnection.</p>
 *
 * <p>This class works together with:</p>
 * <ul>
 *   <li>{@link SensorDataPacket}: to serialize sensor readings into JSON</li>
 *   <li>{@link Protocol}: for consistent message types and roles</li>
 *   <li>{@link AppLogger}: for logging connection and transmission events</li>
 *   <li>{@link SensorNodeMain}: which orchestrates the overall sensor node application</li>
 * </ul>
 *
 * <p>This class does not handle sensor simulation itself, that logic is
 * managed by {@link SensorNodeMain}.</p>
 *
 * @author Ida Soldal
 * @version 29.10.2025
 */
public class NodeAgent {

    private static final Logger log = AppLogger.get(NodeAgent.class);

    private final String nodeId;
    private final String brokerHost;
    private final int brokerPort;
    private Socket socket;
    private OutputStream out;
    private InputStream in;
    private volatile boolean running;
    private Thread listenerThread;
    private DeviceCatalog catalog;

    /**
     * Constructs a NodeAgent with the specified parameters.
     *
     * <p>Creates a new NodeAgent instance responsible for communication with the broker.</p>
     *
     * @param nodeId     The unique identifier of the sensor node (your "caller ID")
     * @param brokerHost The hostname or IP address of the broker (the "phone number")
     * @param brokerPort The TCP port of the broker (the "extension number")
     */
    public NodeAgent(String nodeId, String brokerHost, int brokerPort) {
        this.nodeId = nodeId;
        this.brokerHost = brokerHost;
        this.brokerPort = brokerPort;
        this.running = false;
    }

    /**
     * Sets the device catalog for actuator control.
     *
     * <p>Must be called before actuator commands can be processed.</p>
     *
     * @param catalog The DeviceCatalog instance to use
     */
    public void setCatalog(DeviceCatalog catalog) {
        this.catalog = catalog;
    }

    /**
     * Establishes a TCP connection with the broker and performs the registration handshake.
     *
     * <p><b>Phone Analogy:</b> This is similar to dialing the broker's number, waiting for them
     * to pick up, then saying "Hi, I'm sensor node #42", and waiting for them to say
     * "OK, I've registered you." The line stays open after that for the whole conversation.</p>
     *
     * <p>This method connects to the broker at the configured host and port,
     * then sends a registration message identifying this node.</p>
     *
     * <p><b>Steps:</b></p>
     * <ol>
     *   <li>Open TCP socket connection (dial the number)</li>
     *   <li>Send registration message (introduce yourself)</li>
     *   <li>Wait for acknowledgment (wait for "OK" response)</li>
     *   <li>Start listening for incoming messages (keep ear on the phone)</li>
     * </ol>
     *
     * @see Protocol#TYPE_REGISTER_NODE
     * @throws IOException if connection or registration fails
     */
    public void connect() throws IOException {
        socket = new Socket();
        socket.connect(new InetSocketAddress(brokerHost, brokerPort), 5000);
        socket.setTcpNoDelay(true);
        socket.setKeepAlive(true); // Prevents disconnection on idle
        out = socket.getOutputStream();
        in = socket.getInputStream();

        log.info("Connected to broker at {}:{}", brokerHost, brokerPort);

        // Send registration message
        String registerMsg = JsonBuilder.build(
                "type", Protocol.TYPE_REGISTER_NODE,
                "role", Protocol.ROLE_SENSOR_NODE,
                "nodeId", nodeId,
                "protocolVersion", Protocol.PROTOCOL_VERSION
        );

        send(registerMsg);
        log.info("Registration message sent for node {}", nodeId);
        
        // Wait for ACK from broker
        waitForAck();
        
        // Start listening for incoming messages
        startListening();
    }

    /**
     * Waits for an ACK message from the broker after registration.
     *
     * <p><b>Phone Analogy:</b> After we say "Hi, I'm sensor node #42",
     * we need to wait for the broker to respond with "OK, registered!".</p>
     *
     * @throws IOException if ACK is not received, connection fails or response is null
     */
    private void waitForAck() throws IOException {
        String response = readMessage();
        if (response == null) {
            throw new IOException("Connection closed before REGISTER_ACK was received");
        }

        if (!response.contains("\"type\":\"" + Protocol.TYPE_REGISTER_ACK + "\"")) {
            throw new IOException("Expected REGISTER_ACK, got: " + response);
        }
        
        log.info("Registration acknowledged by broker");
    }

    /**
     * Reads a single length-prefixed message from the broker.
     *
     * <p><b>Phone Analogy:</b> We're listening carefully to what the other person
     * is saying. First we hear "I'm going to say 50 words", then we listen for exactly
     * those 50 words. This prevents cutting them off or mishearing.</p>
     *
     * <p><b>Technical Details:</b> Messages are length-prefixed, meaning the first 4 bytes
     * tell us how long the actual message is. This prevents message boundary issues.</p>
     *
     * @return The message as a JSON string
     * @throws IOException if reading fails
     */
    private String readMessage() throws IOException {
        // Read 4-byte length prefix
        byte[] header = in.readNBytes(4);
        if (header.length < 4) {
            return null; // EOF: broker closed connection
        }

        int length = ((header[0] & 0xFF) << 24) // 24 bits for the first byte
                | ((header[1] & 0xFF) << 16) // 16 bits
                | ((header[2] & 0xFF) << 8) // 8 bits
                | (header[3] & 0xFF); // 0 bits

        // Read JSON payload
        byte[] payload = in.readNBytes(length);
        if (payload.length < length) {
            throw new IOException("Connection closed while reading payload");
        }

        return new String(payload, StandardCharsets.UTF_8);
    }

    /**
     * Starts a background thread to listen for incoming messages from the broker.
     *
     * <p><b>Phone Analogy:</b> This is like keeping our ear on the phone the whole time,
     * even while we're doing other things. A separate "listener" pays attention to
     * anything the broker says, so we don't miss commands like "Turn on the heater!"</p>
     *
     * <p><b>Technical Details:</b> A dedicated thread continuously reads messages from the broker
     * and processes them. This allows the main thread to focus on sending sensor data
     * without blocking.</p>
     */
    private void startListening() {
        running = true;
        listenerThread = new Thread(() -> { // Start a new thread for listening
            while (running && isConnected()) {
                try {
                    String message = readMessage();
                    if (message == null) {
                        log.warn("Broker closed its output stream (EOF). Listener stopping.");
                        break;
                    }
                    handleIncomingMessage(message);
                } catch (IOException e) {
                    if (running) {
                        log.warn("Listener stopped: {}", e.getMessage());
                    }
                    break; // Exit loop instead of killing connection
                }
            }
        }, "NodeAgent-Listener-" + nodeId); // Name the thread for easier debugging (Name + Node ID)

        listenerThread.setDaemon(true); // Set as daemon so it doesn't block JVM exit
        // (JVM = Java Virtual Machine)
        listenerThread.start(); // Start the listener thread
        log.info("Started listening for broker messages");
    }

    /**
     * Handles incoming messages from the broker.
     *
     * <p><b>Phone Analogy:</b> When the broker says something, we need to understand
     * what they mean and respond appropriately. This method listens for messages
     * like "Turn the fan ON" and figures out what to do. If it's just a "Mhm, are you there?" (heartbeat),
     * we simply acknowledge it.</p>
     *
     * <p>Uses {@link MessageParser} to decode JSON messages into DTOs.</p>
     *
     * <p><b>Message Types Handled:</b></p>
     * <ul>
     *   <li>HEARTBEAT → Logged acknowledgement (broker checking if we're alive)</li>
     *   <li>ACTUATOR_COMMAND → Parses and executes actuator control</li>
     * </ul>
     *
     * @param json The received JSON message from the broker
     */
    private void handleIncomingMessage(String json) {
        try { // Try to parse and handle the message
            String type = MessageParser.getType(json); // Extract message type
            if (type == null) {
                log.warn("Received malformed message: {}", json);
                return;
            }

            switch (type) { // Handle based on message type
                case Protocol.TYPE_HEARTBEAT -> { // Heartbeat message
                    log.debug("← HEARTBEAT from broker");
                }

                case Protocol.TYPE_ACTUATOR_COMMAND -> { // Actuator command message
                    var msg = MessageParser.parseActuatorCommand(json);
                    handleActuatorCommand(msg);
                }

                default -> log.debug("Unhandled message type '{}': {}", type, json);
                // Ignore unknown types
            }
        } catch (Exception e) {
            log.error("Error processing message: {}", e.getMessage(), e);
            // Log parsing/handling errors
        }
    }


    /**
     * Handles an actuator command received from the broker.
     *
     * <p><b>Phone Analogy:</b> When the broker says "Turn the fan ON",
     * this method figures out what they mean and makes it happen. It checks
     * which actuator they want to control and what action to perform.</p>
     *
     * <p>Parses the actuator and numeric value, then invokes the actuator's {@link AbstractActuator#act(double)} method.</p>
     *
     * @param msg The parsed {@link ActuatorCommandMessage} containing actuator and action fields
     */
    private void handleActuatorCommand(ActuatorCommandMessage msg) {
        if (catalog == null) { // Ensure catalog is set
            log.warn("No device catalog set — cannot execute actuator command.");
            return;
        }

        String actuatorKey = msg.getActuator(); // e.g., "fan", "window"
        String action = msg.getAction(); // e.g., "ON", "OFF", "open", "close", "75.0"

        if (actuatorKey == null || action == null) { // Validate fields
            log.warn("Invalid ACTUATOR_COMMAND (missing actuator or action): {}", msg);
            return;
        }

        var actuator = catalog.getActuator(actuatorKey); // Lookup actuator by key
        if (actuator == null) { // Unknown actuator
            log.warn("Unknown actuator '{}'", actuatorKey);
            return;
        }

        try { // Try to parse action as a numeric value
            double value = Double.parseDouble(action); // e.g., "75.0" → 75.0
            actuator.act(value); // Execute actuator command
            log.info("Actuator '{}' set to {}", actuatorKey, value);
        } catch (NumberFormatException e) { // Non-numeric action
            log.error("Invalid numeric value '{}' for actuator '{}'", action, actuatorKey);
        } catch (Exception e) { // Actuator execution error
            log.error("Failed to execute actuator command for '{}': {}", actuatorKey, e.getMessage());
        }
    }


    /**
     * Sends a prebuilt message to the broker.
     *
     * <p><b>Phone Analogy:</b> This is like speaking into the phone. But instead of
     * just talking, we first say "I'm about to say 50 words" (the length prefix),
     * then we say exactly those 50 words. This ensures the listener knows exactly
     * where our message ends and the next one begins.</p>
     *
     * <p><b>Thread Safety:</b> This method is synchronized to prevent multiple threads
     * from interleaving their writes, which would corrupt the message framing.</p>
     *
     * @param jsonMessage The message to send (already formatted as JSON)
     * @throws IOException if the connection is closed or the send fails (line went dead)
     */
    public synchronized void send(String jsonMessage) throws IOException {
        if (socket == null || out == null || socket.isClosed()) {
            throw new IOException("Socket is not connected.");
        }

        byte[] payload = jsonMessage.getBytes(StandardCharsets.UTF_8); // Convert message to bytes
        int length = payload.length; // Get length of message

        // Write 4-byte length prefix
        out.write((length >>> 24) & 0xFF);
        out.write((length >>> 16) & 0xFF);
        out.write((length >>> 8) & 0xFF);
        out.write(length & 0xFF);
        // Write actual message
        out.write(payload);
        out.flush();
    }

    /**
     * Builds and sends a sensor data packet to the broker.
     *
     * <p><b>Phone Analogy:</b> We are saying "Hey broker, the temperature is 22.5°C!"
     * We check what the sensor says, package it nicely, and send it over the phone.</p>
     *
     * <p>Called periodically from SensorNodeMain to report sensor readings.</p>
     *
     * @param sensor The sensor to read data from
     */
    public void sendSensorData(Sensor sensor) {
        try {
            double value = sensor.getReading();
            String json = SensorDataPacket.build(nodeId, sensor, value);
            send(json);
            log.debug("Sent sensor data: {}", json);
        } catch (IOException e) {
            log.error("Failed to send sensor data: {}", e.getMessage());
        }
    }

    /**
     * Sends an averaged sensor reading to the broker.
     *
     * <p><b>Phone Analogy:</b> Instead of saying "The temperature is 22.5°C" every second,
     * we say "The average temperature over the last minute is 22.3°C". This reduces
     * the number of messages we send, making our conversation more efficient.</p>
     *
     * <p><b>Current configuration:</b></p>
     * <ul>
     *   <li>The last 5 readings (approximately 1-minute aggregate) are averaged before transmission</li>
     *   <li>Demonstrates support for aggregated data at a coarser resolution</li>
     * </ul>
     *
     * @param sensor The sensor
     * @param averagedValue The averaged value (from multiple samples)
     */
    public void sendAveragedSensorData(Sensor sensor, double averagedValue) {
        try {
            String json = SensorDataPacket.build(nodeId, sensor, averagedValue);
            send(json);
            log.debug("Sent averaged sensor data: {}", json);
        } catch (IOException e) {
            log.error("Failed to send averaged sensor data: {}", e.getMessage());
        }
    }

    /**
     * Sends current actuator status to the broker.
     *
     *<p>Phone Analogy: This is like telling the broker "Hey, the heater is ON right now!",
     * so they know the current state of the heater without having to ask.</p>
     *
     * <p>Reports whether the actuator is ON/OFF based on its current value.
     * Values > 0.5 are considered ON, values <= 0.5 are considered OFF.</p>
     *
     * @param actuator The actuator to report status for
     */
    public void sendActuatorStatus(Actuator actuator) {
        try {
            double currentValue = actuator.getCurrentValue();
            String status;

            if ("window".equals(actuator.getKey()) || "valve".equals(actuator.getKey())) {
                status = String.format(Locale.US, "%.0f", currentValue);
            }

            else {
                status = currentValue > 0.5 ? "ON" : "OFF";
            }
            
            String json = JsonBuilder.build(
                "type", Protocol.TYPE_ACTUATOR_STATUS,
                "nodeId", nodeId,
                "actuatorKey", actuator.getKey(),
                "status", status,
                "value", String.format(Locale.US, "%.2f", currentValue),
                "timestamp", String.valueOf(System.currentTimeMillis())
            );
            send(json);
            log.debug("Sent actuator status: {} = {} (value: {:.2f})", 
                    actuator.getKey(), status, currentValue);
        } catch (IOException e) {
            log.error("Failed to send actuator status: {}", e.getMessage());
        }
    }


    /**
     * Sends a heartbeat message to the broker.
     *
     * <p><b>Phone Analogy:</b> This is like saying "Mhm, I'm still here" on a phone call.
     * We periodically let the broker know we're still alive and connected, so they don't
     * think we hung up or got disconnected.</p>
     *
     * <p><b>Why It's Important:</b> Without heartbeats, the broker can't tell if we're
     * still connected or if our connection died. Regular heartbeats prevent timeouts.</p>
     *
     * <p><b>Use Case:</b> Called periodically (e.g., every 30 seconds) from SensorNodeMain.</p>
     */
    public void sendHeartbeat() {
        try {
            String heartbeat = JsonBuilder.build(
                "type", Protocol.TYPE_HEARTBEAT,
                "direction", Protocol.HB_CLIENT_TO_SERVER,
                "protocolVersion", Protocol.PROTOCOL_VERSION,
                "nodeId", nodeId
            );
            send(heartbeat);
            log.debug("Heartbeat sent");
        } catch (IOException e) {
            log.error("Failed to send heartbeat: {}", e.getMessage());
        }
    }

    /**
     * Closes the TCP connection and releases resources.
     *
     * <p><b>Phone Analogy:</b> This is like saying "Bye!" and hanging up the phone properly.
     * We don't just walk away - we end the call gracefully, close the line, and put the
     * phone down neatly.</p>
     *
     * <p><b>Technical Details:</b> This method ensures that the listener thread stops,
     * and the socket and streams are properly closed to free up system resources.</p>
     *
     * <p>If an error occurs during closing, a warning is logged.</p>
     *
     * @see Socket#close()
     */
    public void disconnect() {
        running = false;
        try {
            // Closing the socket/streams wakes up any blocking reads
            if (in != null) in.close(); // If input stream is open, close it
            if (out != null) out.close(); // If output stream is open, close it
            if (socket != null && !socket.isClosed()) socket.close();

            if (listenerThread != null) {
                listenerThread.join(2000); 
                // If listener thread is running, wait for it to finish
            }
            log.info("Disconnected from broker.");
        } catch (IOException | InterruptedException e) {
            log.warn("Error while closing connection: {}", e.getMessage());
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Checks if the connection to the broker is currently active.
     *
     * <p>We check by verifying that the socket is not null,
     * is connected, and not closed.</p>
     *
     * @return {@code true} if connected, {@code false} otherwise
     */
    public boolean isConnected() {
        return socket != null && socket.isConnected() && !socket.isClosed();
    }
}