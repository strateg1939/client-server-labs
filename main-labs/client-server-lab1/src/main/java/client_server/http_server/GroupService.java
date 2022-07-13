package client_server.http_server;

import client_server.exceptions.DataAccessException;
import client_server.models.ProductGroup;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class GroupService {

    private final Connection connection;

    public GroupService(String dbFile){
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
            String dropTable = "DROP TABLE IF EXISTS groups";
            st1.execute(dropTable);

            Statement st = connection.createStatement();
            String createTable = "CREATE TABLE IF NOT EXISTS groups(" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
                    "name TEXT NOT NULL," +
                    "description TEXT NOT NULL)";
            st.execute(createTable);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public ProductGroup createGroup(ProductGroup group) {
        String sql = "INSERT INTO groups (name, description) VALUES (?,?)";
        String[] generatedColumns = {"ID"};
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql, generatedColumns)) {
            preparedStatement.setString(1, group.getName());
            preparedStatement.setString(2, group.getDescription());
            preparedStatement.execute();

            ResultSet rs = preparedStatement.getGeneratedKeys();
            int id = rs.getInt(1);
            group.setId(id);
            return group;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public List<ProductGroup> getAllGroups() {
        List<ProductGroup> groups = new ArrayList<>();
        String sql = "SELECT g.id, g.name, g.description, " +
                "(SELECT SUM(p.price * p.amount) FROM products p WHERE p.groupId = g.id) totalPrice " +
                "FROM groups g";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                groups.add(new ProductGroup(
                        resultSet.getInt("id"),
                        resultSet.getString("name"),
                        resultSet.getString("description"),
                        resultSet.getDouble("totalPrice")
                ));
            }
            resultSet.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return groups;
    }

    public ProductGroup getOneGroup(int id) {
        ProductGroup result = null;
        String sql = "SELECT * FROM groups WHERE id = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, id);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                result = new ProductGroup(
                        resultSet.getInt("id"),
                        resultSet.getString("name"),
                        resultSet.getString("description")
                );
            }
            resultSet.close();
        } catch (Exception e) {
            System.out.println(e);
            throw new RuntimeException(e);
        }
        return result;
    }

    public ProductGroup updateGroup(int id, ProductGroup group) {
        String sql = "UPDATE groups SET name = ?, description = ? WHERE id = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, group.getName());
            preparedStatement.setString(2, group.getDescription());
            preparedStatement.setInt(3, id);
            preparedStatement.execute();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        group.setId(id);
        return group;
    }

    public void deleteGroup(int id) {
        String sql = "DELETE FROM groups WHERE id = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, id);
            preparedStatement.execute();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public boolean checkIfNameExists(String name) {
        String sql = "SELECT * FROM groups WHERE name = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, name);
            ResultSet resultSet = preparedStatement.executeQuery();
            boolean exists = resultSet.next();
            resultSet.close();
            return exists;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            throw new RuntimeException(e);
        }
    }
}