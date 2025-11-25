/*
 *      Author: Nathaniel Brewer
 * 
 *      Since The server is handling information from both the Coordinator and the Nodes
 *      It needs to have two differnet storages for connections. This is the simpler way 
 *      than refactoring the Superclass of the handler
 */

package server.server_connections.server_connection_manager;

import server.server_connections.ServerConnectionDtoManager;
import server.server_connections.ServerPriority;
import server.server_packet.*;

public class ServerNodeConnectionManager extends ServerConnectionManager {

    // Step 1: (For my sanity) create an instance variable
    private static ServerNodeConnectionManager instance;

    // Step 2: Get instance (if one is not there it is created then returned)
    public static synchronized ServerNodeConnectionManager getInstance() {
        if (instance == null) {
            instance = new ServerNodeConnectionManager();
        }
        return instance;
    }

    @Override
    public boolean sendKeepAlive(ServerPacket keepAliveProbe) {

        activeConnections.forEach((id, connection) -> {
            if(connection.getPriority() == ServerPriority.CRITICAL){

                boolean keptAlive;

                keptAlive = new ServerConnectionDtoManager(connection).send(keepAliveProbe);

                // If the packet fails to send
                if(!keptAlive) {
                    terminateConnection(id);
                }
            }
        });
        return true;
    }

}
