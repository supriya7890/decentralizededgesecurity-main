/*
 *      Author: Nathaniel Brewer
 * 
 *      This is the DTO for any individual connections that this layer may have. This will be the object that gets created
 *      any time a new connection is made. All these objects will be stored in memory since permanence is not a concern here.
 *      
 * 
 */
package node.node_connections;

import java.time.LocalDateTime;

public class NodeConnectionDto {

    private String id;
    private String ip;
    private int port;

    // This is when the expiration happens, which is twice the send time of each KeepAlive packet.
    private int keepAliveTimeoutSeconds;

    private NodePriority priority;
    private LocalDateTime lastActivity;
    private LocalDateTime initalConnectionTime;

    // Constructor
    public NodeConnectionDto(String id, String ip, int port, NodePriority priority) {
        this.id = id;
        this.ip = ip;
        this.port = port;
        this.priority = priority;
        initalConnectionTime = LocalDateTime.now();
        lastActivity = LocalDateTime.now();
        this.keepAliveTimeoutSeconds = 60;
    }

    /*
     *      Getters
     */
    public String getId() { return id; }

    public String getIp() { return ip; }

    public int getPort() { return port; }

    public NodePriority getPriority() { return priority; }

    public int getKeepAliveTimeout() { return keepAliveTimeoutSeconds; }

    public LocalDateTime getInitialConnectionTime() { return initalConnectionTime; }

    public LocalDateTime getLastActivity() { return lastActivity; }

    /*
     * 
     *      Setters
     * 
     */
    public void setId(String id) { this.id = id; }

    public void setIp(String ip) { this.ip = ip; }

    public void setPort(int port) { this.port = port; }

    public void setKeepAliveTimeout(int keepAliveTimeoutSeconds) { this.keepAliveTimeoutSeconds = keepAliveTimeoutSeconds; }

    public void setPriority(NodePriority priority) { this.priority = priority; }

    public void updateLastActivity() { this.lastActivity = LocalDateTime.now(); }

    /*
     *      Stringify
     */

    public String asString() { return "ID - "+id+" | IP - "+ip+":"+port+" ";}
}
