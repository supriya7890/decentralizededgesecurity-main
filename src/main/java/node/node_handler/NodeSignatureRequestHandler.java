package node.node_handler.node_packet_type_handler;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import node.edge_node.EdgeNode;
import node.node_packet.node_packet_class.NodeSignedCredentialPacket;

public class NodeSignatureRequestHandler extends NodePacketHandler {

    private static final Logger logger = LogManager.getLogger(NodeSignatureRequestHandler.class);

    @Override
    public NodeHandlerResponse process() {
        try {
            // Step 1: Extract request details from the generic packet
            // The base handler (NodePacketHandler) would typically load payload into PayloadKeyValuePairs
            String transactionId = recievedPacket.getValueByKey("transactionId");
            String dataToSign = recievedPacket.getValueByKey("dataToSign"); // Data Node A asked Node B to sign

            logger.info("Received SIGNATURE_REQUEST: Transaction ID: {} | Data to Sign: {}", transactionId, dataToSign);

            // ----------------------------------------------------------------------
            // Step 2: Perform cryptographic operations (Placeholder for PGP logic)
            // This is whereNode B signs the dataToSign using its private PGP key.
            
            
            // --- PLACEHOLDER DATA START (To be replaced by actual PGP signing logic) ---
            String nodeBPgpPublicKey = "B_PUBLIC_KEY_CONTENT_PGP_FORMAT"; 
            String pgpSignature = "B_SIGNED_HASH_OF_" + dataToSign + "_" + transactionId; 
            String signedDataHash = "HASH_OF_" + dataToSign; // Represents the hash of the data signed by Node B
            // --- PLACEHOLDER DATA END ---


            // Step 3: Create the response packet (SIGNED_CREDENTIAL)
            NodeSignedCredentialPacket responsePacket = new NodeSignedCredentialPacket(
                EdgeNode.getNodeID(), // Node B's ID (the sender of the response)
                transactionId,
                nodeBPgpPublicKey,
                pgpSignature,
                signedDataHash
            );
            
            
            packetResponse = new NodeHandlerResponse(true, "Signed credential generated and ready to send.");
            // Pass the entire response packet back in the response map as JSON
            packetResponse.addCustomKeyValuePair("responsePacketJson", responsePacket.toJson());
            
        } catch (Exception e) {
            logger.error("Error handling Signature Request packet! {}", e.getMessage());
            packetResponse = new NodeHandlerResponse(false, e, "Error Handling Signature Request.");
        }
        return packetResponse;
    }
}