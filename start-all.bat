@echo off
REM Smart Farm - Launch All Components
REM This script builds the project and starts broker, sensor node, and control panel

echo ==========================================
echo Smart Farm System Startup
echo ==========================================

REM 1. Build the project
echo.
echo [1/4] Building project...
call mvn clean install
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Build failed. Exiting.
    exit /b 1
)

REM 2. Start broker in background
echo.
echo [2/4] Starting broker...
if not exist logs mkdir logs
start "Smart-Farm Broker" /MIN cmd /c "mvn exec:java -pl broker > logs\broker.log 2>&1"
timeout /t 5 /nobreak > nul

REM 3. Start sensor node in background (default: all sensors)
echo.
echo [3/4] Starting sensor node (all sensors)...
start "Smart-Farm Sensor" /MIN cmd /c "mvn exec:java -pl sensor-node > logs\sensor-node.log 2>&1"
timeout /t 3 /nobreak > nul

REM 4. Start control panel GUI (foreground)
echo.
echo [4/4] Launching control panel GUI...
echo.
echo ==========================================
echo System Ready!
echo Check logs/ folder for component logs
echo ==========================================
echo.
mvn -pl control-panel javafx:run

echo.
echo Control panel closed. Broker and sensor node are still running.
echo To stop all components, run: stop-all.bat
