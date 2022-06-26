package client_server;

import client_server.models.MessageObject;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.Socket;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class Decryptor {
    private ThreadPoolExecutor decryptorPool = (ThreadPoolExecutor) Executors.newFixedThreadPool(Constants.NUMBER_OF_THREADS);
    public Decryptor() {

    }

    public void decrypt(byte[] message, Socket socket) {
        decryptorPool.submit(new DecryptorTask(message, socket));
    }

    public void decrypt(byte[] message, DatagramPacket packet) {
        decryptorPool.submit(new DecryptorTask(message, packet));
    }

    public void shutdown(){
        decryptorPool.shutdown();
    }

    public boolean isActive(){
        return decryptorPool.getActiveCount() > 0;
    }
}

class DecryptorTask implements Runnable {
    private byte[] message;
    private Socket socket;
    private DatagramPacket datagramPacket;

    public DecryptorTask(byte[] message, Socket socket){
        this.message = message;
        this.socket = socket;
    }

    public DecryptorTask(byte[] message, DatagramPacket datagramPacket){
        this.message = message;
        this.datagramPacket = datagramPacket;
    }
    @Override
    public void run() {
        try {
            Packet packet = new Packet(this.message);
            if (!Constants.SHOULD_USE_FAKE_CONNECTION && socket != null) {
                Constants.TCP_SERVER.getSocketMap().put(packet.getSrcId(), socket);
            }
            if (!Constants.SHOULD_USE_FAKE_CONNECTION && datagramPacket != null) {
                Constants.UDP_SERVER.getSocketMap().put(packet.getSrcId(), datagramPacket);
            }
            Constants.PROCESSOR.process(packet);
        } catch (RuntimeException e) {
            System.out.println(e.getMessage());
            //if running udp send error message if packet damaged
            if(Constants.IS_RUNNING_UDP) {
                Message errorMessage = new Message(0,0,new MessageObject(Constants.UDP_BAD_PACKET_RESPONSE));
                byte freeSrcId = (byte) -100;
                while (Constants.UDP_SERVER.getSocketMap().containsKey(freeSrcId)) freeSrcId++;
                Constants.UDP_SERVER.getSocketMap().put(freeSrcId, datagramPacket);
                PacketSerializer serializer = new PacketSerializer(errorMessage, freeSrcId);
                try {
                    Constants.UDP_SERVER.sendMessage(serializer.getPacket(), freeSrcId);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }
}
