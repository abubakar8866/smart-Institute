package main;

import model.User;
import service.LoginService;
import service.impl.LoginServiceImpl;

import java.util.Scanner;

public class MainApp {

    public static void main(String[] args) {

        try (var sc = new Scanner(System.in)) {

            LoginService loginService =
                    new LoginServiceImpl();

            System.out.println("=== Smart Institute Management System ===");

            System.out.print("Username: ");
            var username = sc.nextLine();

            System.out.print("Password: ");
            var password = sc.nextLine();

            User user = loginService.login(username, password);

            System.out.println("Login successful!");

            switch (user.getRole().toUpperCase()) {
                case "ADMIN" -> new AdminDashboard().start();
                default -> new UserDashboard(user).start();
            }

        } catch (Exception e) {
            System.out.println("ERROR: " + e.getMessage());
        }
    }
}
