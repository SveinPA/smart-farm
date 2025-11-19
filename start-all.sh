#!/bin/bash
# Smart Farm - Launch All Components
# This script builds the project and starts broker, sensor node, and control panel

echo "=========================================="
echo "Smart Farm System Startup"
echo "=========================================="

# 1. Build the project
echo ""
echo "[1/4] Building project..."
mvn clean install
if [ $? -ne 0 ]; then
    echo "ERROR: Build failed. Exiting."
    exit 1
fi

# 2. Start broker in background
echo ""
echo "[2/4] Starting broker..."
mkdir -p logs  # Ensure logs directory exists
mvn exec:java -pl broker > logs/broker.log 2>&1 &
BROKER_PID=$!
echo "Broker started (PID: $BROKER_PID)"
sleep 5  # Wait for broker to initialize (increased for Mac compatibility)

# 3. Start sensor node in background (default: all sensors)
echo ""
echo "[3/4] Starting sensor node (all sensors)..."
mvn exec:java -pl sensor-node > logs/sensor-node.log 2>&1 &
SENSOR_PID=$!
echo "Sensor node started (PID: $SENSOR_PID)"
sleep 3  # Wait for sensor to connect (increased for Mac compatibility)

# 4. Start control panel GUI (foreground)
echo ""
echo "[4/4] Launching control panel GUI..."
echo ""
echo "=========================================="
echo "System Ready!"
echo "Broker PID: $BROKER_PID"
echo "Sensor PID: $SENSOR_PID"
echo "=========================================="
echo ""
mvn -pl control-panel javafx:run

# When GUI closes, kill background processes
echo ""
echo "Shutting down broker and sensor node..."
kill $BROKER_PID $SENSOR_PID 2>/dev/null
echo "Shutdown complete."
