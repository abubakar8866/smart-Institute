package service;

import java.util.Map;

import model.User;

public interface RegistrationService {

    void registerUser(String username,String password,String role);

    User getUserByUsername(String username);
    
    Map<String, User> getAllUsers();
}
