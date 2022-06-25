package client_server.network_layer;

import client_server.Constants;
import client_server.Message;
import client_server.Packet;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

public class StoreClientTCP {
    private static final int NUMBER_OF_MAX_ATTEMPTS = 4;

    private byte srcId;

    public StoreClientTCP(byte srcId) {
        this.srcId = srcId;
    }

    public Message sendMessage(byte[] message) {
        int attempt = 0;
        while (attempt < NUMBER_OF_MAX_ATTEMPTS) {
            try {
                Socket clientSocket = new Socket("127.0.0.1", Constants.TCP_PORT);
                clientSocket.getOutputStream().write(message);
                InputStream stream = clientSocket.getInputStream();
                clientSocket.getOutputStream().flush();
                clientSocket.shutdownOutput();
                byte[] response = stream.readAllBytes();
                Packet packet = new Packet(response);
                clientSocket.close();
                return packet.getMessage();
            } catch (IOException e) {
                attempt++;
                System.out.println("Could not connect to server");
            }
        }
        return null;
    }
}
