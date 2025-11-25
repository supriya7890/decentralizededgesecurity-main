/*      Author: Nathaniel Brewer     
 *      
 *      This object will define the success/failure message of handling a packet
 *      of any type. 
 * 
 *      For example
 *          If a packetType of INITALIZATION sends a preferred port and no errors
 *          occur in handling the payload on the receiving end, then "success" = True
 *          and the "message" will be something along the lines of "Good request"
 * 
 *          If a packetType of INITALIZATION does not send a preferred port or an
 *          error occurs, then "success" = False and the message will provide adequate
 *          details on what went wrong alongside what exception that may have risen from    
 *          the potential error
 *          
 */

package coordinator.coordinator_handler.coordinator_packet_type_handler;

import java.util.LinkedHashMap;

public class CoordinatorHandlerResponse {

    // If successful, this will be True. If unsuccessful, then False
    private boolean success;

    // The message will provide details on what went wrong if anything at all
    private LinkedHashMap<String, String> messages;

    // If there is an exception, it will be stored here and sent along side the message
    private LinkedHashMap<String, String> exceptions;

    // This is the counter that gets appended to the messages (e.g. Message + 1 = Message1)
    // We are storing this globally here. Since more messages can get added after, we want to save the #
    private int messageCounter = 0;
    private int exceptionCounter = 0;

    public CoordinatorHandlerResponse(boolean success) {
        this.success = success;
        this.exceptions = new LinkedHashMap<>(); 
        this.messages = new LinkedHashMap<>();
    }


    // Since there may be multiple messages, I am using Varargs since we do not know how many messages may be sent (if there is multiple)
    public CoordinatorHandlerResponse(boolean success, String... message){
        this.success = success;
        this.exceptions = new LinkedHashMap<>(); // Empty linked hash map in case of new exceptions;
        this.messages = new LinkedHashMap<>();
        storePayload(message);
    }

    // Overloaded constructor in the case there are exceptions thrown.
    public CoordinatorHandlerResponse(boolean success, Exception exception, String... message){
        this.success = success;
        this.exceptions = new LinkedHashMap<>(); 
        this.messages = new LinkedHashMap<>();
        addException(exception);
        storePayload(message);
    }

    // Converts all the messages to the arraylist for storage
    private void storePayload(String... message) {
        for (String msg : message) {
            messages.put("Message" + messageCounter, msg);
            messageCounter++;
        }
    }

    // Store exceptions in the message map if available
    public LinkedHashMap<String, String> combineMaps() {

        // Will check for empty exceptions map, if not empty then it will add to 
        if(!exceptions.isEmpty()){
            exceptions.forEach( (key, value) -> {
                messages.put(key, value);
            });
        }
        return messages;
    }

    // Used for errors - will print to the console to describe issues
    public void printMessages(){
        // Loops through all the stored messages and will print them to the main terminal
        messages.forEach( (key, value) -> {
            System.out.println("Key: " + key + " - Value: " + value);
        });
    }

    // Used to send this to the payload and make it Json-ified to be sent
    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        // Loop through each individual message in the 'message' array
        this.messages.forEach( (key, value) -> {
             sb.append(key).append("; ").append(value);    // Add ; between the each key/value pair

            // TODO: Check this output for seperation of each Key Value Pair
        });
        if (!exceptions.isEmpty()) {
            sb.append(':');
            this.exceptions.forEach( (key, value) -> {
                sb.append("Exception - ").append(key + ": ").append(value.toString());
            });
        }
        return sb.toString().trim();
    }

    // Changes the success message to indicate a positive/negative success factor, meaning the 
    public void setSuccess(boolean success){
        this.success = success;
    }

    /*
     * 
     *      Adder methods
     * 
     */

    // If any new messages arrive in the stack, before this is sent back to the original sender, they will be added here
    public void addMessage(String... message){
        storePayload(message);
    }

    // Stores a string-ified version of the exeption inside the response message
    public void addException(Exception... exception) {
        for (Exception exc : exception) {
            this.messages.put("Exception" + exceptionCounter, exc.toString());
            exceptionCounter++;
        }
    }

    public void addCustomKeyValuePair(String key, String value) {
        messages.put(key, value);
    }

    /*
     * 
     *      Getter methods
     * 
     */
        // Standard get methods
    public boolean getSuccess(){
        return success;
    }

    public LinkedHashMap<String, String> getMessageMap(){
        return messages;
    }

    public LinkedHashMap<String, String> getExceptionMap(){
        return exceptions;
    }

}