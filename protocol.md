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
- **Sensor Node**: Devices that generate simulated **sensor data** (e.g., temperature, humidity, wind speed, etc.) and use **actuators** to change them
- **Actuator**: Controllable element on a sensor node (e.g., fan, heater, window opener, etc.)
- **Control-Panel Node**: Node providing a user interface to view data and send commands
- **Server**: Central component to relay messages between sensor nodes and control panels
- **IANA**: Internet Assigned Numbers Authority

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
Our protocol is stateful, where the server keeps track of connected nodes. TCP gives us a natural way to know which sensor devices
are available through established connections. When a node disconnects, the server immediately knows that it is no longer available.

###
Our hub-and-spoke architecture, where all communication goes through a central server, fits well with the connection-oriented nature
of TCP. The server must handle multiple simultaneous connections from both sensor devices and control panels, which is what TCP is designed for.

### Performance vs. Reliability
Although TCP has more overhead than UDP, the data volumes in our system (sensor measurements and commands) are relatively modest.
The system does not require extremely low latency, so TCP's millisecond delays are acceptable for greenhouse monitoring and control.

### Easier implementation
TCP handles retransmission, flow control and error detection automatically, which greatly simplifies our implementation. This allows us to
focus on the application logic instead of implementing reliability features at the application level.

## Port Number
Default TCP listening port (server/broker): 23048

- The selected port is within the IANA registered rage (1024 - 49151), meaning it is safe from ephemeral port conflicts.
- Not a commonly used service port (low collision risk)
- Choosen based on course code and group number: 2304 + 8

Configuration:
- Validation: Reject port if 1024 $\leq$ port $\leq$ 49151, with error message

Client usage:
Both Sensor Nodes and Control-Panel Nodes connect to host:port 23048 (unless configured override).

## Architecture
### Actors (nodes)
- Sensor Nodes
- Control-Panel Nodes
- Server

### Clients & Server
- **Clients**: Both sensor nodes and control-panel nodes initiate TCP connections  
- **Server**: Server maintains sessions, routes messages, and tracks registered nodes

### Summary
The system uses a **hub-and-spoke architecture**:  
```text
+------------------+      TCP      +---------+      TCP      +----------------------+
|  Sensor Node(s)  | <-----------> | Server  | <-----------> | Control-Panel Node(s)|
+------------------+                +---------+               +----------------------+
```
All communication flows through the hub/server, which makes the system **scalable** to multiple sensors and multiple control panels.

## Flow of Information

### Communication Patterns
Our protocol implements multiple communication patterns to efficiently handle different types of information exchange:

### Registration Flow
- Connection-based: Nodes establish a TCP connection to the server before any data exchange.
- Identification: Nodes sends registration messages containing their capabilities (sensors/actuators).
- Acknowledgement: Server confirms registration with assigned node IDs.

### Sensor Data Transmission
- Push Model: Sensor nodes actively send data to the server without being polled.
- Periodic Updates: Default interval of 5 seconds (configurable).
- Threshold-based: Additional updates when readings change significantly (e.g., temperature change > 1°C).
- Fan-out distribution: Server forwards data to all connected control panels.

### Command Processing
- Request-Response: Commands follow a clear request → acknowledgement → status update sequence.
- Targeted routing: Commands specify exact target node and actuator.
- Execution confirmation: Two-phase feedback (command received + execution completed).

### Event Notifications
- Publish-Subscribe: Server publishes node status events to all subscribed control panels.
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
- Connection management: Server maintains persistent TCP connections to all nodes.

### Justification

This hybrid communication approach was selected to balance:
- Responsiveness: Critical commands are processed immediately.
- Efficiency: Regular updates avoid unnecessary polling overhead.
- Consistency: All control panels maintain synchronized system views.
- Scalability: Central server handles message distribution to multiple clients.

The centralizes architecture simplifies implementation while providing clear message routing paths.
The push-based sensor data model ensures timely updates without requiring control panels to continuously request information.

## Protocol Type
- **Connection-oriented**: The protocol uses TCP, which establishes a dedicated connection between nodes and the server before exchanging data. This ensures reliable, ordered delivery of messages.
- **Stateful**: The server keeps track of connected nodes, their assigned identifiers, and available sensors/actuators. This state is necessary to support multiple nodes and to route commands from a specific control-panel node to the correct sensor node.


