package client_server.network_layer;

import client_server.Constants;
import client_server.Message;
import client_server.PacketSerializer;
import client_server.models.MessageObject;
import client_server.models.Product;
import client_server.models.ProductGroup;

import java.util.ArrayList;

public class FakeReceiver implements Receiver {
    private Message message;
    private byte srcId;

    public FakeReceiver(){
        this(null, (byte) (Math.random() * Byte.MAX_VALUE));
    }

    public FakeReceiver(Message message, byte srcId) {
        this.message = message;
        this.srcId = srcId;
    }

    @Override
    public void receiveMessage() {
        Message message = this.message;
        if(message == null){
            int code = (int) (Math.random() * 7 + 1);
            message = new Message(code,  srcId, new MessageObject(new Product("prod")));
        }
        PacketSerializer serializer = new PacketSerializer(message, srcId);
        Constants.DECRYPTOR.decrypt(serializer.getPacket());
    }
}
