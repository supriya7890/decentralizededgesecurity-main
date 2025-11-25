/*
 *      Author: Nathaniel Brewer
 * 
 *      Enumeration for easily setting the packetType with visualizable header options
 * 
 *      Setup example
 *          ServerPacket packet = new ServerPacket(PacketType.MESSAGE, "Sender", "Payload!");
 *  
 *      Will pull these and use a switch statement to handle them accordingly.
 *      For example, AUTH will be sent to whatever auth system we decide to use 
 * 
 */
package coordinator.coordinator_packet;

public enum CoordinatorPacketType {
    INITIALIZATION,   // For handshake or setup
    AUTH,             // Authentication packets
    MESSAGE,          // Generic text/data message
    COMMAND,          // Control commands (start, stop, etc.)
    KEEP_ALIVE,        // Keep-alive or ping
    STATUS,           // Status update (health, load, etc.)
    DATA,             // Bulk or sensor data
    ERROR,            // Error or exception reporting
    ACK,              // Acknowledgement of receipt
    DISCONNECT        // Graceful disconnect notice
}