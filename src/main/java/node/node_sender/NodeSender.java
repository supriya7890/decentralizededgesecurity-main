/*
 *      Author: Nathaniel Brewer
 * 
 *      This is the SuperClass any packet sender for the node. Since the node will
 *      be sending packets to the server(up) and to the nodes(down), each one
 *      will have their own packet sender object assigned to them, or their own 
 *      "Sender".
 * 
 *      A child of this class will be the a generic sender that will have grandchildren
 *      of their own. Each grandchild will be a 'Sender' for each individual packet type.
 *      However, this generic sender will hold the socket and act as the main point of
 *      sending. 
 * 
 *      Each of the packet types (maybe excluding the data packet since there could
 *      be different types) will be instantiated during the init process so they can 
 *      easily be accessed, modified, and sent since they will be stored in memory.
 * 
 *      An example of how this will work, we will follow the packet type of INITIALIZE.
 *      When the node starts up, the "PacketSender" will be instantiated(this is 
 *      the child of this class) this will hold the socket and be the main point of
 *      sending as previously stated, then a grandchild will be constructed for the 
 *      INITALIZE that will send the inital connection information. This will inherit
 *      the send command that will send through the socket that is constructed with 
 *      the child of the class you are current reading this in.
 * 
 * 
 *       will be made with the server
 *      (meaning that a init sender)
 */
package node.node_sender;

import java.util.LinkedHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import java.net.Socket;
import java.net.SocketTimeoutException;

// This will allow for Jsonification of packets before sending
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import node.edge_node.EdgeNode;
import node.node_lib.RuntimeTypeAdapterFactory;

import node.node_packet.*;
import node.node_packet.node_packet_class.*;

public abstract class NodeSender {

    // Each class can have its own logger instance
    private static final Logger logger = LogManager.getLogger(NodeSender.class);

    // A timer for retry and for other time based sending
    protected final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    protected Socket socket;    // Socket that sends the packet - will be instantiated on initalization

    // Stores the ip and the sending port to recreate the socket
    protected String ip;
    protected int sendingPort;
    
    /*     
     *          Abstract methods
     */

    // The packetSender will declare this
    public abstract boolean retry(NodePacket packet);


    // Starts the socket
    public void startSocket() throws IOException, SocketTimeoutException {
        this.socket = new Socket(
            this.ip, 
            this.sendingPort
        );
        this.socket.setSoTimeout(1000);
    };

   /*    This is needed in order to construct the packet class from Gson, it will throw an error
    *    without this package since the packet lass is a abstract. 
    *    This allows the Gson structure to build the packet without throwing that error
    *    This is a dynamic way of creating the packet, since we cannot register different subtypes 
    *    since RuntimeTypeAdapterFactory will allow for two unique subtypes with the same base class time
    *    e.g. .registerSubtype(ServerGenericPacket.class, ERROR.name())
    *    and  .registerSubtype(ServerGenericPacket.class, ACK.name())
    *   Will throw an "IllegalArgumentException: types and labels must be unique" exception
    */

    public NodePacket buildGsonWithPacketSubtype(NodePacketType type, String json) {
        RuntimeTypeAdapterFactory<NodePacket> packetAdapterFactory =
        RuntimeTypeAdapterFactory
            .of(NodePacket.class, "packetType")
            .registerSubtype(NodeGenericPacket.class, type.name());

        Gson tempGson = new GsonBuilder()
            .registerTypeAdapterFactory(packetAdapterFactory)
            .create();

        return tempGson.fromJson(json, NodePacket.class);
    }


