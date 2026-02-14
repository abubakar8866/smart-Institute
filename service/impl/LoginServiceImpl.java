package service.impl;

import model.User;
import dao.UserStore;
import exception.InvalidLoginException;
import service.LoginService;
import util.PasswordUtil;
import util.ValidationUtil;
import util.FileUtil;

public class LoginServiceImpl implements LoginService {

    private final UserStore userStore;

    public LoginServiceImpl(UserStore userStore) {
        this.userStore = userStore;
    }

    @Override
    public User login(String username, String password) {

        if (!ValidationUtil.isNotBlank(username) ||
            !ValidationUtil.isNotBlank(password)) {

            throw new InvalidLoginException(
                    "Username and password cannot be empty");
        }

        username = username.trim();
        password = password.trim();

        User user = userStore.findByUsername(username);

        if (user == null) {
            throw new InvalidLoginException(
                    "Invalid username or password");
        }

        String hashedInputPassword =
                PasswordUtil.hashPassword(password);

        if (!user.getPassword().equals(hashedInputPassword)) {
            throw new InvalidLoginException(
                    "Invalid username or password");
        }

        // Optional: Log successful login
        FileUtil.writeToFile(
                "data/login-logs.txt",
                "LOGIN SUCCESS: " + user.getUsername()
        );

        return user;
    }
}
