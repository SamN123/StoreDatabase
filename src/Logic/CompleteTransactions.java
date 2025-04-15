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

public class CompleteTransactions {

    // database connection details
    private static String DB_URL;
    private static String DB_USER;
    private static String DB_PASSWORD;

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
            System.out.println("\n--- Complete Transactions ---");
            // show admin-only label for adding clients if user is not admin
            System.out.println("1. Add a New Client" + (SecurityUtil.hasAdminPermission() ? "" : " (Admin Only)"));
            System.out.println("2. Make a Purchase");
            System.out.println("3. View Customer Purchase History");
            System.out.println("4. Return to Main Menu");
            System.out.print("Enter your choice: ");

            int choice = scanner.nextInt();

            switch (choice) {
                case 1 -> {
                    // only admins can add new clients
                    if (SecurityUtil.hasAdminPermission()) {
                        scanner.nextLine(); // consume newline character
                        System.out.print("First Name: ");
                        String fname = scanner.nextLine();
                        System.out.print("Last Name: ");
                        String lname = scanner.nextLine();
                        System.out.print("Email: ");
                        String email = scanner.nextLine();
                        System.out.print("Phone (XXX-XXX-XXXX): ");
                        String phone = scanner.nextLine();
                        addClient(fname, lname, email, phone);
                    } else {
                        System.out.println("Access denied. Admin privileges required.");
                    }
                }
                case 2 -> {
                    // make a purchase
                    System.out.print("Enter Customer ID: ");
                    int customerId = scanner.nextInt();
                    System.out.print("Enter Product ID: ");
                    String productId = scanner.next();
                    System.out.print("Enter Quantity: ");
                    int quantity = scanner.nextInt();
                    makePurchase(customerId, productId, quantity);
                }
                case 3 -> {
                    // view customer purchase history
                    System.out.print("Enter Customer ID: ");
                    int customerId = scanner.nextInt();
                    viewCustomerHistory(customerId);
                }
                case 4 -> inTransactionMenu = false; // return to main menu
                default -> System.out.println("Invalid choice!");
            }
        }
    }

    // adds a new client to the persons table
    // @param fname first name
    // @param lname last name
    // @param email email
    // @param phone phone number
    public static void addClient(String fname, String lname, String email, String phone) {
        try {
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
            Logger.log(Logger.WARNING, "Attempt to add client with existing email: " + email);
            System.out.println("Error: Email already exists.");
        } catch (SQLException e) {
            String errorMessage = ErrorHandler.handleSQLException(e, "adding client");
            System.err.println(errorMessage);
        } catch (Exception e) {
            String errorMessage = ErrorHandler.handleException(e, "adding client");
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
                String checkCustomerQuery = "SELECT * FROM Persons WHERE PersonID = ?";
                try (PreparedStatement checkCustomerStmt = conn.prepareStatement(checkCustomerQuery)) {
                    checkCustomerStmt.setInt(1, customerId);
                    try (ResultSet customerRs = checkCustomerStmt.executeQuery()) {
                        if (!customerRs.next()) {
                            Logger.log(Logger.WARNING, "Attempt to make purchase with non-existent customer ID: " + customerId);
                            System.out.println("Error: Customer ID does not exist!");
                            return;
                        }
                    }
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
                    }
                }

                // check if there is enough stock available
                if (!isStockAvailable(conn, productId, quantity)) {
                    Logger.log(Logger.WARNING, "Insufficient stock for product: " + productId + ", requested: " + quantity);
                    System.out.println("Not enough inventory available.");
                    return;
                }

                // get product name and price for logging
                String productName = "";
                double productPrice = 0.0;
                String getProductInfoQuery = "SELECT ItemName, ItemPrice FROM Products WHERE ProductID = ?";
                try (PreparedStatement getProductInfoStmt = conn.prepareStatement(getProductInfoQuery)) {
                    getProductInfoStmt.setString(1, productId);
                    try (ResultSet productInfoRs = getProductInfoStmt.executeQuery()) {
                        if (productInfoRs.next()) {
                            productName = productInfoRs.getString("ItemName");
                            productPrice = productInfoRs.getDouble("ItemPrice");
                        }
                    }
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
                }
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

    // checks if there is enough stock for purchase
    // @param conn database connection
    // @param productId product ID
    // @param quantity quantity to check
    // @return true if enough stock is available, false otherwise
    // @throws SQLException if a database error occurs
    private static boolean isStockAvailable(Connection conn, String productId, int quantity) throws SQLException {
        // query to get the current quantity of the product
        String sql = "SELECT ItemQuantity FROM Products WHERE ProductID = ?";

        // prepare and run query
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setString(1, productId);
        ResultSet rs = stmt.executeQuery();

        // if product is found, check its quantity
        if (rs.next()) {
            int available = rs.getInt("ItemQuantity");
            return quantity <= available;  // true if enough stock
        } else {
            System.out.println("Product not found in inventory.");
            return false;
        }
    }

    // views purchase history for a specific customer
    // @param customerId customer ID
    public static void viewCustomerHistory(int customerId) {
        try {
            // validate customer ID
            if (customerId <= 0) {
                throw new ValidationException("Invalid customer ID", "Customer ID");
            }
            
            try (Connection conn = getConnection()) {
                // check if customer exists
                String checkCustomerQuery = "SELECT * FROM Persons WHERE PersonID = ?";
                try (PreparedStatement checkCustomerStmt = conn.prepareStatement(checkCustomerQuery)) {
                    checkCustomerStmt.setInt(1, customerId);
                    try (ResultSet customerRs = checkCustomerStmt.executeQuery()) {
                        if (!customerRs.next()) {
                            Logger.log(Logger.WARNING, "Attempt to view history for non-existent customer ID: " + customerId);
                            System.out.println("Error: Customer ID does not exist!");
                            return;
                        }
                        
                        // get customer name for logging
                        String firstName = customerRs.getString("FName");
                        String lastName = customerRs.getString("LName");
                        
                        // log the history view
                        Logger.log(Logger.INFO, "Viewing purchase history for customer: " + 
                                  firstName + " " + lastName + " (ID: " + customerId + ")");
                    }
                }
                
                // query to get customer purchase history
                String query = "SELECT p.FName, p.LName, pr.ItemName, pu.QuantityPurchased, pu.Date " +
                        "FROM Purchase pu " +
                        "JOIN Persons p ON pu.PersonID = p.PersonID " +
                        "JOIN Products pr ON pu.ProductID = pr.ProductID " +
                        "WHERE pu.PersonID = ? " +
                        "ORDER BY pu.Date DESC";

                try (PreparedStatement stmt = conn.prepareStatement(query)) {
                    // set customer ID parameter
                    stmt.setInt(1, customerId);
                    try (ResultSet rs = stmt.executeQuery()) {
                        boolean hasData = false;
                        while (rs.next()) {
                            hasData = true;
                            // get data from result set
                            String firstName = rs.getString("FName");
                            String lastName = rs.getString("LName");
                            String itemName = rs.getString("ItemName");
                            int quantity = rs.getInt("QuantityPurchased");
                            Timestamp date = rs.getTimestamp("Date");

                            // display purchase information
                            System.out.printf("Customer: %s %s\nProduct: %s | Quantity: %d | Date: %s%n",
                                    firstName, lastName, itemName, quantity, date.toString());
                        }
                        if (!hasData) {
                            System.out.println("No purchase history found for this customer.");
                            Logger.log(Logger.INFO, "No purchase history found for customer ID: " + customerId);
                        }
                    }
                }
            }
        } catch (ValidationException e) {
            String errorMessage = ErrorHandler.handleValidationException(e, e.getField());
            System.err.println(errorMessage);
        } catch (SQLException e) {
            String errorMessage = ErrorHandler.handleSQLException(e, "retrieving customer history");
            System.err.println(errorMessage);
        } catch (Exception e) {
            String errorMessage = ErrorHandler.handleException(e, "retrieving customer history");
            System.err.println(errorMessage);
        }
    }
}
