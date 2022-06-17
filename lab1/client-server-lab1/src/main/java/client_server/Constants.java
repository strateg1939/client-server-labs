package client_server;

import client_server.models.Product;
import client_server.models.ProductGroup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Constants {
    public static final int NUMBER_OF_THREADS = 15;
    public static final Decryptor DECRYPTOR = new Decryptor();
    public static final Processor PROCESSOR = new Processor();
    public static final Encryptor ENCRYPTOR = new Encryptor();
    public static final List<ProductGroup> DATA = Collections.synchronizedList(new ArrayList<>());


    public static final int COMMAND_GET_PRODUCT_AMOUNT = 1;
    public static final int COMMAND_REMOVE_PRODUCT_AMOUNT = 2;
    public static final int COMMAND_ADD_PRODUCT_AMOUNT = 3;
    public static final int COMMAND_ADD_PRODUCT = 4;
    public static final int COMMAND_ADD_GROUP = 5;
    public static final int COMMAND_SET_PRICE = 6;

    public static boolean anyThreadsActive() {
        return DECRYPTOR.isActive() || PROCESSOR.isActive() || ENCRYPTOR.isActive();
    }
}
