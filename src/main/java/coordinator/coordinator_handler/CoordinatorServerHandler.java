/*
 *      Author: Nathaniel Brewer
 * 
 *      This is the main handling point Packets recieved from the server.
 *      All recieved packets will be sent from it's respective thread to
 *      here where the packet will be checked in this order:
 *          - Proper termination
 *              - Will respond with a failure packet if delimiter is not at the end
 *          - Packet Type 
 *              - Sends to a switch case with the different packet types which will in
 *                turn be handled differently dependent on the type. Each packet type
 *                handler will be in their own class
 * 
 *      Once the packet has been handled, the repective Packet Type handler will have 
 *      created a 'HandleResponse' object, that will be the response messages, exceptions
 *      (If there are exceptions) and the success status(Boolean). If the success status
 *      is true, then a ACK packet will be sent, else a Failure packet or Error packet will
 *      be sent instead.
 */

package coordinator.coordinator_handler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import java.net.Socket;

import java.util.UUID;
import java.util.Map;
import java.util.HashMap;
import java.util.function.Function;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import coordinator.coordinator_connections.*;

import coordinator.coordinator_handler.coordinator_packet_type_handler.*;

import coordinator.coordinator_lib.RuntimeTypeAdapterFactory;

import coordinator.coordinator_packet.*;
import coordinator.coordinator_packet.coordinator_packet_class.*;

import coordinator.edge_coordinator.EdgeCoordinator;

public class CoordinatorServerHandler implements Runnable {

    private static final Logger logger = LogManager.getLogger(CoordinatorServerHandler.class);

    private static CoordinatorConnectionManager connectionManager = CoordinatorConnectionManager.getInstance();

    private Socket serverSocket;
    private String serverIp;

    private CoordinatorPacket serverPacket;

    private BufferedReader reader;

    // This will will be instantiated based on the PacketType that needs to handle this. I.e initalizationHandler
    CoordinatorPacketHandler packetHandler;    

    // Packet designed to be sent back to the initial sender, generic type so the type will need to be specified on instantiation
    private CoordinatorPacket responsePacket;

    private CoordinatorHandlerResponse packetResponse;

    // Dictionary for lookup to handle different packet types
    private final Map<CoordinatorPacketType, Function<CoordinatorPacket, Void>> actionMap = new HashMap<>();

    public CoordinatorServerHandler(Socket socket) {
        this.serverSocket = socket;
        initActionMap();
    }

    private void initActionMap() {
        actionMap.put(CoordinatorPacketType.INITIALIZATION, this::handleInitalization);
        actionMap.put(CoordinatorPacketType.MESSAGE, this::handleMessage);
        actionMap.put(CoordinatorPacketType.KEEP_ALIVE, this::handleKeepAlive);
    }

    private void generatePacketResponse(CoordinatorPacket serverPacket) {
        // Allow for the packet response to be created based on the handling response
        packetResponse = packetHandler.handle(serverPacket);

        // If good send ack
        if (packetResponse.getSuccess() == true) {

            // Construct the ack packet
            ack(packetResponse);
        } else if (packetResponse.getSuccess() == false) {
            logger.error("Error Handling Packet of Type: \tINITIALIZATION\n\tDetails:");

            // Detail the errors
            packetResponse.printMessages();

            // Construct the failure packet based on the response
            failure(packetResponse);
        }
    }

    /*
     * 
     *          Actions 
     * 
     */

    private void readAction(CoordinatorPacketType packetAction, CoordinatorPacket serverPacket) {
        // Dictionary lookup
        actionMap.get(packetAction).apply(serverPacket);

        // Handle the packet
        generatePacketResponse(serverPacket);
    }

    private Void handleInitalization(CoordinatorPacket serverPacket) {
        // Assign an id to the server - this will be sent back to the server
        String serverId = UUID.randomUUID().toString();

        connectionManager.addConnection(
            new CoordinatorConnectionDto(
                serverId,
                serverIp,
                0, // TEMP, this will be updated inside the InitalizationHandler
                CoordinatorPriority.CRITICAL
            )
        );
        serverPacket.setId(serverId);

        // Create the packetType based handler
        packetHandler = new CoordinatorInitalizationHandler();

        // Need to return null to fulfill the 'Void' return data type   
        return null;
    }

    private Void handleMessage(CoordinatorPacket serverPacket) {
        // Create the packetType based handler
        packetHandler = new CoordinatorMessageHandler();

        return null;
    }

    private Void handleKeepAlive(CoordinatorPacket serverPacket) {
        // Create the packetType based handler
        packetHandler = new CoordinatorKeepAliveHandler();

        return null;
    }

