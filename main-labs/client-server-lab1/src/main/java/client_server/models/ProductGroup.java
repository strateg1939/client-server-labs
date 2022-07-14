package client_server.models;

public class ProductGroup {
    private int id;
    private String name;
    private String description;

    public ProductGroup() {
    }

    public ProductGroup(int id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }


    public ProductGroup(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public int getId() {
        return id;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}