package src.Logic;

import src.Authentication.AuthenticationService;
import src.Objects.Person;
import src.Security.SecurityUtil;
import src.Util.ErrorHandler;
import src.Util.Logger;
import src.Util.ValidationException;
import java.sql.*;
import java.util.InputMismatchException;
import java.util.Scanner;

public class OptimizedCompleteTransactions {
    // database connection details
    private static String DB_URL;
    private static String DB_USER;
    private static String DB_PASSWORD;
    private static final int DEFAULT_PAGE_SIZE = 10; // default number of items per page

    // sets the database connection information
    // @param url database url
    // @param user database username
    // @param password database password
    public static void setConnectionInfo(String url, String user, String password) {
        DB_URL = url;
        DB_USER = user;
        DB_PASSWORD = password;
    }

    // gets a database connection
    // @return a connection to the database
    // @throws SQLException if a database error occurs
    private static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

    // main method for transaction menu
    // @param scanner scanner for user input
    public static void TransactionMenu(Scanner scanner) {
        // verify user is authenticated before allowing access
        if (!SecurityUtil.hasUserPermission()) {
            System.out.println("Authentication required to access transactions.");
            return;
        }
        
        boolean inTransactionMenu = true;

        while (inTransactionMenu) {
            try {
                System.out.println("\n--- Complete Transactions ---");
                // show admin-only label for adding clients if user is not admin
                System.out.println("1. Add a New Client" + (SecurityUtil.hasAdminPermission() ? "" : " (Admin Only)"));
                System.out.println("2. Search for Products");
                System.out.println("3. Make a Purchase");
                System.out.println("4. View Customer Purchase History");
                System.out.println("5. View Customer Purchase Summary");
                System.out.println("6. Return to Main Menu");
                System.out.print("Enter your choice: ");

                int choice = scanner.nextInt();
                scanner.nextLine(); // consume newline

                switch (choice) {
                    case 1 -> {
                        // only admins can add new clients
                        if (SecurityUtil.hasAdminPermission()) {
                            addClient(scanner);
                        } else {
                            Logger.log(Logger.WARNING, "Unauthorized access attempt to add client");
                            System.out.println("Access denied. Admin privileges required.");
                        }
                    }
                    case 2 -> {
                        // search for products
                        searchProducts(scanner);
                    }
                    case 3 -> {
                        // make a purchase
                        makePurchaseWithProductSearch(scanner);
                    }
                    case 4 -> {
                        // view customer purchase history
                        System.out.print("Enter Customer ID: ");
                        int customerId = scanner.nextInt();
                        scanner.nextLine(); // consume newline
                        // use OptimizedCustomerHistory method
                        OptimizedCustomerHistory.viewCustomerPurchaseHistory(scanner, customerId);
                    }
                    case 5 -> {
                        // view customer purchase summary
                        System.out.print("Enter Customer ID: ");
                        int customerId = scanner.nextInt();
                        scanner.nextLine(); // consume newline
                        // use OptimizedCustomerHistory method
                        OptimizedCustomerHistory.viewCustomerPurchaseSummary(customerId);
                    }
                    case 6 -> inTransactionMenu = false; // return to main menu
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
                String errorMessage = ErrorHandler.handleException(e, "processing transaction menu");
                System.out.println(errorMessage);
            }
        }
    }

    // adds a new client to the persons table
    // @param scanner scanner for user input
    private static void addClient(Scanner scanner) {
        try {
            // get client details from user
            System.out.print("First Name: ");
            String fname = scanner.nextLine();
            System.out.print("Last Name: ");
            String lname = scanner.nextLine();
            System.out.print("Email: ");
            String email = scanner.nextLine();
            System.out.print("Phone (XXX-XXX-XXXX): ");
            String phone = scanner.nextLine();
            
            // validate input
            if (fname == null || fname.trim().isEmpty()) {
                throw new ValidationException("First name cannot be empty", "First Name");
            }
            if (lname == null || lname.trim().isEmpty()) {
                throw new ValidationException("Last name cannot be empty", "Last Name");
            }
            if (email == null || email.trim().isEmpty()) {
                throw new ValidationException("Email cannot be empty", "Email");
            }
            if (phone == null || phone.trim().isEmpty()) {
                throw new ValidationException("Phone cannot be empty", "Phone");
            }
            
            // validate email format
            if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                throw new ValidationException("Invalid email format", "Email");
            }
            
            // validate phone format (XXX-XXX-XXXX)
            if (!phone.matches("^[0-9]{3}-[0-9]{3}-[0-9]{4}$")) {
                throw new ValidationException("Invalid phone format (XXX-XXX-XXXX required)", "Phone");
            }
            
            try (Connection conn = getConnection()) {
                // check if email already exists
                String checkEmailQuery = "SELECT COUNT(*) FROM Persons WHERE Email = ?";
                try (PreparedStatement checkEmailStmt = conn.prepareStatement(checkEmailQuery)) {
                    checkEmailStmt.setString(1, email);
                    try (ResultSet rs = checkEmailStmt.executeQuery()) {
                        if (rs.next() && rs.getInt(1) > 0) {
                            Logger.log(Logger.WARNING, "Attempt to add client with existing email: " + email);
                            System.out.println("Error: Email already exists.");
                            return;
                        }
                    }
                }
                
                // insert query for adding a new client
                String sql = "INSERT INTO Persons (FName, LName, Email, Phone) VALUES (?, ?, ?, ?)";
                try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                    // set parameters for the insert query
                    stmt.setString(1, fname);
                    stmt.setString(2, lname);
                    stmt.setString(3, email);
                    stmt.setString(4, phone);
                    int rowsAffected = stmt.executeUpdate();

                    if (rowsAffected > 0) {
                        // get the generated person ID
                        try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                            if (generatedKeys.next()) {
                                int personId = generatedKeys.getInt(1);
                                
                                // log the client addition
                                Logger.log(Logger.INFO, "New client added: " + fname + " " + lname + " (ID: " + personId + ")");
                                
                                System.out.println("Client added successfully.");
                                System.out.println("Assigned Person ID: " + personId);
                                System.out.println("Please use this ID when making a purchase.");
                            }
                        }
                    }
                }
            }
        } catch (ValidationException e) {
            String errorMessage = ErrorHandler.handleValidationException(e, e.getField());
            System.err.println(errorMessage);
        } catch (SQLIntegrityConstraintViolationException e) {
            // handle duplicate email error
            Logger.log(Logger.WARNING, "Attempt to add client with existing email");
            System.out.println("Error: Email already exists.");
        } catch (SQLException e) {
            String errorMessage = ErrorHandler.handleSQLException(e, "adding client");
            System.err.println(errorMessage);
        } catch (Exception e) {
            String errorMessage = ErrorHandler.handleException(e, "adding client");
            System.err.println(errorMessage);
        }
    }
    
    // search for products with multiple criteria
    // @param scanner scanner for user input
    private static void searchProducts(Scanner scanner) {
        try {
            // get search criteria
            System.out.println("\n--- Search Products ---");
            
            System.out.print("Enter product name (or press Enter to skip): ");
            String nameSearch = scanner.nextLine().trim();
            if (nameSearch.isEmpty()) {
                nameSearch = null;
            }
            
            Float minPrice = null;
            Float maxPrice = null;
            Boolean inStockOnly = false;
            
            System.out.print("Enter minimum price (or press Enter to skip): ");
            String minPriceStr = scanner.nextLine().trim();
            if (!minPriceStr.isEmpty()) {
                try {
                    minPrice = Float.parseFloat(minPriceStr);
                    if (minPrice < 0) {
                        throw new ValidationException("Minimum price cannot be negative", "Minimum Price");
                    }
                } catch (NumberFormatException e) {
                    throw new ValidationException("Invalid minimum price format", "Minimum Price");
                }
            }
            
            System.out.print("Enter maximum price (or press Enter to skip): ");
            String maxPriceStr = scanner.nextLine().trim();
            if (!maxPriceStr.isEmpty()) {
                try {
                    maxPrice = Float.parseFloat(maxPriceStr);
                    if (maxPrice < 0) {
                        throw new ValidationException("Maximum price cannot be negative", "Maximum Price");
                    }
                    
                    if (minPrice != null && maxPrice < minPrice) {
                        throw new ValidationException("Maximum price cannot be less than minimum price", "Maximum Price");
                    }
                } catch (NumberFormatException e) {
                    throw new ValidationException("Invalid maximum price format", "Maximum Price");
                }
            }
            
            System.out.print("Show only in-stock items? (y/n): ");
            String inStockStr = scanner.nextLine().trim().toLowerCase();
            inStockOnly = inStockStr.startsWith("y");
            
            // log search criteria
            Logger.log(Logger.INFO, "Searching products with criteria - Name: " + 
                      (nameSearch != null ? nameSearch : "any") + 
                      ", Min Price: " + (minPrice != null ? minPrice : "any") + 
                      ", Max Price: " + (maxPrice != null ? maxPrice : "any") + 
                      ", In Stock Only: " + inStockOnly);
            
            // execute search
            try (Connection connection = getConnection();
                 CallableStatement stmt = connection.prepareCall("{CALL SearchProducts(?, ?, ?, ?)}")) {
                
                // set parameters for the stored procedure
                stmt.setString(1, nameSearch);
                
                if (minPrice != null) {
                    stmt.setFloat(2, minPrice);
                } else {
                    stmt.setNull(2, Types.FLOAT);
                }
                
                if (maxPrice != null) {
                    stmt.setFloat(3, maxPrice);
                } else {
                    stmt.setNull(3, Types.FLOAT);
                }
                
                stmt.setBoolean(4, inStockOnly);
                
                // execute and display results
                try (ResultSet rs = stmt.executeQuery()) {
                    System.out.println("\n--- Search Results ---");
                    System.out.printf("%-10s %-30s %-10s %-10s%n", 
                            "ID", "Name", "Price", "Quantity");
                    System.out.println("------------------------------------------------------");
                    
                    boolean hasResults = false;
                    int count = 0;
                    
                    while (rs.next()) {
                        hasResults = true;
                        count++;
                        
                        // format and display each product
                        System.out.printf("%-10s %-30s $%-9.2f %-10d%n",
                                rs.getString("ProductID"),
                                rs.getString("ItemName"),
                                rs.getDouble("ItemPrice"),
                                rs.getInt("ItemQuantity"));
                    }
                    
                    if (!hasResults) {
                        System.out.println("No products found matching your criteria.");
                    } else {
                        System.out.println("------------------------------------------------------");
                        System.out.println("Found " + count + " products matching your criteria.");
                    }
                }
            }
        } catch (ValidationException e) {
            String errorMessage = ErrorHandler.handleValidationException(e, e.getField());
            System.err.println(errorMessage);
        } catch (SQLException e) {
            String errorMessage = ErrorHandler.handleSQLException(e, "searching products");
            System.err.println(errorMessage);
        } catch (Exception e) {
            String errorMessage = ErrorHandler.handleException(e, "searching products");
            System.err.println(errorMessage);
        }
    }

    // make a purchase with product search functionality
    // @param scanner scanner for user input
    private static void makePurchaseWithProductSearch(Scanner scanner) {
        try {
            // get customer ID
            System.out.print("Enter Customer ID: ");
            int customerId = scanner.nextInt();
            scanner.nextLine(); // consume newline
            
            // validate customer ID
            if (customerId <= 0) {
                throw new ValidationException("Invalid customer ID", "Customer ID");
            }
            
            // check if customer exists
            if (!OptimizedCustomerHistory.customerExists(customerId)) {
                Logger.log(Logger.WARNING, "Attempt to make purchase with non-existent customer ID: " + customerId);
                System.out.println("Error: Customer ID does not exist!");
                return;
            }
            
            // search for product
            System.out.println("\nSearch for product to purchase:");
            System.out.print("Enter product name or ID (or press Enter to see all products): ");
            String searchTerm = scanner.nextLine().trim();
            
            String productId = null;
            
            if (searchTerm.isEmpty()) {
                // show all products
                try (Connection conn = getConnection();
                     PreparedStatement stmt = conn.prepareStatement(
                             "SELECT ProductID, ItemName, ItemPrice, ItemQuantity FROM Products WHERE ItemQuantity > 0")) {
                    
                    try (ResultSet rs = stmt.executeQuery()) {
                        System.out.println("\n--- Available Products ---");
                        System.out.printf("%-10s %-30s %-10s %-10s%n", 
                                "ID", "Name", "Price", "Quantity");
                        System.out.println("------------------------------------------------------");
                        
                        boolean hasProducts = false;
                        
                        while (rs.next()) {
                            hasProducts = true;
                            
                            // format and display each product
                            System.out.printf("%-10s %-30s $%-9.2f %-10d%n",
                                    rs.getString("ProductID"),
                                    rs.getString("ItemName"),
                                    rs.getDouble("ItemPrice"),
                                    rs.getInt("ItemQuantity"));
                        }
                        
                        if (!hasProducts) {
                            System.out.println("No products available.");
                            return;
                        }
                    }
                }
                
                // ask for product ID
                System.out.print("\nEnter Product ID to purchase: ");
                productId = scanner.nextLine();
            } else {
                // check if search term is a product ID
                try (Connection conn = getConnection();
                     PreparedStatement stmt = conn.prepareStatement(
                             "SELECT ProductID, ItemName, ItemPrice, ItemQuantity FROM Products WHERE ProductID = ?")) {
                    
                    stmt.setString(1, searchTerm);
                    
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            // found product by ID
                            productId = searchTerm;
                            
                            // display product details
                            System.out.println("\n--- Product Found ---");
                            System.out.printf("%-10s %-30s %-10s %-10s%n", 
                                    "ID", "Name", "Price", "Quantity");
                            System.out.println("------------------------------------------------------");
                            System.out.printf("%-10s %-30s $%-9.2f %-10d%n",
                                    rs.getString("ProductID"),
                                    rs.getString("ItemName"),
                                    rs.getDouble("ItemPrice"),
                                    rs.getInt("ItemQuantity"));
                        } else {
                            // search by name
                            try (PreparedStatement nameStmt = conn.prepareStatement(
                                    "SELECT ProductID, ItemName, ItemPrice, ItemQuantity FROM Products " +
                                    "WHERE ItemName LIKE ? AND ItemQuantity > 0")) {
                                
                                nameStmt.setString(1, "%" + searchTerm + "%");
                                
                                try (ResultSet nameRs = nameStmt.executeQuery()) {
                                    System.out.println("\n--- Search Results ---");
                                    System.out.printf("%-10s %-30s %-10s %-10s%n", 
                                            "ID", "Name", "Price", "Quantity");
                                    System.out.println("------------------------------------------------------");
                                    
                                    boolean hasResults = false;
                                    
                                    while (nameRs.next()) {
                                        hasResults = true;
                                        
                                        // format and display each product
                                        System.out.printf("%-10s %-30s $%-9.2f %-10d%n",
                                                nameRs.getString("ProductID"),
                                                nameRs.getString("ItemName"),
                                                nameRs.getDouble("ItemPrice"),
                                                nameRs.getInt("ItemQuantity"));
                                    }
                                    
                                    if (!hasResults) {
                                        System.out.println("No products found matching your search term.");
                                        return;
                                    }
                                }
                                
                                // ask for product ID
                                System.out.print("\nEnter Product ID to purchase: ");
                                productId = scanner.nextLine();
                            }
                        }
                    }
                }
            }
            
            // validate product ID
            if (productId == null || productId.trim().isEmpty()) {
                throw new ValidationException("Product ID cannot be empty", "Product ID");
            }
            
            // get quantity
            System.out.print("Enter Quantity: ");
            int quantity = scanner.nextInt();
            scanner.nextLine(); // consume newline
            
            // validate quantity
            if (quantity <= 0) {
                throw new ValidationException("Quantity must be greater than zero", "Quantity");
            }
            
            // make the purchase
            makePurchase(customerId, productId, quantity);
            
        } catch (ValidationException e) {
            String errorMessage = ErrorHandler.handleValidationException(e, e.getField());
            System.err.println(errorMessage);
        } catch (InputMismatchException e) {
            String errorMessage = ErrorHandler.handleException(e, "reading purchase information");
            System.err.println(errorMessage);
            System.out.println("Please enter valid numeric values.");
            scanner.nextLine(); // consume invalid input
        } catch (SQLException e) {
            String errorMessage = ErrorHandler.handleSQLException(e, "processing purchase");
            System.err.println(errorMessage);
        } catch (Exception e) {
            String errorMessage = ErrorHandler.handleException(e, "processing purchase");
            System.err.println(errorMessage);
        }
    }

    // handles purchase transaction
    // @param customerId customer ID
    // @param productId product ID
    // @param quantity quantity to purchase
    public static void makePurchase(int customerId, String productId, int quantity) {
        try {
            // validate input
            if (customerId <= 0) {
                throw new ValidationException("Invalid customer ID", "Customer ID");
            }
            if (productId == null || productId.trim().isEmpty()) {
                throw new ValidationException("Product ID cannot be empty", "Product ID");
            }
            if (quantity <= 0) {
                throw new ValidationException("Quantity must be greater than zero", "Quantity");
            }
            
            try (Connection conn = getConnection()) {
                // check if customer exists
                if (!OptimizedCustomerHistory.customerExists(customerId)) {
                    Logger.log(Logger.WARNING, "Attempt to make purchase with non-existent customer ID: " + customerId);
                    System.out.println("Error: Customer ID does not exist!");
                    return;
                }
                
                // check if product exists
                String checkProductQuery = "SELECT * FROM Products WHERE ProductID = ?";
                try (PreparedStatement checkProductStmt = conn.prepareStatement(checkProductQuery)) {
                    checkProductStmt.setString(1, productId);
                    try (ResultSet productRs = checkProductStmt.executeQuery()) {
                        if (!productRs.next()) {
                            Logger.log(Logger.WARNING, "Attempt to purchase non-existent product: " + productId);
                            System.out.println("Error: Product ID does not exist!");
                            return;
                        }
                        
                        // get product name and price for logging
                        String productName = productRs.getString("ItemName");
                        double productPrice = productRs.getDouble("ItemPrice");
                        
                        // check if there is enough stock available
                        int availableQuantity = productRs.getInt("ItemQuantity");
                        if (quantity > availableQuantity) {
                            Logger.log(Logger.WARNING, "Insufficient stock for product: " + productId + 
                                      ", requested: " + quantity + ", available: " + availableQuantity);
                            System.out.println("Not enough inventory available. Only " + availableQuantity + " in stock.");
                            return;
                        }
                        
                        // call the stored procedure to make the purchase
                        String call = "{CALL MakePurchase(?, ?, ?)}";
                        try (CallableStatement stmt = conn.prepareCall(call)) {
                            // set parameters for the stored procedure
                            stmt.setInt(1, customerId);
                            stmt.setString(2, productId);
                            stmt.setInt(3, quantity);
                            stmt.execute();
                            
                            // log the purchase
                            double totalPrice = productPrice * quantity;
                            Logger.log(Logger.INFO, "Purchase completed: Customer ID " + customerId + 
                                      " purchased " + quantity + " of " + productName + 
                                      " (ID: " + productId + ") for $" + totalPrice);
                            
                            // get current user for user action logging
                            Person currentUser = AuthenticationService.getCurrentUser();
                            if (currentUser != null) {
                                Logger.logUserAction(currentUser.getPersonID(), "Purchase", 
                                                   "Processed purchase of " + quantity + " " + productName + 
                                                   " for customer " + customerId);
                            }
                            
                            System.out.println("Purchase completed successfully.");
                            System.out.println("Total price: $" + String.format("%.2f", totalPrice));
                        }
                    }
                }
            } catch (SQLException e) {
                throw e; // rethrow to be caught by the outer catch block
            }
        } catch (ValidationException e) {
            String errorMessage = ErrorHandler.handleValidationException(e, e.getField());
            System.err.println(errorMessage);
        } catch (SQLException e) {
            String errorMessage = ErrorHandler.handleSQLException(e, "processing purchase");
            System.err.println(errorMessage);
        } catch (Exception e) {
            String errorMessage = ErrorHandler.handleException(e, "processing purchase");
            System.err.println(errorMessage);
        }
    }
    
}
