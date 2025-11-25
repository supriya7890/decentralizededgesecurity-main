/*
 *      Author: Nathaniel Brewer
 * 
 *      This is where all the ConnectionInfo objects will be stored and where the storage wise checks will be had for
 *      expired keepAlive.
 * 
 *      This is desined as a Singleton Pattern, to prevent multiple instances of this connection map.
 *      
 */
package coordinator.coordinator_connections;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import coordinator.edge_coordinator.EdgeCoordinator;

import coordinator.coordinator_connections.CoordinatorConnectionDto;
import coordinator.coordinator_connections.CoordinatorConnectionDtoManager;
import coordinator.coordinator_connections.CoordinatorPriority;

import coordinator.coordinator_packet.*;

import coordinator.coordinator_services.CoordinatorKeepAliveService;

public class CoordinatorConnectionManager {

    // Each class can have its own logger instance
    private static final Logger logger = LogManager.getLogger(CoordinatorConnectionManager.class);
        
    // This "ConcurentHashMap" allows for multi-threaded visability.
    private final Map<String, CoordinatorConnectionDto> activeConnections = new ConcurrentHashMap<>();

    // Step 1: (For my sanity) create an instance variable
    private static CoordinatorConnectionManager instance;

    // Step 2: No-Args private constructor to prevent external instantiation
    private CoordinatorConnectionManager() { }

    // Step 3: Get instance (if one is not there it is created then returned)
    public static synchronized CoordinatorConnectionManager getInstance() {
        if (instance == null) {
            instance = new CoordinatorConnectionManager();
        }
        return instance;
    }

    public void checkExpiredConnections() {

        // Create an iterator for the ConccurentHashMap since it needs an iterator
        Iterator<Map.Entry<String , CoordinatorConnectionDto>> iterator =
            activeConnections.entrySet().iterator();

        while(iterator.hasNext()) {
            // Check if each entry is expired, if it is, check the priority to see if it needs to be removed or kept alive
            Map.Entry<String, CoordinatorConnectionDto> entry = iterator.next();
            if(new CoordinatorConnectionDtoManager(entry.getValue()).isExpired()){
                
                if(entry.getValue().getPriority() == CoordinatorPriority.CRITICAL){

                    CoordinatorPacket keepAliveProbe =  
                        CoordinatorKeepAliveService.createKeepAliveProbe(
                            EdgeCoordinator.getCoordinatorId(),
                            entry.getValue().getId()
                        );

                    boolean sendSuccess = new CoordinatorConnectionDtoManager(entry.getValue()).send( keepAliveProbe );
                    if(!sendSuccess) {
                        logger.warn("Connection with Server: {} has been terminated due to failed retry!", entry.getValue().getId());
                        terminateConnection(entry.getValue().getId());
                    }
                }
                else {
                    logger.warn("Connection with Server: {} has been terminated due to priority status!", entry.getValue().getId()); 
                    terminateConnection(entry.getValue().getId());
                }
            }
        }
    }

    /*
     *      Response to expiry
     */

    public boolean sendKeepAlive(CoordinatorPacket keepAliveProbe) {

        activeConnections.forEach((id, connection) -> {
            if(connection.getPriority() == CoordinatorPriority.CRITICAL){

                boolean keptAlive;

                keptAlive = new CoordinatorConnectionDtoManager(connection).send(keepAliveProbe);

                // If the packet fails to send
                if(!keptAlive) {
                    terminateConnection(id);
                }
            }
        });
        return true;
    }

    public void terminateConnection(String id) {
        CoordinatorConnectionDto remove = activeConnections.get(id);
        logger.info("Terminated Connection: \n  ID:" + remove.getId() 
            + "\n  IP - " + remove.getIp() + ":" + remove.getPort()
        );
        activeConnections.remove(remove.getId());
    }

    /*
     *      Mutators
     */

    // Can allow for multiple connections to be added at once(if needed)
    public void addConnection(CoordinatorConnectionDto... connection) {
        for(CoordinatorConnectionDto connected : connection) {
            activeConnections.put(connected.getId(), connected);
        }
    }

    // When recieving a 
    public void updateById(String id, Object updator) {
        CoordinatorConnectionDto updatee = activeConnections.get(id);

        // TODO: Figure out a system for updating

       // updatee.update(updator)
    }

    /*
     *      Getters
     */
    public CoordinatorConnectionDto getConnectionInfoById(String id) {
        return activeConnections.get(id);
    }

    public Map<String, CoordinatorConnectionDto> getActiveConnections() {
        return activeConnections;
    }

    public String[] getAllIds() {
        return activeConnections.keySet().toArray(new String[0]);
    }

    public int getActiveConnectionCount() {

        int connectionCount = 0;

        Iterator<Map.Entry<String , CoordinatorConnectionDto>> iterator =
            activeConnections.entrySet().iterator();

        while(iterator.hasNext()){
            connectionCount++;
        }

        return connectionCount;
    }
}
