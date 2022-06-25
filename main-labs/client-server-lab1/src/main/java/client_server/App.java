package client_server;


import client_server.models.MessageObject;
import client_server.models.Product;
import client_server.models.ProductGroup;
import client_server.network_layer.FakeReceiver;
import client_server.network_layer.Receiver;
import client_server.network_layer.StoreClientTCP;
import client_server.network_layer.StoreClientUDP;

import java.net.SocketException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

public class App
{
    public static void main(String[] args) throws NoSuchAlgorithmException, InterruptedException, SocketException {
        ProductGroup productGroup = new ProductGroup("default", new ArrayList<>());
        Constants.DATA.add(productGroup);
        productGroup.getProducts().add(new Product("new", 50, 0, productGroup.getName()));

    /*
        for (int i = 0; i < 101; i++) {
            Product product = new Product("new", 10, 10, "default");

            Message message = new Message((i % 2 == 0) ? Constants.COMMAND_ADD_PRODUCT_AMOUNT : Constants.COMMAND_REMOVE_PRODUCT_AMOUNT, 1, new MessageObject(product));
            Receiver receiver = new FakeReceiver(message, (byte) 0);
            receiver.receiveMessage();
        }
        */

        Product product = new Product("new", 10, 10, "default");
        StoreClientTCP clientTCP1 = new StoreClientTCP((byte) 1);
        StoreClientTCP clientTCP2 = new StoreClientTCP((byte) 2);
        StoreClientTCP clientTCP3 = new StoreClientTCP((byte) 3);
        var packetSerializerAdd = new PacketSerializer(new Message(Constants.COMMAND_ADD_PRODUCT_AMOUNT, 1, new MessageObject(product)), (byte) 1);
        var packetSerializerRemove = new PacketSerializer(new Message(Constants.COMMAND_REMOVE_PRODUCT_AMOUNT, 1, new MessageObject(product)), (byte) 3);

        clientTCP1.sendMessage(packetSerializerAdd.getPacket());
        clientTCP1.sendMessage(packetSerializerAdd.getPacket());
        clientTCP3.sendMessage(packetSerializerAdd.getPacket());
        clientTCP2.sendMessage(packetSerializerRemove.getPacket());

        Constants.IS_RUNNING_UDP = true;
        StoreClientUDP clientUDP = new StoreClientUDP((byte) 6);
        var packetSerializerUDPRemove = new PacketSerializer(new Message(Constants.COMMAND_REMOVE_PRODUCT_AMOUNT, 1, new MessageObject(product)), (byte) 6);
        clientUDP.sendMessage(packetSerializerUDPRemove.getPacket());
        while (Constants.anyThreadsActive()){
            Thread.sleep(5000);
        }
        Constants.DECRYPTOR.shutdown();
        Constants.PROCESSOR.shutdown();
        Constants.ENCRYPTOR.shutdown();
        Constants.TCP_SERVER.shutdown();
        //should be 60
        System.out.println("Final amount : " + Constants.DATA.get(0).getProducts().get(0).getAmount());
    }
}
