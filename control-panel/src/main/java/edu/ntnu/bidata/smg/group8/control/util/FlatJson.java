package edu.ntnu.bidata.smg.group8.control.util;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
* A tiny, dependency-free JSON helper that parses flat JSON objects
* where all values are strings.
*
* <p>What this parser supports:</p>
* <ul>
*     <li>Flat JSON objects only (no nesting/arrays)</li>
*     <li>String keys and String values (numbers/booleans must be strings)</li>
*     <li>Preserves insertion order (LinkedHashMap)</li>
* </ul>
*
* <p>Supported input examples:</p>
* <pre>
*     {"type":"SENSOR_DATA","nodeId":"7","sensorKey":"temperature"}
* </pre>
*
* <p>Typical use-case: Parsing application-layer protocol messages
* where we control the JSON shape an keep it flat to avoid heavy Json
* libraries.</p>
*
* @author Andrea Sandes
* @version 30.10.25
*/
public final class FlatJson {

  private FlatJson() {}

  /**
  * Parses a flat JSON object into a map of string keys to string values.
  *
  * <p>Rules:</p>
  * <ul>
  *     <li>Returns an empty map for null, empty, or non-object input</li>
  *     <li>Trims whitespace around keys and values</li>
  *     <li>Unquotes and minimally unescapes string values</li>
  *     <li>Entries without a top-level colon (:) are ignored</li>
  * </ul>
  *
  * @param json the JSON string to parse
  * @return a LinkedHashMap preserving the order of entries
  */
  public static Map<String, String> parse(String json) {
    Map<String, String> out = new LinkedHashMap<>();

    if (json == null) {
      return out;
    }

    String object = json.trim();
    if (object.length() < 2 || object.charAt(0) != '{'
            || object.charAt(object.length() - 1) != '}') {
      return out;
    }

    String content = object.substring(1, object.length() - 1).trim();
    if (content.isEmpty()) {
        return out;
    }

    List<String> entries = splitTopLevelByComma(content);

    for (String entry : entries)  {
      String[] kv = splitFirstTopLevelColon(entry);
      if (kv == null) {
        continue;
      }

      String rawKey = kv[0].trim();
      String rawValue = kv[1].trim();

      String key = unquoteAndUnescape(rawKey);
      String value = unquoteAndUnescape(rawValue);

      if (key != null && value != null) {
        out.put(key, value);
      }
    }
    return out;
  }

  /**
  * Splits top-level entries by commas, ignoring commas inside quoted strings.
  */
  private static List<String> splitTopLevelByComma(String s) {
    List<String> parts = new ArrayList<>();
    StringBuilder token = new StringBuilder();
    boolean inString = false;

    for (int i = 0; i < s.length(); i++) {
      char c = s.charAt(i);
      if (c == '"' && (i == 0 || s.charAt(i - 1) != '\\')) {
        inString = !inString;
      }
      if (c == ',' && !inString) {
        parts.add(token.toString());
        token.setLength(0);
      } else {
        token.append(c);
      }
    }
    parts.add(token.toString());
    return parts;
  }

  /**
  * Splits an "entry" (key:value) into two parts on the first colon that
  * is not inside a string literal.
  *
  * @param entry a single key-value segment (e.g. " \"type\" : \"SENSOR_DATA\" ")
  * @return String[2] => {leftOfColon, rightOfColon}, or null if no top-level colon found
  */
  private static String[] splitFirstTopLevelColon(String entry) {
    boolean inString = false;

    for (int i = 0; i < entry.length(); i++) {
      char c = entry.charAt(i);

      if (c == '"' && (i == 0 || entry.charAt(i - 1) != '\\')) {
        inString = !inString;
      }
      if (c == ':' && !inString) {
        String left = entry.substring(0, i);
        String right = entry.substring(i + 1);
        return new String[]{left, right};
      }
    }
    return null;
  }

  /**
  * If the input is a quoted string, strips the quotes and unescapes a small
  * subset of JSON escapes (\", \\, \n, \r, \t, \b). If it is not quoted,
  * returns the trimmed input as-is.
  *
  * @param text the raw key or value text (possibly quoted)
  * @return the unquoted and minimally unescaped string; never null
  */
  private static String unquoteAndUnescape(String text) {
    String s = text.trim();

    if (s.length() >= 2 && s.charAt(0) == '"' && s.charAt(s.length() - 1) == '"') {
      s = s.substring(1, s.length() - 1);

      // unescape minimal set
      s = s.replace("\\\"", "\"")
            .replace("\\\\", "\\")
            .replace("\\n", "\n")
            .replace("\\r", "\r")
            .replace("\\t", "\t")
            .replace("\\b", "\b");
    }
    return s;
  }
}