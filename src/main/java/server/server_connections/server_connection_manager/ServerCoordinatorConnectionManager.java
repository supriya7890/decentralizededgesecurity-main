/*
 *      Author: Nathaniel Brewer
 * 
 *      Since The server is handling information from both the Coordinator and the Nodes
 *      It needs to have two differnet storages for connections. This is the simpler way 
 *      than refactoring the Superclass of the handler
 */

package server.server_connections.server_connection_manager;

import java.util.Iterator;
import java.util.Map;

import server.server_connections.ServerConnectionDtoManager;
import server.server_connections.ServerConnectionDto;
import server.server_packet.ServerPacket;

public class ServerCoordinatorConnectionManager extends ServerConnectionManager {

    // Step 1: (For my sanity) create an instance variable
    private static ServerCoordinatorConnectionManager instance;

    // Step 2: Get instance (if one is not there it is created then returned)
    public static synchronized ServerConnectionManager getInstance() {
        if (instance == null) {
            instance = new ServerCoordinatorConnectionManager();
        }
        return instance;
    }

    @Override
    public boolean sendKeepAlive(ServerPacket keepAlive) {

        boolean sent = false;

        Iterator<Map.Entry<String , ServerConnectionDto>> iterator =
            activeConnections.entrySet().iterator();

        while(iterator.hasNext()){
            Map.Entry<String, ServerConnectionDto> entry = iterator.next();
            sent = new ServerConnectionDtoManager(entry.getValue()).send(keepAlive); 
        }
        return sent;
    }
}
