package service.impl;

import model.User;
import dao.UserStore;
import service.RegistrationService;
import util.PasswordUtil;
import util.IdGenerator;
import util.ValidationUtil;
import util.FileUtil;

public class RegistrationServiceImpl implements RegistrationService {

    private final UserStore userStore;

    public RegistrationServiceImpl(UserStore userStore) {
        this.userStore = userStore;
    }

    @Override
    public void registerUser(String username,
                             String password,
                             String role) {

        if (!ValidationUtil.isNotBlank(username)) {
            throw new IllegalArgumentException("Invalid username");
        }

        if (!ValidationUtil.isNotBlank(password)) {
            throw new IllegalArgumentException("Password cannot be empty");
        }

        if (!ValidationUtil.isNotBlank(role)) {
            throw new IllegalArgumentException("Role cannot be empty");
        }

        // Check if already exists
        if (userStore.findByUsername(username) != null) {
            throw new IllegalArgumentException("Username already exists");
        }

        Integer userId = IdGenerator.generateId();

        String hashedPassword =
                PasswordUtil.hashPassword(password);

        User user = new User(
                userId,
                username,
                hashedPassword,
                role
        );

        userStore.save(user);

        FileUtil.writeToFile(
                "data/users.txt",
                user.toString()
        );
    }

    @Override
    public User getUserByUsername(String username) {

        if (!ValidationUtil.isNotBlank(username)) {
            throw new IllegalArgumentException(
                    "Username cannot be null or blank");
        }

        return userStore.findByUsername(username);
    }
}
