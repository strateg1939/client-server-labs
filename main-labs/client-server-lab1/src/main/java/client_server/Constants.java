package client_server;

import client_server.models.Product;
import client_server.models.ProductGroup;
import client_server.network_layer.StoreServerTCP;
import client_server.network_layer.StoreServerUDP;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Constants {
    public static final int NUMBER_OF_THREADS = 15;
    public static final Decryptor DECRYPTOR = new Decryptor();
    public static final Processor PROCESSOR = new Processor();
    public static final Encryptor ENCRYPTOR = new Encryptor();
    public static final StoreServerTCP TCP_SERVER = new StoreServerTCP();
    public static final StoreServerUDP UDP_SERVER = new StoreServerUDP();
    public static final List<ProductGroup> DATA = Collections.synchronizedList(new ArrayList<>());


    public static final int COMMAND_GET_PRODUCT_AMOUNT = 1;
    public static final int COMMAND_REMOVE_PRODUCT_AMOUNT = 2;
    public static final int COMMAND_ADD_PRODUCT_AMOUNT = 3;
    public static final int COMMAND_ADD_PRODUCT = 4;
    public static final int COMMAND_ADD_GROUP = 5;
    public static final int COMMAND_SET_PRICE = 6;

    public static final int TCP_PORT = 1337;
    public static final int UDP_PORT = 1338;
    //for tests
    public static boolean SHOULD_USE_FAKE_CONNECTION = false;
    public static boolean IS_RUNNING_UDP = false;

    public static final String UDP_BAD_PACKET_RESPONSE = "BAD_PACKET";


    public static boolean anyThreadsActive() {
        return DECRYPTOR.isActive() || PROCESSOR.isActive() || ENCRYPTOR.isActive();
    }
}
