/*
 *      Author: Nathaniel Brewer
 * 
 *      This is a listener that will be instantiated to await packets coming from an Edge Server.
 *      This will be a thread so the main program (in this instance the Coordinator) can still 
 *      send packets. (Such as heartbeat packets) to it's children(Server).
 * 
 *      Once a packet is recieved, the listener will create a new thread to handle the packet,
 *      this is so that thread can process and ID the packet while the listener can return to
 *      listening for other new packets.
 */

package coordinator.coordinator_listener;

import java.io.IOException;

import java.net.Socket;
import java.net.ServerSocket;
import java.net.SocketTimeoutException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import coordinator.coordinator_handler.CoordinatorServerHandler;

public class CoordinatorListener implements Runnable {

    private static final Logger logger = LogManager.getLogger(CoordinatorListener.class);

    private Socket connected;   // The socket that will be created once a connection is made, this will be passed to the thread that handles the packet
    private ServerSocket listenerSocket;    // This is the active listening socket, only recieves
    private int port;       // The port the listener is on
    private int timeout;        // How long the listener refreshes

    private boolean on;     // The boolean value for our while loop for our listener

    // Constructor for the listener
    public CoordinatorListener(int port, int timeout) throws IOException {
        this.port = port;
        this.timeout = timeout;
        this.listenerSocket = new ServerSocket(port);
        this.listenerSocket.setSoTimeout(timeout);
        this.on = true;
    }

    // This is the method that is called that will start the thread, once the thread is started, then 

    @Override
    public void run(){  

        logger.info("Listening on port " + this.port);

        // Main loop that will constantly be running, this allows for constant listening for new packets
        while(on){
            try {
                connected = listenerSocket.accept();        // if a packet comes, create the socket to handle the packet
                Thread handlerThread = new Thread(new CoordinatorServerHandler(connected)); // sends the packet to the packet handler, simultaneously starting a new thread
                handlerThread.start(); // Begins the new thread

                // Keeping the 'SocketTimeoutException' catch statement empty. Since we want to constantly be reseting our listener, we want this exception to be thrown
            } catch (SocketTimeoutException sto) {  
            } catch (IOException ioe) {     
                logger.error("IOException!\n" + ioe);
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
        int oldTimeout = timeout;   // Store the oldtime out locally in case of error. If error we can retain the old timeout
        timeout = newTimeout;   // store new timeout inside the global variable
        try {
            // Try and set new timeout
            listenerSocket.setSoTimeout(timeout);
        } catch (Exception e) {
            // Resets the timeout to what it was prior
            timeout = oldTimeout;
            logger.error("Error setting new timeout on socket: ( " + port + " )\n" + e);
        }
    }

    // This will execute a graceful shutdown of the socket
    public boolean closeSocket() {
        try{
            on = false;     // Turn off our while loop for redundancy
            logger.info("Closing socket on port: + ( " + port + " ) ");
            listenerSocket.close();     // Fully close the socket
        } catch(Exception e) {
            logger.error("Error Closing socket on port: ( " + port + " )\n"+e);
            return false;       // ungraceful shutdown
        }
        return true;        // Graceful shutdown complete
    }
}
