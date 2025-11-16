# Sprint Report + Retrospective - Week [44-46]

**Team [Group 8] | [Smart farm]**

## Sprint Overview

- **Sprint period:** 27.10.25 - 10.11.25
- **Sprint goal:** Complete the fundamental for the project: different nodes, integrate logger, server and start connecting this to the GUI


- **Group members:** 👥
- [Ida Cathrin Bertheussen Soldal]
- [Mona Løvlie Tønsager Amundsen]
- [Svein Åge Pedersen Antonsen]
- [Andrea Sandnes]


- **Project:** 🌱 [Smart farm]

## Completed Work
### Finished tasks
- [] **Task 1:** Create and integrate logger, TCP server and client handler
- [] **Task 2:** Work (try finishing) the GUI
- [] **Task 3:** Clean and start connecting the communication through the console

## Partially completed tasks:
- [◐] **Update protocol.md Draft:** Update to match the new implemented features and structures

## Sprint Goals - status
- Create and integrate Logger, TCP server and ClientHandler - ✅ Completed
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
- More pushes to the remote repository, so others can more easily follow where we are in the process

### Action items for next sprint:
1. **Action 1:**
- Better communication, and status updates of how our tasks are doing [everyone]
2. **Action 2:**
- Push more, so others can more easily follow where we are in the process [everyone]

## Blockers and Challenges during the sprint
- **Blocker/Challenge 1:**
    - Exam project in Statistics can cause time-restrictions
    - Tasks are heavier than expected, can potentially slow down progress
    - Tasks start depending on each-other´s tasks completion, can cause delays or merge-conflicts 

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
- Improve overall GUI and amke sure its relevant

**Expected challenges:** [List]
- Time constraints due to other academic commitments
- Lack of experience with certain technologies, potential learning curve and time investment
- If dependencies between tasks causing delays or merge-conflicts, it can slow down progress.

**Focus areas:** [List]
- Everyone keeps to their respective task-areas to ensure steady progress, and avoid merge conflicts
- Get things done (according to schedule, avoid procrastination)
- Communicate frequently

--- **Reported by:** Mona Amundsen **Date:** 28.10.25
