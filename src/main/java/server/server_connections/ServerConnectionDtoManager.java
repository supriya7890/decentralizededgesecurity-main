/*
 *      Author: Nathaniel Brewer
 * 
 *      This is the manager for the Connection info DTO
 * 
 *      All will have expiry times in which either a connection will dropped or a KeepAlive will be sent from the manager to
 *      the connection. This will be dependent on the status of the connection. For example, one with a CRITICAL status will 
 *      try to be kept alive
 */

 package server.server_connections;

import java.time.LocalDateTime;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import server.server_sender.ServerPacketSender;

import server.server_packet.*;

public class ServerConnectionDtoManager {
    private ServerConnectionDto connectionInfo;
    // Gives each node connection it's own sender that will be created whenever 
    private ServerPacketSender sender;
    private Boolean hasSender = false;

    private static final Logger logger = LogManager.getLogger(ServerConnectionDtoManager.class);

    public ServerConnectionDtoManager(ServerConnectionDto connectionInfo) {
        this.connectionInfo = connectionInfo;
    }

    /*
     *      Main Methods
     */
    
    public boolean isExpired() {
        return connectionInfo.getLastActivity().plusSeconds(connectionInfo.getKeepAliveTimeout())
            .isBefore(LocalDateTime.now());
    }

    public void createSender() {

        if(connectionInfo.getPort() == 0) {
            logger.error("Port is not set for Node " + connectionInfo.getId() +"!");
            return;
        }
        // Create sender for this node
        try {
            this.sender = new ServerPacketSender(connectionInfo.getIp(), connectionInfo.getPort());
            hasSender = true;
            logger.info("Sender Created for Node: " + connectionInfo.getId() + " - " + connectionInfo.getIp() +":" + connectionInfo.getPort());
        } catch (Exception e) {
            logger.error("Failed to create sender for node " + connectionInfo.getId() + ":" + connectionInfo.getPort() + "\n" + e);
        }
    }

    public boolean send(ServerPacket packet) {
        if (sender == null || !hasSender) {
            // if no sender, create one
            createSender();
        }
        boolean sent = sender.send(packet);
        if(!sent) {
            boolean retry = sender.retry(packet);
            if(!retry){
                return false;
            }
        } else {
            return true;
        }

        logger.error("Sender was not created for Connection: {}! Cannot send packet", connectionInfo.getId());
        // TODO: Exception handling
        return false;
    }

    /*
     *      Getters
     */
    public boolean getSenderStatus() { return hasSender; }

    public ServerConnectionDto getConnectionDto() { return connectionInfo; }

}
