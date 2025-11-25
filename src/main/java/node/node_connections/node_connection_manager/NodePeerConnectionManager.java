/*
 *      Author: Nathaniel Brewer
 * 
 *      This class will handle all peer - peer connections. Some specific calls will be:
 *      
 *      PeerSigReq - Send a peer (based either randomly or on some other criteria) and send a signature
 *                   request that will sign the requesting node's ID. 
 * 
 *      
 */
package node.node_connections.node_connection_manager;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import node.node_connections.*;
import node.node_packet.*;

public class NodePeerConnectionManager extends NodeConnectionManager {

    protected final Map<String, NodeConnectionDto> activeConnections = new ConcurrentHashMap<>();

    private static final Logger logger = LogManager.getLogger(NodePeerConnectionManager.class);

        // Step 1: (For my sanity) create an instance variable
    private static NodePeerConnectionManager instance;

    // Step 2: No-Args private constructor to prevent external instantiation
    private NodePeerConnectionManager() { }

    // Step 3: Get instance (if one is not there it is created then returned)
    public static synchronized NodePeerConnectionManager getInstance() {
        if (instance == null) {
            instance = new NodePeerConnectionManager();
        }
        return instance;
    }

    /*
     *      Will send a specific node inside a cluster to sign this current Node's ID.
     */
    public boolean PeerSigReq(NodePacket requestPacket) {

        return true;
    }

    public boolean sendKeepAlive(NodePacket packet) {
        return false;
    }

}
