package client_server;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class Decryptor {
    private ThreadPoolExecutor decryptorPool = (ThreadPoolExecutor) Executors.newFixedThreadPool(Constants.NUMBER_OF_THREADS);
    public Decryptor() {

    }

    public void decrypt(byte[] message) {
        decryptorPool.submit(new DecryptorTask(message));
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

    public DecryptorTask(byte[] message){
        this.message = message;
    }
    @Override
    public void run() {
        try {
            Packet packet = new Packet(this.message);
            Constants.PROCESSOR.process(packet);
        }catch (RuntimeException e) {
            System.out.println(e.getMessage());
        }
    }
}
