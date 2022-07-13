package client_server.models;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class Product {
    private String name;
    private int amount;
    private double price;
    private String description;
    private String producer;
    private int id;
    private int groupId;


    public int getGroupId() {
        return groupId;
    }

    public Product(String name, int amount, double price, String description, String producer, int id, int groupId) {
        this.name = name;
        this.amount = amount;
        this.price = price;
        this.description = description;
        this.producer = producer;
        this.id = id;
        this.groupId = groupId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getProducer() {
        return producer;
    }

    public void setProducer(String producer) {
        this.producer = producer;
    }

    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }


    public int getId() {
        return id;
    }

    public Product(){

    }

    public void setId(int id) {
        this.id = id;
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

}
