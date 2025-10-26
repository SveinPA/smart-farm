package edu.ntnu.bidata.smg.group8.broker.infra.network;

import edu.ntnu.bidata.smg.group8.common.util.AppLogger;
import edu.ntnu.bidata.smg.group8.common.protocol.Protocol;

import org.slf4j.Logger;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.ntnu.bidata.smg.group8.common.util.JsonBuilder;

final class ClientHandler implements Runnable {
    private static final Logger log = AppLogger.get(ClientHandler.class);

    // Heartbeat/idle settings
    private static final int READ_TIMEOUT_MS = 30_000; // read timeout for socket (30s)
    private static final int MAX_IDLE_MISSES = 2;      // after 2 timeouts without traffic -> close

    private final Socket socket;
    private final ConnectionRegistry registry;
    private final AtomicBoolean running = new AtomicBoolean(true);

    private volatile boolean handshaken = false;
    private String role = null;
    private String nodeId = null;

    private long lastSeen = System.currentTimeMillis();
    private int idleMisses = 0;

    ClientHandler(Socket socket, ConnectionRegistry registry) {
        this.socket = socket;
        this.registry = registry;
        try {
            this.socket.setTcpNoDelay(true);
            this.socket.setKeepAlive(true);
            this.socket.setSoTimeout(READ_TIMEOUT_MS); 
        } catch (IOException e) {
            log.warn("Failed to configure socket {}: {}", remote(), e.getMessage());
        }
    }

    @Override
    public void run() {
        final String who = remote();
        log.info("Handler started for {}", who);

        try (InputStream in = socket.getInputStream();
             OutputStream out = socket.getOutputStream()) {

            while (running.get()) {
                try {
                    byte[] frame = FrameCodec.readFrame(in); // may throw SocketTimeoutException
                    String msg = FrameCodec.utf8(frame).trim();
                    lastSeen = System.currentTimeMillis();
                    idleMisses = 0; // reset on traffic

                    // ---- Handshake ----
                    if (!handshaken) {
                        if (!msg.contains("\"" + Protocol.TYPE_REGISTER_NODE + "\"") 
                                && !msg.contains("\"" + Protocol.TYPE_REGISTER_CONTROL_PANEL + "\"")) {
                            log.warn("First message from {} was not a REGISTER_* message: {}", who, msg);
                            break;
                        }
                        role = extractJsonField(msg, "role");
                        nodeId = extractJsonField(msg, "nodeId");
                        if (role == null || nodeId == null) {
                            log.warn("REGISTER_* missing role/nodeId from {}: {}", who, msg);
                            break;
                        }
                        handshaken = true;

                        String ack = JsonBuilder.build(
                            "type", Protocol.TYPE_REGISTER_ACK,
                            "protocolVersion", Protocol.PROTOCOL_VERSION,
                            "role", role,
                            "nodeId", nodeId,
                            "message", "Registration successful"
                        );
                        FrameCodec.writeFrame(out, ack.getBytes(StandardCharsets.UTF_8));
                        log.info("Registration OK for {} role={} nodeId={}", who, role, nodeId);

                        if (Protocol.ROLE_CONTROL_PANEL.equalsIgnoreCase(role)) {
                            registry.registerPanel(out, who);
                            log.info("Panels connected: {}", registry.controlPanelCount());
                        }
                        continue;
                    }

                    // ---- Post-handshake ----
                    String type = extractJsonField(msg, "type");

                    if (Protocol.TYPE_HEARTBEAT.equals(type)) {
                        log.debug("Heartbeat received from {}", who);
                        continue;
                    }

                    log.info("From {} ({}/{}): {}", who, role, nodeId, msg);

                    if (Protocol.TYPE_SENSOR_DATA.equals(type)) {
                        if (!Protocol.ROLE_SENSOR_NODE.equalsIgnoreCase(role)) {
                            log.warn("Rejected SENSOR_DATA from non-sensor {} ({})", who, role);
                        } else {
                           // Broadcast to all connected control panels
                            registry.broadcastToPanels(msg.getBytes(StandardCharsets.UTF_8));
                        }
                    }

                } catch (SocketTimeoutException ste) {
                    // No frame within READ_TIMEOUT_MS
                    idleMisses++;
                    if (handshaken) {
                        try {
                            String heartBeat = JsonBuilder.build(
                                "type", Protocol.TYPE_HEARTBEAT,
                                "direction", Protocol.HB_SERVER_TO_CLIENT,
                                "protocolVersion", Protocol.PROTOCOL_VERSION
                            );
                            FrameCodec.writeFrame(out, heartBeat.getBytes(StandardCharsets.UTF_8));
                            log.debug("HEARTBEAT → {} (idleMisses={})", who, idleMisses);
                        } catch (IOException ioe) {
                            log.warn("Failed to send HEARTBEAT to {}: {}", who, ioe.getMessage());
                            break;
                        }
                    }
                    if (idleMisses > MAX_IDLE_MISSES) {
                        log.info("Closing idle connection {} (role={}, node={}) after {} misses",
                                 who, role, nodeId, idleMisses);
                        break;
                    }
                    // continue loop
                }
            }

        } catch (EOFException eof) {
            log.info("Client {} closed the connection (EOF).", who);
        } catch (IOException ex) {
            if (running.get()) log.warn("I/O error for {}: {}", who, ex.getMessage());
        } finally {
            // cleanup & unregister
            closeQuietly();
            try {
                if (Protocol.ROLE_CONTROL_PANEL.equalsIgnoreCase(role)) {
                    registry.unregisterPanel(socket.getOutputStream(), who);
                    log.info("Panels connected: {}", registry.controlPanelCount());
                }
            } catch (IOException ignored) {
                // stream may already be closed
            }
            log.info("Handler stopped for {}", who);
        }
    }

    void stop() {
        running.set(false);
        closeQuietly();
    }

    private void closeQuietly() {
        try {
            socket.close();
        } catch (IOException ignored) {
            // ignore
        }
    }

    private String remote() {
        return socket.getRemoteSocketAddress() != null
                ? socket.getRemoteSocketAddress().toString()
                : "unknown";
    }

    // Simple temporary JSON field extractor for flat JSON objects with string values
    private static String extractJsonField(String json, String key) {
        Pattern p = Pattern.compile("\"" + key + "\"\\s*:\\s*\"([^\"]+)\"");
        Matcher m = p.matcher(json);
        return m.find() ? m.group(1) : null;
    }
}
