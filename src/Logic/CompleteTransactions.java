package src.Logic;

import java.sql.*;
import java.util.Scanner;

public class CompleteTransactions {

    // Database connection details
    private static String DB_URL;
    private static String DB_USER;
    private static String DB_PASSWORD;

    public static void setConnectionInfo(String url, String user, String password) {
        DB_URL = url;
        DB_USER = user;
        DB_PASSWORD = password;
    }

    private static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

    public static void TransactionMenu(Scanner scanner) {
        boolean inTransactionMenu = true;

        while (inTransactionMenu) {
            System.out.println("\n--- Complete Transactions ---");
            System.out.println("1. Add a New Client");
            System.out.println("2. Make a Purchase");
            System.out.println("3. View Customer Purchase History");
            System.out.println("4. Return to Main Menu");
            System.out.print("Enter your choice: ");

            int choice = scanner.nextInt();

            switch (choice) {
                case 1 -> {
                    scanner.nextLine();
                    System.out.print("First Name: ");
                    String fname = scanner.nextLine();
                    System.out.print("Last Name: ");
                    String lname = scanner.nextLine();
                    System.out.print("Email: ");
                    String email = scanner.nextLine();
                    System.out.print("Phone (XXX-XXX-XXXX): ");
                    String phone = scanner.nextLine();
                    addClient(fname, lname, email, phone);
                }
                case 2 -> {
                    System.out.print("Enter Customer ID: ");
                    int customerId = scanner.nextInt();
                    System.out.print("Enter Product ID: ");
                    String productId = scanner.next();
                    System.out.print("Enter Quantity: ");
                    int quantity = scanner.nextInt();
                    makePurchase(customerId, productId, quantity);
                }
                case 3 -> {
                    System.out.print("Enter Customer ID: ");
                    int customerId = scanner.nextInt();
                    viewCustomerHistory(customerId);
                }
                case 4 -> inTransactionMenu = false;
                default -> System.out.println("Invalid choice!");
            }
        }
    }

    // Adds a new client to the Persons table (ADD EMAIL VERIFICATION LATER?)
    public static void addClient(String fname, String lname, String email, String phone) {
        try (Connection conn = getConnection()) {
            String sql = "INSERT INTO Persons (FName, LName, Email, Phone) VALUES (?, ?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, fname);
                stmt.setString(2, lname);
                stmt.setString(3, email);
                stmt.setString(4, phone);
                int rowsAffected = stmt.executeUpdate();

                if (rowsAffected > 0) {
                    try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            int personId = generatedKeys.getInt(1);
                            System.out.println("Client added successfully.");
                            System.out.println("Assigned Person ID: " + personId);
                            System.out.println("Please use this ID when making a purchase.");
                        }
                    }
                }
            }
        } catch (SQLIntegrityConstraintViolationException e) {
            System.out.println("Error: Email already exists.");
        } catch (SQLException e) {
            System.out.println("Error adding client: " + e.getMessage());
        }
    }

    // Handles purchase transaction
    public static void makePurchase(int customerId, String productId, int quantity) {
        try (Connection conn = getConnection()) {

            // Performs inventory check first
            if (!isStockAvailable(conn, productId, quantity)) {
                System.out.println("Not enough inventory available.");
                return;
            }

            String call = "{CALL MakePurchase(?, ?, ?)}";
            try (CallableStatement stmt = conn.prepareCall(call)) {
                stmt.setInt(1, customerId);
                stmt.setString(2, productId);
                stmt.setInt(3, quantity);
                stmt.execute();
                System.out.println("Purchase completed successfully.");
            }

        } catch (SQLException e) {
            System.out.println("Transaction failed: " + e.getMessage());
        }
    }

    // Checks if there is enough stock for purchase
    private static boolean isStockAvailable(Connection conn, String productId, int quantity) throws SQLException {
        // Finds quantity of requested product
        String sql = "SELECT ItemQuantity FROM Products WHERE ProductID = ?";

        // Prepare and run query
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setString(1, productId);
        ResultSet rs = stmt.executeQuery();

        // If product is found, check its quantity
        if (rs.next()) {
            int available = rs.getInt("ItemQuantity");
            return quantity <= available;  // true if enough stock
        } else {
            System.out.println("Product not found in inventory.");
            return false;
        }
    }

    public static void viewCustomerHistory(int customerId) {
        try (Connection conn = getConnection()) {
            // Updated query includes customer info
            String query = "SELECT p.FName, p.LName, pr.ItemName, pu.QuantityPurchased, pu.Date " +
                    "FROM Purchase pu " +
                    "JOIN Persons p ON pu.PersonID = p.PersonID " +
                    "JOIN Products pr ON pu.ProductID = pr.ProductID " +
                    "WHERE pu.PersonID = ? " +
                    "ORDER BY pu.Date DESC";

            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setInt(1, customerId);
                try (ResultSet rs = stmt.executeQuery()) {
                    boolean hasData = false;
                    while (rs.next()) {
                        hasData = true;
                        String firstName = rs.getString("FName");
                        String lastName = rs.getString("LName");
                        String itemName = rs.getString("ItemName");
                        int quantity = rs.getInt("QuantityPurchased");
                        Timestamp date = rs.getTimestamp("Date");

                        System.out.printf("Customer: %s %s\nProduct: %s | Quantity: %d | Date: %s%n",
                                firstName, lastName, itemName, quantity, date.toString());
                    }
                    if (!hasData) {
                        System.out.println("No purchase history found for this customer.");
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("Error retrieving history: " + e.getMessage());
        }
    }

}
