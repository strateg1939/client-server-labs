package client_server.client;

import java.io.Serializable;

public class Product implements Serializable {
    public String name;
    public String description;
    public String producer;
    public int amount;
    public double price;
    public Group group;


    public Product(String name,
                   String description,
                   String producer,
                   int amount,
                   double price,
                   Group group) {
        this.name = name;
        this.description = description;
        this.amount = amount;
        this.price = price;
        this.producer = producer;
        this.group = group;
    }

    public Product() {

    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "Name: " + name + "; description: " + description + "; amount: " + amount + "; producer: " + producer + "; " + group.toString();

    }

}

