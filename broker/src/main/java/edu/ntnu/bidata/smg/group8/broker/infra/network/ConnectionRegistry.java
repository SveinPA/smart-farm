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
 */
final class ConnectionRegistry {
  private static final Logger log = AppLogger.get(ConnectionRegistry.class);

  private final Map<OutputStream, String> controlPanels = new ConcurrentHashMap<>();

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
}
