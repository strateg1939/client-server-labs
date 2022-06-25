package client_server;

import client_server.models.MessageObject;
import client_server.models.Product;
import client_server.models.ProductGroup;
import client_server.network_layer.FakeReceiver;
import client_server.network_layer.Receiver;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ProcessorTests {
    @Before
    public void resetData() {
        Constants.SHOULD_USE_FAKE_CONNECTION = true;
        Constants.DATA.clear();
    }

    @AfterClass
    public static void shutdown() {
        Constants.DECRYPTOR.shutdown();
        Constants.PROCESSOR.shutdown();
        Constants.ENCRYPTOR.shutdown();
    }

    @Test
    public void AddsTwentyGroupsAfterReceivingAddGroupCommand() throws InterruptedException {
        int numberOfGroups = 20;

        for (int i = 0; i < numberOfGroups; i++) {
            Product product = new Product("new", 10, 10, "group" + i);
            Message message = new Message(Constants.COMMAND_ADD_GROUP, 1, new MessageObject(product));
            receiveMessage(message);
        }

        waitForProcessing();

        assertEquals(numberOfGroups, Constants.DATA.size());
        assertTrue(Constants.DATA.stream().anyMatch(p -> p.getName().equals("group1")));
    }
    @Test
    public void AddsTwentyProductsAfterReceivingAddProductCommands() throws InterruptedException {
        int numberOfProducts = 20;
        ProductGroup initialGroup = new ProductGroup("name", new ArrayList<>());
        Constants.DATA.add(initialGroup);
        for (int i = 0; i < numberOfProducts; i++) {
            Product product = new Product("product" + i, 10, 10, initialGroup.getName());
            Message message = new Message(Constants.COMMAND_ADD_PRODUCT, 1, new MessageObject(product));
            receiveMessage(message);
        }

        waitForProcessing();

        assertEquals(numberOfProducts, initialGroup.getProducts().size());
        assertTrue(initialGroup.getProducts().stream().anyMatch(p -> p.getName().equals("product" + (numberOfProducts - 1))));
    }

    @Test
    public void DoesNotAddProductsIfGroupNameIsIncorrect() throws InterruptedException {
        ProductGroup initialGroup = new ProductGroup("name", new ArrayList<>());
        Constants.DATA.add(initialGroup);
        Product product = new Product("product", 0, 0, "error");
        Message message = new Message(Constants.COMMAND_ADD_PRODUCT, 1, new MessageObject(product));
        receiveMessage(message);

        waitForProcessing();

        assertEquals(1, Constants.DATA.size());
        assertEquals(0, initialGroup.getProducts().size());
    }

    @Test
    public void AddsAndRemovesCorrectProductAmount() throws InterruptedException {
        Product product = setupOneGroupWithOneProduct();
        product.setAmount(100);
        int expectedFinalAmount = 110;
        for (int i = 0; i < 101; i++) {
            Product productMessage = new Product(product.getName(), 10, 10, product.getProductGroupName());
            Message message = new Message((i % 2 == 0) ? Constants.COMMAND_ADD_PRODUCT_AMOUNT : Constants.COMMAND_REMOVE_PRODUCT_AMOUNT, 1, new MessageObject(productMessage));
            receiveMessage(message);
        }

        waitForProcessing();

        assertEquals(expectedFinalAmount, Constants.DATA.get(0).getProducts().get(0).getAmount());
    }

    @Test
    public void DoesNotAddOrRemoveAnythingIfPassedIncorrectProductName() throws InterruptedException {
        Product product = setupOneGroupWithOneProduct();
        product.setAmount(100);
        int expectedFinalAmount = 100;
        for (int i = 0; i < 20; i++) {
            Product productMessage = new Product("error", 10, 10, product.getProductGroupName());
            Message message = new Message(Constants.COMMAND_ADD_PRODUCT_AMOUNT, 1, new MessageObject(productMessage));
            receiveMessage(message);
        }

        waitForProcessing();

        assertEquals(expectedFinalAmount, Constants.DATA.get(0).getProducts().get(0).getAmount());
        assertEquals(1, Constants.DATA.get(0).getProducts().size());
    }

    @Test
    public void SetsPriceAfterReceivingAppropriateCommand() throws InterruptedException {
        double delta = 0.00000001;
        Product product = setupOneGroupWithOneProduct();
        double expectedPrice = 34.233;
        Product productMessage = new Product(product.getName(), 10, expectedPrice, product.getProductGroupName());
        Message message = new Message(Constants.COMMAND_SET_PRICE, 1, new MessageObject(productMessage));
        receiveMessage(message);

        waitForProcessing();

        assertEquals(expectedPrice, Constants.DATA.get(0).getProducts().get(0).getPrice(), delta);
    }

    @Test
    public void DoesNotSetPriceIfReceivedIncorrectProductName() throws InterruptedException {
        double delta = 0.00000001;
        Product product = setupOneGroupWithOneProduct();
        Product productMessage = new Product("error", 10, 100, product.getProductGroupName());
        Message message = new Message(Constants.COMMAND_SET_PRICE, 1, new MessageObject(productMessage));
        receiveMessage(message);

        waitForProcessing();

        assertEquals(0, Constants.DATA.get(0).getProducts().get(0).getPrice(), delta);
        assertEquals(1, Constants.DATA.get(0).getProducts().size());
    }

    private Product setupOneGroupWithOneProduct(){
        ProductGroup productGroup = new ProductGroup("default", new ArrayList<>());
        Constants.DATA.add(productGroup);
        Product product = new Product("new", 0, 0, productGroup.getName());
        productGroup.getProducts().add(product);
        return product;
    }

    private void receiveMessage(Message message) {
        Receiver receiver = new FakeReceiver(message, (byte) 0);
        receiver.receiveMessage();
    }

    private void waitForProcessing() throws InterruptedException {
        while (Constants.anyThreadsActive()) {
            Thread.sleep(100);
        }
    }

}
