package edu.ntnu.bidata.smg.group8.broker.infra.network;

import edu.ntnu.bidata.smg.group8.common.util.AppLogger;
import edu.ntnu.bidata.smg.group8.common.protocol.FrameCodec;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;

/**
 * Registry for active connections.
 * 
 * <p>Maintains two separate registries for different routing patterns:
 * <ul>
 *   <li><strong>Control panels:</strong> Tracked by OutputStream for broadcast operations</li>
 *   <li><strong>Sensor nodes:</strong> Tracked by nodeId for targeted command routing</li>
 * </ul>
 * 
 * <p>Thread-safe using {@link ConcurrentHashMap}. Automatically prunes dead streams
 * during broadcast operations if write operations fail.
 * 
 * <p><strong>AI usage:</strong> Developed with AI assistance (Claude Code) for designing 
 * the dual-registry architecture (broadcast vs targeted routing) and implementing 
 * thread-safe concurrent access patterns. Broadcast resilience with automatic dead stream
 * pruning discussed with AI guidance. All implementation and testing by Svein Antonsen.
 * 
 * @author Svein Antonsen
 * @since 1.0
 */
final class ConnectionRegistry {
  private static final Logger log = AppLogger.get(ConnectionRegistry.class);

  private final Map<OutputStream, String> controlPanels = new ConcurrentHashMap<>();
  private final Map<String, OutputStream> sensorNodes = new ConcurrentHashMap<>();

  /**
   * Register a control panel connection.
   *
   * @param out the output stream of the control panel
   * @param who the identifier of the control panel
   */
  void registerPanel(OutputStream out, String who) {
    controlPanels.put(out, who);
    log.info("Registered CONTROL_PANEL {}", who);
  }

  /**
   * Register a sensor node connection.
   * 
   * @param nodeId the unique identifier of the sensor node
   * @param out the output stream of the sensor node
   * @param who the identifier of the sensor node (for logging)
   */
  void registerSensorNode(String nodeId, OutputStream out, String who) {
    sensorNodes.put(nodeId, out);
    log.info("Registered SENSOR_NODE {} ({})", nodeId, who);
  }

  /**
   * Unregister a sensor node connection.
   * 
   * @param nodeId the unique identifier of the sensor node
   * @param who the identifier of the sensor node (for logging)
   */
  void unregisterSensorNode(String nodeId, String who) {
    if (nodeId != null) {
      sensorNodes.remove(nodeId);
      log.info("Unregistered SENSOR_NODE {} ({})", nodeId, who);
    }
  }

  /**
   * Get the output stream for a sensor node by its node ID.
   * 
   * @param nodeId the unique identifier of the sensor node
   * @return the output stream, or {@code null} if the node is not connected
   */
  OutputStream getSensorNodeStream(String nodeId) {
    return sensorNodes.get(nodeId);
  }

  /**
   * Unregister a control panel connection.
   *
   * @param out the output stream of the control panel
   * @param who the identifier of the control panel
   */
  void unregisterPanel(OutputStream out, String who) {
    if (out != null) {
      controlPanels.remove(out);
      log.info("Unregistered CONTROL_PANEL {}", who);
    }
  }

  /**
   * Get the number of registered sensor nodes.
   */
  int sensorNodeCount() {
    return sensorNodes.size();
  }

  /**
   * Get the number of registered control panels.
   */
  int controlPanelCount() {
    return controlPanels.size();
  }

  /**
   * Broadcast a frame to all connected control panels.
   *
   * @param frame the frame bytes to broadcast
   */
  void broadcastToPanels(byte[] payload) {
    Iterator<Map.Entry<OutputStream, String>> it = controlPanels.entrySet().iterator();
    while (it.hasNext()) {
      Map.Entry<OutputStream, String> e = it.next();
      try {
        FrameCodec.writeFrame(e.getKey(), payload);
      } catch (IOException io) {
        log.warn("Broadcast failed to {}; pruning stream: {}", e.getValue(), io.getMessage());
        it.remove();
      }
    }
  }

  /**
   * Get a comma-separated list of all registered sensor node IDs.
   * 
   * @return comma-separated nodeIDs, or empty string if no nodes registered
   */
  String getSensorNodeIdList() {
    if (sensorNodes.isEmpty()) {
      return "";
    }
    return String.join(",", sensorNodes.keySet());
  }

  /**
   * Broadcast a frame to all connected sensor nodes.
   *
   * <p>This method iterates over all registered sensor nodes and attempts to send
   * the provided frame. If a write operation fails (indicating a dead connection),
   * the corresponding entry is removed from the registry to maintain integrity.</p>
   *
   * @param payload the frame bytes to broadcast
   */
  void broadcastToSensors(byte[] payload) {
      Iterator<Map.Entry<String, OutputStream>> it = sensorNodes.entrySet().iterator();
      while (it.hasNext()) {
          Map.Entry<String, OutputStream> e = it.next();
          try {
              FrameCodec.writeFrame(e.getValue(), payload);
          } catch (IOException io) {
              log.warn("Broadcast to sensor {} failed; pruning stream: {}", e.getKey(), io.getMessage());
              it.remove(); // remove dead node
          }
      }
  }
}
