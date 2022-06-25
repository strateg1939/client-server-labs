package client_server;

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

    public DecryptorTask(byte[] message, Socket socket){
        this.message = message;
        this.socket = socket;
    }
    @Override
    public void run() {
        try {
            Packet packet = new Packet(this.message);
            if (!Constants.SHOULD_USE_FAKE_CONNECTION && socket != null) {
                Constants.TCP_SERVER.getSocketMap().put(packet.getSrcId(), socket);
            }
            Constants.PROCESSOR.process(packet);
        } catch (RuntimeException e) {
            System.out.println(e.getMessage());
        }
    }
}
