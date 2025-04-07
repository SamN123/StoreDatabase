import java.sql.*;

public class CompleteTransactions {

    // Database connection details
    private static final String DB_URL = "jdbc:mysql://localhost:3306/storedb";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "passwordhere"; // Ask about database connection. Cloud Computing?

    private static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

    // Adds a new client to the database
    public static void addClient(String fname, String lname, String email, String phone) {
        // TODO: Implement SQL logic to insert client
    }

    // Handles purchase transaction
    public static void makePurchase(int customerId, String productId, int quantity) {
        try (Connection conn = getConnection()) {

            // Perform inventory check first
            if (!isStockAvailable(conn, productId, quantity)) {
                System.out.println("Quantity is insufficient.");
                return;
            }

            String call = "{CALL MakePurchase(?, ?, ?)}";
            try (CallableStatement stmt = conn.prepareCall(call)) {
                stmt.setInt(1, customerId);
                stmt.setString(2, productId);
                stmt.setInt(3, quantity);
                stmt.execute();
                System.out.println("Thank you for your purchase.");
            }

        } catch (SQLException e) {
            System.out.println("Unsuccessful transaction. " + e.getMessage());
        }
    }

    // Checks if enough product stock is available
    private static boolean isStockAvailable(Connection conn, String productId, int quantity) throws SQLException {
        // Finds the quantity of product
        String sql = "SELECT ItemQuantity FROM Products WHERE ProductID = ?";

        // Prepares and runs query
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setString(1, productId);
        ResultSet rs = stmt.executeQuery();

        // If product is found, checks quantity
        if (rs.next()) {
            int available = rs.getInt("ItemQuantity");
            return quantity <= available;  // true if sufficient
        } 
        else {
            System.out.println("Product not found in inventory.");
            return false;
        }
    }

    // View purchase history for customer
    public static void viewCustomerHistory(int customerId) {
        // TODO: Query and print purchase history from the database
    }
}
