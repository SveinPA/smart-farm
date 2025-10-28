# Sprint Report + Retrospective - Week [44-46]

**Team [Group 8] | [Smart farm]**

## Sprint Overview

- **Sprint period:** 13.10.25 - 27.10.25
- **Sprint goal:** Complete the fundamental for the project: different nodes, server and GUI


- **Group members:** 👥
- [Ida Cathrin Bertheussen Soldal]
- [Mona Løvlie Tønsager Amundsen]
- [Svein Åge Pedersen Antonsen]
- [Andrea Sandnes]


- **Project:** 🌱 [Smart farm]

## Completed Work
### Finished tasks
- [] **Task 1:** Create logger, TCP server  and client handler
- [] **Task 2:** Implement fundamental nodes for sensors
- [] **Task 3:** Implement fundamental nodes for actuators
- [] **Task 4:** Control panel view (create GUI windows)

## Partially completed tasks:
- [◐] **Update protocol.md Draft:** Update to match the new implemented features and structures ##

## Sprint Goals - status
- Create Logger, TCP server and ClientHandler - ✅ Completed
- Implement fundamental nodes for sensors - ✅ Completed
- Implement fundamental nodes for actuators - ✅ Completed
- Control panel view (create GUI windows) - ✅ Completed
- Update protocol.md Draft - ⏳ In Progress

## Retrospective

### What went well?
- Seems every important task is completed
- Clear division of initial tasks among team members

### What can be improved?
- Better communication, status updates
- More pushes to the remote repository

### Action items for next sprint:
1. **Action 1:**
- Better communication, and status updates of how our tasks are doing [everyone]
2. **Action 2:**
- Push more, so others can more easily follow where we are in the process [everyone]

## Blockers and Challenges during the sprint

- **Blocker/Challenge 1:** [Description and solutions/status]
    - Exam project in Statistics can cause time-restrictions
    - Tasks are heavier than expected, can potentially slow down progress
    - Tasks start depending on each-other´s tasks completion, can cause delays

## Next Sprint Focus
- **Priority tasks:** [List]
#### Area 1: Server
- Extend ConnectionRegistry to handle SensorNodes
- Extend ClientHandler to handle COMMAND messages
- Extend ClientHandler to handle ACTUATOR_STATE messages
#### Area 2: Sensor Nodes
- Implement SensorNodeMain
- Create DeviceCatalog class
- Implement ActuatorEffectModel
- Create SensorDataPacket class
#### Area 3: Actuator Nodes
- Create ActuatorManager class
- Create CommandHandler class
#### Area 4: GUI
- Implement PanelAgent class
- Implement CommandInputHandler class

**Expected challenges:** [List]
- Time constraints due to other academic commitments
- Lack of experience with certain technologies, potential learning curve and time investment
- If dependencies between tasks causing delays

**Focus areas:** [List]
- Everyone keeps to their respective task-areas to ensure steady progress, and avoid merge conflicts
- Get things done (according to schedule, avoid procrastination)

--- **Reported by:** Mona Amundsen **Date:** 28.10.25
