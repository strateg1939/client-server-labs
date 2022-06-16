package client_server;

import client_server.models.MessageObject;
import client_server.models.Product;
import client_server.models.ProductGroup;

import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class Processor {
    private ThreadPoolExecutor processorPool = (ThreadPoolExecutor) Executors.newFixedThreadPool(Constants.NUMBER_OF_THREADS);
    public Processor() {

    }

    public void process(Packet message) {
        processorPool.submit(new ProcessorTask(message));
    }

    public void shutdown(){
        processorPool.shutdown();
    }
    public boolean isActive(){
        return processorPool.getActiveCount() > 0;
    }
}

class ProcessorTask implements Runnable {
    private Packet message;

    public ProcessorTask(Packet message){
        this.message = message;
    }
    @Override
    public void run() {
        int commandType = message.getMessage().getCommandType();
        Product messageObject = message.getMessage().getMessageObject().getMessage();
        synchronized (Constants.DATA) {
            switch (commandType) {
                case (Constants.COMMAND_ADD_GROUP) -> {
                    ProductGroup toAdd = new ProductGroup(messageObject.getProductGroupName(), new ArrayList<>());
                    Constants.DATA.add(toAdd);
                    sendOkMessage();
                    break;
                }
                case (Constants.COMMAND_ADD_PRODUCT) -> {
                    ProductGroup toAdd = Constants.DATA.stream().filter(g -> g.getName().equals(messageObject.getProductGroupName())).findFirst().orElse(null);
                    if (toAdd == null) {
                        sendErrorMessage();
                        return;
                    }

                    toAdd.getProducts().add(messageObject);
                    sendOkMessage();
                    break;
                }
                case (Constants.COMMAND_ADD_PRODUCT_AMOUNT) -> {
                    Product product = findProduct(messageObject.getName());
                    if(product == null) {
                        sendErrorMessage();
                        return;
                    }

                    product.setAmount(product.getAmount() + messageObject.getAmount());
                    sendOkMessage();
                    break;
                }
                case (Constants.COMMAND_REMOVE_PRODUCT_AMOUNT) -> {
                    Product product = findProduct(messageObject.getName());
                    if(product == null) {
                        sendErrorMessage();
                        return;
                    }

                    int amount = product.getAmount() - messageObject.getAmount();
                    amount = (amount < 0) ? 0 : amount;
                    product.setAmount(amount);
                    sendOkMessage();
                    break;
                }
                case (Constants.COMMAND_SET_PRICE) -> {
                    Product product = findProduct(messageObject.getName());
                    if(product == null) {
                        sendErrorMessage();
                        return;
                    }

                    product.setPrice(product.getPrice());
                    sendOkMessage();
                    break;
                }
                case (Constants.COMMAND_GET_PRODUCT_AMOUNT) -> {
                    Product product = findProduct(messageObject.getName());
                    if(product == null) {
                        sendErrorMessage();
                        return;
                    }

                    sendMessage("Amount " + product.getAmount());
                    break;
                }
                default -> {
                    sendErrorMessage();
                    return;
                }
            }
        }
    }

    private Product findProduct(String name){
        return Constants.DATA.stream().flatMap(g -> g.getProducts().stream()).filter(g -> g.getName().equals(name)).findFirst().orElse(null);
    }

    private void sendErrorMessage(){
        sendMessage("Error when accessing data");
    }

    private void sendOkMessage(){
        sendMessage("Ok");
    }

    private void sendMessage(String message){
        Message messageToSend = new Message(this.message.getMessage().getCommandType(), this.message.getMessage().getUserId(), new MessageObject(message));
        Constants.ENCRYPTOR.encrypt(messageToSend, this.message.getSrcId());
    }
}
