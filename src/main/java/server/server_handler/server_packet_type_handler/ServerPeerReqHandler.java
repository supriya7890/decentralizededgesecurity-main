/*      Author: Nathaniel Brewer
 * 
 *      This is the handler for the Packet Type PEER_LIST_REQ. This extends the Abstract
 *      class 'Packet handler'
 *      
 *      This will be the second packet recieved from a node. The node will request this list
 *      from the server. The server will return a list of nodes with the data:
 *          (nodeId, nodeIp, nodePort)
 *      Alongside the list of nodes this server will send: ClusterId, Timestamp, RequestId, ResponseId
 * 
 */
package server.server_handler.server_packet_type_handler;

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import server.server_connections.ServerConnectionDto;
import server.server_connections.server_connection_manager.ServerNodeConnectionManager;

import server.server_packet.server_packet_class.ServerPeerListRes;
import server.server_packet.ServerPacket;

public class ServerPeerReqHandler{

    private static final Logger logger = LogManager.getLogger(ServerPeerReqHandler.class);

    private ServerPeerListRes responsePacket;

    private ServerPacket recievedPacket;
   
    public ServerPeerReqHandler(ServerPacket recievedPacket) {
        this.recievedPacket = recievedPacket;
    }

    public ServerPeerListRes process() {
        try{
            ServerNodeConnectionManager nodeConnManager = ServerNodeConnectionManager.getInstance();

            LinkedHashMap<String, ServerConnectionDto> nodeMap = nodeConnManager.getActiveConnections();

            String reqId = null;
            
            for (Map.Entry<String, String> entry : recievedPacket.getPayload().entrySet()) {
                String k = entry.getKey();
                String v = entry.getValue();
                //System.out.println("Message " + messageCounter + ":\n\t\t" + v );

                if ("reqId".equals(k)) {
                    reqId = v;
                }
            }

            responsePacket = new ServerPeerListRes(reqId, nodeMap);


        } catch(Exception e) {
            logger.error("Error Handling packet\n"+e);
            // Generates the response to be put into the failure packet
            //packetResponse = new ServerHandlerResponse(false, e, "Error Handling Packet.");
        }
        return responsePacket;
    }
}

