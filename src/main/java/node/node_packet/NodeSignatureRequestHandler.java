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
            // Step 1: Extract request details
            // The base handler (NodePacketHandler) would typically load payload into PayloadKeyValuePairs
            String transactionId = recievedPacket.getValueByKey("transactionId");
            String requesterId = recievedPacket.getValueByKey("requesterId");

            logger.info("Received SIGNATURE_REQUEST from Requester ID: {} with Transaction ID: {}", requesterId, transactionId);

            // ----------------------------------------------------------------------
            // Step 2: Perform cryptographic operations (Placeholder for PGP logic)
            // This is where Node B performs the signing using its private PGP key.
            // ----------------------------------------------------------------------
            
            // --- PLACEHOLDER DATA START ---
            // In a real implementation: hash dataToSign, sign hash, and retrieve public key.
            String nodeBPgpPublicKey = "B_PUBLIC_KEY_CONTENT_PGP_FORMAT"; 
            String signedData = "B_SIGNED_HASH_OF_" + requesterId + "_" + transactionId; 
            String dataHash = "HASH_OF_DATA_BEING_SIGNED_BY_B"; 
            // --- PLACEHOLDER DATA END ---


            // Step 3: Create the response packet (SIGNED_CREDENTIAL)
            // Node B is the sender of this response.
            NodeSignedCredentialPacket responsePacket = new NodeSignedCredentialPacket(
                EdgeNode.getNodeID(), // Node B's ID
                transactionId,
                nodeBPgpPublicKey,
                signedData,
                dataHash
            );
            
            // Step 4: Prepare the response wrapper for the sender thread (NodeServerHandler)
            packetResponse = new NodeHandlerResponse(true, "Signed credential generated and ready to send.");
            // We pass the entire response packet back in the response map for the NodeServerHandler to send.
            packetResponse.addCustomKeyValuePair("responsePacketJson", responsePacket.toJson());
            
        } catch (Exception e) {
            logger.error("Error handling Signature Request packet! {}", e.getMessage());
            packetResponse = new NodeHandlerResponse(false, e, "Error Handling Signature Request.");
        }
        return packetResponse;
    }
}