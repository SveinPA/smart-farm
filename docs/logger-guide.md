# Smart-Farm Logging guide

## Purpose
We use a shared **SLF4J** + **Logback** logging setup to ensure all modules log in a consistent and readable way.  
The `AppLogger` class in the **common/util** package is a simple helper that standardizes how loggers are created.

## Why use `AppLogger`?
- Provides a **single entry point** for all logging.
- Keeps the code consistent across modules.
- Allows future upgrades without changing every class (Low coupling)
- Automatically uses the shared configuration form `common/src/main/resources/logback.xml`.

## Usage
### 1. Import and initialize
At the top of your class:
```java
import edu.ntnu.bidata.smg.group8.common.util.AppLogger;
import org.slf4j.Logger

public class ExampleClass {
  private static final Logger log = AppLogger.get(ExampleClass.class);
}
```

### 2. Log messages
Use the SLF4J pattern with `{}`placeholders:
```java
log.info("Starting sensor node on port {}", port);
log.warn("Connection dropped for node {}", nodeId);
log.error("Failed to read data from sensor {}", sensorName, exception);
log.degbug("Parsed message: {}", json);
```

## Logging levels (When to use what)

| Level  | When to use  | Example  |
|---|---|---|
| TRACE  | Very detailed internal steps, <br> temporary debugging  | `log.trace("Entering parse loop with {} bytes", buffer.length);`  |
| DEBUG  | Useful debug info during <br> development  | `log.debug("Received {} sensor updates", count);`  |
| INFO  | Normal system events, startup/<br>shutdowns, success logs  | `log.info("Broker started on port {}", port);`  |
| WARN  | Recoverable issues, unexpected but<br> not fatal  | `log.warn("Client {} sent malformed message", id);`  |
| ERROR  | Serious problems or failures  | `log.error("Unhandled exception in TcpServer", ex);`  |

## Configuration
- **File:** `common/src/main/resources/logback.xml`
- **Default Level:** INFO
- **Console format:**
```ruby
HH:mm:ss.SSS [thread] LEVEL logger - message
```
You can temporarily change log level by editing `logback.xml`:
```xml
<root level="DEBUG">
  <appender-ref ref="CONSOLE" />
</root>
```

### What gets displayed in console using different `level` config
| Root Config  | Logs displayed  |
|---|---|
| `TRACE`  | TRACE, DEBUG, INFO, WARN, ERROR  |
| `DEBUG`  | DEBUG, INFO, WARN, ERROR  |
| `INFO`  | INFO, WARN, ERROR  |
| `WARN`  | WARN, ERROR  |
| `ERROR`  | ERROR  |

## Common pitfalls
- Don't use `system.out.println()` --> Always use the logger.
- Don't build strings manually (`"Value: " + value`) --> use `{}` placeholders
- Don't log sensitive data (credentials, tokens, etc.).
- Do keep messages short, structured, and clear.

## Examples across modules

### BrokerMain.java
```java
private static final Logger log = AppLogger.get(BrokerMain.class);

log.info("Broker starting on port {}", port);
```

### SensorNode.java
```java
private static final Logger log = AppLogger.get(SensorNode.class);

log.debug("Sending sensor reading: {}", reading);
```

### ControlPanel.java
```java
private static final Logger log = AppLogger.get("ControlPanelUI");

log.warn("Lost connection to broker");
```

## Summary
- Always get loggers through `AppLogger.get(...)`
- Keep logging meaningful, not verbose
- One logback config for all modules = cleaner debugging