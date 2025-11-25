/*
 *      Author: Nathaniel Brewer
 * 
 *      This is the main service for creating the keep alive packet as it will be an on-demand service that 
 *      creates the packet when needed, rather than storing in memory since packet contents will change.
 * 
 */
package node.node_services;

import java.util.LinkedHashMap;

import node.node_packet.*;
import node.node_packet.node_packet_class.*;

public class NodeKeepAliveService {
    
    /**
     * Creates a KeepAlive packet with current node state
     * @param nodeId Current node ID
     * @param nodeIP Node's IP address
     * @return Configured KeepAlive packet
     */
    public static NodePacket createKeepAlivePacket(String nodeId, String nodeIP) {

        LinkedHashMap<String, String> payload = new LinkedHashMap<>();
        
        payload.put("timestamp", String.valueOf(System.currentTimeMillis()));
        payload.put("nodeIP", nodeIP);
        payload.put("status", "ACTIVE");
        
        // Add system metrics
        //Runtime runtime = Runtime.getRuntime();
        //payload.put("memoryUsage", String.valueOf(runtime.totalMemory() - runtime.freeMemory()));
        
        return new NodeGenericPacket(
            NodePacketType.KEEP_ALIVE,
            nodeId,
            payload
        );

    }

}