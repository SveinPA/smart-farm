package edu.ntnu.bidata.smg.group8.control.infra.network;

import org.junit.jupiter.api.Test;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
//TODO: Add more testing?
/**
* This test Class verifies that the ClientFrameCodec correctly
* reads and writes length-prefixed frames, and that it handles edge
* cases and error situations as expected
*
* @author Andrea Sandnes
* @version 31.10.2025
*/
public class ClientFrameCodecTest {

  /**
  * This test verifies that a payload can be written and then read back
  * identically via round-trip testing.
  * The test writes a string with norwegian characters (æ, ø, å) as
  * UTF-8 bytes, reads it back and confirms that the original string
  * is preserved.
  *
  * @throws Exception if an I/O error occurs during testing
  */
  @Test
  void testWriteThenReadRoundTripPayload() throws Exception {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    String payload = "hello-øæå";
    ClientFrameCodec.writeFrame(baos, payload.getBytes(StandardCharsets.UTF_8));

    ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
    byte[] frame = ClientFrameCodec.readFrame(bais);
    assertEquals(payload, new String(frame, StandardCharsets.UTF_8));
  }

  /**
  * This test verifies that the method readFrame in ClientFrameCodec
  * throws EOFException when the frame length is invalid.
  *
  * @throws Exception if an unexpected I/O error occurs during testing
  */
  @Test
  void testReadFrameThrowsInvalidLength() throws Exception {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    DataOutputStream dout = new DataOutputStream(baos);
    dout.writeInt(-1);
    dout.flush();

    ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
    assertThrows(EOFException.class, () ->
      ClientFrameCodec.readFrame(bais));
  }

  /**
  * This test verifies that the method writeFrame in ClientFrameCodec
  * throws IOException when the payload exceeds the maximum allowed size.
  */
  @Test
  void testWriteFrameThrowsOnTooLargePayload() {
    byte[] large = new byte[ClientFrameCodec.MAX_FRAME_BYTES + 1];
    assertThrows(IOException.class, () ->
      ClientFrameCodec.writeFrame(new ByteArrayOutputStream(), large));
  }

}
