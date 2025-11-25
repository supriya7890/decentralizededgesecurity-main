/*
 *      Author: Nathaniel Brewer
 * 
 *      This is where all the ConnectionInfo objects will be stored and where the storage wise checks will be had for
 *      expired keepAlive.
 * 
 *      This is desined as a Singleton Pattern, to prevent multiple instances of this connection map.
 *      
 */
package node.node_connections.node_connection_manager;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import node.edge_node.EdgeNode;

import node.node_connections.NodeConnectionDto;
import node.node_connections.NodeConnectionDtoManager;
import node.node_connections.NodePriority;

import node.node_packet.*;

import node.node_services.NodeKeepAliveService;

public abstract class NodeConnectionManager {

    /*
     *      Abstraction methods
     */

    public abstract boolean sendKeepAlive(NodePacket packet);

    protected final Map<String, NodeConnectionDto> activeConnections = new ConcurrentHashMap<>();

    // Each class can have its own logger instance
    private static final Logger logger = LogManager.getLogger(NodeConnectionManager.class);

    public void checkExpiredConnections() {

        // Create an iterator for the ConccurentHashMap since it needs an iterator
        Iterator<Map.Entry<String , NodeConnectionDto>> iterator =
            activeConnections.entrySet().iterator();

        while(iterator.hasNext()) {
            // Check if each entry is expired, if it is, check the priority to see if it needs to be removed or kept alive
            Map.Entry<String, NodeConnectionDto> entry = iterator.next();
            if(new NodeConnectionDtoManager(entry.getValue()).isExpired()){
                
                if(entry.getValue().getPriority() == NodePriority.CRITICAL){

                    NodePacket keepAliveProbe =  
                        NodeKeepAliveService.createKeepAlivePacket(
                            EdgeNode.getNodeID(),
                            entry.getValue().getId()
                        );

                    boolean sendSuccess = new NodeConnectionDtoManager(entry.getValue()).send( keepAliveProbe );
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

    public void terminateConnection(String id) {
        NodeConnectionDto remove = activeConnections.get(id);
        logger.info("Terminated Connection: \n  ID:" + remove.getId() 
            + "\n  IP - " + remove.getIp() + ":" + remove.getPort()
        );
        activeConnections.remove(remove.getId());
    }

    /*
     *      Mutators
     */

    // Can allow for multiple connections to be added at once(if needed)
    public void addConnection(NodeConnectionDto... connection) {
        for(NodeConnectionDto connected : connection) {
            activeConnections.put(connected.getId(), connected);
        }
    }

    // When recieving a 
    public void updateById(String id, Object updator) {
        NodeConnectionDto updatee = activeConnections.get(id);

        // TODO: Figure out a system for updating

       // updatee.update(updator)
    }

    /*
     *      Getters
     */
    public NodeConnectionDto getConnectionInfoById(String id) {
        return activeConnections.get(id);
    }

    public Map<String, NodeConnectionDto> getActiveConnections() {
        return activeConnections;
    }

    public String[] getAllIds() {
        return activeConnections.keySet().toArray(new String[0]);
    }

    public int getActiveConnectionCount() {

        int connectionCount = 0;

        Iterator<Map.Entry<String , NodeConnectionDto>> iterator =
            activeConnections.entrySet().iterator();

        while(iterator.hasNext()){
            connectionCount++;
        }

        return connectionCount;
    }
}
