package client_server;

import client_server.models.Product;

//for simple productService test
public class Main {
    public static void main(String[] args) {
        ProductService productService = new ProductService("Products.db");
        for(int i = 0; i < 5; i++){
            productService.insert(new Product("test "+i , (int) (Math.random()*100), Math.random()*100," group1"));
        }

        System.out.println("Product " + productService.getAll().get(0).getName() + "; price : " + productService.getAll().get(0).getPrice());
        productService.deleteAll();
        System.out.println(productService.getAll().size());
    }
}
