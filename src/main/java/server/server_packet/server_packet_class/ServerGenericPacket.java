/*
 *      Author: Nathaniel Brewer    
 * 
 *      This is for creating any sort of simple packet that does not have unique logic.
 * 
 *      Since most packets will be this way, this will be the default way to create most packets
 * 
 */
package server.server_packet.server_packet_class;

import java.util.LinkedHashMap;

import server.server_packet.*;

public class ServerGenericPacket extends ServerPacket {

    // No payload
    public ServerGenericPacket(ServerPacketType packetType) {
        super();
        this.packetType = packetType;
    }
    
    // Constructor for setting a payload
    public ServerGenericPacket(ServerPacketType packetType, LinkedHashMap<String, String> payload) {
        super(payload);
        this.packetType = packetType;
        payload.forEach( (key, value) -> {
            payloadPairCounter++;
        });
    }

    // Contructor with multiple value strings with no key
    public ServerGenericPacket(ServerPacketType packetType, String id, String... value) {
        this.packetType = packetType;
        this.id = id;
        this.payload = new LinkedHashMap<String, String>();

        for(String val : value) {
            payload.put("message" + payloadPairCounter, val);
            payloadPairCounter++;
        }
    }
}
