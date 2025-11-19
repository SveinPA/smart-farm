# Sprint Report + Retrospective - Week [46-47]

**Team [Group 8]|[Smart farm]**

## Sprint Overview

- **Sprint period:** 10.11.25 - 18.11.25
- **Sprint goal:** Finalize project implementation, integrate all components (Sensor Node,
Broker, Control Panel), update documentation and protocol, prepare final presentation and submission.


- **Group members:** 👥
- [Ida Cathrin Bertheussen Soldal]
- [Mona Løvlie Tønsager Amundsen]
- [Svein Åge Pedersen Antonsen]
- [Andrea Sandnes]


- **Project:** 🌱 [Smart farm]

## Completed Work
### Finished tasks
- **Task 1:** Finalize integration between nodes and broker
- **Task 2:** Implement and test final Control Panel and GUI functionality
- **Task 3:** Polish code
- **Task 4:** Update and finalize documentation
- **Task 5:** Prepare final demo and video presentation

## Sprint Goals - status
- Integrate all modules into a unified working system - ✅
- Finalize and double-check documentation - ✅
- Prepare for project delivery & video -  ✅

## Issues for Sprint 4
- **DashboardController (GUI)**: Mona
- **NodeViewModel (GUI)**: Mona
- **Control Panel - Return button**: Mona
- **Clean GUI stuff**: Mona
- **Review and update Protocol if needed**: Svein
- **Add AI useage declaration in either README or as a stand-alone md file**: Svein
- **Deactivate Debug logging mode**: Svein
- **Card 24 Statistics**: Svein
- **Control panel - Memory leak bug**: Andrea
- **Dashboard sensor does not update immediately**: Svein
- **Broker/Control panel - Sensor List**: Svein & Andrea
- **Broker - Error messages**: Svein & Andrea
- **Control Panel - COMMAND_ACK handling**: Svein & Andrea
- **Actuators/Control panel - Actuator status**: Ida
- **Sprint Rapport + retrospective - Uke 7-8**: Ida
- **End-to-End integration test**: Svein
- **(GUI) Fertilizer min, avg, max + Light measured in m/s (Dashboard) (bugfix)**: Svein
- **Add comment about Copilot Git usage (README)**: Svein
- **Project video demonstration**: Everyone

## Retrospective

### What went well?
- Steady reviews of each other's code (PRs), which was important for everyone to get an overview of
what the others had developed
- Team improved upon communication via GitHub (issues, pull requests) and Discord
- More physical meetings allowed for status checks
- Quick coordination and agreement of spreading the final work roles/tasks between each other,
which allowed for a more effective workflow

### What can be improved?
- Documentation updates were slightly delayed as features evolved late in the process
- Time constraints near delivery increased pressure on testing and polish

## Blockers and Challenges during the sprint

- **Blocker/Challenges:**
    - **Time Pressure:** Compressed schedule due to other projects clashing + exam period started
    - **GUI Integration:** Went back and forth between keeping a GUI or a simple console-based UI
    - **Lack of Knowledge:** As a group, we went into this project with limited knowledge about how to proceed. While we had *some* theory background, the group had 0 knowledge in how to develop the project of this extent to one, single component. The lectures and mandatory assignments didn't help much on this specific project
    - **Sudden Changes from Assignment Deliverable**: Had to redo/add all issues into sprint report as specified by exam deliverable, even though this was not specified in the actual assignment itself


## Final Reflections
- This sprint marked the **completion** of all planned features and documentation:
    - **Everyone contributed meaningfully** to both coding and documentation, ensuring a balanced workload
    - Everyone demonstrated **good coding principles**, and did not hesitate to reach out to other members
    to get their second opinion of their code
    - The final product follows the assignment requirements and includes **extras** like node discovery
    and averaged sensor data
    - **Large Learning Curve**: A lot of research had to be done underway as the project was being implemented
    - The team wished to integrate more extras and an even better GUI, but due to **time constraints**,
    we had to prioritize the most important features
    - **Vague Assignment**: The project description felt extremely vague at certain parts. While some of the requirements were nice to follow step-by-step, some things were more difficulty to interpret for the group, such as:
        - If we were supposed to have user login -> but no persistence requirement
        - How configurable the sensor nodes were meant to be
        - How many extras at minimum needed to be implemented to get a better grade
    - **Accidental Unbalanced Work**:
        - While most of the project was evenly balanced and people had their "primary" responsibility areas, there were still some unforeseen issues
        - One of our group members, for example, ended up having to refactor most of her code due to starting GUI layout before the backend was finished or even halfway implemented
        - This caused there to be a large difference between her and the rest of the members in amount of work/commits done
        - This is something the group has reflected on that we need to better ourselves in planning for the next project
        - Individually, the members need to get better at letting the group know if their work is growing too much
    - If the project was ever to be **revisited**, the team would implement/polish these features:
        - Further improvements to **GUI**
        - Some more extras (such as **security**)
        - One single launch command line

--- **Reported by:** [Ida Soldal] **Date:** 18.11.25.