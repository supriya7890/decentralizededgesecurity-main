package server.server_connections;

public enum ServerPriority {
    
    CRITICAL,       // Will try to keep alive since this is a critical device
    GENERIC         // Will remove since this device, if needed, can just reconnect

}
