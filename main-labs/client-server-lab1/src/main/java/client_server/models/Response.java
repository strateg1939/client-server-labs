package client_server.models;

public class Response {
    private String message;

    public Response(String message) {
        this.message = message;
    }
    public Response(){}

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
