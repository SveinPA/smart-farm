# Sprint Report + Retrospective - Week [44-46]

**Team [Group 8] | [Smart farm]**

## Sprint Overview

- **Sprint period:** 13.10.25 - 27.10.25
- **Sprint goal:** Complete the fundamental for the project: different nodes, integrate logger, server and star connecting this to the each-other and GUI.


- **Group members:** 👥
- [Ida Cathrin Bertheussen Soldal]
- [Mona Løvlie Tønsager Amundsen]
- [Svein Åge Pedersen Antonsen]
- [Andrea Sandnes]


- **Project:** 🌱 [Smart farm]

## Completed Work
### Finished tasks
- **Create logger, TCP server  and client handler** - ✅ Completed
- **Implement fundamental nodes for sensors** - ✅ Completed
- **Implement fundamental nodes for actuators** - ✅ Completed
- **Control panel view improvements (create dashboard/cards/control-panel)** - ✅ Completed

### Partially completed tasks:
- **Update protocol.md Draft:** Update to match the new implemented features and structures

## Issues For Sprint 3
- **issue #83:** Implement CommandInputHandler, Assignee: Andrea
- **issue #82:** Implement PanelAgent, Assignee: Andrea
- **issue #60:** Add integration test: broadcast skips closed panels, Assignee: Svein
- **issue #65:** BrokerMain config polishing, Assignee: Svein
- **issue #85:** Integrate Classes in to ControlPanelMain, Assignee: Andrea
- **issue #84:** Implement DisplayManager, Assignee: Andrea
- **issue #64:** heartbeat client handling, Assignee: Svein
- **issue #63:** Add ACTUATOR_COMMAND routing (server pass-through), Assignee: Svein
- **issue #71:** Integrate logger into actuators, Assignee: Mona
- **issue #62:** Replace regex JSON parsing with minimal DTO + tiny parser, Assignee: Svein
- **issue #75:** SensorDataPacket, Assignee: Ida
- **issue #74:** NodeAgent, Assignee: Ida
- **issue #70:** ActuatorStatePacket, Assignee: Mona
- **issue #68:** ActuatorManager, Assignee: Mona
- **issue #69:** CommandHandler, Assignee: Mona
- **issue #59:** Move FrameCodec to common, Assignee: Svein
- **issue #72:** DeviceCatalog, Assignee: Ida
- **issue #36:** SensorNode, Assignee: Ida
- **issue #73:** ActuatorEffectModel, Assignee: Ida

## Retrospective

### What went well?
- Seems every important task is completed
- Clearer division of initial tasks among team members
- Team had extra status meeting halfway through the sprint, which was much needed - this ended up resolving unnecessary re-work

### What can be improved?
- Better communication, status updates
- Even more detailed planning, to avoid dependencies between tasks and unnecessary work (and scrapping work due to changes in requirements)

### Action items for next sprint:
1. **Action 1:**
- Better communication, and status updates of how our tasks are doing [everyone]
2. **Action 2:**
- Push more, so others can more easily follow where we are in the process, and what we are working on [everyone]
3. **Action 3:**
- Plan and document issues better so everyone knows what issues implies, and reduce unnecessary issues [everyone]
- 
## Blockers and Challenges during the sprint

- **Blocker/Challenges :**
  - Exam project in Statistics made it harder to manage time for this project 
  - Tasks where heavier than expected, which caused some member(s) to have way too much workload
  - Tasks start depending on each-other´s tasks completion, caused some merge-conflicts
  - Task(s) not fully planned/analyzed in relation to the rest of the code before starting implementation, causing unnecessary work and waste of time 

## Next Sprint Focus
- **Priority tasks:**
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
- 
#### Area 4: GUI
- Implement PanelAgent class
- Implement CommandInputHandler class

**Expected challenges:**
- Time constraints due to other academic commitments
- Lack of experience with certain technologies, potential learning curve and time investment
- Dependencies between tasks can cause delays or merge-conflicts (especially when we are starting to connect different parts of the system)
- Connecting backend to GUI, nad making sure the data that is shown is correct and corresponds to the backend data (read through the console)

**Focus areas:**
- Everyone keeps to their respective task-areas to ensure steady progress, and avoid merge conflicts if possible
- More communication within the team regarding status of tasks and what is being worked on, so potential conflicts is avoided and resolved early
- Get things done (according to schedule, avoid procrastination)
- Plan and document issues better so everyone knows what issues implies, and reduce unnecessary issues
- "Analyze" the tasks and break them down even better before, so we can avoid uneven distribution of work, and overworking certain members
- Ask for help when needed, especially when there is too much workload on certain members

## Other Notes
Most issues were completed as planned, but some tasks ended up being a "dead-end" and the work had to be scrapped entirely (see issues: #68, #69, #70), this resulted in one member
contributed less than planned for half of the sprint. Luckily the team scheduled a mid-sprint meeting where it was resolved and concluded that the team can redirect some responsible 
areas. This helped even out the workload a bit more in the GUI section of the work (where we went over and improved relevancy of each card in the control-panel).

Connecting the backend more was a minor challenge due to lack of experience, and resulted in being a bigger learning curve than expected. 
However, with good teamwork and communication the team managed to overcome this and get the connections working as intended.

The teams ability to adapt halfway through this sprint was crucial to the overall success of the sprint, and hopefully 
can be a lesson learned for future/next sprint(s).
  
--- **Reported by:** Mona Amundsen **Date:** 28.10.25
