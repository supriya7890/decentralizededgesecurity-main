/*
 *      Author: Nathaniel Brewer
 * 
 *      This is the manager for the Connection info DTO
 * 
 *      All will have expiry times in which either a connection will dropped or a KeepAlive will be sent from the manager to
 *      the connection. This will be dependent on the status of the connection. For example, one with a CRITICAL status will 
 *      try to be kept alive
 */

 package coordinator.coordinator_connections;

import java.time.LocalDateTime;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import coordinator.coordinator_sender.CoordinatorPacketSender;

import coordinator.coordinator_packet.*;

public class CoordinatorConnectionDtoManager {
    private CoordinatorConnectionDto connectionInfo;
    // Gives each node connection it's own sender that will be created whenever 
    private CoordinatorPacketSender sender;
    private Boolean hasSender = false;

    private static final Logger logger = LogManager.getLogger(CoordinatorConnectionDtoManager.class);

    public CoordinatorConnectionDtoManager(CoordinatorConnectionDto connectionInfo) {
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
            logger.error("Port is not set for Server " + connectionInfo.getId() +"!");
            return;
        }
        // Create sender for this node
        try {
            this.sender = new CoordinatorPacketSender(connectionInfo.getIp(), connectionInfo.getPort());
            hasSender = true;
            logger.info("Sender Created for Server: " + connectionInfo.getId() + " - " + connectionInfo.getIp() +":" + connectionInfo.getPort());
        } catch (Exception e) {
            logger.error("Failed to create sender for Server " + connectionInfo.getId() + ":" + connectionInfo.getPort() + "\n" + e);
        }
    }

    public boolean send(CoordinatorPacket packet) {
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

    public CoordinatorConnectionDto getConnectionDto() { return connectionInfo; }

}
