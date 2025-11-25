/*
 *      Author: Nathaniel Brewer    
 * 
 *      This is for creating any sort of simple packet that does not have unique logic.
 * 
 *      Since most packets will be this way, this will be the default way to create most packets
 * 
 */
package coordinator.coordinator_packet.coordinator_packet_class;

import java.util.LinkedHashMap;

import coordinator.coordinator_packet.*;

public class CoordinatorGenericPacket extends CoordinatorPacket {

    // No payload
    public CoordinatorGenericPacket(CoordinatorPacketType packetType, String id) {
        this.packetType = packetType;
        this.id = id;
        this.payload = new LinkedHashMap<String, String>();
    }
    // Constructor for setting a payload
    public CoordinatorGenericPacket(CoordinatorPacketType packetType, String id, LinkedHashMap<String, String> payload) {
        this.packetType = packetType;
        this.id = id;
        this.payload = payload;

        payload.forEach( (key, value) -> {
            payloadPairCounter++;
        });
    }
    // Contructor with multiple value strings with no key
    public CoordinatorGenericPacket(CoordinatorPacketType packetType, String id, String... value) {
        this.packetType = packetType;
        this.id = id;
        this.payload = new LinkedHashMap<String, String>();

        for(String val : value) {
            payload.put("message" + payloadPairCounter, val);
            payloadPairCounter++;
        }
    }

}
