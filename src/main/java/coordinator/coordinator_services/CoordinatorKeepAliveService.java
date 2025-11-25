/*
 *      Author: Nathaniel Brewer
 * 
 *      This is the main service for creating the keep alive packet as it will be an on-demand service that 
 *      creates the packet when needed, rather than storing in memory since packet contents will change.
 * 
 */
package coordinator.coordinator_services;

import java.util.LinkedHashMap;

import coordinator.coordinator_packet.*;
import coordinator.coordinator_packet.coordinator_packet_class.*;

public class CoordinatorKeepAliveService {

    /**
     * Creates a KeepAlive probe to check if a CRITICAL connection is still alive
     * @param coordinatorId Current coordinator ID
     * @param targetConnectionId ID of the connection to probe
     * @return KeepAlive probe packet
     */
    public static CoordinatorPacket createKeepAliveProbe(String coordinatorId, String targetConnectionId) {
        LinkedHashMap<String, String> payload = new LinkedHashMap<>();
        
        // Essential probe information
        payload.put("timestamp", String.valueOf(System.currentTimeMillis()));
        payload.put("probeType", "CRITICAL_CHECK");
        payload.put("targetId", targetConnectionId);
        payload.put("timeout", "10000"); // 10 second timeout for response
        
        // coordinator status for context
        payload.put("coordinatorStatus", "ACTIVE");
        payload.put("lastReceived", "OVERDUE");
        
        return new CoordinatorGenericPacket(
            CoordinatorPacketType.KEEP_ALIVE,
            coordinatorId,
            payload
        );
    }

}