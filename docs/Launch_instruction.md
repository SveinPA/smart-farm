# Launch Instruction

To launch the application, follow these steps:

### Step 1: Install the common module (run from the repo root)
#### mvn -pl common -am -DskipTests installmvn -pl common -am -DskipTests install
- This is done to install the common module into your local Maven repository so the control-panel module can resolve its dependency on common when run by itself.

Note: Step 1 only needs to be done once, unless you make changes to the common module.gi

### Step 2: Run the Control Panel JavaFX app (still from the repo root)
#### mvn -f control-panel/pom.xml clean javafx:run
- This is done because the JavaFX plugin is configured in the control-panel module. Using -f sets that module's POM as the execution root, clean ensures a fresh build, and javafx:run launches the App.