package main;

import model.Role;
import model.User;
import service.LoginService;
import service.RegistrationService;
import service.impl.LoginServiceImpl;
import service.impl.RegistrationServiceImpl;

import java.util.Scanner;

import exception.AdminAlreadyExistsException;

public class MainApp {

	private final RegistrationService registrationService;
	private final LoginService loginService;
	private final Scanner scanner;

	public MainApp() {
		this.registrationService = new RegistrationServiceImpl();
		this.loginService = new LoginServiceImpl();
		this.scanner = new Scanner(System.in);
	}

	public static void main(String[] args) {
		new MainApp().start();
	}

	private void start() {

		while (true) {
			
			printMainMenu();

			int choice = readIntInput();

			switch (choice) {
			case 1 -> registerUser();
			case 2 -> loginUser();
			case 3 -> exitApplication();
			default -> System.out.println("Invalid choice. Try again.");
			}
		}
	}

	// ================= MAIN MENU =================

	private void printMainMenu() {
		System.out.println("\n===== MAIN MENU =====");
		System.out.println("1. Register");
		System.out.println("2. Login");
		System.out.println("3. Exit");
		System.out.print("Choose: ");
	}

	private void registerUser() {

		System.out.print("Username: ");
		String username = scanner.nextLine();

		System.out.print("Password: ");
		String password = scanner.nextLine();

		System.out.print("Role (ADMIN/USER): ");
		String role = scanner.nextLine();

		try {
			registrationService.registerUser(username, password, role);
			System.out.println("Registered Successfully");
		} catch (AdminAlreadyExistsException e) {
			System.out.println(e.getMessage());
		} catch (IllegalArgumentException e) {
			System.out.println(e.getMessage());
		}
	}

	private void loginUser() {

		System.out.print("Username: ");
		String username = scanner.nextLine();

		System.out.print("Password: ");
		String password = scanner.nextLine();

		try {
			User user = loginService.login(username, password);
			showDashboard(user);
		} catch (Exception e) {
			System.out.println("Login Failed ❌");
		}
	}

	private void exitApplication() {
		System.out.println("Exiting...");
		scanner.close();
		System.exit(0);
	}

	// ================= DASHBOARD =================

	private void showDashboard(User user) {

		if (user.getRole() == Role.ADMIN) {
			new AdminDashboard().start();
		} else {
			new UserDashboard().start();
		}
	}

	// ================= COMMON METHODS ================

	private int readIntInput() {

		while (!scanner.hasNextInt()) {
			System.out.println("Invalid input! Enter number.");
			scanner.nextLine();
		}

		int value = scanner.nextInt();
		scanner.nextLine();
		return value;
	}
}
