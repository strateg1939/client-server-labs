package client_server;

import client_server.exceptions.DataAccessException;
import client_server.http_server.ProductService;
import client_server.models.Product;
import client_server.models.ProductFilter;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

import java.util.List;


public class ProductServiceTests {
    private static ProductService productService;

    @BeforeClass
    public static void CreateService() {
        productService = new ProductService("Products.db");
        productService.deleteAll();
    }

    @Test
    public void testInsert(){
        String productName = "testProduct";
        Product product = new Product(productName, 1, 1, "group");
        ProductFilter filter = new ProductFilter();
        filter.setName(productName);

        productService.insert(product);
        List<Product> insertedProducts = productService.listByCriteria(1, filter);
        Product insertedProduct = insertedProducts.get(0);

        assertEquals(product.getName(), insertedProduct.getName());
        assertEquals(product.getProductGroupName(), insertedProduct.getProductGroupName());
        assertEquals(product.getAmount(), insertedProduct.getAmount());
    }

    @Test
    public void testDoesNotInsertIfSameName() {
        String productName = "testProduct";
        Product product = new Product(productName, 1, 1, "group");
        ProductFilter filter = new ProductFilter();
        filter.setName(productName);
        int expected = 1;

        productService.insert(product);
        productService.insert(product);
        List<Product> insertedProducts = productService.listByCriteria(10, filter);

        assertEquals(expected, insertedProducts.size());
    }

    @Test
    public void testUpdate(){
        String productName = "testProduct";
        Product product = new Product(productName, 1, 1, "group");
        productService.insert(product);

        String newName = "updatedName";
        Product newProduct = productService.getAll().get(0);
        newProduct.setName(newName);
        ProductFilter filter = new ProductFilter();
        filter.setName(newName);

        productService.update(newProduct);
        List<Product> updatedProducts = productService.listByCriteria(1, filter);
        Product updatedProduct = updatedProducts.get(0);

        assertEquals(newProduct.getName(), updatedProduct.getName());
        assertEquals(newProduct.getProductGroupName(), updatedProduct.getProductGroupName());
        assertEquals(newProduct.getAmount(), updatedProduct.getAmount());
    }

    @Test(expected = DataAccessException.class)
    public void testGetThrowsIfIdWrong(){
        productService.get(-1);
    }

    @Test
    public void testGetAll(){
        int expected = 0;

        int actual = productService.getAll().size();

        assertEquals(expected, actual);
    }

    @Test
    public void testFilterGetsOnlyFilteredProducts(){
        ProductFilter filter = new ProductFilter();
        filter.setFromPrice(5.01);
        filter.setToPrice(10.01);
        int expected = 5;
        int allProducts = 15;

        for(double i = 0; i < allProducts; i++){
            productService.insert(new Product("test" + i, 1, i,"test"));
        }

        List<Product> products = productService.listByCriteria(allProducts, filter);

        assertEquals(expected, products.size());
    }

    @Test
    public void testDelete(){
        String productName = "testProduct";
        Product product = new Product(productName, 1, 1, "group");
        productService.insert(product);

        List<Product> allProducts = productService.getAll();
        Product expectedProduct = allProducts.get(0);
        int expected = allProducts.size() - 1;

        productService.delete(expectedProduct.getId());

        assertEquals(expected, productService.getAll().size());
    }

    @After
    public void DeleteGeneratedProducts() {
        productService.deleteAll();
    }
}
