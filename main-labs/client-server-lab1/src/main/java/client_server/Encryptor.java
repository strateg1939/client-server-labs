package client_server;

import client_server.network_layer.FakeSender;
import client_server.network_layer.Sender;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class Encryptor {
    private ThreadPoolExecutor encryptPool = (ThreadPoolExecutor) Executors.newFixedThreadPool(Constants.NUMBER_OF_THREADS);
    public Encryptor() {

    }

    public void encrypt(Message message, byte srcId) {
        Sender sender = new FakeSender();
        if(Constants.IS_RUNNING_UDP) sender = Constants.UDP_SERVER;
        else if (!Constants.SHOULD_USE_FAKE_CONNECTION) sender = Constants.TCP_SERVER;
        encryptPool.submit(new EncryptorTask(message, srcId, sender));
    }

    public void shutdown(){
        encryptPool.shutdown();
    }
    public boolean isActive(){
        return encryptPool.getActiveCount() > 0;
    }
}

class EncryptorTask implements Runnable {
    private Message message;
    private byte srcId;
    private Sender sender;

    public EncryptorTask(Message message, byte srcId, Sender sender){
        this.message = message;
        this.srcId = srcId;
        this.sender = sender;
    }
    @Override
    public void run() {
        PacketSerializer serializer = new PacketSerializer(this.message, this.srcId);
        System.out.println("Sending message : " + this.message.getMessageObject().getMessageString());
        try {
            sender.sendMessage(serializer.getPacket(), srcId);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
