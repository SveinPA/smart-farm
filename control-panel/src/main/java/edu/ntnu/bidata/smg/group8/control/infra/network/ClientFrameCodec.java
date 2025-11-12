package edu.ntnu.bidata.smg.group8.control.infra.network;

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
* Utility class for encoding and decoding length-prefixed frames for
* network communication.
*
* <p>This codec implements a simple framing protocol where each frame
* consists of:</p>
* <ol>
*     <li>A 4-byte integer indicating the payload length</li>
*     <li>The payload bytes</li>
* </ol>
*
* <p>The maximum allowed frame size is 1 MB to prevent memory exhaustion attacks.
* Frames exceeding this limit will be rejected.</p>
*
* @author Andrea Sandnes
* @version 30.10.25
*/
public class ClientFrameCodec {
  private static final Logger log = AppLogger.get(ClientFrameCodec.class);

  public static final int MAX_FRAME_BYTES = 1_048_576; // 1MB

  /**
  * Private constructor to prevent instantiation of this utility class.
  */
  private ClientFrameCodec() {}

  /**
  * Reads a length-prefixed frame from the input stream.
  * This method reads a 4-byte integer indicating the payload length,
  * then reads exactly that many bytes for the payload. If the length is
  * invalid (zero, negative or exceeds MAX_FRAME_BYTES), an exception is
  * thrown.
  *
  * @param in the input stream to read from
  * @return the payload bytes for the frame
  * @throws IOException if an I/O error occurs while reading
  * @throws EOFException if the stream ends prematurely or the frame
  * length is invalid.
  */
  public static byte[] readFrame(InputStream in) throws IOException {
    if (in == null) {
      throw new IllegalArgumentException("InputStream cannot be null");
    }
    DataInputStream din = (in instanceof DataInputStream)
            ? (DataInputStream) in :
            new DataInputStream(in);
    int len = din.readInt();
    log.trace("Reading frame of length: {}", len);
    if (len <= 0) {
      throw  new EOFException("Invalid frame length (must be positive): " + len);
    }
    if (len > MAX_FRAME_BYTES) {
      throw new EOFException("Frame length " + len + " exceeds maximum " + MAX_FRAME_BYTES);
    }
    byte[] payload = new byte[len];
    din.readFully(payload);
    return payload;
  }

  /**
  * Writes a length-prefixed frame to the output stream.
  * This method writes a 4-byte integer indicating the payload length,
  * followed by the payload bytes. The stream is flushed after writing
  * to ensure immediate transmission. If the payload is null, an empty
  * frame (length 0) is written
  *
  * @param out the output stream to write to
  * @param payload the payload bytes to write, or null for an empty frame
  * @throws IOException if an I/O error occurs while writing, or if the
  * payload exceeds MAX_FRAME_BYTES
  */
  public static void writeFrame(OutputStream out, byte[] payload) throws IOException {
    if (payload == null) {
      payload = new byte[0];
    }
    if (payload.length > MAX_FRAME_BYTES) {
      throw new IOException("Frame too large: " + payload.length);
    }
    DataOutputStream dout = (out instanceof DataOutputStream)
            ? (DataOutputStream) out :
            new DataOutputStream(out);
    dout.writeInt(payload.length);
    dout.write(payload);
    dout.flush();
  }

  /**
  * Decodes a byte array to UTF-8 string.
  * This is a convenience method for converting frame payloads to
  * strings when the payload contains text data (JSON).
  *
  * @param bytes the byte array to decode
  * @return the decoded UTF-8 string
  */
  public static String utf8(byte[] bytes) {
    return new String(bytes, StandardCharsets.UTF_8);
  }

  /**
  * Encodes a string to a UTF-8 byte array.
  * This is a convenience method for converting text data (JSON)
  * to bytes before writing as a frame payload.
  *
  * @param s the string to encode
  * @return the UTF-8 encoded byte array
  */
  public static byte[] utf8(String s) {
    return s.getBytes(StandardCharsets.UTF_8);
  }
}
