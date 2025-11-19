#!/bin/bash
# Smart Farm - Stop All Components

echo "Stopping Smart Farm components..."

# Kill all Maven processes for this project
pkill -f "mvn.*broker"
pkill -f "mvn.*sensor-node"
pkill -f "mvn.*control-panel"

echo "All components stopped."
