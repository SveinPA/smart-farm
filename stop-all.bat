@echo off
REM Smart Farm - Stop All Components

echo Stopping Smart Farm components...

REM Kill Maven processes
taskkill /FI "WINDOWTITLE eq Smart-Farm Broker" /F > nul 2>&1
taskkill /FI "WINDOWTITLE eq Smart-Farm Sensor" /F > nul 2>&1

echo All components stopped.
