# Smart Farm Control System 🌱 🏡
A distributed IoT-style system for monitoring and controlling a **simulated smart greenhouse.**
Built using **Java**, **TCP sockets**, and a custom lightweight **JSON protocol**.

**DEVELOPED BY GROUP 8**

## Project Overview 🔎
This system simulates a **smart farm** with sensor nodes that monitor environmental conditions and control actuators. A central broker manages communication between sensor nodes and control panels.

### Architecture

```
┌─────────────────┐         ┌─────────────┐         ┌─────────────────┐
│  Sensor Nodes   │ ◄─────► │   Broker    │ ◄─────► │ Control Panels  │
│                 │   TCP   │             │   TCP   │                 │
│ - Sensors       │         │  Message    │         │  - Visualization│
│ - Actuators     │         │  Router     │         │  - Commands     │
└─────────────────┘         └─────────────┘         └─────────────────┘
```

### Components

- **Sensor Nodes**: Simulate greenhouse environments with **sensors** (temperature, humidity, light, pH, fertilizer, wind) and **actuators** (fan, heater, window, valve)
- **Broker**: Central message router handling communication between nodes and control panels
- **Control Panel**: GUI for monitoring sensor data and controlling actuators

## Features 💡

- Real-time sensor data monitoring (**6** sensor types)
- Actuator control (**4** actuator types)
- Automatic node discovery
- Multi-threaded client handling
- Connection error handling
- Custom TCP-based protocol with JSON messages

## Extras ⭐
- Sensor data averaging (5-sample rolling window) to represent coarser time resolution **(EXTRA #6)**
- Automatic unique sensor node IDs **(EXTRA #3)**
- Flexible actuator commands — broadcast to ALL sensor nodes **(EXTRA #5)**
  - The control panel console (*not GUI*) allows sending actuator commands to *all* connected sensor nodes at once
  - **Usage:**
    ```
    all <actuator> <value>
    ```
  - **Example:**
    ```
    all fan 100
    ```


## Prerequisites ⚠️

- Java 21 or higher
- Maven 3.6 or higher

## Getting Started 🛠️

### 1. Build the Project ⚙️

```bash
mvn clean install
```

### 2. Start the Broker 🌐

```bash
mvn exec:java -pl broker
```

Expected output:
```
INFO  Broker starting on port 23048
INFO  TcpServer started; listening on port 23048
INFO  Broker started. Press Ctrl+C to exit.
```

### 3. Start Sensor Node(s) 🖥

In a new terminal, start a sensor node. You have two options:

#### Option A: Default Configuration (All Sensors)

```bash
mvn exec:java -pl sensor-node
```

This creates a node with all 6 sensor types (temp, humidity, light, pH, fertilizer, wind) and all 4 actuators.

#### Option B: Custom Sensor Configuration

```bash
mvn exec:java -pl sensor-node -Dexec.args="localhost 23048 my-node-id temp,hum"
```

**Arguments:**
- `localhost` - Broker hostname
- `23048` - Broker port
- `my-node-id` - Custom node identifier (or auto-generated if omitted)
- `temp,hum` - Comma-separated sensor types to include

**Available sensor types:** `temp`, `hum`, `light`, `ph`, `fert`, `wind`

**Examples:**

```bash
# Temperature and humidity only
mvn exec:java -pl sensor-node -Dexec.args="localhost 23048 greenhouse-1 temp,hum"

# Light sensor only (for outdoor monitoring)
mvn exec:java -pl sensor-node -Dexec.args="localhost 23048 outdoor-light light"

# Soil monitoring (pH and fertilizer)
mvn exec:java -pl sensor-node -Dexec.args="localhost 23048 soil-station ph,fert"
```

Expected output:
```
INFO  Configuration:
INFO    Node ID: sensor-node-XXXX
INFO    Broker: localhost:23048
INFO    Sensors: all (default)
INFO  Connected to broker at localhost:23048
INFO  Periodic tasks started (sensor data + actuator status every 12s, heartbeat every 30s)
```

### 4. Start the Control Panel 🎛️

In a new terminal, **launch the JavaFX GUI:**

```bash
mvn -pl control-panel javafx:run
```

**Alternative (console mode for testing):**
```bash
mvn exec:java -pl control-panel
```

The GUI will open and automatically display connected sensor nodes with real-time data updates.

## Project Structure 🗂️

Simple overview of the project structure (**does not include all components**):
```
smart-farm/
├── common/              # Shared protocol and utilities
│   └── protocol/        # Message DTOs and parsers
├── broker/              # Central message broker
│   └── infra/network/   # TCP server and client handling
├── sensor-node/         # Sensor and actuator simulation
│   ├── entity/          # Sensor and actuator implementations
│   └── logic/           # Business logic and communication
└── control-panel/       # GUI application
│    ├── ui/              # JavaFX views and controllers
│    └── infra/network/   # Network communication
│
├── protocol.md           # Documentation of all message types and flows
├── README.md             # This file
└── pom.xml               # Maven multi-module configuration
```

## Protocol 📑

The system uses a custom TCP protocol with JSON messages. Each message is prefixed with a 4-byte length header.

### Main Message Types

- `REGISTER_SENSOR_NODE` / `REGISTER_CONTROL_PANEL` - Node registration
- `SENSOR_DATA` - Periodic sensor readings
- `ACTUATOR_STATUS` - Current actuator states
- `ACTUATOR_COMMAND` - Control actuator (from panel to node)
- `HEARTBEAT` - Keep-alive messages
- `NODE_CONNECTED` / `NODE_DISCONNECTED` - Node lifecycle events

For more details, see **protocol.md**.

## Configuration 🧩

**Default** configuration:
- Broker port: `23048`
- Broker host: `localhost`
- Sensor update interval: **12** seconds
- Heartbeat interval: **30** seconds

## Testing 🔄

Run all tests:

```bash
mvn test
```

Run tests for specific module:

```bash
mvn test -pl common
mvn test -pl sensor-node
mvn test -pl broker
mvn test -pl control-panel
```

## Troubleshooting 🆘

⚠️ **Broker won't start - "Address already in use"**
- Another process is using port 23048
- Find and kill the process: `lsof -i :23048` (macOS/Linux) or change the port

⚠️ **Sensor node can't connect**
- Ensure broker is running first
- Check firewall settings
- Verify broker host/port configuration

⚠️ **Control panel shows "Waiting for sensor nodes..."**
- Ensure broker is running
- Start at least one sensor node
- Check that all components use the same port (23048 by default)

⚠️ **Connection lost during operation**
- Sensor nodes will automatically exit
- Restart the node to reconnect

⚠️ **My issue isn't listed here**
- Running `mvn clean install` again or `mvn clean compile` usually fixes most issues

## Authors 👤
Authors and their main responsibility areas:

- **10004 (LordManBat)** – Actuators, documentation, GUI
- **10050 (SveinPA)** – Broker, protocol, registry, message handling
- **10012 (Andsandn)** – Control panel UI/GUI, panel logic, communication
- **10024 (captnsundy)** – Sensor node logic, NodeAgent, documentation

## AI Assistance Declaration
This project was developed with assistance from various AI tools including Claude Code, ChatGPT, Ninja, and GitHub Copilot. AI assistance was used as a learning aid for code design, debugging, documentation, and testing strategies.
All code was written, reviewed, and understood by team members. Detailed AI usage information can be found in JavaDoc comments of relevant classes.

## License ©️
This project was created as part of the **IDATA2304 Computer Communication & Network Programming** course at **NTNU Ålesund.** It is intended for educational use in the context of the course assignment.