## Types and Special Values
The protocol uses text-based messages separated by semicolons. Each message has a message type
identifier, followed by Key-value pairs that describes the content.
### Message types
| Type            | Direction          | Description                                         |
|-----------------|--------------------|-----------------------------------------------------|
| SENSOR_DATA     | Sensor → Control   | Periodic sensor readings from sensor node           |
| ACTUATOR_STATUS | Sensor → Control   | Current state of actuator                           |
| COMMAND         | Control → Sensor   | Command to control an actuator (TURN_ON, SET_LEVEL) |
| ACK             | Sensor → Control   | Acknowledgment of successful command execution       |
| ERROR           | Any → Any          | Indicates invalid message or command failure         |

### Common Fields
| Field     | Description                                     | Example               | 
|-----------|-------------------------------------------------|-----------------------|
| NODE_ID   | Unique identifier for each sensor/actuator node | NODE_ID=7             |
| TYPE      | Sensor or actuator type                         | TYPE?TEMP or TYPE=FAN |
| VALUE     | Sensor reading or actuator setting              | VALUE=25.6            |
| STATUS    | Actuator state                                  | STATUS=ON             |
| TIMESTAMP | UNIX timestamp of message                       | TIMESTAMP=1728439923  |

### Special Values
| Constant    | Meaning                                         | 
|-------------|-------------------------------------------------|
| OK          | Unique identifier for each sensor/actuator node | 
| FAIL        | Sensor or actuator type                         |
| NA          | Sensor reading or actuator setting              | 
| ALL         | Actuator state                                  | 
| ERR_FORMAT  | UNIX timestamp of message                       |
| ERR_UNK_CMD | Unknown command received                        |

## Message Format
### Allowed Message Types
The protocol defines a clear seperation between **data messages** and **command messages**.  
Each message includes a **type identifier** and a structured payload.  
|  Category 	|   Message Type	|  Direction 	|  Description 	|
|---	|---	|---	|---	|
|  Registration 	|  `REGISTER_NODE`, <br>`REGISTER_CONTROL_PANEL`, <br> `REGISTER_ACK`, `NODE_LIST`	|  Sensor/Control &rarr; Server 	|  Used when nodes join or reconnect to the system. 	|
|  Sensor Data	|  `SENSOR_DATA`, `ACTUATOR_STATUS` 	|  Sensor &rarr; Server &rarr; Control 	|  Periodic updates containing sensor reading and actuator states 	|
|  Commands 	|   `ACTUATOR_COMMAND`, `COMMAND_ACK`	|  Control &rarr; Server &rarr; Sensor 	|  Commands sent by the control panel to trigger actuator actions 	|
|  Connection Events 	|  `NODE_CONNECTED`, `NODE_DISCONNECTED`  	|  Server &rarr; Control 	|   Used to update all control panels when node connectivity changes	|
|  Errors 	|  `ERROR` 	|  Any 	|   Sent when invalid data ir an unknown command is received	|

### Marshalling and Encoding
* **Encoding type**: Seperator-based (human-readable) text format
* **Seperator**: Semicolon(`;`) between key-value pairs
* **Rationale**: Easy to parse and debug during testing. Avoid binary complexity

**Example Messages:**
```text
SENSOR_DATA;nodeId=42;temp=22.4;humidity=55;status=OK
ACTUATOR_COMMAND;targetNode=42;actuator=fan;action=ON
ACTUATOR_STATUS;nodeId=42;actuator=fan;status=ON
ERROR;code=400;reason=InvalidMessageFormat
```
If future efficiency improvements are needed, the format can be upgraded to **TLV (Type-Length-Value)** or binary marshalling without changing the logical structure of the protocol.

### Message Direction
|  Node Type | Sends  | Receives  |
|---|---|---|
| Sensor Node  | `REGISTER_NODE`, `SENSOR_DATA`, <br> `ACTUATOR_STATUS`  |  `ACTUATOR_COMMAND`, `REGISTER_ACK` |
|  Control Panel | `REGISTER_CONTROL_PANEL`, <br> `ACTUATOR_COMMAND`  | `NODE_LIST`, `SENSOR_DATA`, `ACTUATOR_STATUS`, <br> `COMMAND_ACK`, `ERROR`  |
|  Server | Routes all messages  |  Receives all messages and forwards appropriately |

