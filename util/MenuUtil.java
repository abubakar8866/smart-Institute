package util;

public final class MenuUtil {

    private MenuUtil() { }

    public static void printMainMenu() {
        System.out.println("\n===== MAIN MENU =====");
        System.out.println("1. Manage Students");
        System.out.println("2. Manage Courses");
        System.out.println("3. Manage Payments");
        System.out.println("4. Generate Reports");
        System.out.println("5. Exit");
    }

    public static void printAdminMenu() {
        System.out.println("\n===== ADMIN MENU =====");
        System.out.println("1. Add Course");
        System.out.println("2. Delete Course");
        System.out.println("3. View All Courses");
    }

    public static void printUserMenu() {
        System.out.println("\n===== USER MENU =====");
        System.out.println("1. View Profile");
        System.out.println("2. View Attendance");
        System.out.println("3. View Payments");
    }
}
