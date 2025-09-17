# Protocol Description
## Introduction
Short introduction

## Terminology
A list of special terms you use

## Transport Choice - TCP / UDP
What underlying transport did we choose and why

## Port Number
The used port number

## Architecture
* Who are the actors (nodes) in your solution?
* Who are the clients, who is/are the server(s)?

## Flow of Information
When and how the messages are sent?

## Protocol Type
* Is your protocol connection-oriented or connection-less?
* Is the protoccol state-full or state-less?

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
The security mechanisms in you protocol, if you have any