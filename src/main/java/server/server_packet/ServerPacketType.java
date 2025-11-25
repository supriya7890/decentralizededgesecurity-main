/*
 *      Author: Nathaniel Brewer
 * 
 *      Enumeration for easily setting the packetType with visualizable header options
 * 
 *      Setup example
 *          ServerPacket packet = new ServerPacket(PacketType.MESSAGE, "Sender", "Payload");
 *  
 *      Will pull these and use a dictonary lookup to handle each one accordingly
 *      For example, AUTH will be sent to whatever auth system we decide to use 
 * 
 */
package server.server_packet;

public enum ServerPacketType {
    INITIALIZATION,     // For handshake or setup - Will send the preferred listening port 
    PEER_LIST_REQUEST,  // To request the entire list of Nodes
    MESSAGE,            // Generic text/data message
    KEEP_ALIVE,         // Keep-alive or ping
    ERROR,              // Error or exception reporting
    ACK,                // Acknowledgement of receipt
    DISCONNECT          // Graceful disconnect notice
}