package jm.task.core.jdbc;

import jm.task.core.jdbc.model.User;
import jm.task.core.jdbc.service.UserService;
import jm.task.core.jdbc.service.UserServiceImpl;

public class Main {
    public static void main(String[] args) {
        // реализуйте алгоритм здесь
        UserService service = new UserServiceImpl();

        service.dropUsersTable();
        service.createUsersTable();

        for (int i = 0; i < 4; i++) {
            String name = String.format("Rustam%d", i);
            String lastName = String.format("Gulyamov%d", i);
            byte age = (byte) i;

            service.saveUser(name, lastName, age);
        }

        System.out.println(service.getAllUsers());

        service.cleanUsersTable();
        service.dropUsersTable();
    }
}
