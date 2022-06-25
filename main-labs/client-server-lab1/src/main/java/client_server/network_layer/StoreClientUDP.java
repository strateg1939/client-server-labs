package client_server.network_layer;

import client_server.Constants;
import client_server.Message;
import client_server.Packet;
import client_server.exceptions.IllegalPacketException;

import java.io.IOException;
import java.io.InputStream;
import java.net.*;

public class StoreClientUDP {
    private static final int NUMBER_OF_MAX_ATTEMPTS = 6;

    private byte srcId;
    private DatagramSocket socket;

    public StoreClientUDP(byte srcId) throws SocketException {
        this.srcId = srcId;
        socket = new DatagramSocket();
        socket.setSoTimeout(10000);
    }

    public Message sendMessage(byte[] message) {
        int attempt = 0;
        while (attempt < NUMBER_OF_MAX_ATTEMPTS) {
            try {
                DatagramPacket packet
                    = new DatagramPacket(message, message.length, InetAddress.getByName("localhost"), Constants.UDP_PORT);
                socket.send(packet);
                byte[] response = new byte[1024];
                packet = new DatagramPacket(response, response.length);
                socket.receive(packet);
                Packet responsePacket = new Packet(packet.getData());
                if(responsePacket.getMessage().getMessageObject().getMessageString().equals(Constants.UDP_BAD_PACKET_RESPONSE)) {
                    continue;
                }
                return responsePacket.getMessage();
            } catch (IOException e) {
                attempt++;
                System.out.println("Could not connect to server");
            }
            catch (IllegalPacketException e) {
                System.out.println("Return packet damaged");
                return null;
            }
        }
        return null;
    }
}