package client_server;


import client_server.models.MessageObject;
import client_server.models.Product;
import client_server.models.ProductGroup;
import client_server.network_layer.FakeReceiver;
import client_server.network_layer.Receiver;

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

public class App
{
    public static void main(String[] args) throws NoSuchAlgorithmException, InterruptedException {
        ProductGroup productGroup = new ProductGroup("default", new ArrayList<>());
        Constants.DATA.add(productGroup);
        productGroup.getProducts().add(new Product("new", 50, 0, productGroup.getName()));

        for (int i = 0; i < 5; i++) {
            Product product = new Product("new", 10, 10, "default");

            Message message = new Message((i % 2 == 1) ? Constants.COMMAND_ADD_PRODUCT_AMOUNT : Constants.COMMAND_REMOVE_PRODUCT_AMOUNT, 1, new MessageObject(product));
            Receiver receiver = new FakeReceiver(message, (byte) 0);
            receiver.receiveMessage();
        }
        while (Constants.anyThreadsActive()){
            Thread.sleep(100);
        }
        Constants.DECRYPTOR.shutdown();
        Constants.PROCESSOR.shutdown();
        Constants.ENCRYPTOR.shutdown();
        System.out.println("Final amount : " + Constants.DATA.get(0).getProducts().get(0).getAmount());
    }
}
