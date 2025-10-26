package edu.ntnu.bidata.smg.group8.broker.infra.network;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;

import edu.ntnu.bidata.smg.group8.common.util.AppLogger;

final class FrameCodec {
  private static final Logger log = AppLogger.get(FrameCodec.class);
  static final int MAX_FRAME_BYTES = 1_048_576; // 1 MB

  private FrameCodec() {
    // No instances allowed
  }

  static byte[] readFrame(InputStream in) throws IOException {
    DataInputStream din = (in instanceof DataInputStream) 
        ? (DataInputStream) in 
        : new DataInputStream(in);

    int len = din.readInt(); // big-endian
    if (len <= 0 || len > MAX_FRAME_BYTES) {
      throw new EOFException("Invalid frame length: " + len);
    }
    byte[] payload = new byte[len];
    din.readFully(payload);
    return payload;
  }

  static void writeFrame(OutputStream out, byte[] payload) throws IOException {
    if (payload == null) payload = new byte[0];
    if (payload.length > MAX_FRAME_BYTES) {
      throw new IOException("Frame too large: " + payload.length);
    }
    DataOutputStream dout = (out instanceof DataOutputStream) ? (DataOutputStream) out : new DataOutputStream(out);
    dout.writeInt(payload.length); // big-endian
    dout.write(payload);
    dout.flush();
  }

  static String utf8(byte[] bytes) {
    return new String(bytes, StandardCharsets.UTF_8);
  }
  
}
