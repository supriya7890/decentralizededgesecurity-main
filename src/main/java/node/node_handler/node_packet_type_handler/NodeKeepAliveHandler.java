/*      Author: Nathaniel Brewer
 * 
 *      This is the handler for the Packet Type KEEP_ALIVE. This extends the Abstract
 *      class 'Packet handler'
 *      
 *      This handler will handle KEEP_ALIVE probes from the server, for when the server
 *      has expected a KEEP_ALIVE packet from the node, if one is not recieved then a KEEP_ALIVE
 *      from the server will be sent here and will need a response
 * 
 *      The recieved packet will be instansiated inside the NodeServerHandler and 
 *      then sent here when the payload will be handled accordingly
 * 
 *      Response packet will be generated in the NodeServerHandler
 */
package node.node_handler.node_packet_type_handler;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class NodeKeepAliveHandler extends NodePacketHandler{
    
    // Each class can have its own logger instance
    private static final Logger logger = LogManager.getLogger(NodeKeepAliveHandler.class);

        @Override
    public NodeHandlerResponse process() {

        try{
            // Lambda function - HashMap has a ForEach function that receives the all the keys(k) and their corresponding values(v) and will loop through each one individually and send it to the config
            PayloadKeyValuePairs.forEach( (k, v) -> { 
            });

            // Generates the success response to be put into the ack packet 
            packetResponse = new NodeHandlerResponse(true, "Recieved");

        } catch(Exception e) {
            logger.error("Error Handling packet" + e);
            // Generates the response to be put into the failure packet
            packetResponse = new NodeHandlerResponse(false, e, "Error Handling Packet.");
        }
        return packetResponse;
    }

}
