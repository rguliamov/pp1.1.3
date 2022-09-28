package jm.task.core.jdbc.dao;

import jm.task.core.jdbc.model.User;
import jm.task.core.jdbc.util.Util;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class UserDaoJDBCImpl implements UserDao {
    private Util util = new Util();

    public UserDaoJDBCImpl() {

    }

    public void createUsersTable() {
        String sql = "CREATE TABLE USER (" +
                "id bigint  PRIMARY KEY AUTO_INCREMENT," +
                "name varchar(32) NOT NULL," +
                "lastName varchar(32) NOT NULL," +
                "age integer," +
                "check (age >= 0 AND age <= 127))";

        try {
            util.statement(stmt -> {
                stmt.executeUpdate(sql);
            });
        } catch (SQLException e) {
        }
    }


    public void dropUsersTable() {
        String sql = "DROP TABLE USER";
        try {
            util.statement(stmt -> {
                stmt.executeUpdate(sql);
            });
        } catch (SQLException e) {
        }
    }

    public void saveUser(String name, String lastName, byte age) {
        String sql = "INSERT INTO USER(name, lastName, age) values (?, ?, ?)";
        try {
            util.preparedStatement(sql, stmt -> {
                stmt.setString(1, name);
                stmt.setString(2, lastName);
                stmt.setByte(3,age);
                stmt.executeUpdate();
            });
        } catch (SQLException e) {
        }
    }

    public void removeUserById(long id) {
        String sql = "DELETE FROM USER WHERE id=?";
        try {
            util.preparedStatement(sql, stmt -> {
                stmt.setLong(1, id);
                stmt.executeUpdate();
            });
        } catch (SQLException e) {
        }
    }

    public List<User> getAllUsers() {
        String sql = "SELECT * FROM USER";
        List<User> users = new ArrayList<>();
        try {
            util.statement(stmt -> {
                ResultSet resultSet = stmt.executeQuery(sql);

                while (resultSet.next()) {
                    User user = new User();
                    user.setId(resultSet.getLong(1));
                    user.setName(resultSet.getString(2));
                    user.setLastName(resultSet.getString(3));
                    user.setAge(resultSet.getByte(4));
                    users.add(user);
                }
            });
        } catch (SQLException e) {
        }

        return users;
    }

    public void cleanUsersTable() {
        String sql = "DELETE FROM USER";
        try {
            util.statement(stmt -> {
                stmt.executeUpdate(sql);
            });
        } catch (SQLException e) {
        }
    }
}
