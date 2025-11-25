/*
 *      Author: Nathaniel Brewer
 * 
 *      This is the main thread of the Server (2nd tier) of the hierarchy.
 *      The server is the middle man that connects the Node layer (bottom or 1sts tier)
 *      to the coordinator layer (top or the 3rd tier)
 *      
 *      In this file, an INITALIZATION packet is sent to the Coordinator to provide 
 *      it with the preferred listening port for further commands. This is part of the
 *      server Initalization process that will establish the connection to the Coordinator
 *      
 *      If the initalization is successful, then the server will create listeners for
 *      both the Node layer and the coordinator layer, on seperate ports. These listeners
 *      will be started on different threads.
 *      
 */
package server.edge_server;

import java.net.SocketException;
import java.net.UnknownHostException;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import server.server_config.ServerConfig;

import server.server_listener.ServerListener;

import server.server_packet.server_packet_class.*;
import server.server_services.ServerKeepAliveService;
import server.server_packet.*;

import server.server_connections.*;
import server.server_connections.server_connection_manager.*;

import server.server_services.ServerClusterManager;

public class EdgeServer {

    private static final Logger logger = LogManager.getLogger(EdgeServer.class);

    private static volatile String serverId = null;

    private static String IP;      // The IP of this devices

    private static ServerListener coordinatorListener;  // The socket that will be listening to requests from the Edge Coordinator
    private static ServerListener nodeListener;     // The socket that will be listening for Nodes

    private static ServerConfig config;

    private static ServerConnectionManager nodeConnectionManager;   // This will manage all connections and check keep alive
    private static ServerConnectionManager coordinatorConnectionManager;

    // Timer components
    private static ScheduledExecutorService timerScheduler; // The timer that will send out to keepAlives to the coordinator and check Node expiry   

    /*
     *  Initalize the Edge Server
     */
    // This is the function that will be called first that will have the inital handshake between the Coordinator
    public static void init() {

        // Create the connection manager
        nodeConnectionManager = ServerNodeConnectionManager.getInstance();

        // try to generate the IP from the machines IP - Throws UnknownHostException if it cannot determine
        try{
            IP = config.grabIP();
            logger.info("\t\tEDGE SERVER\tStarting Server at " + IP);
        } catch (UnknownHostException e) {
            logger.error("Error: Unable to determine local host IP address.\n" + e);
        } catch (SocketException e) {
            logger.error("Error: Unable to determine IP Address");
        }

        /*          Try to create senders        */
        try{

            coordinatorConnectionManager = ServerCoordinatorConnectionManager.getInstance();
            
            coordinatorConnectionManager.addConnection(
                new ServerConnectionDto(
                    "1",  // TEMP Will update when get the ack respones
                    config.getIPByKey("Coordinator.IP"), 
                    config.getPortByKey("Coordinator.listeningPort"),
                    ServerPriority.CRITICAL
                )
            );

            LinkedHashMap<String, String> payload = new LinkedHashMap<>();
            payload.put(
                "Server.listeningPort",
                String.valueOf(config.getPortByKey("Server.coordinatorListeningPort"))
            );

            // Create the initalization packet
            ServerPacket initPacket = new ServerGenericPacket(
                ServerPacketType.INITIALIZATION,                  
                payload
            );  

            new ServerConnectionDtoManager(coordinatorConnectionManager.getConnectionInfoById("1")).send(initPacket);
            
        } catch (Exception e) {
            logger.error("Error Sending Initalization Packet: " + e);
        }

        /*
         *          Listeners
         */

        // Instantiate a listening port for the coordinator
        try {
            coordinatorListener = new ServerListener(
                config.getPortByKey("Server.coordinatorListeningPort"), 
                5000,
                "coordinator"
            );
        } catch (Exception e) {
            logger.error(
                "Error creating Listening Socket on port " 
                + config.getPortByKey("Server.coordinatorListeningPort")
                + "\n" + e
            );
            // TODO: try to grab new port if this one is unavailable
        }

        // Instantiate a listening port for the Nodes
        try {
            nodeListener = new ServerListener(
                config.getPortByKey("Server.nodeListeningPort"),
                 1000,
                 "node"
            );
        } catch (Exception e) {
            logger.error(
                "Error creating Listening Socket on port " 
                + config.getPortByKey("Server.nodeListeningPort")
                + "\n" + e
            );
            // TODO: try to grab new port if this one is unavailable
        }
        /*
         *      Try and create Timers
         */
        try {
            initializeTimers();
        } catch (Exception e) {
            logger.error("Error creating Timers: \n" + e);
        }

        /*
         *     Create cluster ID 
         */

        try {
            ServerClusterManager.initializeClusterIdentity();
        } catch (Exception e) {
            logger.error("Error creating Cluster ID: " + e);
        }
    }
    /*
     *      ID assignment 
     */
    // Thread-safe setter for ID assignment
    public static synchronized void setServerId(String id) {
        serverId = id;
        logger.info("Server ID assigned: " + id);
    }

