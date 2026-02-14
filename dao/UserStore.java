package dao;

import model.User;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import exception.DuplicateUserException;

public class UserStore {

    private final Map<String, User> users = new ConcurrentHashMap<>();

    public void save(User user) {

        if (user == null || user.getUsername() == null) {
            throw new IllegalArgumentException("User or username cannot be null");
        }

        if (users.putIfAbsent(user.getUsername(), user) != null) {
            throw new DuplicateUserException(
                    "User already exists with username: " + user.getUsername());
        }
    }

    public User findByUsername(String username) {
        return users.get(username);
    }

    public boolean exists(String username) {
        return users.containsKey(username);
    }
}
