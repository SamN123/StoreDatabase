package src.Authentication;

import java.util.Scanner;

public class LoginScreen {
    
    // displays the login screen and handles user authentication
    // @param scanner scanner for user input
    // @return true if login is successful, false otherwise
    public static boolean showLoginScreen(Scanner scanner) {
        boolean loginSuccess = false;
        boolean exitLogin = false;
        
        while (!loginSuccess && !exitLogin) {
            System.out.println("\n--- Login ---");
            System.out.println("1. Login");
            System.out.println("2. Register");
            System.out.println("3. Exit");
            System.out.print("Enter your choice: ");
            
            int choice = scanner.nextInt();
            scanner.nextLine(); // consume newline character after reading integer
            
            switch (choice) {
                case 1 -> loginSuccess = login(scanner); // attempt login
                case 2 -> register(scanner); // register new user
                case 3 -> exitLogin = true; // exit login screen
                default -> System.out.println("Invalid choice!");
            }
        }
        
        return loginSuccess;
    }
    
    // handles user login
    // @param scanner scanner for user input
    // @return true if login is successful, false otherwise
    private static boolean login(Scanner scanner) {
        System.out.print("Enter email: ");
        String email = scanner.nextLine();
        
        System.out.print("Enter password: ");
        String password = scanner.nextLine();
        
        // attempt to authenticate with provided credentials
        boolean success = AuthenticationService.authenticate(email, password);
        
        if (success) {
            System.out.println("Login successful!");
            return true;
        } else {
            System.out.println("Invalid email or password. Please try again.");
            return false;
        }
    }
    
    // handles user registration
    // @param scanner scanner for user input
    private static void register(Scanner scanner) {
        System.out.println("\n--- Register New User ---");
        
        System.out.print("Enter first name: ");
        String firstName = scanner.nextLine();
        
        System.out.print("Enter last name: ");
        String lastName = scanner.nextLine();
        
        System.out.print("Enter email: ");
        String email = scanner.nextLine();
        
        System.out.print("Enter phone (XXX-XXX-XXXX): ");
        String phone = scanner.nextLine();
        
        System.out.print("Enter password: ");
        String password = scanner.nextLine();
        
        // attempt to register with provided information
        boolean success = AuthenticationService.register(firstName, lastName, email, phone, password);
        
        if (success) {
            System.out.println("Registration successful! You can now login.");
        } else {
            System.out.println("Registration failed. Please try again.");
        }
    }
}
