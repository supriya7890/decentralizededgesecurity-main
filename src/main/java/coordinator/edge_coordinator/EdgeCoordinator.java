/*
 *      Author: Nathaniel Brewer
 * 
 *      This is the top of the hierarchy in our edge network, this is the connection point between the
 *      hierarchy and the cloud. Since this is the top of the network, it's instantiation is the highest
 *      priority, and will be started first.
 * 
 *      It's only direct child is the Server. This coordinator may potentially have multiple so that will
 *      need to be considered
 * 
 *      When started the Coordinator will connected to 'coordinatorConfig.properties' through the 
 *      coordinatorConfig.java file as the 'config' object. This will allow for the getting of the IP address
 *      and the Port(s).
 * 
 *      After these items are grabbed, a listener for the server will be instantiated to be used later. This
 *      will finish the initalization process. Once this is done, the listener will be thrown into the thread
 *      to be constantly ran seperate of this project
 */
package coordinator.edge_coordinator;

import java.io.IOException;

import java.net.SocketException;
import java.net.UnknownHostException;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.Scanner;
import java.util.UUID;
import java.util.HashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import coordinator.coordinator_config.CoordinatorConfig;

import coordinator.coordinator_listener.CoordinatorListener;

import coordinator.coordinator_packet.*;
import coordinator.coordinator_packet.coordinator_packet_class.*;
import node.node_config.NodeConfig;
import coordinator.coordinator_connections.*;

public class EdgeCoordinator {

    private static final Logger logger = LogManager.getLogger(EdgeCoordinator.class);

    private static volatile String coordinatorId = null;

    private static CoordinatorConnectionManager serverConnectionManager;

    private static String IP;

    private static CoordinatorListener serverListener;  // The socket that will be listening to requests from the Edge server.

    private static ScheduledExecutorService timerScheduler;

    private static CoordinatorConfig config;

    /*
     *  Initalizes the Node Coordinator - This will be the first thing that runs when the Node Coordinator is started up
     */

    public static void init() {

        // Give the Coordinator an ID
        setCoordinatorId(UUID.randomUUID().toString());

        serverConnectionManager = CoordinatorConnectionManager.getInstance();

        // try/catch to generate the IP from ./Config.java - Throws UnknownHostException if it cannot determine the IP
        try{
            logger.info("\t\tEDGE COORDINATOR");
            // Get the IP address for this coordinator
            IP = config.grabIP(); 
        } catch (UnknownHostException e) {
            logger.error("Error: Unable to determine local host IP address.\n" + e);
        } catch (SocketException e) {
            logger.error("Error: Unable to determine IP Address");
        }

        // Try to create a serverSocket to listen to requests 
        try {
            // Construct the listener - sending the
            serverListener = new CoordinatorListener(
                config.getPortByKey("Coordinator.listeningPort"),
                 5000
            );  

        } catch (IOException e) {
            logger.error(
                "Error creating Listening Socket on port " 
                + config.getPortByKey("Coordinator.listeningPort")
                + "\n" + e
            );
            // TODO: try to grab new port if this one is unavailable
        } catch (Exception e) {
            logger.error("Unknown Error creating listener ports!\n" + e);
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
    public static synchronized void setCoordinatorId(String id) {
        coordinatorId = id;
        logger.info("Coordinator ID assigned: " + id);
    }

    public static synchronized String getCoordinatorId() {
        return coordinatorId;
    }

        /*
     *      Timer creation
     */
    private static void initializeTimers() {
        // Creates the timer for 
        timerScheduler = Executors.newScheduledThreadPool(2, r -> {
            Thread t = new Thread(r, "EdgeCoordinator-Timer");
            //t.setDaemon(true);
            return t;
        });
        // Schedules overdue packet check every 20 seconds
        timerScheduler.scheduleAtFixedRate(() -> {
            try{
                serverConnectionManager.checkExpiredConnections();
            } catch (Exception e){
                logger.error("Exception in expiry timer: ", e);
            }
        }, 20, 20, TimeUnit.SECONDS);
        logger.info("Timer for checking Expired connections created!");
    }
    /*
     *          MAIN
     */
    public static void main(String[] args) {

        String instanceId = args.length > 0 ? args[0] : null; // Can start multiple instances of the Coordinator that can load the

        if(instanceId != null) {
            // Start up new instance of a coordinator with a specific config file
            config = new CoordinatorConfig(instanceId);
            logger.info("Starting Coordinator Instance with ID: {}", instanceId);
        } else {
            config = new CoordinatorConfig();
            logger.info("Starting default Coordinator Config");
        }

        init();         // Begins the initalization process 

        // Instaniate the thread and send the serverListener to it
        Thread listeningThread = new Thread(serverListener);

        // Start the thread
        listeningThread.start();

        // TODO: DEMO
        Scanner in = new Scanner(System.in);

        boolean on = true;
        while(on){
            // TODO: Stuff
            System.out.println("Manually send message to server: ");
            String message = in.nextLine();

            if(!message.isEmpty()) {
                CoordinatorPacket messagePacket = new CoordinatorGenericPacket(
                    CoordinatorPacketType.MESSAGE,
                    getCoordinatorId(),
                    message
                );

                String[] serverIdArray = serverConnectionManager.getAllIds();

                HashMap<Integer, String> servers = new HashMap<>();

                boolean hasNum = false;

                int trys = 0;

                while(!hasNum) {
                    servers.clear();
                    System.out.println("Chose the Server to send to based on the Number next to it!");
                    for(int i = 1; i < serverIdArray.length + 1; i++) {
                        System.out.println(i + " : " + serverIdArray[i-1]);
                        servers.put(i, serverIdArray[i-1]);
                    }
                    int serverNum = in.nextInt();
                    if(!servers.get(serverNum).isEmpty()) {
                        new CoordinatorConnectionDtoManager(
                            serverConnectionManager.getConnectionInfoById(servers.get(serverNum)))
                            .send(messagePacket);
                        hasNum = true;
                    }
                    else if(trys > 2){
                        System.out.println("Try again next time loser!");
                        hasNum = true;
                    }
                    trys++;
                }
            }
        }
        in.close();
    }
}