### Structure Summary
Each message follows a consistent pattern:
```text
<MESSAGE_TYPE>;<field1>=<value1>;<field2>=<value2>;...;<fieldN>=<valueN>
```
**Example communication flow**:
```text
ControlPanel → Server → SensorNode:
ACTUATOR_COMMAND;targetNode=42;actuator=window;action=open

SensorNode → Server → ControlPanel:
ACTUATOR_STATUS;nodeId=42;actuator=window;status=open
```

### Justification
We chose a **seperator-based**, **text-oriented message format** because it offers high **readability** and **low complexity** during development and debugging. It allows straightforward inspection of network traffic with tools like wireshar or simple console logs.  
Although binary protocols may achieve higher throughput, the small message size and moderate update frequency in our greenhouse simulation make human-readable encoding ideal.  
Additionally, this format ensures **extensibility**, allowing new sensors, actuators, or fields to be added later without breaking existing implementations.

## Error handling
### Overview
Error handling in our protocolensures that communication remains **robust** and that  nodes can recover gracefully from common faults.  
Error can occur due to malformed messages, connection loss, or invalid commands. The protocol defiens a standard response mechanism using the `ERROR`message type.

### Error Categories
| Error type  | Code  | Typical Cause  | Handling Strategy  |
|---|---|---|---|
| **Malformed Message**   | `400`  |  Missing fields, wrong seperators, or <br> unknown message type | The receiver ignores the message and sends <br> `ERROR;code=404;reason=InvalidMessageFormat` <br> to the sender.  |
| **Uknown command**  | `404`  | Command name or actuator not <br> recognized by the target node | The receiver replies with <br> `ERROR;code=404;reason=UknownCommand`  |
| **Unauthorized Action**  | `403`  | A control panel tries to control a <br> node it is not authorized for  | The server rejects the request and <br> notifies the sender  |
| **Disconnected Node**  | `408`  |  A control panel issues a command <br> to an offline node  | The server returns <br> `ERROR;code=408;reason=NodeUnavailable`  |
|  **Internal Server Error** | `500`  | Unexpected exceptions or state <br> corruption within the server  | Server logs the error, notifies all <br> connected control panels, and keeps <br> running  |  

### Error Propagation
* **Local errors (non-critical)**: <br>
Handled at the node level (e.g., malformed data ignored). No connection reset is required.
* **Critical errors (connection-level)**: <br>
The server may close the TCP connection and mark the node as **disconnected**. <br>
Control panels are notified via a `NODE_DISCONNECTED` message.

### Example Flows
**Example 1: Malformed message**
```text
SensorNode → Server: SENSOR_DATA nodeId=42 temp=22.4   (missing separators)
Server → SensorNode: ERROR;code=400;reason=InvalidMessageFormat
```
**Example 2: Node Unavailable**
```text
ControlPanel → Server: ACTUATOR_COMMAND;targetNode=99;actuator=fan;action=ON
Server → ControlPanel: ERROR;code=408;reason=NodeUnavailable
```

### Justification
Explicit error messages allow for **transparent debugging** and **predictable system behavior** without interrupting other active sessions.  
This approach ensures that communication errors affect only the problematic node, not the entire network.  
Since TCP already provides delivery guarantees, our error handling focuses on **application-level validation** and **state consistency**, keeping the system stable even under partial failures.

## Scenarioes 

### Scenario 1: Startup and Registration
When the system boots up, the following happens:

1. **The server starts** and starts listening on port 23048

2. **Sensor nodes connect to the server**:
   - Each sensor node establishes a TCP connection to the server.
   - The sensor node sends a 'REGISTER_NODE' message with information about its sensors and actuators.
   - The server stores the information and sends back an acknowledgement with the assigned node ID.
   - The sensor node start sending sensor data periodically.

3. **Control panel connects to the server**:
   - The control panel establishes a TCP connection to the server.
   - The control panel sends a 'REGISTER_CONTROL_PANEL' message.
   - The server responds with a list of all registered sensor nodes and their capabilities.
   - The control panel displays this information to the user.

