package edu.ntnu.bidata.smg.group8.control.util;

import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * This test Class verifies that FlatJson parses flat JSON-objects
 * with string values correctly and that it handles edge cases and
 * invalid input as expected.
 *
* @author Andrea Sandnes
* @version 31.10.2025
*/
public class FlatJsonTest {

  /**
  * This test verifies that the parser returns a correct map for a
  * valid flat JSON object with multiple key-value pairs.
  */
  @Test
  void testParseReturnsMapForValidFlatObject() {
    String json = "{\"type\":\"SENSOR_DATA\",\"nodeId\":\"7\",\"sensorKey\":\"temperature\",\"value\":\"22.8\"}";
    Map<String, String> m = FlatJson.parse(json);

    assertEquals("SENSOR_DATA", m.get("type"));
    assertEquals("7", m.get("nodeId"));
    assertEquals("temperature", m.get("sensorKey"));
    assertEquals("22.8", m.get("value"));
    assertEquals(4, m.size());
  }

  /**
  * This test verifies that the parser returns an empty map for
  * invalid input-types such as null, empty string, invalid JSON
  * and arrays.
  */
  @Test
  void testParseReturnsEmptyMapForInvalidInput() {
    assertTrue(FlatJson.parse(null).isEmpty());
    assertTrue(FlatJson.parse("").isEmpty());
    assertTrue(FlatJson.parse("not-json").isEmpty());
    assertTrue(FlatJson.parse("[]").isEmpty());
  }

  /**
  * This test verifies that the parser correctly handles escape-sequences
  * and commas inside a string value without splitting them.
  */
  @Test
  void testParseHandlesEscapesAndCommasInsideString() {
    String json = "{\"msg\":\"Hello, \\\"greenhouse\\\"\",\"note\":\"a,b,c\"}";
    Map<String, String> m = FlatJson.parse(json);

    assertEquals("Hello, \"greenhouse\"", m.get("msg"));
    assertEquals("a,b,c", m.get("note"));
  }

  /**
  * This test verifies that the parser ignores entries lacking colon,
  * but still processes valid entries.
  */
  @Test
  void testParseIgnoreEntriesWithoutColon() {
    String json = "{\"a\":\"b\", \"broken\" , \"c\":\"d\"}";
    Map<String, String> m = FlatJson.parse(json);

    assertEquals("b", m.get("a"));
    assertEquals("d", m.get("c"));
    assertEquals(2, m.size());
  }

  /**
  * This test verifies that the parser correctly handles all supported
  * escape-sequences: backslash, newline, tab and quotes.
  */
  @Test
  void testParseHandlesVariousEscapeSequences() {
    Map<String, String> m1 = FlatJson.parse("{\"path\":\"C:\\\\Users\\\\Test\"}");
    assertEquals("C:\\Users\\Test", m1.get("path"));

    Map<String, String> m2 = FlatJson.parse("{\"text\":\"Line1\\nLine2\\tTabbed\"}");
    assertEquals("Line1\nLine2\tTabbed", m2.get("text"));

    Map<String, String> m3 = FlatJson.parse("{\"msg\":\"Hello \\\"welcome\\\" to greenhouse\"}");
    assertEquals("Hello \"welcome\" to greenhouse", m3.get("msg"));
  }

  /**
  * This test verifies that special characters such as colons
  * and commas inside values do not interfere with the parsing.
  */
  @Test
  void testParseHandlesSpecialCharactersInValues() {
    Map<String, String> m1 = FlatJson.parse("{\"url\":\"http://example.com:8080\"}");
    assertEquals("http://example.com:8080", m1.get("url"));

    Map<String, String> m2 = FlatJson.parse("{\"list\":\"a,b,c,d,e\"}");
    assertEquals("a,b,c,d,e", m2.get("list"));
  }

  /**
  * This test verifies that the parser treats numeric and
  * boolean values as strings, since FlatJson only supports
  * string values.
  */
  @Test
  void testParseHandlesStringifiedPrimitives() {
    String json = "{\"temp\":\"22.5\",\"humidity\":\"65\",\"active\":\"true\"}";
    Map<String, String> m = FlatJson.parse(json);

    assertEquals("22.5", m.get("temp"));
    assertEquals("65", m.get("humidity"));
    assertEquals("true", m.get("active"));
  }

  /**
  * This test verifies that the parser handles edge cases such as
  * empty objects, whitespace and empty string values.
  */
  @Test
  void testParseHandlesEdgeCases() {
    assertTrue(FlatJson.parse("{}").isEmpty());

    Map<String, String> m1 = FlatJson.parse("  {  \"key\"  :  \"value\"  }  ");
    assertEquals("value", m1.get("key"));

    Map<String, String> m2 = FlatJson.parse("{\"key\":\"\"}");
    assertEquals("", m2.get("key"));
    assertTrue(m2.containsKey("key"));
  }

  /**
  * This test verifies that the LinkedHashMap is used, so that
  * the insertion order of the keys is preserved.
  */
  @Test
  void testParsePreservesInsertionOrder() {
    String json = "{\"z\":\"1\",\"a\":\"2\",\"m\":\"3\"}";
    Map<String, String> m = FlatJson.parse(json);

    List<String> keys = new ArrayList<>(m.keySet());
    assertEquals("z", keys.get(0));
    assertEquals("a", keys.get(1));
    assertEquals("m", keys.get(2));
  }

  /**
  * This test verifies that the parser handles multiple broken entries
  * in the same JSON-object without it affecting valid entries.
  */
  @Test
  void testParseIgnoresMultipleBrokenEntries() {
    String json = "{\"a\":\"b\", \"broken1\" , \"c\":\"d\", \"broken2\", \"e\":\"f\"}";
    Map<String, String> m = FlatJson.parse(json);

    assertEquals("b", m.get("a"));
    assertEquals("d", m.get("c"));
    assertEquals("f", m.get("e"));
    assertEquals(3, m.size());
  }



}
