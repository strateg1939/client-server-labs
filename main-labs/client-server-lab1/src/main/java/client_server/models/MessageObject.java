package client_server.models;

/**
 * This is example POJO object
 */
public class MessageObject {
    private Product message;
    private String messageString;

    public MessageObject() {
        
    }
    public MessageObject(Product message, String messageString) {
        this.message = message;
        this.messageString = messageString;
    }
    public MessageObject(String messageString) {
        this.messageString = messageString;
    }

    public MessageObject(Product message){
        this.message = message;
    }

    public Product getMessage() {
        return message;
    }
    public void setMessage(Product message) {
        this.message = message;
    }

    public String getMessageString() {
        return messageString;
    }

    public void setMessageString(String messageString) {
        this.messageString = messageString;
    }
}
