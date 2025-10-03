# Protocol Description
## Introduction
Short introduction

## Terminology
- **Sensor Node**: Devices that generate simulated **sensor data** (e.g., temperature, humidity, wind speed, etc.) and use **actuators** to change them
- **Actuator**: Controllable element on a sensor node (e.g., fan, heater, window opener, etc.)
- **Control-Panel Node**: Node providing a user interface to view data and send commands
- **Server**: Central component to relay messages between sensor nodes and control panels

## Transport Choice - TCP / UDP
//TODO: Hvorfor?
* TCP 

## Port Number
The used port number

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
* The allowed message types (sensor messages, command messages)
* The type of marshalling used (fixed size, seperators, TLV?)
* Whitch messages are send by whitch node? For example, are some messages only sent by the control-panel node?

## Error handling
The different errors that can occur and how each node should react on the errors. For example, what if a message in an unexpected format is reveived? Is it ignored, or does the recipient send a reply with an error code?

## Scenarioes 
Describe a realistic scenario: What would happend from a user perspective and what messages would be sent over the network?

## Reliability mechanisms
The reliability mechanisms in your protocol (handling of network errors), if you have any

## Security
- **Initial phase**: No authentication or encryption. The system runs in a trusted environment. Future development might readjust this.
