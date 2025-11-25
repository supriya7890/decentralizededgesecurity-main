/*      
 *      Author: Nathaniel Brewer
 * 
 *      This is the child of "Sender". 
 */

package server.server_sender;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import server.server_packet.*;

public class ServerPacketSender extends ServerSender{

    protected static final Logger logger = LogManager.getLogger(ServerPacketSender.class);

    protected int maxRetries = 3;
    protected int attempts = 0;
    protected boolean ackRecieved;
    
    //
    public ServerPacketSender(String ip, int sendingPort) { 
        this.ip = ip;
        this.sendingPort = sendingPort;
    }

    /*  
     *  The default method for retrying packet sending, if a failure occurs then retry will be invoked. 
     *  This can and more than likely will be overwritten with certain PacketTypes that may not want to retry.
     * 
    */
    public boolean retry(ServerPacket packet) {
        ackRecieved = false;
        while (!ackRecieved){
            // If the attempt limit is reached the server will shutdown
            if (attempts == maxRetries) {
                logger.error("Attempt limit reached trying to recieve ACK!");
                attempts = 0;
                return ackRecieved;
            }
            // Retry the connection - must reopen the socket to create a new connection
            else if (attempts < maxRetries && !ackRecieved) {
                // Wait to retry and increment attempts after 1 second
                try{
                    Thread.sleep(1000);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
                logger.warn("Failed to recieve ACK - retrying...");      
                ackRecieved = send(packet);
            }
            // Inc attemps
            attempts++;
        }
        // reset attempts for reuses (if applicable)
        attempts = 0;
        return ackRecieved;
    }  
}
