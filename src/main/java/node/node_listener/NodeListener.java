/*
 *      Author: Nathaniel Brewer
 * 
 *      TODO: Add description
 */
package node.node_listener;

import java.io.IOException;

import java.net.Socket;
import java.net.ServerSocket;
import java.net.SocketTimeoutException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import node.node_handler.NodeServerHandler;

public class NodeListener implements Runnable {

    // Each class can have its own logger instance
    private static final Logger logger = LogManager.getLogger(NodeListener.class);
    
    private Socket connected;
    private ServerSocket listenerSocket;
    private int port;
    private int timeout;

    public NodeListener(int port, int timeout) throws IOException {
        this.port = port;
        this.timeout = timeout;
        this.listenerSocket = new ServerSocket(port);
        this.listenerSocket.setSoTimeout(timeout);
    }

    @Override
    public void run(){  

        logger.info("Listening on port " + port);

        boolean on = true;
        while(on){
            try {
                connected = listenerSocket.accept();
                Thread handlerThread = new Thread(new NodeServerHandler(connected)); // sends the message to a handler
                handlerThread.start(); // Begins the new thread
            } catch (SocketTimeoutException sto) {
            } catch (IOException ioe) {
                logger.error("I/O Exception! " + ioe);
            }
        }
    }

    /*          Accessor methods        */

    public int getActivePort() {
        return port;
    }

    public int getActiveTimeout() {
        return timeout;
    }

    /*          Changer Methods          */

    public void changePort(int newPort) {
        port = newPort;
        // TODO: Activly change the port in the socket
    }

    public void changeTimeout(int newTimeout) {
        timeout = newTimeout;
        try {
            listenerSocket.setSoTimeout(timeout);
        } catch (Exception e) {
            logger.error("Error setting new timeout on socket: ( " + port + " ) " + e);
        }
    }

    public boolean closeSocket() {
        try{
            listenerSocket.close();
            logger.warn("Listening Socket Closed on port " + port + "!");
        } catch(Exception e) {
            logger.error("Error Closing socket on port" + port + "! " + e);
            return false;
        }
        return true;
    }
}