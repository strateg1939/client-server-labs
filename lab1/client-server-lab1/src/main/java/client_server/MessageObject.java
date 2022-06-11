package client_server;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * This is example POJO object
 */
public class MessageObject {
    private String message;

    public MessageObject() {
        
    }
    public MessageObject(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }
}
