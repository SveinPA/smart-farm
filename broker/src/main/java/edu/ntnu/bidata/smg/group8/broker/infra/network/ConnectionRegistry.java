package edu.ntnu.bidata.smg.group8.broker.infra.network;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.slf4j.Logger;

import edu.ntnu.bidata.smg.group8.common.util.AppLogger;

final class ConnectionRegistry {
  private static final Logger log = AppLogger.get(ConnectionRegistry.class);

  private final List<OutputStream> controlPanels = new CopyOnWriteArrayList<>();

  void registerPanel(OutputStream out, String who) {
    controlPanels.add(out);
    log.info("Registered CONTROL_PANEL {}", who);
  }

  void unregisterPanel(OutputStream out, String who) {
    controlPanels.remove(out);
    log.info("Unregistered CONTROL_PANEL {}", who);
  }

  int controlPanelCount() {
    return controlPanels.size();
  }

  void broadcastToPanels(byte[] frame) {
    for (OutputStream out : controlPanels) {
      try {
        FrameCodec.writeFrame(out, frame);
      } catch (IOException ignored) {
        // removed by its own handler`s cleanup
      }
    }
  }
}
