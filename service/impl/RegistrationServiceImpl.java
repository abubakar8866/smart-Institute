package service.impl;

import model.User;
import service.RegistrationService;
import util.*;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RegistrationServiceImpl implements RegistrationService {

	static final Map<String, User> USERS = new ConcurrentHashMap<>();

	@Override
	public void registerUser(String username, String password, String role) {

		if (!ValidationUtil.isNotBlank(username) || !ValidationUtil.isNotBlank(password) || !ValidationUtil.isNotBlank(role)) {
			throw new IllegalArgumentException("Invalid input");
		}

		username = username.trim();

		if (USERS.containsKey(username)) {
			throw new IllegalArgumentException("Username already exists");
		}

		User user = new User(IdGenerator.generateId(), username, PasswordUtil.hashPassword(password), role.trim());

		USERS.put(username, user);

		FileUtil.writeToFile("data/users.txt", user.toString());
	}

	@Override
	public User getUserByUsername(String username) {
		return USERS.get(username);
	}
}
