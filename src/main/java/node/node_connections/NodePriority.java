/*
 *      Author: Nathaniel Brewer
 * 
 * 
 */
package node.node_connections;

public enum NodePriority {
    
    CRITICAL,       // Will try to keep alive since this is a critical device
    GENERIC         // Will remove since this device, if needed, can just reconnect

}
