package client_server.network_layer;

import java.util.Arrays;

public class FakeSender implements Sender{

    @Override
    public void sendMessage(byte[] message) {
        System.out.println("Fake sender sending " + Arrays.toString(message));
    }
}
