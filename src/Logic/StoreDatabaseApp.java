package src.Logic;

import src.Objects.*;
import java.sql.*;
import java.util.Scanner;

public class StoreDatabaseApp {
    // Database connection details
    private static final String DB_URL = "jdbc:mysql://localhost:3306/storedb";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "password";

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        ManageProducts.setConnectionInfo(DB_URL, DB_USER, DB_PASSWORD);
        CompleteTransactions.setConnectionInfo(DB_URL, DB_USER, DB_PASSWORD);
        CustomerHistory customers = new CustomerHistory(); // Ask about connection info procedure

        boolean running = true;

        while (running) {
            System.out.println("Welcome to Product Management System");
            System.out.println("1. Manage Products");
            System.out.println("2. Complete Transactions");
            System.out.println("3. View Customer History");
            System.out.println("4. Exit");
            System.out.print("Enter your choice: ");

            int choice = scanner.nextInt();

            switch (choice) {
                case 1 -> ManageProducts.manageProducts(scanner);
                case 2 -> CompleteTransactions.TransactionMenu(scanner);
                //case 3 -> CustomerHistory(scanner); Ask about completion
                case 4 -> {
                    System.out.println("Exiting the application. Goodbye!");
                    running = false;
                }
                default -> System.out.println("Invalid choice. Please try again.");
            }
        }

        scanner.close();
    }
}
