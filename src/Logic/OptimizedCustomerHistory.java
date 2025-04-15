package src.Logic;
    
import src.Objects.*;
import src.Security.SecurityUtil;
import src.Util.ErrorHandler;
import src.Util.Logger;
import src.Util.ValidationException;

import java.util.Scanner;
import java.sql.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.InputMismatchException;

public class OptimizedCustomerHistory {
    private static String dbUrl; // database url
    private static String dbUser; // database username
    private static String dbPassword; // database password
    private static final int DEFAULT_PAGE_SIZE = 10; // default number of items per page
    
    // sets the database connection information
    // @param url database url
    // @param user database username
    // @param password database password
    public static void setConnectionInfo(String url, String user, String password) {
        dbUrl = url;
        dbUser = user;
        dbPassword = password;
    }

    // gets a database connection
    // @return a connection to the database
    // @throws SQLException if a database error occurs
    private static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(dbUrl, dbUser, dbPassword);
    }
    
    // main method for customer history menu
    // @param scanner scanner for user input
    public static void customerHistoryMenu(Scanner scanner) {
        try {
            // verify user is authenticated before allowing access
            if (!SecurityUtil.hasUserPermission()) {
                Logger.log(Logger.WARNING, "Unauthorized access attempt to customer history");
                System.out.println("Authentication required to access customer history.");
                return;
            }
            
            // get current user for logging
            Person currentUser = src.Authentication.AuthenticationService.getCurrentUser();
            if (currentUser != null) {
                Logger.logUserAction(currentUser.getPersonID(), "Access", "Accessed customer history menu");
            }
            
            boolean managing = true;

            while (managing) {
                try {
                    System.out.println("\n--- Customer History ---");
                    System.out.println("1. Search Customer by Email");
                    System.out.println("2. View Customer Purchase History");
                    System.out.println("3. View Customer Purchase Summary");
                    // show admin-only label for viewing all purchases if user is not admin
                    System.out.println("4. View Past Purchases" + (SecurityUtil.hasAdminPermission() ? "" : " (Admin Only)"));
                    System.out.println("5. Return to Main Menu");
                    System.out.print("Enter your choice: ");
                    
                    int choice = scanner.nextInt();
                    scanner.nextLine(); // consume newline character

                    switch (choice) {
                        case 1 -> {
                            // search for a customer by email
                            System.out.print("Enter customer email to search: ");
                            String email = scanner.nextLine();
                            searchCustomer(email);
                        }
                        case 2 -> {
                            // view customer purchase history with pagination
                            System.out.print("Enter customer ID: ");
                            int customerId = scanner.nextInt();
                            scanner.nextLine(); // consume newline
                            viewCustomerPurchaseHistory(scanner, customerId);
                        }
                        case 3 -> {
                            // view customer purchase summary
                            System.out.print("Enter customer ID: ");
                            int customerId = scanner.nextInt();
                            scanner.nextLine(); // consume newline
                            viewCustomerPurchaseSummary(customerId);
                        }
                        case 4 -> {
                            // only admins can view all past purchases
                            if (SecurityUtil.hasAdminPermission()) {
                                viewPastPurchasesPaginated(scanner);
                            } else {
                                Logger.log(Logger.WARNING, "Unauthorized attempt to view all purchases by user ID: " + 
                                          (currentUser != null ? currentUser.getPersonID() : "unknown"));
                                System.out.println("Access denied. Admin privileges required.");
                            }
                        }
                        case 5 -> {
                            Logger.log(Logger.INFO, "Exiting customer history menu");
                            managing = false; // return to main menu
                        }
                        default -> {
                            Logger.log(Logger.WARNING, "Invalid menu choice: " + choice);
                            System.out.println("Invalid choice!");
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
                    String errorMessage = ErrorHandler.handleException(e, "processing customer history menu");
                    System.out.println(errorMessage);
                }
            }
        } catch (Exception e) {
            // handle unexpected errors
            String errorMessage = ErrorHandler.handleException(e, "accessing customer history");
            System.err.println(errorMessage);
        }
    }
    
    // searches for a customer by email
    // @param email email to search for
    private static void searchCustomer(String email) {
        try {
            // validate email
            if (email == null || email.trim().isEmpty()) {
                throw new ValidationException("Email cannot be empty", "Email");
            }
            
            // validate email format
            if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                throw new ValidationException("Invalid email format", "Email");
            }
            
            Logger.log(Logger.INFO, "Searching for customer with email: " + email);
            
            // optimized query to find customer by email using index
            String sql = "SELECT PersonID, FName, LName, Email, Phone FROM Persons WHERE Email = ?";

            try (Connection conn = getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {

                // set email parameter
                pstmt.setString(1, email);

                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        // create person object from result set
                        Person person = new Person(
                                rs.getInt("PersonID"),
                                rs.getString("FName"),
                                rs.getString("LName"),
                                rs.getString("Phone"),
                                rs.getString("Email")
                        );
                        
                        Logger.log(Logger.INFO, "Customer found: " + person.getPersonID() + " - " + 
                                  person.getFirstName() + " " + person.getLastName());
                        
                        System.out.println("Customer Found: " + person);
                        
                        // show purchase summary
                        viewCustomerPurchaseSummary(person.getPersonID());
                    } else {
                        Logger.log(Logger.INFO, "No customer found with email: " + email);
                        System.out.println("No customer was found with the email: " + email);
                    }
                }
            }
        } catch (ValidationException e) {
            String errorMessage = ErrorHandler.handleValidationException(e, e.getField());
            System.err.println(errorMessage);
        } catch (SQLException e) {
            String errorMessage = ErrorHandler.handleSQLException(e, "searching for customer");
            System.err.println(errorMessage);
        } catch (Exception e) {
            String errorMessage = ErrorHandler.handleException(e, "searching for customer");
            System.err.println(errorMessage);
        }
    }
    
    // view customer purchase history with pagination
    // @param scanner scanner for user input
    // @param customerId customer ID
    public static void viewCustomerPurchaseHistory(Scanner scanner, int customerId) {
        try {
            // validate customer ID
            if (customerId <= 0) {
                throw new ValidationException("Invalid customer ID", "Customer ID");
            }
            
            // check if customer exists
            if (!customerExists(customerId)) {
                Logger.log(Logger.WARNING, "Attempt to view history for non-existent customer ID: " + customerId);
                System.out.println("Error: Customer ID does not exist!");
                return;
            }
            
            // get current user for permission check
            Person currentUser = src.Authentication.AuthenticationService.getCurrentUser();
            
            // check if user has permission to view this customer's history
            // admins can view any customer's history, regular users can only view their own
            if (!SecurityUtil.hasAdminPermission() && 
                (currentUser == null || currentUser.getPersonID() != customerId)) {
                Logger.log(Logger.WARNING, "Unauthorized attempt to view customer history for ID: " + 
                          customerId + " by user ID: " + 
                          (currentUser != null ? currentUser.getPersonID() : "unknown"));
                System.out.println("Access denied. You can only view your own purchase history.");
                return;
            }
            
            int page = 1;
            int pageSize = DEFAULT_PAGE_SIZE;
            boolean viewing = true;
            
            while (viewing) {
                try (Connection conn = getConnection();
                     CallableStatement stmt = conn.prepareCall("{CALL GetCustomerPurchaseHistory(?, ?, ?)}")) {
                    
                    // set parameters for the stored procedure
                    stmt.setInt(1, customerId);
                    stmt.setInt(2, page);
                    stmt.setInt(3, pageSize);
                    
                    Logger.log(Logger.INFO, "Viewing purchase history for customer ID: " + 
                              customerId + " (page " + page + ")");
                    
                    boolean hasResults = stmt.execute();
                    int totalPurchases = 0;
                    
                    if (hasResults) {
                        // display purchase history
                        try (ResultSet rs = stmt.getResultSet()) {
                            System.out.println("\n--- Purchase History (Page " + page + ") ---");
                            System.out.printf("%-5s %-20s %-15s %-25s %-10s %-10s %-10s%n", 
                                    "ID", "Date", "Product ID", "Product Name", "Quantity", "Price", "Total");
                            System.out.println("-----------------------------------------------------------------------------------------");
                            
                            boolean hasPurchases = false;
                            int count = 0;
                            
                            while (rs.next()) {
                                hasPurchases = true;
                                count++;
                                
                                // format and display each purchase
                                System.out.printf("%-5d %-20s %-15s %-25s %-10d $%-9.2f $%-9.2f%n",
                                        rs.getInt("TransactionID"),
                                        rs.getTimestamp("Date").toString(),
                                        rs.getString("ProductID"),
                                        rs.getString("ItemName"),
                                        rs.getInt("QuantityPurchased"),
                                        rs.getDouble("ItemPrice"),
                                        rs.getDouble("TotalPrice"));
                            }
                            
                            if (!hasPurchases) {
                                System.out.println("No purchase history found for this customer on this page.");
                            } else {
                                System.out.println("-----------------------------------------------------------------------------------------");
                                System.out.println("Showing " + count + " purchases");
                            }
                        }
                        
                        // get total count for pagination
                        if (stmt.getMoreResults()) {
                            try (ResultSet countRs = stmt.getResultSet()) {
                                if (countRs.next()) {
                                    totalPurchases = countRs.getInt("TotalPurchases");
                                    int totalPages = (int) Math.ceil((double) totalPurchases / pageSize);
                                    System.out.println("Page " + page + " of " + totalPages + 
                                                     " (Total purchases: " + totalPurchases + ")");
                                }
                            }
                        }
                    }
                    
                    // pagination menu
                    if (totalPurchases > 0) {
                        System.out.println("\n--- Navigation ---");
                        System.out.println("1. Next Page");
                        System.out.println("2. Previous Page");
                        System.out.println("3. Return to Customer History Menu");
                        System.out.print("Enter your choice: ");
                        
                        int navChoice = scanner.nextInt();
                        scanner.nextLine(); // consume newline
                        
                        switch (navChoice) {
                            case 1 -> {
                                // calculate total pages
                                int totalPages = (int) Math.ceil((double) totalPurchases / pageSize);
                                if (page < totalPages) {
                                    page++;
                                } else {
                                    System.out.println("Already on the last page.");
                                }
                            }
                            case 2 -> {
                                if (page > 1) {
                                    page--;
                                } else {
                                    System.out.println("Already on the first page.");
                                }
                            }
                            case 3 -> viewing = false; // return to customer history menu
                            default -> System.out.println("Invalid choice!");
                        }
                    } else {
                        System.out.println("\nPress Enter to continue...");
                        scanner.nextLine();
                        viewing = false;
                    }
                }
            }
        } catch (ValidationException e) {
            String errorMessage = ErrorHandler.handleValidationException(e, e.getField());
            System.err.println(errorMessage);
        } catch (InputMismatchException e) {
            String errorMessage = ErrorHandler.handleException(e, "reading pagination choice");
            System.err.println(errorMessage);
            System.out.println("Please enter a number corresponding to the menu options.");
            scanner.nextLine(); // consume invalid input
        } catch (SQLException e) {
            String errorMessage = ErrorHandler.handleSQLException(e, "retrieving customer purchase history");
            System.err.println(errorMessage);
        } catch (Exception e) {
            String errorMessage = ErrorHandler.handleException(e, "viewing customer purchase history");
            System.err.println(errorMessage);
        }
    }
    
    // view customer purchase summary
    // @param customerId customer ID
    public static void viewCustomerPurchaseSummary(int customerId) {
        try {
            // validate customer ID
            if (customerId <= 0) {
                throw new ValidationException("Invalid customer ID", "Customer ID");
            }
            
            // check if customer exists
            if (!customerExists(customerId)) {
                Logger.log(Logger.WARNING, "Attempt to view summary for non-existent customer ID: " + customerId);
                System.out.println("Error: Customer ID does not exist!");
                return;
            }
            
            // get current user for permission check
            Person currentUser = src.Authentication.AuthenticationService.getCurrentUser();
            
            // check if user has permission to view this customer's summary
            // admins can view any customer's summary, regular users can only view their own
            if (!SecurityUtil.hasAdminPermission() && 
                (currentUser == null || currentUser.getPersonID() != customerId)) {
                Logger.log(Logger.WARNING, "Unauthorized attempt to view customer summary for ID: " + 
                          customerId + " by user ID: " + 
                          (currentUser != null ? currentUser.getPersonID() : "unknown"));
                System.out.println("Access denied. You can only view your own purchase summary.");
                return;
            }
            
            Logger.log(Logger.INFO, "Viewing purchase summary for customer ID: " + customerId);
            
            // query to get customer purchase summary
            String query = "SELECT * FROM CustomerPurchaseSummary WHERE PersonID = ?";
            
            try (Connection conn = getConnection();
                 PreparedStatement stmt = conn.prepareStatement(query)) {
                
                stmt.setInt(1, customerId);
                
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        // display customer information
                        System.out.println("\n--- Customer Purchase Summary ---");
                        System.out.println("Customer ID: " + rs.getInt("PersonID"));
                        System.out.println("Name: " + rs.getString("FName") + " " + rs.getString("LName"));
                        System.out.println("Email: " + rs.getString("Email"));
                        
                        // display purchase summary
                        int totalTransactions = rs.getInt("TotalTransactions");
                        
                        if (totalTransactions > 0) {
                            System.out.println("\nPurchase Statistics:");
                            System.out.println("Total Transactions: " + totalTransactions);
                            System.out.println("Total Items Purchased: " + rs.getInt("TotalItemsPurchased"));
                            System.out.println("Total Amount Spent: $" + String.format("%.2f", rs.getDouble("TotalSpent")));
                            System.out.println("Last Purchase Date: " + rs.getTimestamp("LastPurchaseDate"));
                        } else {
                            System.out.println("\nNo purchase history found for this customer.");
                        }
                    } else {
                        System.out.println("No customer found with ID: " + customerId);
                    }
                }
            }
        } catch (ValidationException e) {
            String errorMessage = ErrorHandler.handleValidationException(e, e.getField());
            System.err.println(errorMessage);
        } catch (SQLException e) {
            String errorMessage = ErrorHandler.handleSQLException(e, "retrieving customer purchase summary");
            System.err.println(errorMessage);
        } catch (Exception e) {
            String errorMessage = ErrorHandler.handleException(e, "viewing customer purchase summary");
            System.err.println(errorMessage);
        }
    }
    
    // check if customer exists
    // @param customerId customer ID
    // @return true if customer exists, false otherwise
    // @throws SQLException if a database error occurs
    public static boolean customerExists(int customerId) throws SQLException {
        String query = "SELECT COUNT(*) FROM Persons WHERE PersonID = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, customerId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }
    
    // displays all transactions from the database with pagination
    // @param scanner scanner for user input
    private static void viewPastPurchasesPaginated(Scanner scanner) {
        try {
            Logger.log(Logger.INFO, "Viewing all past purchases with pagination");
            
            int page = 1;
            int pageSize = DEFAULT_PAGE_SIZE;
            boolean viewing = true;
            
            while (viewing) {
                // calculate offset for pagination
                int offset = (page - 1) * pageSize;
                
                // optimized query to get paginated transactions with joins
                String countQuery = "SELECT COUNT(*) FROM Purchase";
                String dataQuery = "SELECT pu.TransactionID, pu.Date, pu.QuantityPurchased, " +
                        "pr.ProductID, pr.ItemName, pr.ItemPrice, " +
                        "p.PersonID, p.FName, p.LName, " +
                        "(pu.QuantityPurchased * pr.ItemPrice) AS TotalPrice " +
                        "FROM Purchase pu " +
                        "JOIN Products pr ON pu.ProductID = pr.ProductID " +
                        "JOIN Persons p ON pu.PersonID = p.PersonID " +
                        "ORDER BY pu.Date DESC " +
                        "LIMIT ? OFFSET ?";
                
                try (Connection conn = getConnection()) {
                    // get total count for pagination
                    int totalPurchases = 0;
                    try (PreparedStatement countStmt = conn.prepareStatement(countQuery);
                         ResultSet countRs = countStmt.executeQuery()) {
                        if (countRs.next()) {
                            totalPurchases = countRs.getInt(1);
                        }
                    }
                    
                    // get paginated data
                    try (PreparedStatement dataStmt = conn.prepareStatement(dataQuery)) {
                        dataStmt.setInt(1, pageSize);
                        dataStmt.setInt(2, offset);
                        
                        try (ResultSet rs = dataStmt.executeQuery()) {
                            System.out.println("\n--- All Purchases (Page " + page + ") ---");
                            System.out.printf("%-5s %-20s %-15s %-20s %-10s %-15s %-10s%n", 
                                    "ID", "Date", "Customer", "Product", "Quantity", "Price", "Total");
                            System.out.println("-----------------------------------------------------------------------------------------");
                            
                            boolean hasPurchases = false;
                            int count = 0;
                            
                            while (rs.next()) {
                                hasPurchases = true;
                                count++;
                                
                                // format and display each purchase
                                System.out.printf("%-5d %-20s %-15s %-20s %-10d $%-9.2f $%-9.2f%n",
                                        rs.getInt("TransactionID"),
                                        rs.getTimestamp("Date").toString(),
                                        rs.getString("FName") + " " + rs.getString("LName"),
                                        rs.getString("ItemName"),
                                        rs.getInt("QuantityPurchased"),
                                        rs.getDouble("ItemPrice"),
                                        rs.getDouble("TotalPrice"));
                            }
                            
                            if (!hasPurchases) {
                                System.out.println("No purchases found on this page.");
                            } else {
                                System.out.println("-----------------------------------------------------------------------------------------");
                                System.out.println("Showing " + count + " purchases");
                            }
                            
                            // display pagination information
                            int totalPages = (int) Math.ceil((double) totalPurchases / pageSize);
                            System.out.println("Page " + page + " of " + totalPages + 
                                             " (Total purchases: " + totalPurchases + ")");
                            
                            // pagination menu
                            if (totalPurchases > 0) {
                                System.out.println("\n--- Navigation ---");
                                System.out.println("1. Next Page");
                                System.out.println("2. Previous Page");
                                System.out.println("3. Return to Customer History Menu");
                                System.out.print("Enter your choice: ");
                                
                                int navChoice = scanner.nextInt();
                                scanner.nextLine(); // consume newline
                                
                                switch (navChoice) {
                                    case 1 -> {
                                        if (page < totalPages) {
                                            page++;
                                        } else {
                                            System.out.println("Already on the last page.");
                                        }
                                    }
                                    case 2 -> {
                                        if (page > 1) {
                                            page--;
                                        } else {
                                            System.out.println("Already on the first page.");
                                        }
                                    }
                                    case 3 -> viewing = false; // return to customer history menu
                                    default -> System.out.println("Invalid choice!");
                                }
                            } else {
                                System.out.println("\nPress Enter to continue...");
                                scanner.nextLine();
                                viewing = false;
                            }
                        }
                    }
                }
            }
        } catch (InputMismatchException e) {
            String errorMessage = ErrorHandler.handleException(e, "reading pagination choice");
            System.err.println(errorMessage);
            System.out.println("Please enter a number corresponding to the menu options.");
            scanner.nextLine(); // consume invalid input
        } catch (SQLException e) {
            String errorMessage = ErrorHandler.handleSQLException(e, "retrieving all purchases");
            System.err.println(errorMessage);
        } catch (Exception e) {
            String errorMessage = ErrorHandler.handleException(e, "viewing all purchases");
            System.err.println(errorMessage);
        }
    }
}
