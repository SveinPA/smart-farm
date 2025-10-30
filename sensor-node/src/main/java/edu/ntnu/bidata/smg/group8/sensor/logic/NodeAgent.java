package edu.ntnu.bidata.smg.group8.sensor.logic;

import edu.ntnu.bidata.smg.group8.common.protocol.Protocol;
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
     * <p><b>Phone Analogy:</b> When the broker says something (like "Turn heater ON"),
     * this method figures out what they said and what to do about it.</p>
     *
     * <p><b>Message Types Handled:</b></p>
     * <ul>
     *  <li>Heartbeat: Just logs receipt (broker checking if we're alive)</li
     * <li>Actuator Command: Parses command and controls the specified actuator</li>
     * </ul>
     *
     * <p>This method is meant primarily for actuator commands; sensor data
     * is handled separately.</p>
     *
     * @param json The received JSON message from the broker
     */
    private void handleIncomingMessage(String json) {
        if (json.contains("\"type\":\"" + Protocol.TYPE_HEARTBEAT + "\"")) {
            log.debug("Heartbeat ← broker");
            return;
        }
        if (json.contains("\"type\":\"" + Protocol.TYPE_ACTUATOR_COMMAND + "\"")) {
            try {
                String actuatorKey = extractField(json, "actuatorKey");
                String valueStr = extractField(json, "value");
                double value = Double.parseDouble(valueStr);

                if (catalog == null) {
                    log.warn("Catalog not set! Cannot control actuators.");
                    return;
                }
                var actuator = catalog.getActuator(actuatorKey);
                if (actuator != null) {
                    actuator.act(value);
                    log.info("Actuator '{}' set to {}", actuatorKey, value);
                } else {
                    log.warn("Unknown actuator: {}", actuatorKey);
                }
            } catch (Exception e) {
                log.error("Failed to handle ACTUATOR_COMMAND: {}", e.getMessage());
            }
            return;
        }
        log.debug("Received from broker: {}", json);
    }

    /**
     * Extracts a field from a JSON string.
     *
     * <p>The purpose of this method is to retrieve the value associated with a specific key
     * from a JSON-formatted string. It uses a regular expression to find the key-value
     * pair and returns the value as a string.</p>
     *
     * @param json The JSON string to extract from
     * @param key  The key of the field to extract
     * @return The value of the field, or an empty string if not found
     */
    private String extractField(String json, String key) {
        String pattern = "\"" + key + "\"\\s*:\\s*\"([^\"]+)\"";
        java.util.regex.Matcher m = java.util.regex.Pattern.compile(pattern).matcher(json);
        return m.find() ? m.group(1) : "";
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