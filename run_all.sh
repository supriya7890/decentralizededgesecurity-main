#!/bin/bash

# Base directory = where this script is located
BASEDIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# Clean and compile all code for maven
echo "Cleaning and compiling Maven project..."
mvn clean compile -e
echo "Finished compiling Maven project"

# Use Maven to copy dependencies into lib folder
echo "Copying Maven dependencies."
mvn dependency:copy-dependencies -DoutputDirectory="$BASEDIR/lib"
echo "Finished maven dependencies commands."
read -p "Press enter to continue..."

echo "Maven compilation complete - all classes ready in target/classes"
read -p "Press enter to continue..."

# Get instance IDs from user input or command line arguments
echo ""
echo "===== Instance Configuration ====="
echo "You can specify instance IDs for the server and node components."
echo "Leave blank to use default configurations."
echo ""

# Use command line arguments if provided, otherwise prompt for input
if [ -z "$1" ]; then
    read -p "Enter Server Instance ID (e.g., server1, server2, or leave blank for default): " SERVER_INSTANCE
else
    SERVER_INSTANCE="$1"
    echo "Using Server Instance ID from command line: $SERVER_INSTANCE"
fi

if [ -z "$2" ]; then
    if [ -z "$1" ]; then
        read -p "Enter Node Instance ID (e.g., node1, node2, or leave blank for default): " NODE_INSTANCE
    else
        read -p "Enter Node Instance ID (e.g., node1, node2, or leave blank for default): " NODE_INSTANCE
    fi
else
    NODE_INSTANCE="$2"
    echo "Using Node Instance ID from command line: $NODE_INSTANCE"
fi

echo ""
if [ -z "$SERVER_INSTANCE" ]; then
    echo "Starting EdgeServer with default configuration"
else
    echo "Starting EdgeServer with instance ID: $SERVER_INSTANCE"
fi

if [ -z "$NODE_INSTANCE" ]; then
    echo "Starting EdgeNode with default configuration"
else
    echo "Starting EdgeNode with instance ID: $NODE_INSTANCE"
fi

echo "Starting EdgeCoordinator with default configuration"
echo ""
read -p "Press enter to start all components..."

cd "$BASEDIR"

gnome-terminal -- bash -c "cd '$BASEDIR' && java -cp target/classes:'$BASEDIR'/lib/* coordinator.edge_coordinator.EdgeCoordinator; exec bash" &

if [ -n "$SERVER_INSTANCE" ]; then
    gnome-terminal -- bash -c "cd '$BASEDIR' && java -cp target/classes:'$BASEDIR'/lib/* server.edge_server.EdgeServer $SERVER_INSTANCE; exec bash" &
else
    gnome-terminal -- bash -c "cd '$BASEDIR' && java -cp target/classes:'$BASEDIR'/lib/* server.edge_server.EdgeServer; exec bash" &
fi

if [ -n "$NODE_INSTANCE" ]; then
    gnome-terminal -- bash -c "cd '$BASEDIR' && java -cp target/classes:'$BASEDIR'/lib/* node.edge_node.EdgeNode $NODE_INSTANCE; exec bash" &
else
    gnome-terminal -- bash -c "cd '$BASEDIR' && java -cp target/classes:'$BASEDIR'/lib/* node.edge_node.EdgeNode; exec bash" &
fi