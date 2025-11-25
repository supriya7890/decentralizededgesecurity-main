/*
 *      Author: Nathaniel Brewer
 *      
 *      This is the connection manager that handles the server and the node's connection to it
 */
package node.node_connections.node_connection_manager;

import node.node_connections.NodeConnectionDtoManager;
import node.node_connections.NodePriority;
import node.node_packet.*;

public class NodeServerConnectionManager extends NodeConnectionManager {

    // Step 1: (For my sanity) create an instance variable
    private static NodeServerConnectionManager instance;

    // Step 2: Get instance (if one is not there it is created then returned)
    public static synchronized NodeServerConnectionManager getInstance() {
        if (instance == null) {
            instance = new NodeServerConnectionManager();
        }
        return instance;
    }

    @Override
    public boolean sendKeepAlive(NodePacket keepAliveProbe) {

        activeConnections.forEach((id, connection) -> {
            if(connection.getPriority() == NodePriority.CRITICAL){

                boolean keptAlive;

                keptAlive = new NodeConnectionDtoManager(connection).send(keepAliveProbe);

                // If the packet fails to send
                if(!keptAlive) {
                    terminateConnection(id);
                }
            }
        });
        return true;
    }

}