package src.Logic;

import src.Objects.*;
import src.Authentication.AuthenticationService;
import src.Authentication.LoginScreen;
import src.Security.SecurityUtil;
import src.Util.ErrorHandler;
import src.Util.Logger;
import java.sql.*;
import java.util.InputMismatchException;
import java.util.Scanner;

public class StoreDatabaseApp {
    // database connection details
    private static final String DB_URL = "jdbc:mysql://localhost:3306/storedb";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "password";

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        
        try {
            // initialize logger
            Logger.init();
            Logger.log(Logger.INFO, "Starting Store Database Management System");
            
            // set connection info for all services
            OptimizedManageProducts.setConnectionInfo(DB_URL, DB_USER, DB_PASSWORD);
            OptimizedCompleteTransactions.setConnectionInfo(DB_URL, DB_USER, DB_PASSWORD);
            OptimizedCustomerHistory.setConnectionInfo(DB_URL, DB_USER, DB_PASSWORD);
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
            Logger.logUserAction(currentUser.getPersonID(), "Login", "User logged in successfully");
            System.out.println("Welcome, " + currentUser.getFirstName() + " " + currentUser.getLastName() + "!");
        
            boolean running = true;

            while (running) {
                try {
                    System.out.println("\n--- Main Menu ---");
                    // show admin-only label for manage products if user is not admin
                    System.out.println("1. Manage Products" + (SecurityUtil.hasAdminPermission() ? "" : " (Admin Only)"));
                    System.out.println("2. Complete Transactions");
                    System.out.println("3. View Customer History");
                    System.out.println("4. Logout");
                    System.out.println("5. Exit");
                    System.out.print("Enter your choice: ");

                    int choice = scanner.nextInt();
                    scanner.nextLine(); // consume newline

                    switch (choice) {
                        case 1 -> {
                            // only admins can manage products
                            if (SecurityUtil.hasAdminPermission()) {
                                Logger.logUserAction(currentUser.getPersonID(), "Access", "Accessed product management");
                                OptimizedManageProducts.manageProducts(scanner);
                            } else {
                                Logger.log(Logger.WARNING, "Unauthorized access attempt to product management by user " + currentUser.getPersonID());
                                System.out.println("Access denied. Admin privileges required.");
                            }
                        }
                        case 2 -> {
                            Logger.logUserAction(currentUser.getPersonID(), "Access", "Accessed transactions menu");
                            OptimizedCompleteTransactions.TransactionMenu(scanner);
                        }
                        case 3 -> {
                            Logger.logUserAction(currentUser.getPersonID(), "Access", "Accessed customer history");
                            OptimizedCustomerHistory.customerHistoryMenu(scanner);
                        }
                        case 4 -> {
                            // logout and show login screen again
                            System.out.println("Logging out...");
                            Logger.logUserAction(currentUser.getPersonID(), "Logout", "User logged out");
                            AuthenticationService.logout();
                            loginSuccess = LoginScreen.showLoginScreen(scanner);
                            
                            if (!loginSuccess) {
                                System.out.println("Authentication required. Exiting the application.");
                                running = false;
                            } else {
                                // get the newly authenticated user
                                currentUser = AuthenticationService.getCurrentUser();
                                Logger.logUserAction(currentUser.getPersonID(), "Login", "User logged in successfully");
                                System.out.println("Welcome back, " + currentUser.getFirstName() + " " + currentUser.getLastName() + "!");
                            }
                        }
                        case 5 -> {
                            Logger.logUserAction(currentUser.getPersonID(), "Exit", "User exited the application");
                            System.out.println("Exiting the application. Goodbye!");
                            running = false;
                        }
                        default -> {
                            Logger.log(Logger.WARNING, "Invalid menu choice: " + choice);
                            System.out.println("Invalid choice. Please try again.");
                        }
                    }
                } catch (InputMismatchException e) {
                    // handle invalid input
                    String errorMessage = ErrorHandler.handleException(e, "processing menu choice");
                    System.out.println(errorMessage);
                    System.out.println("Please enter a number corresponding to the menu options.");
                    scanner.nextLine(); // consume invalid input
                } catch (Exception e) {
                    // handle unexpected errors
                    String errorMessage = ErrorHandler.handleException(e, "processing main menu");
                    System.out.println(errorMessage);
                }
            }
        } catch (Exception e) {
            // handle application startup errors
            String errorMessage = ErrorHandler.handleException(e, "starting the application");
            System.err.println(errorMessage);
            e.printStackTrace();
        } finally {
            // ensure scanner is closed
            scanner.close();
            Logger.log(Logger.INFO, "Application shutdown complete");
        }
    }
}
