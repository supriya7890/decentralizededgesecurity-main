# DecentralizedEdgeSecurity

## Overview

DecentralizedEdgeSecurity is a research project for building a basic 3-tiered edge network implemented in Java with Maven. The system consists of a Coordinator, Server, and Node, each running as separate Java processes that establish TCP connections and exchange JSON-formatted messages to demonstrate connectivity between network tiers.

**Current features:**
- **Maven-based build system** with proper dependency management
- **Hierarchical package structure** following Java naming conventions
- **JSON packet communication** using Gson library for serialization
- **Multi-threaded architecture** with concurrent connection handling
- **Extensible packet system** supporting multiple message types
- **TCP socket management** with proper connection lifecycle handling
- **Comprehensive logging** using Log4j2 framework

---

## Architecture

### Three-Tier Network Structure
```
┌─────────────────┐
│   Coordinator   │  ← Top Tier (Network Management)
│   (Port 5001)   │
└─────────────────┘
         ↑
         │ TCP Connection
         ↓
┌─────────────────┐
│     Server      │  ← Middle Tier (Edge Processing)
│   (Port 5002)   │
└─────────────────┘
         ↑
         │ TCP Connection
         ↓
┌─────────────────┐
│      Node       │  ← Bottom Tier (Edge Device)
│   (Port 5003)   │
└─────────────────┘
```

---

## Project Structure

```
DecentralizedEdgeSecurity/
├── pom.xml                    # Maven configuration
├── run_all.bat               # Build and run script
├── config/                   # Configuration files
│   ├── coordinator_config/
│   ├── node_config/
│   └── server_config/
├── src/main/
│   ├── java/
│   │   ├── coordinator/      # Coordinator module
│   │   │   ├── edge_coordinator/
│   │   │   ├── coordinator_config/
│   │   │   ├── coordinator_handler/
│   │   │   ├── coordinator_listener/
│   │   │   ├── coordinator_packet/
│   │   │   └── coordinator_sender/
│   │   ├── server/           # Server module
│   │   │   ├── edge_server/
│   │   │   ├── server_config/
│   │   │   ├── server_handler/
│   │   │   ├── server_listener/
│   │   │   ├── server_packet/
│   │   │   └── server_sender/
│   │   └── node/             # Node module
│   │       ├── edge_node/
│   │       ├── node_config/
│   │       ├── node_handler/
│   │       ├── node_listener/
│   │       ├── node_packet/
│   │       └── node_sender/
│   └── resources/
│       └── log4j2.xml        # Logging configuration
└── target/                   # Maven build output (auto-generated)
```

---

## Dependencies

The project uses Maven for dependency management with the following key libraries:

- **Java 17** - Target runtime environment
- **Gson 2.13.1** - JSON serialization/deserialization
- **Log4j 2.23.1** - Logging framework
- **Error Prone Annotations** - Code quality annotations

Dependencies are automatically managed by Maven and stored in `target/classes` after compilation.

---

## Coordinator

The Coordinator acts as the entry point for Servers in the network. When a Server connects, the Coordinator performs the following steps:

1. **Connection Handling:**  
   The Coordinator listens for incoming TCP connections from Servers on a configured port. Each new connection is handled in a separate thread to allow concurrent processing.

2. **Packet Reception and Parsing:**  
   Upon receiving a connection, the Coordinator reads a line of input from the Server. This input is expected to be a JSON-formatted string representing a packet. The Coordinator uses the Gson library to parse this JSON into a `CoordinatorPacket` object, which contains:
   - `packetType`: An enum indicating the type of message (e.g., INITIALIZATION, AUTH, MESSAGE, etc.).
   - `sender`: The identity of the sender.
   - `payload`: The message content.

3. **Packet Type Handling:**  
   The Coordinator inspects the `packetType` field and uses a switch statement to determine how to process the message:
   - **INITIALIZATION:** Handles handshake/setup logic for new Servers. This may include storing the Server's IP and preparing for further communication.
   - **AUTH:** Placeholder for future authentication logic.
   - **MESSAGE, COMMAND, HEARTBEAT, STATUS, DATA, ERROR, ACK, DISCONNECT:** Each type is recognized, and the Coordinator can be extended to process these accordingly. Currently, only the INITIALIZATION type is handled with any logic; others are placeholders for future development.

4. **Response:**  
   After processing the packet, the Coordinator sends a simple greeting response ("Hi, edge server, this is the edge coordinator!") back to the Server over the same connection.

5. **Connection Closure:**  
   The Coordinator closes the input/output streams and the socket after handling the message.

This design allows the Coordinator to flexibly handle different types of messages from Servers and provides a foundation for implementing more advanced logic (such as authentication or status tracking) in the future.

---

## Server

The Server is a crucial component that connects to the Coordinator and manages communication with Nodes. Its responsibilities include:

