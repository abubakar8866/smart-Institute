package service.impl;

import model.Role;
import model.User;
import service.RegistrationService;
import util.*;

import java.io.File;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RegistrationServiceImpl implements RegistrationService {

    private static final String USERS_FILE = "data/users.csv";

    static final Map<String, User> USERS = new ConcurrentHashMap<>();

    static {
        loadUsersFromFile();
    }

    // ================= LOAD =================

    private static void loadUsersFromFile() {

        try {

            File file = new File(USERS_FILE);
            if (!file.exists())
                return;

            String content = FileUtil.readFile(USERS_FILE);
            if (content.isBlank())
                return;

            String[] lines = content.split("\\R");

            boolean isFirstLine = true;

            for (String line : lines) {

                if (isFirstLine) {   // skip header
                    isFirstLine = false;
                    continue;
                }

                String[] parts = line.split(",");

                if (parts.length != 4)
                    continue;

                Integer id = Integer.parseInt(parts[0]);
                String username = parts[1];
                String password = parts[2];

                Role role;

                try {
                    role = Role.valueOf(parts[3].toUpperCase());
                } catch (IllegalArgumentException e) {
                    System.out.println("Invalid role in CSV: " + parts[3]);
                    continue;
                }

                User user = new User(id, username, password, role);
                USERS.put(username, user);
            }

        } catch (Exception e) {
            System.out.println("Error loading users: " + e.getMessage());
        }
    }

    // ================= REWRITE =================

    private static void rewriteUsersFile() {

        StringBuilder sb = new StringBuilder();

        // CSV Header
        sb.append("id,username,password,role")
          .append(System.lineSeparator());

        for (User user : USERS.values()) {

            sb.append(user.getUserId()).append(",")
              .append(user.getUsername()).append(",")
              .append(user.getPassword()).append(",")
              .append(user.getRole())
              .append(System.lineSeparator());
        }

        FileUtil.overwriteFile(USERS_FILE, sb.toString());
    }

    // ================= REGISTER =================

    @Override
    public void registerUser(String username, String password, String roleInput) {

        if (!ValidationUtil.isNotBlank(username) ||
            !ValidationUtil.isNotBlank(password) ||
            !ValidationUtil.isNotBlank(roleInput)) {

            throw new IllegalArgumentException("Invalid input");
        }

        username = username.trim();

        if (USERS.containsKey(username)) {
            throw new IllegalArgumentException("Username already exists");
        }

        Role role;

        try {
            role = Role.valueOf(roleInput.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid role. Use ADMIN or USER.");
        }

        User user = new User(
                IdGenerator.generateId(),
                username,
                PasswordUtil.hashPassword(password),
                role
        );

        USERS.put(username, user);

        rewriteUsersFile();
    }    

    // ================= GETTERS =================

    @Override
    public User getUserByUsername(String username) {
        return USERS.get(username);
    }

    @Override
    public Map<String, User> getAllUsers() {
        return USERS;
    }
}
