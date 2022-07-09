package client_server.http_server;

import client_server.exceptions.DataAccessException;
import client_server.models.User;
import org.apache.commons.codec.digest.DigestUtils;

import java.sql.*;

public class UserService {
    private Connection connection;

    public UserService(String filename) {
        try {
            Class.forName("org.sqlite.JDBC");
            this.connection = DriverManager.getConnection("jdbc:sqlite:" + filename);
        } catch (ClassNotFoundException e) {
            throw new DataAccessException("Can't find SQLite JDBC class");
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        }

        initTable();
    }

    private void initTable() {
        try (Statement statement = connection.createStatement()) {
            statement.execute("create table if not exists 'users'('id' INTEGER PRIMARY KEY AUTOINCREMENT, 'name' VARCHAR(50) NOT NULL, 'password' VARCHAR(250) NOT NULL, UNIQUE ('name'))");
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        }
    }


    public User getByName(String name) {
        try (PreparedStatement insertStatement = connection.prepareStatement("select * from 'users' where name = ?")) {
            insertStatement.setString(1, name);
            ResultSet resultSet = insertStatement.executeQuery();

            if (resultSet.next()) {
                return new User(resultSet.getInt("id"),resultSet.getString("name"), resultSet.getString("password"));
            }
            return null;
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        }
    }


    public void insert(User user) {
        try (PreparedStatement insertStatement = connection.prepareStatement("insert into 'users'('name', 'password') values (?, ?)")) {
            insertStatement.setString(1, user.getName());
            insertStatement.setString(2, DigestUtils.md5Hex(user.getPassword()));
            insertStatement.execute();
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        }
    }

    public void deleteAll() {
        try(Statement statement = connection.createStatement()) {
            String query = "delete from 'users'";
            statement.execute(query);
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        }
    }
}
