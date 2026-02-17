package main;

import model.User;
import service.LoginService;
import service.RegistrationService;
import service.impl.LoginServiceImpl;
import service.impl.RegistrationServiceImpl;

import java.util.Scanner;

public class MainApp {

	public static void main(String[] args) {

		RegistrationService registrationService = new RegistrationServiceImpl();

		LoginService loginService = new LoginServiceImpl();

		Scanner scanner = new Scanner(System.in);

		while (true) {

			System.out.println("\n===== MAIN MENU =====");
			System.out.println("1. Register");
			System.out.println("2. Login");
			System.out.println("3. Exit");
			System.out.print("Choose: ");

			int choice = scanner.nextInt();
			scanner.nextLine();

			switch (choice) {

			case 1 -> {
				System.out.print("Username: ");
				String u = scanner.nextLine();

				System.out.print("Password: ");
				String p = scanner.nextLine();

				System.out.print("Role (ADMIN/USER): ");
				String r = scanner.nextLine();

				registrationService.registerUser(u, p, r);
				System.out.println("Registered Successfully");
			}

			case 2 -> {
				System.out.print("Username: ");
				String u = scanner.nextLine();

				System.out.print("Password: ");
				String p = scanner.nextLine();

				try {
					User user = loginService.login(u, p);
					showDashboard(user, scanner);
				} catch (Exception e) {
					System.out.println("Login Failed");
				}
			}

			case 3 -> {
				System.out.println("Exiting...");
				scanner.close();
				return;
			}

			default -> System.out.println("Invalid choice");
			}
		}
	}

	private static void showDashboard(User user, Scanner scanner) {

		while (true) {

			System.out.println("\n=== DASHBOARD ===");
			System.out.println("Welcome " + user.getUsername());
			System.out.println("Role: " + user.getRole());

			if (user.getRole().equalsIgnoreCase("ADMIN")) {

				System.out.println("1. View Profile");
				System.out.println("2. View All Users");
				System.out.println("3. Logout");

				System.out.print("Choose: ");
				int option = scanner.nextInt();
				scanner.nextLine();

				switch (option) {

				case 1 -> {
					System.out.println("\n--- PROFILE ---");
					System.out.println("User ID: " + user.getUserId());
					System.out.println("Username: " + user.getUsername());
					System.out.println("Role: " + user.getRole());
				}

				case 2 -> {
					System.out.println("\n--- ALL REGISTERED USERS ---");

					RegistrationServiceImpl.getAllUsers().values().forEach(System.out::println);
				}

				case 3 -> {
					System.out.println("Logged out successfully ✅");
					return;
				}

				default -> System.out.println("Invalid option");
				}

			} else {

				// Normal User Menu
				System.out.println("1. View Profile");
				System.out.println("2. Logout");

				System.out.print("Choose: ");
				int option = scanner.nextInt();
				scanner.nextLine();

				switch (option) {

				case 1 -> {
					System.out.println("\n--- PROFILE ---");
					System.out.println("User ID: " + user.getUserId());
					System.out.println("Username: " + user.getUsername());
					System.out.println("Role: " + user.getRole());
				}

				case 2 -> {
					System.out.println("Logged out successfully");
					return;
				}

				default -> System.out.println("Invalid option");
				}
			}
		}
	}

}
