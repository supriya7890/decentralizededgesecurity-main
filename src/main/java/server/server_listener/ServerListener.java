/*
 *      Author: Nathaniel Brewer
 * 
 *      This is the logic for listening for packets from the server, used by the Coordinator.
 *      This is a thread and will be created by the Main coordinator file once initialization
 *      is complete.
 * 
 *      
 * 
 */
package server.server_listener;

import java.io.*;
import java.net.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import server.server_handler.ServerCoordinatorHandler;
import server.server_handler.ServerNodeHandler;

public class ServerListener implements Runnable {

    private static final Logger logger = LogManager.getLogger(ServerListener.class);
    
    private Socket connected;   // This is the socket sent to the handler after the connection has been made by the listener
    private ServerSocket listenerSocket;    // this is the active listening socket
    private int port;   
    private int timeout;

    private String type;

    // Constructor
    public ServerListener(int port, int timeout, String type) throws IOException {
        this.port = port;
        this.timeout = timeout;
        this.type = type;
        this.listenerSocket = new ServerSocket(port);
        this.listenerSocket.setSoTimeout(timeout);
    }

    @Override
    public void run(){  

        logger.info("Listening on port " + this.port);

        boolean on = true;

        //TODO: Look into thread pool since the server will have a lot of clients

        while(on){
            try {
                if(type.equals("node")) {
                    connected = listenerSocket.accept();
                    Thread handlerThread = new Thread(
                        new ServerNodeHandler(connected)
                    ); // sends the message to a handler
                    handlerThread.start(); // Begins the new thread
                } else if (type.equals("coordinator")) {
                    connected = listenerSocket.accept();
                    Thread handlerThread = new Thread(
                        new ServerCoordinatorHandler(connected)
                    ); // sends the message to a handler
                    handlerThread.start(); // Begins the new thread
                }
            } catch (SocketTimeoutException sto) {
                // You need this exception handling here because it will brick at trying to start the connection
                // Did not add any handling to this exception as it would constatly throw an error, so this just prevents it from clogging up the console
            } catch (IOException ioe) {
                logger.error("IOException!\n" + ioe);
            } catch (Exception e) {
                logger.error("Unknown Exception!\n" + e);
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


    /*          Changer methods        */

    public void changePort(int newPort) {
        port = newPort;
        // TODO: Activly change the port in the socket
    }

    public void changeTimeout(int newTimeout) {
        timeout = newTimeout;
        try {
            listenerSocket.setSoTimeout(timeout);
        } catch (Exception e) {
            logger.error("Error setting new timeout on socket: ( " + port + " )\n" + e);
        }
    }

    public boolean closeSocket() {
        try{
            logger.info("Closing socket on port: + ( " + port + " )\n");
            listenerSocket.close();
        } catch(Exception e) {
            logger.error("Error Closing socket on port: ( " + port + " )\n" + e);
            return false;
        }
        return true;
    }
}
