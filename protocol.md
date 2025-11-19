# Protocol Description
## Introduction
This document describes the custom application-layer communication protocol
designed for the Smart Farm System developed by our group as part of the 
IDATA2304 course project, at NTNU.
The protocol enables communication between sensor/actuator nodes and control-panel
nodes. Is purpose is to ensure reliable data transfer of sensor readings (such as temperature and
humidity) and commands across a network.

The protocol works over TCP sockets, ensuring reliable, ordered and error-checked
delivery of messages. It defines message formats, value types, and mechanisms for detecting and handling network
or message errors to maintain robust communication.

## Terminology

### Core Components
- **Node**: A network-connected device in the system (either a sensor node or control-panel node)
- **Sensor Node**: A device that monitors environmental conditions using sensors (e.g., temperature, humidity, wind speed) and controls them using actuators (e.g., fan, heater, window opener)
- **Control-Panel Node**: A node providing a user interface for users to view sensor data and send actuator commands
- **Broker**: Central server component that relays messages between sensor nodes and control panels

### Devices and Data
- **Sensor**: A device that measures environmental data (e.g., temperature sensor, humidity sensor, light sensor)
- **Actuator**: A controllable element on a sensor node that affects environmental conditions (e.g., fan, heater, window opener, valve)

### Protocol Concepts
- **Message**: A unit of communication between nodes, formatted as JSON and transmitted with a 4-byte length prefix
- **Registration**: The process where a node identifies itself to the broker and reports its capabilities (sensors/actuators)

### Network Standards
- **IANA**: Internet Assigned Numbers Authority (defines port number ranges)

## Transport Choice - TCP / UDP
We have chosen **TCP** as our transport protocol for the following reasons:

### Reliability
Our system requires messages to arrive. Sensor readings and commands to actuators cannot be lost, 
as this could lead to errors in the environmental control of the greenhouse. For example, a command to turn off a heater must
actually reach the sensor unit. TCP gives us guaranteed message delivery and automatic packet handling.

