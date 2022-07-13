package client_server.client;

import java.io.Serializable;
import java.util.ArrayList;

public class Group implements Serializable, CreateDelete {
    public String name;
    public String description;
    public ArrayList<Product> products;

    public Group(String name, String description) {
        this.name = name;
        this.description = description;
        products = new ArrayList<>();
    }
    public Group() {

    }

    @Override
    // Adds a new product to the group
    public void add(Object product) {
        products.add((Product) product);
    }

    @Override
    // Removes a product from the group
    public void remove(int index) {
        products.remove(index);
    }

    @Override
    public String toString() {
        return "Group : " + name;

    }
}
