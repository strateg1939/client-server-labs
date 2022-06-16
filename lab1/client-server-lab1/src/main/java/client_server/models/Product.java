package client_server.models;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class Product {
    private String name;
    private int amount;
    private double price;
    private String productGroupName;

    public Product(String name, int amount, double price, String productGroupName) {
        this.name = name;
        this.amount = amount;
        this.price = price;
        this.productGroupName = productGroupName;
    }
    public Product(){

    }

    public Product(String name){
        this(name, 0, 0, null);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getProductGroupName() {
        return productGroupName;
    }

}
