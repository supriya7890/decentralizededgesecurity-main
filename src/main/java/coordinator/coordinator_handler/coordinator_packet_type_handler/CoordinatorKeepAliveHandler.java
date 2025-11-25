/*      Author: Nathaniel Brewer
 * 
 *      This is the handler for the Packet Type KEEP_ALIVE. This extends the Abstract
 *      class 'Packet handler'
 * 
 *      The recieved packet will be instansiated inside the CoordinatorServerHandler and 
 *      then sent here when the payload will be handled accordingly
 * 
 *      Response packet will be generated in the CoordinatorNodeHandler
 */

package coordinator.coordinator_handler.coordinator_packet_type_handler;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import coordinator.coordinator_connections.*;

public class CoordinatorKeepAliveHandler extends CoordinatorPacketHandler{

    private static CoordinatorConnectionManager serverManager = CoordinatorConnectionManager.getInstance();
    
    private static final Logger logger = LogManager.getLogger(CoordinatorKeepAliveHandler.class);

    @Override
    public CoordinatorHandlerResponse process() {
        try{
            payloadKeyValuePairs.forEach((k, v) -> {
                //TODO: Handle info and update
            });
            // Generates the success response to be put into the ack packet 
            packetResponse = new CoordinatorHandlerResponse(true, "Recieved");
        } catch(Exception e) {
            logger.error("Error handling Keep Alive packet from server: {}.\n {} ", recievedPacket.getId(), e);
            // Generates the response to be put into the failure packet
            packetResponse = new CoordinatorHandlerResponse(false, e, "Error Handling Packet.");
        }
        return packetResponse;
    }

}