    public static synchronized String getServerId() {
        return serverId;
    }

    /* Creates the timers for KeepAlive sending and keep alive checking */ 
    private static void initializeTimers() {
        // Creates the timer for 
        timerScheduler = Executors.newScheduledThreadPool(2, r -> {
            Thread t = new Thread(r, "EdgeServer-Timer");
            //t.setDaemon(true);
            return t;
        });
        // Schedules the sending for the keepAlive packet every 30 seconds
        timerScheduler.scheduleAtFixedRate(() -> {
            try {
                boolean keepAliveSent;

                keepAliveSent = coordinatorConnectionManager.sendKeepAlive(
                    ServerKeepAliveService.createKeepAlivePacket(
                        IP,
                        nodeConnectionManager
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
        logger.info("Timer for sending keep Alive packets created!");

        // Schedules overdue packet check every 20 seconds
        timerScheduler.scheduleAtFixedRate(() -> {
            try{
                nodeConnectionManager.checkExpiredConnections();
            } catch (Exception e){
                logger.error("Exception in expiry timer: ", e);
            }
        }, 20, 20, TimeUnit.SECONDS);
        logger.info("Timer for checking Expired connections created!");
    }

    public static void main(String[] args) {

        // Create instance ID through command-line args

        String instanceId = args.length > 0 ? args[0] : null; // When starting the server arguments depicting an instance number (i.e. server1, server2)

        if(instanceId != null) {
            config = new ServerConfig(instanceId);
            logger.info("Starting Edge Server Instance: " + instanceId);
        } else {        
            config = new ServerConfig();
            logger.info("Starting default server config");
        }

        init();         // Begins the initalization process 

        // Starts listening for messages on from the coordinator
        Thread coordinatorThread = new Thread(coordinatorListener); 
        coordinatorThread.start();

        // Starts listening for messages from the Node
        Thread serverThread = new Thread(nodeListener);
        serverThread.start();

        // DEMO
        Scanner in = new Scanner(System.in);
        boolean on = true;
        while (on) {
            try{
                Thread.sleep(1000);
            } catch(InterruptedException e) {
                logger.error("Main thread interrrupted!\n" + e);
            }

            System.out.println("\nManually Send Message: ");
            String message = in.nextLine();

            if(!message.isEmpty()) {
                ServerPacket messagePacket = new ServerGenericPacket(
                    ServerPacketType.MESSAGE,
                    getServerId(),
                    message
                );

                System.out.println("Send message to Node or to Coordinator?");
                String recipient = in.nextLine();

                // Send message to the Node
                if(recipient.equals("node") || recipient.equals("Node")) {

                    String[] nodeIdArray = nodeConnectionManager.getAllIds();

                    HashMap<Integer, String> nodes = new HashMap<>();

                    boolean hasNum = false;
                    int trys = 0;

                    while(!hasNum) {
                        nodes.clear();
                        System.out.println("Chose the Node to send to based on the Number next to it!");
                        for(int i = 1; i < nodeIdArray.length + 1; i++) {
                            System.out.println(i + " : " + nodeIdArray[i-1]);
                            nodes.put(i, nodeIdArray[i-1]);
                        }
                        int nodeNum = in.nextInt();
                        if(!nodes.get(nodeNum).isEmpty()) {
                            new ServerConnectionDtoManager(
                                nodeConnectionManager.getConnectionInfoById(nodes.get(nodeNum))
                            ).send(messagePacket);
                            hasNum = true;
                        }
                        else if(trys > 2){
                            System.out.println("Try again next time loser!");
                            hasNum = true;
                        }
                        trys++;
                    }
                } else if(recipient.equals("coordinator") || recipient.equals("Coordinator")) {
                    new ServerConnectionDtoManager(
                        coordinatorConnectionManager.getConnectionInfoById("1"))
                        .send(messagePacket);
                }
                else {
                    System.out.println("Unknown Recipient! \nYour input was: " + recipient + "\nTry again.");
                }
            }
            // Clear message to prevent while loop running again
            message = "";
        }
        in.close();
    }
}