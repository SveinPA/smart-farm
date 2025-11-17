# Sprint Report + Retrospective - Week [42-43]

**Team [Group 8]|[Smart farm]**

## Sprint Overview

- **Sprint period:** 12.10.2025 - 27.10.2025
- **Sprint goal:** Establish a working module foundation with broker network layer, logging infrastructure, sensor/actuator implementations, and GUI framework 


- **Group members:** 👥 
- Ida Cathrin Bertheussen Soldal
- Mona Løvlie Tønsager Amundsen
- Svein Åge Pedersen Antonsen
- Andrea Sandnes


- **Project:** 🌱 [Smart farm] 

## Completed Work
### Finished tasks
- [x] **Task 1: Broker Network Layer**
Implemented TCP server with basic connection handling, verified through unit and integration tests. FrameCodec for length-prefixed message framing completed. No live sensor node connection yet (Planned for sprint 3).
- [x] **Task 2: Logging Infrastructrue**
Implemented SLF4J + Logback logging framework in `common` module. Ready to be adopted by all team members across `broker`, `sensor-node`, and `control-panel` modules for consistent logging.
- [x] **Task 3: Sensors and Actuators** 
Implemented initial Sensors and Actuators. Implementation verified by comprehensive unit tests.
- [x] **Task 4: GUI Framework** 
JavaFX application structure established with main window and basic layout. UI components ready but not yet connected to network layer.

## Not completed tasks:
- **Issue 47**: DashboardController (GUI) - Blocked by network integration dependencies
- **Issue 38**: ActuatorType(Enum) - Deprioritized in favor of class-based approch
- **Issue 48**: NodeViewModel(GUI) - Requires working network connection to implement properly

These are moved back to `Backlog` for Sprint 3.


## Sprint Goals - status
** GOAL MET:** All core modules now have working foundations
- Broker: TCP server operational with test coverage
- Common: Logging and protocol utilities in place
- Sensor-node: Initial sensor/actuators types implemented
- Control-panel: JavaFX framework established

** Next milestone:** Connect the modules (Sprint 3)

## Retrospective
### What went well?
- Better sprint planning with clearer task breakdown resulted in smoother workflow and fewer dependencies conflicts
- All critical foundational work completed - every module now has a solid base to build on
- Test-driven approach caught issues early (broker connection handling, sensor edge cases)

### What can be improved?
- **Planning:** Need to identify task dependencies earlier (e.g., GUI controllers blocked by network layer)
- **Documentation:** Outdated documentation - Agree on a better structure for documentation moving forward.

### Action items for next sprint:
1. **SensorNode-Broker Connection:** Implement NodeAgent to establish live TCP connection and registration flow
2. **GUI Network Integration:** Connect control panel to broker, implement message reception and display
3. **Broker Message Routing:** Extend broker to handle SENSOR_DATA and ACTUATOR_COMMAND message types
4. **Logger Adoption:** Add logging statements to all classes for debugging network issues

## Blockers and Challenges during the sprint

 - **TCP Socket Complexity:** Managing client connections, handling disconnections gracefully, and implementing the length-prefixed framing protocol proved more challenging than anticipated. Required
  significant research and multiple refactoring iterations.
- **Technology Stack Knowledge Gap:** Second time implementing a custom TCP protocol for most team members. Spent considerable time understanding concepts like framing, message parsing, and concurrent
  client handling.


## Next Sprint Focus 

- **Priority tasks:** 
  1. Add `Logger`
  2. Establish live sensor-broker connection
  3. Connect GUI to broker for data display

- **Expected challenges:** 
  - Thread safety in broker (concurrent client handling)
  - JavaFX threading (UI updates from network thread)

- **Focus areas:**
  - End-to-end integration
  - Network communication stability
  - Error handling and connection recovery

--- 
**Reported by:** Svein Antonsen/Group 8  
**Date:** 27.10.2025