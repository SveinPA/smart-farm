package edu.ntnu.bidata.smg.group8.common.protocol;

import edu.ntnu.bidata.smg.group8.common.util.AppLogger;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;



/**
 * Codec for length-prefixed frames.
 *
 * <p>Frames are encoded as a big-endian 4-byte length prefix followed by the payload bytes.
 * The maximum allowed frame size is {@value #MAX_FRAME_BYTES} bytes.
 *
 * @see FrameCodec#readFrame(InputStream)
 * @see FrameCodec#writeFrame(OutputStream, byte[])
 */
public final class FrameCodec {
  private static final Logger log = AppLogger.get(FrameCodec.class);
  static final int MAX_FRAME_BYTES = 1_048_576; // 1 MB

  private FrameCodec() {
    // No instances allowed
  }

  /**
   * Read a length-prefixed frame from the given input stream.
   *
   * @param in the input stream
   * @return the frame payload bytes
   */
  public static byte[] readFrame(InputStream in) throws IOException {
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

  /**
   * Write a length-prefixed frame to the given output stream.
   *
   * @param out the output stream
   * @param payload the frame payload bytes
   */
  public static void writeFrame(OutputStream out, byte[] payload) throws IOException {
    if (payload == null) {
      payload = new byte[0];
    } 
    if (payload.length > MAX_FRAME_BYTES) {
      throw new IOException("Frame too large: " + payload.length);
    }
    DataOutputStream dout = (out instanceof DataOutputStream) ? (DataOutputStream) out 
        : new DataOutputStream(out);
    dout.writeInt(payload.length); // big-endian
    dout.write(payload);
    dout.flush();
  }

  /**
   * Decode the given bytes as a UTF-8 string.
   *
   * @param bytes the bytes to decode
   * @return the decoded string
   */
  public static String utf8(byte[] bytes) {
    return new String(bytes, StandardCharsets.UTF_8);
  }
  
}