Message flow:
```text
SensorNode -> Server: REGISTER_NODE {sensors:[temp, humidity], actuators:[fan, heater]}
Server -> SensorNode: REGISTER_ACK {nodeId: 42}
SensorNode -> Server: SENSOR_DATA {nodeId: 42, readings: {temp: 23,5, humidity: 65}}
ControlPanel -> Server: REGISTER_CONTROL_PANEL
Server -> ControlPanel: NODE_LIST {nodes: [{id: 42, sensors:[temp, humidity], actuators:[fan, heater]}]
```

### Scenario 2: Actuator control
A farmer notices that the temperatures in the greenhouse is too high and wants to open a window:

1. **User interacts with the control panel**:
   - The farmer sees on the control panel that the temperature is 28°C in the greenhouse 3.
   - The farmer selects sensor node 42 from the list and clicks "Open window"

2. **Command sent to the server**:
   - The control panel sends and 'ACTUATORS_COMMAND' message to the server.
   - The server forwards the command to the specific sensor unit.

3. **Sensor node executes the command**:
   - Sensor node 42 receives the command and activates the window opener.
   - Sensor node 42 sends a status update back to the server.
   - The server forwards the status update to the control panel.
   - The control panel updates the user interface to show that the window is open.

Message flow:
```text
ControlPanel -> Server: ACTUATOR_COMMAND {targetNode: 42, actuator: "window", action: "open"}
Server -> SensorNode: ACTUATOR_COMMAND {actuator: "window", action: "open"}
SensorNode -> Server: ACTUATOR_STATUS {nodeId: 42, actuator: "window", status: "open"}
Server -> ControlPanel: ACTUATOR_STATUS {nodeId: 42, actuator: "window", status: "open"}
```   

### Scenario 3: Handling a disconnected node
A sensor node loses power or network connection:

1. **TCP connection broken**:
    - The server detects that the TCP connection to sensor node 42 is broken.
    - The server marks the node as disconnected in its internal state.

2. **Update to control panels**:
    - The server sends a 'NODE_DISCONNECTED' message to all connected control panels.
    - The control panels update their user interfaces to show that the node is unavailable.
    - Any commands to the disconnected node will be rejected.

3. **Reconnect**:
    - When sensor node 42 regains power/network, it reconnects to the server.
    - The registration process is repeated as in Scenario 1.
    - The server sends a 'NODE_CONNECTED' message to all control panels.
    - The control panels update the user interface to show that the node is available again.

Message flow:
```text
Server -> ControlPanel: NODE_DISCONNECTED {nodeId: 42}

[Later, when the node reconnects]

SensorNode -> Server: REGISTER_NODE {sensors: [temp, humidity], actuators: [fan, heater]}
Server -> SensorNode: REGISTER_ACK {nodeId: 42}
Server -> ControlPanel: NODE_CONNECTED {nodeId: 42, sensors: [temp, humidity], actuators: [fan, heater]}
```   

## Reliability mechanisms
The current version of the protocol does not yet include reliability mechanisms beyond
what is provided by the underlying Transport layer(TCP). Several mechanisms are planned for future
implementation to improve robustness and fault tolerance.
### Planned Reliability Features
- **Acknowledgement(ACK/NACK) System**:
   - Each command message will require an explicit acknowledgment(ACK) from the receiver.
   - If no acknowledgment is received within a specifies timeout, the sender will
    automatically retransmit the message up to a limited number of times.
  
- **Heartbeat / Keep-Alive Messages**:
   - Sensor and control nodes will periodically send small "heartbeat" packets to confirm active connections.
   - If a node does not respond within a certain timeframe, the server will mark is as disconnected/offline,
    and reconnection attempts will begin.
  
- **Message Sequence Numbers**:
    - Each message will include a unique sequence number.
    - This allows detection of duplication, lost, or out-of-order messages, in case of temporary network issues.
  
- **Buffered Data and Resend on Reconnect**
    - Sensor nodes will temporarily buffer unsent data.
    - When the connection to the control node is restored all buffered sensor readings will be
    retransmitted to ensure no data is lost.

- **Error Handling and Recovery**
    - When malformed or invalid messages are received, the node will respond with an ERROR message
    indicating the issue stated as an error code.
    - This ensures both sides can handle unexpected communication.

## Security
- **Initial phase**: No authentication or encryption. The system runs in a trusted environment. Future development might readjust this.
