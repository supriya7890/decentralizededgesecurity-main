/*
 *      Author: Nathaniel Brewer
 * 
 * 
 */
package coordinator.coordinator_connections;

public enum CoordinatorPriority {
    
    CRITICAL,       // Will try to keep alive since this is a critical device
    GENERIC         // Will remove since this device, if needed, can just reconnect

}