- **Initialization and Registration:**
  - Connects to the Coordinator and sends an INITIALIZATION packet containing its configuration (e.g., listening port).
  - Waits for an ACK from the Coordinator to confirm successful registration.

- **Node Communication:**
  - Listens for incoming connections from Nodes on its configured port.
  - Receives and parses packets from Nodes using the generic packet structure.
  - Responds to Node messages and can send/receive various packet types (e.g., MESSAGE, ACK).

- **Packet Parsing and Handling:**
  - Inspects the `packetType` field of received packets to determine the type of message.
  - Handles MESSAGE and ACK packet types with appropriate logic (e.g., printing messages, sending acknowledgments).

- **Thread Management:**
  - Manages each Node connection in a separate thread for concurrent processing.
  - Demonstrates proper socket, stream, and thread management for robust operation.

---

## Node

- Connects to the Server and sends a greeting.
- Prints the response from the Server.

---

## Packet Structure: Generic Packet

All packets exchanged between components (Coordinator, Server, Node) follow a common JSON structure. This structure allows for flexible message types and payloads.

**Fields:**
- `packetType`: The type of the packet (e.g., INITIALIZATION, AUTH, MESSAGE, COMMAND, HEARTBEAT, STATUS, DATA, ERROR, ACK, DISCONNECT).
- `sender`: The name or identifier of the sender (e.g., EdgeServer, EdgeNode, Coordinator).
- `payload`: The message content, which may be a string, JSON object, or key-value pairs depending on the packet type.

**Example JSON:**
```json
{
  "packetType": "MESSAGE",
  "sender": "EdgeNode",
  "payload": "Hello, server!"
}
```

**Notes:**
- The meaning and format of `payload` depend on the `packetType`.
- All packets must include these three fields.

---

## Packet Structure: INITIALIZATION Type

When a Server connects to the Coordinator, it sends an INITIALIZATION packet. This packet is serialized as JSON and contains the following fields:

- `packetType`: The type of the packet. For initialization, this is `INITIALIZATION`.
- `sender`: The name or identifier of the sender (e.g., `EdgeServer`).
- `payload`: A string containing key-value pairs separated by semicolons, describing initialization parameters (e.g., the server's listening port).

**Example JSON:**
```json
{
  "packetType": "INITIALIZATION",
  "sender": "EdgeServer",
  "payload": "server.listeningPort:5003"
}
```

**Payload Format:**
- The payload is a string of key-value pairs separated by semicolons (`;`).
- Each key and value are separated by a colon (`:`).
- Example: `"server.listeningPort:5003;anotherKey:anotherValue"`

**Coordinator Handling:**
- The Coordinator parses the payload, extracts each key-value pair, and stores them in its configuration.
- After processing, the Coordinator responds with an ACK packet to confirm successful initialization.

---

## Getting Started

### Prerequisites
- **Java 17 or higher** installed and configured
- **Maven 3.6+** for build management
- **Git** for version control

### Quick Start

1. **Clone the repository:**
   ```bash
   git clone https://github.com/brewern5/DecentralizedEdgeSecurity.git
   cd DecentralizedEdgeSecurity
   ```

2. **Build and run the entire system:**
   ```bash
   # Windows
   run_all.bat
   
   # Or manually with Maven
   mvn clean compile
   mvn dependency:copy-dependencies -DoutputDirectory=lib
   ```

3. **The script will:**
   - Clean and compile all Java sources using Maven
   - Download and copy all dependencies to the `lib/` directory
   - Launch three separate terminal windows for Coordinator, Server, and Node
   - Each component will start with proper logging and connection handling

### Manual Execution

If you prefer to run components individually:

```bash
# Compile the project
mvn clean compile

# Run Coordinator
java -cp target/classes;lib/* coordinator.edge_coordinator.EdgeCoordinator

# Run Server (in new terminal)
java -cp target/classes;lib/* server.edge_server.EdgeServer

# Run Node (in new terminal)  
java -cp target/classes;lib/* node.edge_node.EdgeNode
```

### Configuration

Configuration files are located in the `config/` directory:
- `coordinator_config/coordinatorConfig.properties`
- `server_config/serverConfig.properties`  
- `node_config/nodeConfig.properties`

Each component reads its respective configuration on startup.

---

## Development

### Building from Source
```bash
# Clean build
mvn clean compile

# Run tests (when implemented)
mvn test

# Package application
mvn package
```

### IDE Setup
The project follows standard Maven conventions and can be imported into any Java IDE:
- **IntelliJ IDEA**: Open the `pom.xml` file
- **Eclipse**: Import as Maven project
- **VS Code**: Open folder with Java Extension Pack

### Package Structure
All packages follow hierarchical naming:
- `coordinator.*` - Coordinator module packages
- `server.*` - Server module packages  
- `node.*` - Node module packages

---

## Current Status

> **Note:** This project is in active development and demonstrates basic connectivity and message exchange between the three network tiers. Future enhancements will include authentication, encryption, load balancing, and advanced edge computing capabilities.
