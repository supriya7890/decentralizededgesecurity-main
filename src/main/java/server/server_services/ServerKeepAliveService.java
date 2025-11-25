/*
 *      Author: Nathaniel Brewer
 * 
 *      This is the main service for creating the keep alive packet as it will be an on-demand service that 
 *      creates the packet when needed, rather than storing in memory since packet contents will change.
 * 
 */
package server.server_services;

import java.util.LinkedHashMap;

import server.server_packet.*;
import server.server_packet.server_packet_class.*;

import server.server_connections.server_connection_manager.*;

public class ServerKeepAliveService {
    
    /**
     * Creates a KeepAlive packet with current server state
     * @param serverIP Server's IP address
     * @param nodeManager Connection manager for node count
     * @return Configured KeepAlive packet
     */
    public static ServerPacket createKeepAlivePacket(String serverIP, ServerConnectionManager nodeManager) {

        LinkedHashMap<String, String> payload = new LinkedHashMap<>();
        
        payload.put("timestamp", String.valueOf(System.currentTimeMillis()));
        payload.put("status", "ACTIVE");
        
        // Add system metrics
        //Runtime runtime = Runtime.getRuntime();
        //payload.put("memoryUsage", String.valueOf(runtime.totalMemory() - runtime.freeMemory()));
        
        return new ServerGenericPacket(
            ServerPacketType.KEEP_ALIVE,
            payload
        );
    }

    /**
     * Creates a KeepAlive probe to check if a CRITICAL connection is still alive
     * @param targetConnectionId ID of the connection to probe
     * @return KeepAlive probe packet
     */
    public static ServerPacket createKeepAliveProbe(String targetConnectionId) {
        LinkedHashMap<String, String> payload = new LinkedHashMap<>();
        
        // Essential probe information
        payload.put("timestamp", String.valueOf(System.currentTimeMillis()));
        payload.put("probeType", "CRITICAL_CHECK");
        payload.put("targetId", targetConnectionId);
        payload.put("responseRequired", "true");
        payload.put("timeout", "10000"); // 10 second timeout for response
        
        // Server status for context
        payload.put("serverStatus", "ACTIVE");
        payload.put("lastReceived", "OVERDUE");
        
        return new ServerGenericPacket(
            ServerPacketType.KEEP_ALIVE,
            payload
        );
    }

}