    // This will send the completed packet
    public boolean send(NodePacket packet){

        try{
            startSocket();  

            // If the acknowledgement is not recieved then it will call upon retry (if the child class uses a retry method)
            boolean ackReceived = false;

            // This is where the node will wait for a proper Ack from the server - if not received, will retry 3 times
            String json = packet.toDelimitedString();     

            // Initalized inside nested control structure
            NodePacket responsePacket;

            // Creates the input and the output for the socket.
            PrintWriter output = new PrintWriter(
                this.socket.getOutputStream(), 
                true
            );

            // Sends the packet through the socket to the Server
            output.println(json);
            output.flush();
            logger.info("Sending packet of type {}", packet.getPacketType());

            // Will be where the response is read from
            BufferedReader input = new BufferedReader(
                new InputStreamReader(this.socket.getInputStream())
            );

            // Retrieves the response packet from the Server
            String response = input.readLine();

            // Checks if the payload is properly terminated. If not, the packet is incomplete or an unsafe packet was sent
            try{
                if(response == null) {
                    throw new Exception("No Response Packet Received!");
                }
                else if(!response.endsWith("||END||")) {
                    throw new IllegalArgumentException(
                        "Payload not properly terminated. "
                        + "\tPossible Causes:\n\t" 
                        + "- Incomplete Packet\n\t"
                        + "- Unsafe Packet"
                    );
                }

                // If true, the packet will remove the delimiter so it can properly deserialize the Json (Since ||END|| is not json)
                response = response.substring(
                    0, 
                    response.length() - "||END||".length()
                );

                // Pre-parses the Json in order to grab the packetType to use for the switch statement
                JsonObject jsonObj = JsonParser.parseString(response).getAsJsonObject();

                String packetTypeStr = jsonObj.get("packetType").getAsString();

                // Covert the string to the ENUM packetType
                NodePacketType packetType = NodePacketType.valueOf(packetTypeStr);

                // Sends the the json and the packetType to be built by Gson dynamically
                responsePacket = buildGsonWithPacketSubtype(packetType, response);

                // Print out the packet 
                logger.info(
                    "Response Recieved:\n" 
                    + "\n\tSender ID: \t" + responsePacket.getId() 
                    + "\n\tPacket Type: \t" + responsePacket.getPacketType() 
                    + "\n\tPayload: \t" + responsePacket.getPayload()  
                );

                LinkedHashMap<String, String> payload = responsePacket.getPayload();

                // If the Payload has an ID value
                if(payload.get("id") != null) {
                    payload.forEach( (k, v) -> {
                        if(k.equals("id")) {
                            EdgeNode.setNodeID(v);
                        }
                    });
                }

                // Set the Cluster Id
                if(payload.get("clusterId") != null) {
                    payload.forEach( (k, v) -> {
                        if(k.equals("clusterId")) {
                            EdgeNode.setClusterId(v);
                        }
                    });
                }

                // If the packet type is a ACK packet - then it is a good connection made and the Server will close this socket.
                if (responsePacket.getPacketType() != NodePacketType.ACK) {
                    throw new IllegalStateException("Expected ACK packet, but received: " + responsePacket.getPacketType());
                } else {
                    ackReceived = true; // Break out of while loop to contiune initalization
                }


            } catch(IllegalArgumentException illegalArg) {
                // The exception if the packet is empty or has no termination 
                logger.error("Error: " + illegalArg);
            } catch(IllegalStateException illegalState) {
                // The exception if the packet is not of the type ACK
                logger.error("Error: " + illegalState);
            } catch(Exception e) {
                logger.error("Unknown Error! " + e);
            } finally {
                // Always close the ports no matter the success status. 
                output.close();          //
                input.close();          // Close the port, we cannot reuse the same socket connection if a retry is needed
                this.socket.close();   //
            }
                
            // Return the true/false value of the packet being recieved
            return ackReceived;
                
        } catch(SocketTimeoutException e) {
            logger.error("Failed waiting on a response from server at " + this.ip + ":" + this.sendingPort + " " + e);
        } catch(IOException e) {
            logger.error("Failed to connect to server at " + this.ip + ":" + this.sendingPort + " " + e);
        } catch (Exception e) {
            logger.error("Unknown Error! " + e);
        }
        // Return false as if this section is reached, the packet was not sent properly meanign an error
        return false;
    }
}
