package node.node_packet.node_packet_class;

import java.util.LinkedHashMap;
import node.node_packet.NodePacket;
import node.node_packet.NodePacketType;

public class NodeSignedCredentialPacket extends NodePacket {

    private String transactionId;
    private String peerPublicKey;   // Node B's public key (to verify the signature)
    private String pgpSignature;    // The actual PGP signature value
    private String signedDataHash;  // The hash of the data Node B signed

    public NodeSignedCredentialPacket(
        String senderId, 
        String transactionId, 
        String peerPublicKey, 
        String pgpSignature, 
        String signedDataHash
    ) {
        super();
        
        this.packetType = NodePacketType.SIGNED_CREDENTIAL;
        this.id = senderId; // The ID of the signer (Node B)
        this.transactionId = transactionId;
        this.peerPublicKey = peerPublicKey;
        this.pgpSignature = pgpSignature;
        this.signedDataHash = signedDataHash;

        // Initialize and populate the inherited payload map for serialization
        this.payload = new LinkedHashMap<>();
        this.payload.put("transactionId", transactionId);
        this.payload.put("peerPublicKey", peerPublicKey);
        this.payload.put("pgpSignature", pgpSignature);
        this.payload.put("signedDataHash", signedDataHash);
    }

    // Getters for easy data extraction in Node A's handler
    public String getTransactionId() { return transactionId; }
    public String getPeerPublicKey() { return peerPublicKey; }
    public String getPgpSignature() { return pgpSignature; }
    public String getSignedDataHash() { return signedDataHash; }
}