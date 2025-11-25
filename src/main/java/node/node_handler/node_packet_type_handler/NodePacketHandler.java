/*      Author: Nathaniel Brewer
 * 
 *      THis is an abstract class created to be the catalyst for handling
 *      all the packet types. This allows for overwritting the 
 *      handling method.
 *      
 *      Since this class will only ever instantiated once inside any handler
 *      Payloads will be stored inside the "PayloadKeyValuePairs" Variable to 
 *      easily be instantiated from the 'handle' method which will /Usually/ 
 *      not have to be overwritten. 
 */

package node.node_handler.node_packet_type_handler;

import java.util.LinkedHashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import node.node_packet.NodePacket;

public abstract class NodePacketHandler {

    // Stores the recieved payload into a map (key Value) so the 
    protected LinkedHashMap<String, String> PayloadKeyValuePairs = new LinkedHashMap<>(); // Protected means any class inside this package can access this variable

    // This is the object that will be instantiated if the packet is handled succesfuly or an error gets thrown
    protected NodeHandlerResponse packetResponse;

    // Each class can have its own logger instance
    private static final Logger logger = LogManager.getLogger(NodePacketHandler.class);
    
    // Tears the packet apart and seperates the head from the body
    public NodeHandlerResponse handle(NodePacket recievedPacket){

        // Print out the packet 
        logger.info(
            "Packet recieved from ID: " + recievedPacket.getId() 
            + "\nPacket Type: \t" + recievedPacket.getPacketType() 
            + "\nPayload: \t" + recievedPacket.getPayload()
        );

        // Grab the payload from the packet
        LinkedHashMap<String, String> payload = recievedPacket.getPayload();

        // If the payload is empty , then an error will be sent back to the original packet sender
        if(payload.isEmpty()){
            return new NodeHandlerResponse(
                false, 
                new Exception("No payload Sent."),
                 "Error handling payload for PacketType " + recievedPacket.getPacketType()
            );
        }

        return process();
    }

    /*
     * 
     *      Abstract methods    -    these methods will need to be overwritten by subclasses
     * 
     */

    // Will process the data and handle it according to what packet type is overwritting this method
    public abstract NodeHandlerResponse process();

}
