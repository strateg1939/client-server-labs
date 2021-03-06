package client_server.http_server;

import client_server.exceptions.DataAccessException;
import client_server.exceptions.DataIncorrectException;
import client_server.models.Product;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductService {

    private final Connection connection;

    public ProductService(String dbFile){
        try {
            Class.forName("org.sqlite.JDBC");
            this.connection = DriverManager.getConnection("jdbc:sqlite:"+dbFile);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            throw new DataAccessException(e.getMessage());
        } catch (SQLException e) {
            e.printStackTrace();
            throw new DataAccessException(e.getMessage());
        }
        try {
            Statement st1 = connection.createStatement();
            String dropTable = "DROP TABLE IF EXISTS products";
            st1.execute(dropTable);

            Statement st = connection.createStatement();
            String createTable = "CREATE TABLE IF NOT EXISTS products(" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
                    "name varchar(256) NOT NULL," +
                    "groupId INT NOT NULL," +
                    "description TEXT NOT NULL," +
                    "manufacturer varchar(256) NOT NULL," +
                    "price double NOT NULL," +
                    "amount INT NOT NULL," +
                    "FOREIGN KEY(groupId) REFERENCES groups(id) ON DELETE CASCADE)";
            st.execute(createTable);
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        }
    }

    public Product createProduct(Product product) {
        String sql = "INSERT INTO products (name, groupId, description, manufacturer, price, amount) VALUES (?,?,?,?,?,?)";
        String[] generatedColumns = {"ID"};
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql, generatedColumns)) {
            if (nameExists(product.getName())) throw new DataIncorrectException("Name exists");
            preparedStatement.setString(1, product.getName());
            preparedStatement.setInt(2, product.getGroupId());
            preparedStatement.setString(3, product.getDescription());
            preparedStatement.setString(4, product.getProducer());
            preparedStatement.setDouble(5, product.getPrice());
            preparedStatement.setInt(6, product.getAmount());
            preparedStatement.execute();

            ResultSet rs = preparedStatement.getGeneratedKeys();
            int id = rs.getInt(1);
            product.setId(id);
            return product;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new DataAccessException(e.getMessage());
        }
    }

    public List<Product> getAllProducts() {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT * FROM products";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                products.add(new Product(
                        resultSet.getString("name"),
                        resultSet.getInt("amount"),
                        resultSet.getDouble("price"),
                        resultSet.getString("description"),
                        resultSet.getString("manufacturer"),
                        resultSet.getInt("id"),
                        resultSet.getInt("groupId")
                ));
            }
            resultSet.close();
        } catch (Exception e) {
            throw new DataAccessException(e.getMessage());
        }
        return products;
    }

    public Product getOneProduct(int id) {
        Product result = null;
        String sql = "SELECT * FROM products WHERE id = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, id);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                result = new Product(
                        resultSet.getString("name"),
                        resultSet.getInt("amount"),
                        resultSet.getDouble("price"),
                        resultSet.getString("description"),
                        resultSet.getString("manufacturer"),
                        resultSet.getInt("id"),
                        resultSet.getInt("groupId")
                );
            }
            resultSet.close();
        } catch (Exception e) {
            throw new DataAccessException(e.getMessage());
        }
        return result;
    }

    public List<Product> getByGroupId(int id) {
        ArrayList<Product> products = new ArrayList<Product>();
        String sql = "SELECT * FROM products WHERE groupId = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, id);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                products.add(new Product(
                        resultSet.getString("name"),
                        resultSet.getInt("amount"),
                        resultSet.getDouble("price"),
                        resultSet.getString("description"),
                        resultSet.getString("manufacturer"),
                        resultSet.getInt("id"),
                        resultSet.getInt("groupId")
                ));
            }
            resultSet.close();
        } catch (Exception e) {
            throw new DataAccessException(e.getMessage());
        }
        return products;
    }

    public Product updateProduct(int id, Product product) {
        String sql = "UPDATE products SET name = ?, description = ?, manufacturer = ?, price = ?, amount = ? WHERE id = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, product.getName());
            preparedStatement.setString(2, product.getDescription());
            preparedStatement.setString(3, product.getProducer());
            preparedStatement.setDouble(4, product.getPrice());
            preparedStatement.setInt(5, product.getAmount());
            preparedStatement.setInt(6, id);
            preparedStatement.execute();
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        }
        product.setId(id);
        return product;
    }

    public void deleteProduct(int id) {
        String sql = "DELETE FROM products WHERE id = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, id);
            preparedStatement.execute();
        } catch (Exception e) {
            throw new DataAccessException(e.getMessage());
        }
    }

    public boolean nameExists(String name) {
        String sql = "SELECT * FROM products WHERE name = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, name);
            ResultSet resultSet = preparedStatement.executeQuery();
            return resultSet.next();
        } catch (Exception e) {
            throw new DataAccessException(e.getMessage());
        }
    }
}