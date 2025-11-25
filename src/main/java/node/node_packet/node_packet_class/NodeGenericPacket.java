/*
 *      Author: Nathaniel Brewer    
 * 
 *      This is for creating any sort of simple packet that does not have unique logic.
 * 
 *      Since most packets will be this way, this will be the default way to create most packets
 * 
 */
package node.node_packet.node_packet_class;

import java.util.LinkedHashMap;

import node.node_packet.*;

public class NodeGenericPacket extends NodePacket {

    // No payload
    public NodeGenericPacket(NodePacketType packetType, String id) {
        this.packetType = packetType;
        this.id = id;
        this.payload = new LinkedHashMap<>();
    }
    // Constructor for setting a LinkdHashMap payload
    public NodeGenericPacket(NodePacketType packetType, String id, LinkedHashMap<String, String> payload) {
        this.packetType = packetType;
        this.id = id;
        this.payload = payload;

        payload.forEach( (key, value) -> {
            payloadPairCounter++;
        });
    }
    // Contructor with multiple value strings with no key
    public NodeGenericPacket(NodePacketType packetType, String id, String... value) {
        this.packetType = packetType;
        this.id = id;
        this.payload = new LinkedHashMap<String, String>();

        for(String val : value) {
            payload.put("message" + payloadPairCounter, val);
            payloadPairCounter++;
        }
    }
}
