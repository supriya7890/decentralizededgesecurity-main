/*
 *      Author: Nathaniel Brewer
 * 
 *      This is where all the ConnectionInfo objects will be stored and where the storage wise checks will be had for
 *      expired keepAlive.
 * 
 *      This is desined as a Singleton Pattern, to prevent multiple instances of this connection map.
 *      
 */
package server.server_connections.server_connection_manager;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import server.server_connections.ServerConnectionDto;
import server.server_connections.ServerConnectionDtoManager;
import server.server_connections.ServerPriority;

import server.server_packet.*;

import server.server_services.ServerKeepAliveService;

public abstract class ServerConnectionManager {

    /*
     *      Abstraction methods
     */

    public abstract boolean sendKeepAlive(ServerPacket packet);

    protected final LinkedHashMap<String, ServerConnectionDto> activeConnections = new LinkedHashMap<>();

    // Each class can have its own logger instance
    private static final Logger logger = LogManager.getLogger(ServerConnectionManager.class);

    public void checkExpiredConnections() {

        // Create an iterator for the ConccurentHashMap since it needs an iterator
        Iterator<Map.Entry<String , ServerConnectionDto>> iterator =
            activeConnections.entrySet().iterator();

        while(iterator.hasNext()) {
            // Check if each entry is expired, if it is, check the priority to see if it needs to be removed or kept alive
            Map.Entry<String, ServerConnectionDto> entry = iterator.next();
            if(new ServerConnectionDtoManager(entry.getValue()).isExpired()){
                
                if(entry.getValue().getPriority() == ServerPriority.CRITICAL){

                    ServerPacket keepAliveProbe =  
                        ServerKeepAliveService.createKeepAliveProbe(
                            entry.getValue().getId()
                        );

                    boolean sendSuccess = new ServerConnectionDtoManager(entry.getValue()).send( keepAliveProbe );
                    if(!sendSuccess) {
                        logger.warn("Connection with Node: {} has been terminated due to failed retry!", entry.getValue().getId());
                        terminateConnection(entry.getValue().getId());
                    }
                }
                else {
                    logger.warn("Connection with Node: {} has been terminated due to priority status!", entry.getValue().getId()); 
                    terminateConnection(entry.getValue().getId());
                }
            }
        }
    }

    /*
     *      Response to expiry
     */

    public void terminateConnection(String id) {
        ServerConnectionDto remove = activeConnections.get(id);
        logger.info("Terminated Connection: \n  ID:" + remove.getId() 
            + "\n  IP - " + remove.getIp() + ":" + remove.getPort()
        );
        activeConnections.remove(remove.getId());
    }

    /*
     *      Mutators
     */

    // Can allow for multiple connections to be added at once(if needed)
    public void addConnection(ServerConnectionDto... connection) {
        for(ServerConnectionDto connected : connection) {
            activeConnections.put(connected.getId(), connected);
        }
    }

    // When recieving a 
    public void updateById(String id, Object updator) {
        ServerConnectionDto updatee = activeConnections.get(id);

        // TODO: Figure out a system for updating

       // updatee.update(updator)
    }

    /*
     *      Getters
     */
    public ServerConnectionDto getConnectionInfoById(String id) {
        return activeConnections.get(id);
    }

    public LinkedHashMap<String, ServerConnectionDto> getActiveConnections() {
        return activeConnections;
    }

    public String[] getAllIds() {
        return activeConnections.keySet().toArray(new String[0]);
    }

    public int getActiveConnectionCount() {

        int connectionCount = 0;

        Iterator<Map.Entry<String , ServerConnectionDto>> iterator =
            activeConnections.entrySet().iterator();

        while(iterator.hasNext()){
            connectionCount++;
        }

        return connectionCount;
    }
}
