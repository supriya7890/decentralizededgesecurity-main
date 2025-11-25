/*
 *      Author: Nathaniel Brewer
 * 
 *      This is the main thread for the edge node (1st tier) of the hierarchy.
 *      This will have multiple instances per server and will switch between different servers
 *      
 *      In this file, an INITALIZATION packet is sent to the inital Server to provide 
 *      it with the preferred listening port for further commands. This is part of the
 *      node Initalization process that will establish the connection to the server
 *      
 *      If the initalization is successful, then a listener will be created for the 
 *      server. A sender will also be for the server
 */
package node.edge_node;

import java.net.SocketException;
import java.net.UnknownHostException;

import java.util.LinkedHashMap;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import node.node_listener.NodeListener;

import node.node_packet.*;
import node.node_packet.node_packet_class.*;

import node.node_config.NodeConfig;

import node.node_connections.*;

import node.node_connections.node_connection_manager.*;
import node.node_services.*;

public class EdgeNode {

    private static volatile String nodeId = null;

    private static volatile String clusterId = null;

    private static String IP;

    private static NodeListener serverListener;            // The socket that will be listening to requests from the Edge server.

    // Each class can have its own logger instance
    private static final Logger logger = LogManager.getLogger(EdgeNode.class);

    private static NodeServerConnectionManager serverConnectionManager; // Manage the connection between the server and the node

    // Timer components
    private static ScheduledExecutorService timerScheduler; //the timer that will send out the keepAlives to server

    private static NodeConfig config;

    /*
     *  Initalizes the Edge Node
     */
    public static void init() {

        // try/catch to generate the IP from ../config/Config.java - Throws UnknownHostException if it cannot determine the IP
        try{
            IP = config.grabIP();
            logger.info("Starting Node at " + IP);
        } catch (UnknownHostException e) {
            logger.error("Error: Unable to determine local host IP address.");
            e.printStackTrace();
        } catch (SocketException e){
            logger.error("Error: Unable to determine IP Address");
        }

        // Try to connect to the server
        try {
            // init connection with the server
            serverConnectionManager = NodeServerConnectionManager.getInstance();

            // Add connection to connection manager
            serverConnectionManager.addConnection(
                new NodeConnectionDto(
                    "1",
                    config.getIPByKey("Server.IP"),
                    config.getPortByKey("Server.listeningPort"),
                    NodePriority.CRITICAL
                )
            );

            // Create and store payload for INITALIZATION packet
            LinkedHashMap<String, String> payload = new LinkedHashMap<>();
            payload.put(
                "Node.listeningPort", 
                String.valueOf(config.getPortByKey("Node.listeningPort"))
            );

            // Create the initalization packet
            NodePacket initPacket = new NodeGenericPacket(
                NodePacketType.INITIALIZATION, 
                getNodeID(),                   
                payload
            );  

            // Send and get confirmation (ACK packet from server)
            boolean sent = new NodeConnectionDtoManager(serverConnectionManager.getConnectionInfoById("1")).send(initPacket);

            if(!sent) {
                logger.error("INITALIZATION PACKET WAS NOT SENT!");
            }

        } catch(Exception e){
            logger.error("Error Sending Initalization Packet: " + e);
        }

        /*
         *          Listeners
         */

        try {
            serverListener = new NodeListener(
                config.getPortByKey("Node.listeningPort"), 
                2000
            );
        } catch (Exception e) {
            logger.error(
                "Error creating Listening Socket on port " 
                + config.getPortByKey("Node.listeningPort")
                + e
            );
            // TODO: try to grab new port if this one is unavailable
        }
        /*
         *      Try and Create timers
         */
        try {
            initializeTimers();
        } catch (Exception e) {
            logger.error("Error creating Timers: \n" + e);
        }
    }
    /*
     *      ID assignment 
     */
    // Thread-safe setter for ID assignment
    public static synchronized void setNodeID(String id) {
        nodeId = id;
        logger.info("Node ID assigned: " + id);
    }

    public static synchronized String getNodeID() {
        return nodeId;
    }

    public static synchronized void setClusterId(String newClusterId) {
        clusterId = newClusterId;
        logger.info("Cluster ID assigned: " + newClusterId);
    }
    public static synchronized String getClusterId() {
        return clusterId;
    }

    /*
     *      Timer creation
     */
    private static void initializeTimers() {
        // Creates the timer for 
        timerScheduler = Executors.newScheduledThreadPool(2, r -> {
            Thread t = new Thread(r, "EdgeNode-Timer");
            //t.setDaemon(true);
            return t;
        });
        // Schedules the sending for the keepAlive packet every 30 seconds
        timerScheduler.scheduleAtFixedRate(() -> {
            try {
                boolean keepAliveSent;

                keepAliveSent = serverConnectionManager.sendKeepAlive(
                    NodeKeepAliveService.createKeepAlivePacket(
                        getNodeID(),
                        IP
                    )
                );

                if(!keepAliveSent){
                    logger.error("Keep alive was not sent!");
                    //TODO: something to stop the keep alive sender
                }

            } catch(Exception e) {
                logger.error("Exception in keep-alive timer: ", e);
            }
        }, 5, 30, TimeUnit.SECONDS);
    }

    /*
     *      Main Loop
     */

    public static void main(String[] args) {

        // Create instance ID through command-line args

        String instanceId = args.length > 0 ? args[0] : null; // When starting the server arguments depicting an instance number (i.e. server1, server2)

        if(instanceId != null) {
            config = new NodeConfig(instanceId);
            logger.info("Starting Edge Node Instance with ID: {}", instanceId);
        } else {        
            config = new NodeConfig();
            logger.info("Starting default Node config");
        }

        init();         // Begins the initalization process 

        Thread serverThread = new Thread(serverListener);
        serverThread.start();

        // TODO: DEMO
        Scanner in = new Scanner(System.in);

        boolean on = true;
        while(on){
            
            // TODO: DEMO
            System.out.println("Manually Send Message to Server: ");
            String message = in.nextLine();

            if(!message.isEmpty()) {
                NodePacket messagePacket = new NodeGenericPacket(
                    NodePacketType.MESSAGE,
                    getNodeID(),
                    message
                );

                new NodeConnectionDtoManager(serverConnectionManager.getConnectionInfoById("1")).send(messagePacket);
            }
        }       
        in.close();
    }
}
