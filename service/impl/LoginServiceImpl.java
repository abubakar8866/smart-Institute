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

        if (!ValidationUtil.isNotBlank(username) || !ValidationUtil.isNotBlank(password)) {
            throw new InvalidLoginException("Invalid username or password");
        }

        username = username.trim();
        password = password.strip();

        User user = USERS.get(username);

        // ✅ Compare plain text passwords directly
        if (user == null || !password.equals(user.getPassword())) {
            FileUtil.writeToFile("data/login-logs.txt", "LOGIN FAILED: " + username);
            throw new InvalidLoginException("Invalid username or password");
        }

        FileUtil.writeToFile("data/login-logs.txt", "LOGIN SUCCESS: " + user.getUsername());

        return user;
    }
}