/*
 *      Author: Nathaniel Brewer
 * 
 *      Manages the cluster's ID for the server
 */

package server.server_services;

import java.security.SecureRandom;
import java.util.Base64;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class ServerClusterManager {
    private static final SecureRandom secureRandom = new SecureRandom();

    private static String clusterId;

    private static final Logger logger = LogManager.getLogger(ServerClusterManager.class);
    

    public static void initializeClusterIdentity() {

        clusterId = generateId();

        logger.info("Created Cluster ID : " + clusterId);

    }

    public static String generateId() {
        byte[] randomBytes = new byte[16];
        secureRandom.nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }

    public static String getClusterId() { return clusterId; }
}