### Order
Commands must be executed in the correct order. If a control panel sends several commands one after the other (e.g. "turn off the heater",
"open the window, "turn on the fan"), these must be processed in the correct sequence. TCP ensures that messages are delivered in the same
order they were sent.

### Connection Management
Our protocol is stateful, where the broker keeps track of connected nodes. TCP gives us a natural way to know which sensor devices
are available through established connections. When a node disconnects, the broker immediately knows that it is no longer available.

###
Our hub-and-spoke architecture, where all communication goes through a central broker, fits well with the connection-oriented nature
of TCP. The broker must handle multiple simultaneous connections from both sensor devices and control panels, which is what TCP is designed for.

### Performance vs. Reliability
Although TCP has more overhead than UDP, the data volumes in our system (sensor measurements and commands) are relatively modest.
The system does not require extremely low latency, so TCP's millisecond delays are acceptable for greenhouse monitoring and control.

### Easier implementation
TCP handles retransmission, flow control and error detection automatically, which greatly simplifies our implementation. This allows us to
focus on the application logic instead of implementing reliability features at the application level.

## Port Number
Default TCP listening port (broker): 23048

- The selected port is within the IANA registered rage (1024 - 49151), meaning it is safe from ephemeral port conflicts.
- Not a commonly used service port (low collision risk)
- Choosen based on course code and group number: 2304 + 8

Configuration:
- Validation: Accept only ports in 1024 $\leq$ port $\leq$ 49151. Reject and exit on any value **outside** this range.

Client usage:
Both Sensor Nodes and Control-Panel Nodes connect to host:port 23048 (unless configured override).

## Architecture
### Actors (nodes)
- Sensor Nodes
- Control-Panel Nodes
- Broker

### Clients & Broker
- **Clients**: Both sensor nodes and control-panel nodes initiate TCP connections  
- **Broker**: Broker maintains sessions, routes messages, and tracks registered nodes

### Summary
The system uses a **hub-and-spoke architecture**:  
```text
+------------------+      TCP      +---------+      TCP      +----------------------+
|  Sensor Node(s)  | <-----------> | Broker   | <-----------> | Control-Panel Node(s)|
+------------------+                +---------+               +----------------------+
```
All communication flows through the hub/broker, which makes the system **scalable** to multiple sensors and multiple control panels.

## Flow of Information

### Communication Patterns
Our protocol implements multiple communication patterns to efficiently handle different types of information exchange:

### Registration Flow
- Connection-based: Nodes establish a TCP connection to the broker before any data exchange.
- Identification: Nodes sends registration messages containing their capabilities (sensors/actuators).
- Acknowledgement: Broker confirms registration with assigned node IDs.

### Sensor Data Transmission
- Push Model: Sensor nodes actively send data to the broker without being polled.
- Periodic Updates: Default interval of 12 seconds (configurable).
- Threshold-based: Additional updates when readings change significantly (e.g., temperature change > 1°C).
- Fan-out distribution: Broker forwards data to all connected control panels.

### Command Processing
- Request-Response: Commands follow a clear request → acknowledgement → status update sequence.
- Targeted routing: Commands specify exact target node and actuator.
- Execution confirmation: Two-phase feedback (command received + execution completed).

### Event Notifications
- Publish-Subscribe: Broker publishes node status events to all subscribed control panels.
- Immediate delivery: Status changes are communicated without delay.

### Message Timing

#### Scheduled Messages
- Sensor data: Transmitted at regular intervals (default every 5 seconds).
- heartbeat messages: Exchanged every 30 seconds to verify connection health.
- Node list updates: Sent after any change in node availability.

#### Event-Driven Messages
- Actuator commands: Sent immediately when triggered by user action.
- Status updates: Transmitted after actuator state changes.
- Error notifications: Sent when protocol violations or system errors occur.

#### Flow Control
- Rate limiting: Maximum message frequency enforced to prevent network congestion.
- Message prioritization: Command messages processed before routine updates.
- Connection management: Broker maintains persistent TCP connections to all nodes.

### Justification

This hybrid communication approach was selected to balance:
- Responsiveness: Critical commands are processed immediately.
- Efficiency: Regular updates avoid unnecessary polling overhead.
- Consistency: All control panels maintain synchronized system views.
- Scalability: Central broker handles message distribution to multiple clients.

The centralizes architecture simplifies implementation while providing clear message routing paths.
The push-based sensor data model ensures timely updates without requiring control panels to continuously request information.

## Protocol Type
- **Connection-oriented**: The protocol uses TCP, which establishes a dedicated connection between nodes and the broker before exchanging data. This ensures reliable, ordered delivery of messages.
- **Stateful**: The broker keeps track of connected nodes, their assigned identifiers, and available sensors/actuators. This state is necessary to support multiple nodes and to route commands from a specific control-panel node to the correct sensor node.


## Types and Special Values
The protocol uses JSON-formatted messages for structured data exchange.
### Message types
| Message Type           | Direction                    | Description                                              |
|------------------------|------------------------------|----------------------------------------------------------|
| REGISTER_NODE          | Sensor → Broker              | Sensor node registration request                         |
| REGISTER_CONTROL_PANEL | Control Panel → Broker       | Control panel registration request                       |
| REGISTER_ACK           | Broker → Client              | Registration acknowledgment                              |
| SENSOR_DATA            | Sensor → Broker → Panels     | Periodic sensor readings                                 |
| ACTUATOR_COMMAND       | Panel → Broker → Sensor      | Command to control an actuator                           |
| ACTUATOR_STATUS        | Sensor → Broker → Panels     | Current actuator state (periodic)                        |
| ACTUATOR_STATE         | Sensor → Broker → Panels     | Immediate actuator state update                          |
| COMMAND_ACK            | Sensor → Broker → Panel      | Command execution acknowledgment                         |
| NODE_CONNECTED         | Broker → Panels              | Notification when sensor node connects                   |
| NODE_DISCONNECTED      | Broker → Panels              | Notification when sensor node disconnects                |
| NODE_LIST              | Broker → Panel               | List of connected sensor nodes (on panel registration)   |
| HEARTBEAT              | Bidirectional                | Keep-alive message                                       |
| ERROR                  | Any → Any                    | Error notification                                       |

### Common Fields
| Field     | Description                                     | Example                             | 
|-----------|-------------------------------------------------|-------------------------------------|
| NODE_ID   | Unique identifier for each sensor/actuator node | "nodeId":"Sensor-1"                 |
| TYPE      | Sensor or actuator type                         | "type":"SENSOR_DATA"                |
| VALUE     | Sensor reading or actuator setting              | "value":"22.5"                      |
| STATUS    | Actuator state                                  | "status":"ON"                       |
| TIMESTAMP | UNIX timestamp of message                       | "timestamp":"2025-11-16T14:30:00Z"  |

## Message Format
- **Framing**: Each message is sent as **length-prefixed**: a 4-byte **big-endian** unsigned length `N`, followed by `N` bytes of payload.
- **Payload**: UTF-8 **JSON** object
- Simple to parse, efficient streaming and human-readable during development. 
### Allowed Message Types
The protocol defines a clear seperation between **data messages** and **command messages**.  
Each message includes a **type identifier** and a structured payload.  
|  Category 	|   Message Type	|  Direction 	|  Description 	|
|---	|---	|---	|---	|
|  Registration 	|  `REGISTER_NODE`, <br>`REGISTER_CONTROL_PANEL`, <br> `REGISTER_ACK`, `NODE_LIST`	|  Client &rarr; Broker (first two), Broker &rarr; Client (last two)   	|  Join/acknowledge and initial inventory. 	|
|  Sensor Data	|  `SENSOR_DATA`, `ACTUATOR_STATUS` 	|  Sensor &rarr; Broker &rarr; Control 	|  Periodic readings and actuator states. 	|
|  Commands 	|   `ACTUATOR_COMMAND`, `COMMAND_ACK`	|  Control &rarr; Broker &rarr; Sensor 	|  Actuator commands and acknowledgements 	|
|  Connection Events 	|  `NODE_CONNECTED`, `NODE_DISCONNECTED`  	|  Broker &rarr; Control 	|   Broadcast connectivity changes	|
|  Errors 	|  `ERROR` 	|  Any 	|   Application-level error reporting	|

### JSON Encoding
All messages use **JSON (JavaScript Object Notation)** format for structured, human-readable data exchange.

**Advantages of JSON:**
- **Structured:** Nested objects and arrays support complex data
- **Language-agnostic:** Parsers available for all platforms
- **Human-readable:** Easy to debug and log
- **Extensible:** New fields can be added without breaking existing parsers

### Framing
Each message is prefixed with a **4-byte big-endian length header** indicating the size of the JSON payload in bytes. This allows the receiver to know exactly how many bytes to read for each complete
message.

**Frame Structure:**
[4 bytes: length][N bytes: JSON payload]

**Example:**
Length: 0x00 0x00 0x00 0x4A (74 bytes)
Payload: {"type":"SENSOR_DATA","nodeId":"node-1","sensorKey":"temp","value":"22.5","unit":"°C","timestamp":"2025-11-16T14:30:00Z"}

### Message Examples

**SENSOR_DATA:**
```json
{"type":"SENSOR_DATA","nodeId":"sensor-node-abc123","sensorKey":"temp","value":"22.5","unit":"°C","timestamp":"2025-11-16T14:30:00Z"}

ACTUATOR_COMMAND:
{"type":"ACTUATOR_COMMAND","targetNode":"sensor-node-abc123","actuator":"heater","action":"ON","value":"25"}

HEARTBEAT:
{"type":"HEARTBEAT","direction":"SERVER_TO_CLIENT","protocolVersion":"1.0"}

REGISTER_ACK:
{"type":"REGISTER_ACK","nodeId":"sensor-node-abc123","message":"Registration successful"}
```

## Error handling
### Overview
Error handling in our protocolensures that communication remains **robust** and that  nodes can recover gracefully from common faults.  
Error can occur due to malformed messages, connection loss, or invalid commands. The protocol defiens a standard response mechanism using the `ERROR`message type.

### Error Categories

**NOTE: Error codes are currently not implemented in the protocol. ERROR messages contain only `type` and `message` fields. The codes below represents a planned feature we had to drop because of time limitations.

| Error type  | Code  | Typical Cause  | Handling Strategy  |
|---|---|---|---|
| **Malformed Message**   | `400`  |  Missing fields, wrong separators, or <br> unknown message type | The receiver ignores the message and sends <br> `ERROR;code=404;reason=InvalidMessageFormat` <br> to the sender.  |
| **Uknown command**  | `404`  | Command name or actuator not <br> recognized by the target node | The receiver replies with <br> `ERROR;code=404;reason=UknownCommand`  |
| **Unauthorized Action**  | `403`  | A control panel tries to control a <br> node it is not authorized for  | The broker rejects the request and <br> notifies the sender  |
| **Disconnected Node**  | `408`  |  A control panel issues a command <br> to an offline node  | The broker returns <br> `ERROR;code=408;reason=NodeUnavailable`  |
|  **Internal Server Error** | `500`  | Unexpected exceptions or state <br> corruption within the broker  | Server logs the error, notifies all <br> connected control panels, and keeps <br> running  |  

### Error Propagation
* **Local errors (non-critical)**: <br>
Handled at the node level (e.g., malformed data ignored). No connection reset is required.
* **Critical errors (connection-level)**: <br>
The broker may close the TCP connection and mark the node as **disconnected**. <br>
Control panels are notified via a `NODE_DISCONNECTED` message.

### Example Flows
**Example 1: Malformed message**
```json
SensorNode -> Broker: {"type":"REGISTER_NODE","role":"SENSOR_NODE","nodeId":"dev-1"}
Broker -> SensorNode: {"type":"REGISTER_ACK","protocolVersion":"1.0","message":"Registration successful"}
```
**Example 2: Node Unavailable**
```json
ControlPanel -> Broker: {"type":"REGISTER_CONTROL_PANEL","role":"CONTROL_PANEL","nodeId":"ui-1"}
Broker -> ControlPanel: {"type":"NODE_LIST", ...}
```

### Justification
Explicit error messages allow for **transparent debugging** and **predictable system behavior** without interrupting other active sessions.  
This approach ensures that communication errors affect only the problematic node, not the entire network.  
Since TCP already provides delivery guarantees, our error handling focuses on **application-level validation** and **state consistency**, keeping the system stable even under partial failures.

## Scenarioes 

### Scenario 1: Startup and Registration
When the system boots up, the following happens:

1. **The broker starts** and starts listening on port 23048

2. **Sensor nodes connect to the broker**:
   - Each sensor node establishes a TCP connection to the broker.
   - The sensor node sends a 'REGISTER_NODE' message with information about its sensors and actuators.
   - The broker stores the information and sends back an acknowledgement with the assigned node ID.
   - The sensor node start sending sensor data periodically.

3. **Control panel connects to the broker**:
   - The control panel establishes a TCP connection to the broker.
   - The control panel sends a 'REGISTER_CONTROL_PANEL' message.
   - The broker responds with a list of all registered sensor nodes and their capabilities.
   - The control panel displays this information to the user.

Message flow:
```text
SensorNode -> Broker: REGISTER_NODE {sensors:[temp, humidity], actuators:[fan, heater]}
Broker -> SensorNode: REGISTER_ACK {nodeId: 42}
SensorNode -> Broker: SENSOR_DATA {nodeId: 42, readings: {temp: 23,5, humidity: 65}}
ControlPanel -> Broker: REGISTER_CONTROL_PANEL
Broker -> ControlPanel: NODE_LIST {nodes: [{id: 42, sensors:[temp, humidity], actuators:[fan, heater]}]
```

### Scenario 2: Actuator control
A farmer notices that the temperatures in the greenhouse is too high and wants to open a window:

1. **User interacts with the control panel**:
   - The farmer sees on the control panel that the temperature is 28°C in the greenhouse 3.
   - The farmer selects sensor node 42 from the list and clicks "Open window"

2. **Command sent to the broker**:
   - The control panel sends and 'ACTUATORS_COMMAND' message to the broker.
   - The broker forwards the command to the specific sensor unit.

3. **Sensor node executes the command**:
   - Sensor node 42 receives the command and activates the window opener.
   - Sensor node 42 sends a status update back to the broker.
   - The broker forwards the status update to the control panel.
   - The control panel updates the user interface to show that the window is open.

Message flow:
```text
ControlPanel -> Broker: ACTUATOR_COMMAND {targetNode: 42, actuator: "window", action: "open"}
Broker -> SensorNode: ACTUATOR_COMMAND {actuator: "window", action: "open"}
SensorNode -> Broker: ACTUATOR_STATUS {nodeId: 42, actuator: "window", status: "open"}
Broker -> ControlPanel: ACTUATOR_STATUS {nodeId: 42, actuator: "window", status: "open"}
```   

### Scenario 3: Handling a disconnected node
A sensor node loses power or network connection:

1. **TCP connection broken**:
    - The broker detects that the TCP connection to sensor node 42 is broken.
    - The broker marks the node as disconnected in its internal state.

2. **Update to control panels**:
    - The broker sends a 'NODE_DISCONNECTED' message to all connected control panels.
    - The control panels update their user interfaces to show that the node is unavailable.
    - Any commands to the disconnected node will be rejected.

3. **Reconnect**:
    - When sensor node 42 regains power/network, it reconnects to the broker.
    - The registration process is repeated as in Scenario 1.
    - The broker sends a 'NODE_CONNECTED' message to all control panels.
    - The control panels update the user interface to show that the node is available again.

Message flow:
```text
Broker -> ControlPanel: NODE_DISCONNECTED {nodeId: 42}

[Later, when the node reconnects]

SensorNode -> Broker: REGISTER_NODE {sensors: [temp, humidity], actuators: [fan, heater]}
Broker -> SensorNode: REGISTER_ACK {nodeId: 42}
Broker -> ControlPanel: NODE_CONNECTED {nodeId: 42, sensors: [temp, humidity], actuators: [fan, heater]}
```   

## Reliability mechanisms
The current version of the protocol does not yet include reliability mechanisms beyond
what is provided by the underlying Transport layer(TCP). Several mechanisms are planned for future
implementation to improve robustness and fault tolerance.

## Current status
**Currently Implemented:**
- HEARTBEAT messages (30-second intervals)
- NODE_CONNECTED/NODE_DISCONNECTED events
- ERROR messages (without error codes)
- COMMAND_ACK for actuator command acknowledgment

## Planned, but had to drop:
- ACK/NACK with automatic retransmission
- Message sequence numbers
- Buffered data and resend on reconnect

## Security
- **Initial phase**: No authentication or encryption. The system runs in a trusted environment. Future development might readjust this.
