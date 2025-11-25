/*      
 *      Author: Nathaniel Brewer
 * 
 *      This is an abstract class created to be the catalyst for handling
 *      all the packet types. This allows for overwritting the 
 *      handling method.
 *      
 *      Since this class will only ever instantiated once inside any handler
 *      Payloads will be stored inside the "PayloadKeyValuePairs" Variable to 
 *      easily be instantiated from the 'handle' method which will /Usually/ 
 *      not have to be overwritten. 
 */

package server.server_handler.server_packet_type_handler;

import java.util.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import server.server_connections.server_connection_manager.*;
import server.server_packet.*;

import server.server_services.ServerClusterManager;

public abstract class ServerPacketHandler {

    // Gets sent into the constructor for the child class (E.g. new ServerMessageHandler(ServerCoordinatorConnectionManager.getInstance()))
    protected static ServerConnectionManager connectionManager;

    private static final Logger logger = LogManager.getLogger(ServerPacketHandler.class);

    // Stores the recieved payload into a map (key Value) so the 
    protected LinkedHashMap<String, String> payloadKeyValuePairs = new LinkedHashMap<>();

    // This is the object that will be instantiated if the packet is handled succesfuly or an error gets thrown
    protected ServerHandlerResponse packetResponse;

    // Cluster ID 
    protected String clusterId = ServerClusterManager.getClusterId();

    // The recieved Packet
    protected ServerPacket recievedPacket;
    
    // Tears the packet apart and seperates the head from the body
    public ServerHandlerResponse handle(ServerPacket recievedPacket){

        this.recievedPacket = recievedPacket;

        // Print out the packet 
        logger.info(
            "\n\tID: \t" + recievedPacket.getId() 
            + "\n\tPacket Type: \t" + recievedPacket.getPacketType() 
            + "\n\tPayload: \t" + recievedPacket.getPayload()
        );

        // Grab the payload from the packet
        LinkedHashMap<String, String> payload = recievedPacket.getPayload();

        // If the payload is empty , then an error will be sent back to the original packet sender
        if(payload.isEmpty()){
            return new ServerHandlerResponse(
                false, 
                new Exception("No payload Sent."),
                 "Error handling payload for PacketType " + recievedPacket.getPacketType()
            );
        }
        // Check to see if the packet has an ID, if it is not an INITALIZATION packet
        if(recievedPacket.getId() == null && recievedPacket.getPacketType() != ServerPacketType.INITIALIZATION) {
            return new ServerHandlerResponse(
                false, 
                new Exception("Unknown Node."),
                 "Error handling payload for PacketType " + recievedPacket.getPacketType()
            );
        } 

        // Process the packet first, this will get the port out of the packet
        if(recievedPacket.getPacketType() == ServerPacketType.INITIALIZATION) {
            return process();
        }

        try{ 
            // This gets the connected senders ID from storage and updates the last activity since it is clearly sending packets
            connectionManager.getConnectionInfoById(recievedPacket.getId()).updateLastActivity();
        } catch (NullPointerException e) {
            logger.error("ID: " + recievedPacket.getId() + " was not found!\n" + e);
             // TODO: if the ID is not found in the connectionManager send a re-INIT packet to assign an ID
        }
            // TODO: 
        //connectionManager.updateById(recievedPacket.getId(), payload);

        return process();
    }

    /*
     *      Abstract methods    -    these methods will need to be overwritten by subclasses
     */

    // Will process the data and handle it according to what packet type is overwritting this method
    public abstract ServerHandlerResponse process();

}
