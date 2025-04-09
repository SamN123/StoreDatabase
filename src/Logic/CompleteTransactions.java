package src.Logic;

import java.sql.*;

public class CompleteTransactions {

    // Database connection details
    private static final String DB_URL = "jdbc:mysql://localhost:3306/storedb";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "password";

    private static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

    // Adds a new client to the Persons table (ADD EMAIL VERIFICATION LATER?)
    public static void addClient(String fname, String lname, String email, String phone) {
        try (Connection conn = getConnection()) {
            String sql = "INSERT INTO Persons (FName, LName, Email, Phone) VALUES (?, ?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, fname);
                stmt.setString(2, lname);
                stmt.setString(3, email);
                stmt.setString(4, phone);
                stmt.executeUpdate();
                System.out.println("Client added successfully.");
            }
        } catch (SQLIntegrityConstraintViolationException e) {
            System.out.println("Email already exists.");
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

    // Views purchase history for customer
    public static void viewCustomerHistory(int customerId) {
        try (Connection conn = getConnection()) {
            String query = "SELECT pr.ItemName, pu.QuantityPurchased, pu.Date " +
                    "FROM Purchase pu " +
                    "JOIN Products pr ON pu.ProductID = pr.ProductID " +
                    "WHERE pu.PersonID = ? " +
                    "ORDER BY pu.Date DESC";

            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setInt(1, customerId);
                try (ResultSet rs = stmt.executeQuery()) {
                    boolean hasData = false;
                    while (rs.next()) {
                        hasData = true;
                        String name = rs.getString("ItemName");
                        int quantity = rs.getInt("QuantityPurchased");
                        Timestamp date = rs.getTimestamp("Date");
                        System.out.printf("Product: %s | Quantity: %d | Date: %s%n", name, quantity, date.toString());
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
