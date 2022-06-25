package client_server.exceptions;

public class IllegalPacketException extends RuntimeException {
    public IllegalPacketException(String message) {
        super(message);
    }
}
