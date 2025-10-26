package edu.ntnu.bidata.smg.group8.common.util;

/**
 * Minimal helper for building flat JSON objects without extra libraries.
 * Only supports string key/value pairs for simple protocol messages.
 *
 * Example:
 *   String msg = JsonBuilder.build(
 *       "type", "REGISTER_ACK",
 *       "protocolVersion", "1.0",
 *       "message", "Registration successful"
 *   );
 */
public final class JsonBuilder {
    private JsonBuilder() {}

    /**
     * Builds a simple JSON object string from alternating key/value pairs.
     * Example: build("type", "HELLO", "nodeId", "42")
     * -> {"type":"HELLO","nodeId":"42"}
     */
    public static String build(String... kvPairs) {
        if (kvPairs.length % 2 != 0) {
            throw new IllegalArgumentException("Expected even number of arguments (key/value pairs)");
        }

        StringBuilder sb = new StringBuilder("{");
        for (int i = 0; i < kvPairs.length; i += 2) {
            if (i > 0) sb.append(',');
            sb.append('"').append(escape(kvPairs[i])).append('"')
              .append(':')
              .append('"').append(escape(kvPairs[i + 1])).append('"');
        }
        sb.append('}');
        return sb.toString();
    }

    /** Escapes basic JSON special characters in a value. */
    private static String escape(String s) {
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t")
                .replace("\b", "\\b");

    }
}

