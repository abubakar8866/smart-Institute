package service.impl;

import model.User;
import service.RegistrationService;
import util.*;

import java.io.File;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RegistrationServiceImpl implements RegistrationService {

	static final Map<String, User> USERS = new ConcurrentHashMap<>();

	static {
		loadUsersFromFile();
	}

	private static void loadUsersFromFile() {
		try {

			File file = new File("data/users.txt");
			if (!file.exists())
				return;

			String content = FileUtil.readFile("data/users.txt");
			if (content.isBlank())
				return;

			String[] lines = content.split("\\R");

			for (String line : lines) {
				String[] parts = line.split(",");

				if (parts.length != 4)
					continue;

				Integer id = Integer.parseInt(parts[0]);
				String username = parts[1];
				String password = parts[2];
				String role = parts[3];

				User user = new User(id, username, password, role);
				USERS.put(username, user);
			}

		} catch (Exception e) {
			System.out.println("Error loading users: " + e.getMessage());
		}
	}

	@Override
	public void registerUser(String username, String password, String role) {

		if (!ValidationUtil.isNotBlank(username) || !ValidationUtil.isNotBlank(password)
				|| !ValidationUtil.isNotBlank(role)) {
			throw new IllegalArgumentException("Invalid input");
		}

		username = username.trim();

		if (USERS.containsKey(username)) {
			throw new IllegalArgumentException("Username already exists");
		}

		User user = new User(IdGenerator.generateId(), username, PasswordUtil.hashPassword(password), role.trim());

		USERS.put(username, user);

		String record = user.getUserId() + "," + user.getUsername() + "," + user.getPassword() + "," + user.getRole();

		FileUtil.writeToFile("data/users.txt", record);
	}

	@Override
	public User getUserByUsername(String username) {
		return USERS.get(username);
	}

	public static Map<String, User> getAllUsers() {
		return USERS;
	}

}
