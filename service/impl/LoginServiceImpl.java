package service.impl;

import model.User;
import service.LoginService;
import exception.InvalidLoginException;
import util.*;

import java.util.Map;

public class LoginServiceImpl implements LoginService {

    // Share same storage
    private static final Map<String, User> USERS = RegistrationServiceImpl.USERS;

    @Override
    public User login(String username, String password) {

        if (!ValidationUtil.isNotBlank(username) ||
            !ValidationUtil.isNotBlank(password)) {

            throw new InvalidLoginException(
                    "Invalid username or password");
        }

        username = username.trim();
        password = password.trim();

        User user = USERS.get(username);

        if (user == null ||
            !user.getPassword().equals(
                PasswordUtil.hashPassword(password))) {

            throw new InvalidLoginException(
                    "Invalid username or password");
        }

        FileUtil.writeToFile(
                "data/login-logs.txt",
                "LOGIN SUCCESS: " + user.getUsername()
        );

        return user;
    }
}
