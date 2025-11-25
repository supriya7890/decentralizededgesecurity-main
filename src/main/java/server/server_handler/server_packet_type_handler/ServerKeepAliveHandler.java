/*      Author: Nathaniel Brewer
 * 
 *      This is the handler for the Packet Type KEEP_ALIVE. This extends the Abstract
 *      class 'Packet handler'
 * 
 *      The recieved packet will be instansiated inside the ServerHandler and 
 *      then sent here when the payload will be handled accordingly
 * 
 *      Response packet will be generated in the CoordinatorNodeHandler
 */
package server.server_handler.server_packet_type_handler;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import server.server_connections.server_connection_manager.ServerConnectionManager;

public class ServerKeepAliveHandler extends ServerPacketHandler{
    
    private static ServerConnectionManager connectionManager;

    private static final Logger logger = LogManager.getLogger(ServerKeepAliveHandler.class);

    public ServerKeepAliveHandler(ServerConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    @Override
    public ServerHandlerResponse process() {
        try{
            payloadKeyValuePairs.forEach((k, v) -> {
                //TODO: Handle info and update
            });
            // Generates the success response to be put into the ack packet 
            packetResponse = new ServerHandlerResponse(true, "Recieved");
        } catch(Exception e) {
            logger.error("Error handling Keep Alive packet from server: {}.\n {} ", recievedPacket.getId(), e);
            // Generates the response to be put into the failure packet
            packetResponse = new ServerHandlerResponse(false, e, "Error Handling Packet.");
        }
        return packetResponse;
    }
}
