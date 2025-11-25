/*
 *      Author: Nathaniel Brewer
 * 
 *      The packet that will be used as a response for the PEER_LIST_REQ packet.
 */

package server.server_packet.server_packet_class;

import java.util.LinkedHashMap;

import java.time.LocalDateTime;

import server.server_connections.ServerConnectionDto;
import server.server_packet.*;

public class ServerPeerListRes extends ServerPacket {

    private LinkedHashMap<String, ServerConnectionDto> nodeList;
    private String reqId;
    private String resId;
    private LocalDateTime timestamp;
    
    public ServerPeerListRes(String reqId, LinkedHashMap<String, ServerConnectionDto> nodeList) {
        super();
        this.reqId = reqId;
        this.nodeList = nodeList;
        this.timestamp = LocalDateTime.now();
        payload.forEach( (key, value) -> {
            payloadPairCounter++;
        });
    }

    /*
     *      Getters
     */

    public LinkedHashMap<String, ServerConnectionDto> getNodeList() { return nodeList; }

    public String getReqIq() { return reqId; }

    public String getResId() { return resId; }

    public LocalDateTime getTimestamp() { return timestamp; }

    /*
     *      Mutators
     */

    public void setReqId() {}

    public void setResId() {}

    public void setTimeStamp() { timestamp = LocalDateTime.now(); }

    public void setNodeList(LinkedHashMap<String, ServerConnectionDto> nodeList) { this.nodeList = nodeList; }

}   
