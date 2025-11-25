/*
 *      Author: Nathaniel Brewer
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
 *      Response packet will be generated in the CoordinatorServerHandler
 */
package coordinator.coordinator_handler.coordinator_packet_type_handler;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import coordinator.coordinator_connections.*;

public class CoordinatorInitalizationHandler extends CoordinatorPacketHandler{

    private static final Logger logger = LogManager.getLogger(CoordinatorInitalizationHandler.class);

    private CoordinatorConnectionManager connectionManager = CoordinatorConnectionManager.getInstance();

    /* This method will be called from the 'PacketHandler' SuperClass's "handle" method.
     * This particular method will seperate the handled KeyValue pairs and seperate them
     * into a key and a value. In this instance it is the Servers preferred recieving port.
     * Once recieved and handled, this method will put the Key Value into the config file
     */
    @Override
    public CoordinatorHandlerResponse process() {

        try{

            // Get all the values from the payload
            String[] values = recievedPacket.getAllPayloadValues();

            // If there are either less or more than 1 value, throw error - we need only one value
            if(values.length < 1 || values.length > 1){
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

            // Generates the success response to be put into the ack packet 
            packetResponse = new CoordinatorHandlerResponse(true);

            // Add the ID to the payload
            packetResponse.addCustomKeyValuePair("id", recievedPacket.getId());

        } catch(Exception e) {
            logger.error("Error Handling packet.\n" + e);
            e.printStackTrace();
            // Generates the response to be put into the failure packet
            packetResponse = new CoordinatorHandlerResponse(false, e, "Error Handling Packet.");
        }
        return packetResponse;
    }
}