package client_server;


import client_server.exceptions.DataAccessException;
import client_server.models.Product;
import client_server.models.ProductFilter;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

        initializeTable();
    }

    private void initializeTable() {
        try (Statement statement = connection.createStatement()) {
            String query = "create table if not exists 'products' " +
                "('id' INTEGER PRIMARY KEY AUTOINCREMENT, 'name' varchar not null, 'price' double not null, 'amount' integer not null, 'productGroupName' varchar not null);";
            statement.execute(query);
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        }

    }

    public Product get(int id) {
        try (Statement statement = connection.createStatement()) {
            String sql = "select * from 'products' where id = " + id;
            ResultSet resultSet = statement.executeQuery(sql);
            resultSet.next();
            Product product = new Product(resultSet.getString("name"),
                resultSet.getInt("amount"),
                resultSet.getDouble("price"),
                resultSet.getString("productGroupName"),
                resultSet.getInt("id"));

            return product;
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        }
    }

    public void insert(Product product) {
        if(isNameUnique(product.getName())) {
            String query = "insert into 'products'" + " ('name', 'amount', 'price', 'productGroupName') values (?, ?, ?, ?);";
            try(PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setString(1, product.getName());
                preparedStatement.setInt(2, product.getAmount());
                preparedStatement.setDouble(3, product.getPrice());
                preparedStatement.setString(4, product.getProductGroupName());

                preparedStatement.execute();
            } catch (SQLException e) {
                throw new DataAccessException(e.getMessage());
            }
        }
    }

    public void update(Product product){
        if(isNameUnique(product.getName())) {
            try (PreparedStatement preparedStatement =
                     connection.prepareStatement("update 'products' set name = ?, amount = ?, price = ?, productGroupName = ? where id = ?")) {
                preparedStatement.setString(1, product.getName());
                preparedStatement.setInt(2, product.getAmount());
                preparedStatement.setDouble(3, product.getPrice());
                preparedStatement.setString(4, product.getProductGroupName());
                preparedStatement.setInt(5, product.getId());

                preparedStatement.executeUpdate();
            } catch (SQLException e) {
                throw new DataAccessException(e.getMessage());
            }
        }
    }

    public void delete(int id){
        try(PreparedStatement preparedStatement = connection.prepareStatement("delete from 'products' where id = ?")) {
            preparedStatement.setInt(1, id);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        }
    }


    public List<Product> getAll() {
        return listByCriteria(-1, new ProductFilter());
    }
    public List<Product> getAll(int size){
        return listByCriteria(size, new ProductFilter());
    }

    public List<Product> listByCriteria(int size, ProductFilter filter) {
        try(Statement statement = connection.createStatement()) {
            String query = Stream.of(
                    more("price", filter.getFromPrice()),
                    less("price", filter.getToPrice()),
                    stringEquals("productGroupName", filter.getGroup()),
                    stringEquals("name", filter.getName())
                )
                .filter(Objects::nonNull)
                .collect(Collectors.joining(" AND "));

            String where = query.isEmpty() ? "" : " where " + query;
            String sql = String.format("select * from 'products' %s limit %s", where, size);
            if(size == -1) {
                sql = String.format("select * from 'products' %s", where);
            }
            ResultSet resultSet = statement.executeQuery(sql);

            List<Product> products = new ArrayList<>();
            while(resultSet.next()) {
                products.add(new Product(
                    resultSet.getString("name"),
                    resultSet.getInt("amount"),
                    resultSet.getDouble("price"),
                    resultSet.getString("productGroupName"),
                    resultSet.getInt("id")));
            }
            return products;
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        }
    }

    public void deleteAll() {
        try(Statement statement = connection.createStatement()) {
            String query = "delete from 'products'";
            statement.execute(query);
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        }
    }

    private String more(String fieldName, Double value) {
        if(value == null){
            return null;
        }
        return fieldName + " >= " + value;
    }

    private String stringEquals(String fieldName, String value) {
        if(value == null){
            return null;
        }
        return fieldName + " = '" + value +"'";
    }


    private String less(String fieldName, Double value) {
        if(value == null){
            return null;
        }
        return fieldName + " <= " + value;
    }


    private boolean isNameUnique(String productName){
        try(Statement statement = connection.createStatement()) {
            ResultSet resultSet = statement.executeQuery(
                String.format("select count(*) as count from 'products' where name = '%s'", productName)
            );
            resultSet.next();
            return resultSet.getInt("count") == 0;
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        }
    }

}


