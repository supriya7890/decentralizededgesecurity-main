/*
 *      Author: Nathaniel Brewer
 * 
 *      Configuration for each of the nodes in the hierarchy. Will grab machine IP address and will try to generate avaiable ports.
 *      From ./node_config/nodeConfig.properties
 */
package node.node_config;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.Inet4Address;
import java.net.InetAddress;            // Used for grabbing the machine's IP address
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;   // Error for trying to grab IP address

import java.util.Properties;            // Utility for getting properties from any .properties file

public class NodeConfig {

    // Each class can have its own logger instance
    private static final Logger logger = LogManager.getLogger(NodeConfig.class);

    private static Properties properties = new Properties();      // Generate the properties object
    private Properties instanceProperties; // This is for instances properties files
    private String instanceId;  // The ID of the specific isntance of the server - this will determine the config file that is loaded

    private static String defaultConfigPath = "config/node_config/nodeConfig.properties";
    private String instanceConfigPath;

    static {
        try {
            FileInputStream in = new FileInputStream(defaultConfigPath);
            properties.load(in);
            in.close();
        } catch (IOException e){
            logger.error("Error opening properties file!" + e);
        }
    }

    public NodeConfig() {
        this.instanceProperties = properties;
        this.instanceId = null;
    }

    public NodeConfig(String instanceId) {
        this.instanceId = instanceId;
        this.instanceProperties = new Properties();

        // Try to open specific instance file - if not it will load the default one
        try {
            instanceConfigPath = "config/node_config/nodeConfig_" + instanceId + ".properties";
            FileInputStream in = new FileInputStream(instanceConfigPath);
            instanceProperties.load(in);
            in.close();
            logger.info("Loaded instance config for: " + instanceId + " from path: " + instanceConfigPath);
            logger.info("Instance config contains " + instanceProperties.size() + " properties:");
            for (String key : instanceProperties.stringPropertyNames()) {
                logger.info("  " + key + " = " + instanceProperties.getProperty(key));
            }
        } catch(IOException e) {
            logger.error("No instance config found for: " + instanceId + ". using default config instead.");
            instanceProperties = properties;
            instanceConfigPath = null; // Clear the failed path so writeToConfig uses default
            logger.info("Fallback to default config contains " + instanceProperties.size() + " properties:");
            for (String key : instanceProperties.stringPropertyNames()) {
                logger.info("  " + key + " = " + instanceProperties.getProperty(key));
            }
        }
    }


    /**
     *   Grabs the machines IP and will return the IP.
     *   @return a string of the IP if found, if not found then a Null value
     */
    public String grabIP() throws UnknownHostException, SocketException {

        String realIp = null;

        for(NetworkInterface ni: java.util.Collections.list(NetworkInterface.getNetworkInterfaces())) {
            if(ni.isLoopback() || !ni.isUp()) continue;
            for(InetAddress addr : java.util.Collections.list(ni.getInetAddresses())) {
                if(addr instanceof Inet4Address) {
                    realIp = addr.getHostAddress();
                    break;
                }
            }
            if(realIp != null) break;
        }

        writeToConfig("Node.IP", realIp);
        
        // Since the Node and Server are running on the same machine they both use the same IP address
        // TODO: REMOVE IN DEPLOYMENT
        writeToConfig("Server.IP", realIp);

        return realIp;
    }

    public String getIPByKey(String key) {

        String IP = "";
        try{
            IP = properties.getProperty(key);
        } catch (Error e) {
            logger.error("Error getting " + key + "'s IP from config file!" + e);
        }
        return IP;
    }
    

    /**
     *  Will try and grab available port for the machine.    
     *  @param key - Which node is trying to grab the port
     *  @return int, if port is found. If not found 0
     */

    public int getPortByKey(String key){

        int port = 0;
        try{
            String portString = instanceProperties.getProperty(key);
            port = Integer.parseInt(portString);
            logger.info("Retrieved port for key '" + key + "': " + port + " from " + 
                (instanceId != null ? "instance config (ID: " + instanceId + ")" : "default config"));
        } catch (Exception e){
            logger.error("Error getting port from config file for key'" + key +"': " + e);
        }
        return port;
    }

    private OutputStream openFile(String filePath) throws IOException {

        OutputStream out;

        try{
            out = new FileOutputStream(filePath);
            return out;
        } catch (Exception e) {
            logger.error("File from path: {}, could not be opened!", filePath);
        }

        throw new IOException("Could not open File from path: " + filePath + " could not be opened!");
    }

    /**
     *  Will try to write/overwrite a key/value pair in config.properties    
     *  @param key - will look for this key in the config, if not there it will write it there
     *  @param value - the value to the key that will be inserted once the key is either found or written
     */

    public void writeToConfig(String key, String value) {

        // Trying to check to see if the config file has the node key, whatever that may be
        try{

            // Set the property in the instance properties
            instanceProperties.setProperty(key, value);

            // Determine which file path to use - instance config if available, otherwise default
            String configPath = (instanceId != null && instanceConfigPath != null) ? instanceConfigPath : defaultConfigPath;

            // Write the key/value back to the appropriate file
            try(OutputStream outputStream = openFile(configPath)){
                instanceProperties.store(outputStream, null);
                logger.info("Overwrote:\t Key: ( " + key + " )\t Value: ( " + value + " ) in file: " + configPath);
            } catch(IOException ioe) {
                logger.error("Error adding value: ( " + value + " ) to key: ( " + key + " ) to config file: " + configPath + "\n" + ioe);
                
                // If instance config fails and we were trying to write to instance, fall back to default
                if(instanceId != null && instanceConfigPath != null && !configPath.equals(defaultConfigPath)) {
                    logger.info("Falling back to default config file...");
                    try(OutputStream outputStream = openFile(defaultConfigPath)) {
                        instanceProperties.store(outputStream, null);
                        logger.info("Overwrote:\t Key: ( " + key + " )\t Value: ( " + value + " ) in default file: " + defaultConfigPath);
                    } catch(IOException e) {
                        logger.error("Error adding value: ( " + value + " ) to key: ( " + key + " ) to default config file!\n" + e);
                    }
                }
            } catch(Exception e) {
                logger.error("Unknown error adding value: ( " + value + " ) to key: ( " + key + " ) to config file!\n" + e);
            }

        }catch (Exception e) { // Generic Exception
            logger.error("Error writing key: ( " + key + " ) to config file!\n" + e);
        }
    }
}
