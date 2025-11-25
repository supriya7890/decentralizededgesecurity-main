/*      
 *   Author: Nathaniel Brewer
 * 
 *      This is the handler for the Packet Type INITALIZATION. This extends the Abstract
 *      class 'Packet handler'
 * 
 *      The handle method splits the recieved packet's Payload's Json into a Java HashMap
 *      variable called "PayloadKeyValuePair" (HashMap is still Key Value pair just makes it
 *      accessable). This variable is declared in the SuperClass 'PacketHandler'
 * 
 *      The recieved packet will be instansiated inside the CoordinatorServerHandler and 
 *      then sent here when the payload will be handled accordingly
 * 
 * 
 *      Response packet will be generated in the CoordinatorServerHandler
 */
package server.server_handler.server_packet_type_handler;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import server.server_connections.server_connection_manager.*;

public class ServerInitalizationHandler extends ServerPacketHandler{

    private static final Logger logger = LogManager.getLogger(ServerInitalizationHandler.class);

    public ServerInitalizationHandler(ServerConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    /* This method will be called from the 'PacketHandler' SuperClass's "handle" method.
     * This particular method will seperate the handled KeyValue pairs and seperate them
     * into a key and a value. In this instance it is the Servers preferred recieving port.
     */
    @Override
    public ServerHandlerResponse process() {

        try{

            // Get all the values from the payload
            String[] values = recievedPacket.getAllPayloadValues();

            // If there are either less or more than 1 value, throw error - we need only one value
            if(values.length < 1 || values.length > 1) {
                throw new Exception("Expected Payload length of 1. Recieved length of " + values.length);
            }

            int port;

            try{
                port = Integer.parseInt(values[0]);
            } catch (NumberFormatException e){
                throw new Exception("Invalid Format!"); // Keeping this genericand vauge on purpose!
            }

            // Relying on the fact the client should send the port here!
            connectionManager.getConnectionInfoById(recievedPacket.getId()).setPort(port);

            // Try and create the sender for the node now that it has a port assigned
            // connectionManager.getConnectionInfoById(recievedPacket.getId()).createSender();

            // Generates the success response to be put into the ack packet 
            packetResponse = new ServerHandlerResponse(true);

            // Add the node ID to the payload
            packetResponse.addCustomKeyValuePair("id", recievedPacket.getId());

            // Add the cluster ID to the payload
            packetResponse.addCustomKeyValuePair("clusterId", clusterId);

        } catch(Exception e) {
            logger.error("Error Handling packet\n"+e);
            // Generates the response to be put into the failure packet
            packetResponse = new ServerHandlerResponse(false, e, "Error Handling Packet.");
        }
        return packetResponse;
    }
}