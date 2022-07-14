package client_server.http_server;

import client_server.exceptions.DataAccessException;
import client_server.exceptions.DataIncorrectException;
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
                    "name varchar(256) NOT NULL," +
                    "description TEXT NOT NULL)";
            st.execute(createTable);
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        }
    }

    public ProductGroup createGroup(ProductGroup group) {
        String sql = "INSERT INTO groups (name, description) VALUES (?,?)";
        String[] generatedColumns = {"ID"};
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql, generatedColumns)) {
            if(nameExists(group.getName())) throw new DataIncorrectException("Name exists");
            preparedStatement.setString(1, group.getName());
            preparedStatement.setString(2, group.getDescription());
            preparedStatement.execute();

            ResultSet rs = preparedStatement.getGeneratedKeys();
            int id = rs.getInt(1);
            group.setId(id);
            return group;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new DataAccessException(e.getMessage());
        }
    }

    public List<ProductGroup> getAllGroups() {
        List<ProductGroup> groups = new ArrayList<>();
        String sql = "SELECT * FROM groups";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                groups.add(new ProductGroup(
                    resultSet.getInt("id"),
                    resultSet.getString("name"),
                    resultSet.getString("description")
                ));
            }
            resultSet.close();
        } catch (Exception e) {
            throw new DataAccessException(e.getMessage());
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
            throw new DataAccessException(e.getMessage());
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
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
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
            throw new DataAccessException(e.getMessage());
        }
    }

    public boolean nameExists(String name) {
        String sql = "SELECT * FROM groups WHERE name = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, name);
            ResultSet resultSet = preparedStatement.executeQuery();
            return resultSet.next();
        } catch (Exception e) {
            throw new DataAccessException(e.getMessage());
        }
    }
}