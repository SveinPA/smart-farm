package edu.ntnu.bidata.smg.group8.broker.infra.network;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;



class FrameCodecTest {

  @Test
  void roundTripUtf8() throws Exception {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    String s = "{\"type\":\"HELLO\"}";
    byte[] p = s.getBytes(StandardCharsets.UTF_8);

    FrameCodec.writeFrame(baos, p);
    byte[] raw = baos.toByteArray();

    ByteArrayInputStream bais = new ByteArrayInputStream(raw);
    byte[] back = FrameCodec.readFrame(bais);

    assertArrayEquals(p, back);
  }

  @Test
  void rejectsTooLarge() {
    byte[] huge = new byte[FrameCodec.MAX_FRAME_BYTES + 1];
    assertThrows(IOException.class, () -> FrameCodec.writeFrame(new ByteArrayOutputStream(), huge));
  }

  @Test
  void rejectsInvalidLength() {
    // length=0
    byte[] hdr = new byte[] {0, 0, 0, 0};
    assertThrows(EOFException.class, () -> FrameCodec.readFrame(new ByteArrayInputStream(hdr)));
  }
}
