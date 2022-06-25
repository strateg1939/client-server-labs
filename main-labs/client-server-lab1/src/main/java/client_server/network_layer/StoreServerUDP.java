package client_server.network_layer;

import client_server.Constants;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class StoreServerUDP implements Sender {
    private ThreadPoolExecutor storeUDPReceiver = (ThreadPoolExecutor) Executors.newFixedThreadPool(1);
    private Map<Byte, DatagramPacket> map = new ConcurrentHashMap<>();
    private DatagramSocket serverSocket;
    public StoreServerUDP(){
        try {
            serverSocket = new DatagramSocket(Constants.UDP_PORT);
        } catch (IOException e) {
            e.printStackTrace();
        }
        storeUDPReceiver.submit(new StoreServerUDPTask(serverSocket));
    }

    public Map<Byte, DatagramPacket> getSocketMap(){
        return map;
    }

    public void shutdown(){
        storeUDPReceiver.shutdownNow();
    }

    @Override
    public void sendMessage(byte[] message, byte srcId) throws IOException {
        DatagramPacket packet = map.get(srcId);
        InetAddress address = packet.getAddress();
        int port = packet.getPort();
        packet = new DatagramPacket(message, message.length, address, port);
        serverSocket.send(packet);
        map.remove(srcId);
    }
}

class StoreServerUDPTask extends Thread {
    private DatagramSocket serverSocket;

    public StoreServerUDPTask(DatagramSocket serverSocket){
        this.serverSocket = serverSocket;
    }

    public void run() {
        try {
            while(!Thread.interrupted()) {
                byte[] receivedPacket = new byte[1024];
                DatagramPacket packet = new DatagramPacket(receivedPacket, receivedPacket.length);
                serverSocket.receive(packet);
                Constants.DECRYPTOR.decrypt(packet.getData(), packet);
            }

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}