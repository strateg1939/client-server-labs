package client_server.network_layer;

import java.io.IOException;

public interface Sender {
    void sendMessage(byte[] message, byte srcId) throws IOException;
}
