package client_server.client;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.Serializable;
import java.util.ArrayList;

public class Group implements Serializable {
    public String name;
    public String description;
    @JsonIgnore
    public ArrayList<Product> products;
    public int id;

    public Group(String name, String description) {
        this.name = name;
        this.description = description;
        products = new ArrayList<>();
    }
    public Group() {
        products = new ArrayList<>();
    }

    @Override
    public String toString() {
        return "Group : " + name;

    }
}