    /*
     *          Packet Reconstruction
     */

    private CoordinatorPacket buildGsonWithPacketSubtype(CoordinatorPacketType type, String json) {
        RuntimeTypeAdapterFactory<CoordinatorPacket> packetAdapterFactory =
        RuntimeTypeAdapterFactory
            .of(CoordinatorPacket.class, "packetType")
            .registerSubtype(CoordinatorGenericPacket.class, type.name());

        Gson tempGson = new GsonBuilder()
            .registerTypeAdapterFactory(packetAdapterFactory)
            .create();

        return tempGson.fromJson(json, CoordinatorPacket.class);
    }

    /*          End Packet Reconstruction
     * 
     * 
     *          Responses
     * 
     */

    // This is a good response, it will be sent back to the server to ensure a packet was recieved 
    private void ack(CoordinatorHandlerResponse packetResponse) {

        responsePacket = new CoordinatorGenericPacket(
            CoordinatorPacketType.ACK,        // Packet type
            EdgeCoordinator.getCoordinatorId(),             // Sender
            packetResponse.combineMaps()    // Payload
        );
        respond();
    }

    // This is the creation of the failure packet based on the packet response 
    private void failure(CoordinatorHandlerResponse packetResponse) {
        
        // Construct a new failure packet
        responsePacket = new CoordinatorGenericPacket(
            CoordinatorPacketType.ERROR,            // Packet type
            EdgeCoordinator.getCoordinatorId(),                   // Sender
            packetResponse.combineMaps() // Payload
        );
        // Send the packet
        respond();
    }

    /*          End Responses
     * 
     * 
     *          Respond
     * 
     */

    // Takes an already initalized response packet and returns to sender
    private void respond() {

        // Puts the contents of the packet to JSON with a non-JSON compatable delimiter at the end to be handled prior to pakcet content hanlding
        String json = responsePacket.toDelimitedString();

        try{
            // The responder object
            PrintWriter output = new PrintWriter(
                serverSocket.getOutputStream(), 
                true
            );
            // Send the jsonified packet as a response
            output.println(json);
            output.close();
        } catch (IOException e) {
            logger.error("Error sending response packet of type: " + responsePacket.getPacketType() + "\n"+ e);
        }
    }
    /*
     *                      Main run loop
     */

    @Override
    public void run(){

        logger.info(
            "Server connected: \n\t"
            + serverSocket.getInetAddress().toString()
            + ":" 
            + serverSocket.getPort()
        );

        // Handle client events
        try{

            // This is what decodes the incoming packet
            reader = new BufferedReader(
                new InputStreamReader(
                    serverSocket.getInputStream()
                )
            );
            
            // Stores the payload as a string to check (and potentially remove) the delimiter
            String payload = reader.readLine();

            // Checks if the payload is properly terminated. If not, the packet is incomplete or an unsafe packet was sent
            if(payload.endsWith("||END||")){
                payload = payload.substring(0, payload.length() - "||END||".length());
            }
            else{
                failure(new CoordinatorHandlerResponse(
                    false, 
                    new Exception("Payload not terminated."), 
                    "Incomplete Packet")
                );
                return; // Early exit
            }

            // Reads the packet as json
            String json = payload;

            // Checks if empty packet
            if (json != null) {

                // Grabs the server IP in order to be saved in config file
                serverIp = serverSocket.getInetAddress().toString();
                serverIp = serverIp.substring(1); // Removes the forward slash

                // Pre-parses the Json in order to grab the packetType to use for the switch statement
                JsonObject jsonObj = JsonParser.parseString(json).getAsJsonObject();

                String packetTypeStr = jsonObj.get("packetType").getAsString();

                // Covert the string to the ENUM packetType
                CoordinatorPacketType packetType = CoordinatorPacketType.valueOf(packetTypeStr);

                try {
                    // Reconstruct the recieved packet into a 'CoordinatorPacket' type 
                    serverPacket = buildGsonWithPacketSubtype(packetType, json);
                    //logger.info("Recieved:\n" + serverPacket.toJson());
                    readAction(packetType, serverPacket); 
                } catch(IllegalArgumentException e) {
                    logger.error("Recieved unknown packet of type: " + packetTypeStr);
                    return; // Early exit
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if( reader != null){ reader.close(); }
                if( serverSocket != null && !serverSocket.isClosed()) { serverSocket.close(); }
            } catch (IOException e) {
                logger.error("Error closing socket!\n" + e);
            }
        }   
    }
}