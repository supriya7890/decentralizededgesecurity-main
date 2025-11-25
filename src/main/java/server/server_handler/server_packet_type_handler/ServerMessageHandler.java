/*      Author: Nathaniel Brewer
 * 
 *      This is the handler for the Packet Type MESSAGE. This extends the Abstract
 *      class 'Packet handler'
 * 
 *      The handle method splits the recieved packet's Payload's Json into a Java HashMap
 *      variable called "PayloadKeyValuePair" (HashMap is still Key Value pair just makes it
 *      accessable). This variable is declared in the SuperClass 'PacketHandler'
 * 
 *      The recieved packet will be instansiated inside the ServerNodeHandler and 
 *      then sent here when the payload will be handled accordingly
 * 
 *      Response packet will be generated in the ServerNodeHandler
 */
package server.server_handler.server_packet_type_handler;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import server.server_connections.server_connection_manager.ServerConnectionManager;

public class ServerMessageHandler extends ServerPacketHandler{

    private static final Logger logger = LogManager.getLogger(ServerMessageHandler.class);

    public ServerMessageHandler(ServerConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    private int messageCounter = 0;

    /* This method will be called from the 'PacketHandler' SuperClass's "handle" method.
     * This particular method will seperate the handled KeyValue pairs and seperate them
     * into a key and a value. In this instance it is the Servers preferred recieving port.
     * Once recieved and handled, this method will put the Key Value into the config file
     */
    @Override
    public ServerHandlerResponse process() {

        try{
            // Lambda function - HashMap has a ForEach function that receives the all the keys(k) and their corresponding values(v) and will loop through each one individually
            payloadKeyValuePairs.forEach( (k, v) -> { 
                //System.out.println("Message " + messageCounter + ":\n\t\t" + v );
                messageCounter++;
            });

            // Generates the success response to be put into the ack packet 
            packetResponse = new ServerHandlerResponse(true, "Recieved");

        } catch(Exception e) {
            logger.error("Error Handling packet\n"+e);
            // Generates the response to be put into the failure packet
            packetResponse = new ServerHandlerResponse(false, e, "Error Handling Packet.");
        }
        return packetResponse;
    }
}