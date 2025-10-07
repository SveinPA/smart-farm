# Protocol Description
## Introduction
Short introduction

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
When and how the messages are sent?

## Protocol Type
- **Connection-oriented**: The protocol uses TCP, which establishes a dedicated connection between nodes and the server before exchanging data. This ensures reliable, ordered delivery of messages.
- **Stateful**: The server keeps track of connected nodes, their assigned identifiers, and available sensors/actuators. This state is necessary to support multiple nodes and to route commands from a specific control-panel node to the correct sensor node.


## Types and Special Values
The different types and special values (constants) used

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
The different errors that can occur and how each node should react on the errors. For example, what if a message in an unexpected format is reveived? Is it ignored, or does the recipient send a reply with an error code?

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
The reliability mechanisms in your protocol (handling of network errors), if you have any

## Security
- **Initial phase**: No authentication or encryption. The system runs in a trusted environment. Future development might readjust this.
