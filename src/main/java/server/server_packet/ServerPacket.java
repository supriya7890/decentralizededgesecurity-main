/*
 *      Author: Nathaniel Brewer
 * 
 *      Packet class that easily allows for creation of packets that are in the Json Format for
 *      digestability and ease of use.
 * 
 *      I chose the Json format for easy formatting purposes and the preservation of variables
 *      Gson Docs: https://github.com/google/gson/blob/main/UserGuide.md
 * 
 *      Need to find an approach that will tell the reciever when the end of the packet is reached:
 *          - Delimited approach (adding clear ending to the message such as: ||END||);
 *          - Add character counter, this seems like too much to run    
 * 
 */
package server.server_packet;

import java.util.LinkedHashMap;

import com.google.gson.Gson;    // external library that allows for jsonify of java objects. Located in root/lib 

import server.edge_server.EdgeServer;
import server.server_services.ServerClusterManager;

public abstract class ServerPacket {

    protected int payloadPairCounter = 0;

    protected String id;

    protected String clusterId;

    protected ServerPacketType packetType;     // Enum for easy constant assignment
    protected LinkedHashMap<String, String> payload;     

    // No-args constructor
    public ServerPacket() {
        id = EdgeServer.getServerId();
        clusterId = ServerClusterManager.getClusterId();
    } 

    public ServerPacket(LinkedHashMap<String, String> payload) {
        id = EdgeServer.getServerId();
        clusterId = ServerClusterManager.getClusterId();
        this.payload = payload;
    }

    /*
     * 
     *      Packet Type Methods
     * 
     */

    public ServerPacketType getPacketType() { return packetType; }

    public void setPacketType(ServerPacketType packetType) { this.packetType = packetType; }

    /*
     * 
     *      ID methods
     * 
     */

    public String getId() { return id; }

    public void setId(String id) { this.id = id; }

    /*
     * 
     *      Payload methods
     * 
     */

    public LinkedHashMap<String, String> getPayload() { return payload; }

    public void setPayload(LinkedHashMap<String, String> payload) { this.payload = payload; }

    public void addStringValue(String... value) {
        for(String val : value) {
            payload.put("Message" + payloadPairCounter, val);
            payloadPairCounter++;
        }
    }   
    
    public void addKeyValueToPayload(String key, String value) { payload.put(key, value); }

    public String[] getAllPayloadKeys() {
        return payload.keySet().toArray(new String[0]);
    }

    public String[] getAllPayloadValues() {
        return payload.values().toArray(new String[0]);
    } 

    public String getValueByKey(String key) {
        return payload.get(key);
    }

    /*
     * 
     *      Stringify Methods
     * 
     */

    // converts the packet to a key/value String
    public String toJson() {
        return new Gson().toJson(this);
    }

    // adds a clear end of message line that will be handled 
    public String toDelimitedString() {
        return new Gson().toJson(this) + "||END||";
    }

}
