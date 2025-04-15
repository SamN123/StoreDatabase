package src.Logic;

import src.Objects.*;
import src.Authentication.AuthenticationService;
import src.Authentication.LoginScreen;
import src.Security.SecurityUtil;
import java.sql.*;
import java.util.Scanner;

public class StoreDatabaseApp {
    // database connection details
    private static final String DB_URL = "jdbc:mysql://localhost:3306/storedb";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "password";

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        
        // set connection info for all services
        ManageProducts.setConnectionInfo(DB_URL, DB_USER, DB_PASSWORD);
        CompleteTransactions.setConnectionInfo(DB_URL, DB_USER, DB_PASSWORD);
        CustomerHistory.setConnectionInfo(DB_URL, DB_USER, DB_PASSWORD);
        AuthenticationService.setConnectionInfo(DB_URL, DB_USER, DB_PASSWORD);

        System.out.println("Welcome to Store Database Management System");
        
        // require login before accessing the application
        boolean loginSuccess = LoginScreen.showLoginScreen(scanner);
        
        if (!loginSuccess) {
            System.out.println("Authentication required. Exiting the application.");
            scanner.close();
            return;
        }
        
        // get the authenticated user
        Person currentUser = AuthenticationService.getCurrentUser();
        System.out.println("Welcome, " + currentUser.getFirstName() + " " + currentUser.getLastName() + "!");
        
        boolean running = true;

        while (running) {
            System.out.println("\n--- Main Menu ---");
            // show admin-only label for manage products if user is not admin
            System.out.println("1. Manage Products" + (SecurityUtil.hasAdminPermission() ? "" : " (Admin Only)"));
            System.out.println("2. Complete Transactions");
            System.out.println("3. View Customer History");
            System.out.println("4. Logout");
            System.out.println("5. Exit");
            System.out.print("Enter your choice: ");

            int choice = scanner.nextInt();

            switch (choice) {
                case 1 -> {
                    // only admins can manage products
                    if (SecurityUtil.hasAdminPermission()) {
                        ManageProducts.manageProducts(scanner);
                    } else {
                        System.out.println("Access denied. Admin privileges required.");
                    }
                }
                case 2 -> CompleteTransactions.TransactionMenu(scanner);
                case 3 -> CustomerHistory.customerHistoryMenu(scanner);
                case 4 -> {
                    // logout and show login screen again
                    System.out.println("Logging out...");
                    AuthenticationService.logout();
                    loginSuccess = LoginScreen.showLoginScreen(scanner);
                    
                    if (!loginSuccess) {
                        System.out.println("Authentication required. Exiting the application.");
                        running = false;
                    } else {
                        // get the newly authenticated user
                        currentUser = AuthenticationService.getCurrentUser();
                        System.out.println("Welcome back, " + currentUser.getFirstName() + " " + currentUser.getLastName() + "!");
                    }
                }
                case 5 -> {
                    System.out.println("Exiting the application. Goodbye!");
                    running = false;
                }
                default -> System.out.println("Invalid choice. Please try again.");
            }
        }

        scanner.close();
    }
}
