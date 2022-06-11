package client_server;

import client_server.ciphers.CipherString;
import client_server.exceptions.IllegalPacketException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.nio.ByteBuffer;

public class Message {
    private int commandType;
    private int userId;
    private MessageObject messageObject;

    public Message(ByteBuffer byteBuffer, int length) {
        this.commandType = byteBuffer.getInt();
        this.userId = byteBuffer.getInt();
        int lengthOfMessage = length - Integer.BYTES * 2;
        if(lengthOfMessage < 0) {
            throw new IllegalPacketException("Incorrect message length");
        }
        byte[] message = new byte[lengthOfMessage];
        byteBuffer.get(message, 0, lengthOfMessage);

        try {
            var cipherString = new CipherString();
            var string  = cipherString.decrypt(message);
            var objectMapper = new ObjectMapper();
            messageObject = objectMapper.readValue(string, MessageObject.class);
        } catch (Exception e) {
            throw new IllegalPacketException(e.getMessage());
        }
    }

    public Message(int commandType, int userId, MessageObject messageObject) {
        this.commandType = commandType;
        this.userId = userId;
        this.messageObject = messageObject;
    }

    public int getCommandType() {
        return commandType;
    }

    public int getUserId() {
        return userId;
    }

    public MessageObject getMessageObject() {
        return messageObject;
    }
}
