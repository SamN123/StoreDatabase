import java.sql.*;

// Logic Class used to handle operations and tasks for Customer History sub menu
public class CustomerHistory {

    // Database information stored in variables to be used by getConnection() method
    private static final String DB_URL = "jdbc:mysql://localhost:3306/storedb";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "password";

    // getConnection() method is used to simplify the connection process,
    // will be called in try with resources block to ensure it is closed after use
    private static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

    // searchCustomer() method uses the unique email address to search for a client.
    // this ensures that the right client is found, as many clients may have the same
    // first or last name.
    public static void searchCustomer(String email) {
        // query statement is stored in a string variable 'sql'
        String sql = "SELECT PersonID, FName, LName, Email, Phone FROM Persons WHERE Email = ?";
        // conn object created in try with resources parameter and used to create prepared statement
        // for the sql string
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            // email used in argument is bound to the 1st placeholder attribute using setString()
            pstmt.setString(1, email);

            try (ResultSet rs = pstmt.executeQuery()) {
                // rs.next iterates through the tabulated data
                if (rs.next()) {
                    // A new person object is created and resultSet getter methods are used to acquire
                    // tabulated data
                    Person person = new Person(
                            rs.getInt("PersonID"),
                            rs.getString("FName"),
                            rs.getString("LName"),
                            rs.getString("Phone"),
                            rs.getString("Email")
                    );
                    System.out.println("Customer Found: " + person);
                } else {
                    System.out.println("No customer was found with the email: " + email);
                }
            }
        } catch (SQLException e) {
            System.out.println("Error searching for a customer with the given email: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // viewPastPurchases() takes the customerId as the argument and uses it to
    // display all past purchases of the given client
    public static void viewPastPurchases(int customerId) {

        // Optimized query using JOIN statement to gather relevant data from all three tables
        String sql = "SELECT pu.TransactionID, pu.Date, pu.QuantityPurchased, " +
                "pr.ItemName, pr.ItemPrice, p.FName, p.LName " +
                "FROM Purchase pu " +
                "JOIN Products pr ON pu.ProductID = pr.ProductID " +
                "JOIN Persons p ON pu.PersonID = p.PersonID " +
                "WHERE pu.PersonID = ? " +
                "ORDER BY pu.Date DESC";

        // connection object created and prepared statement declared in the
        // try with resources parameter to ensure it is closed after use
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // binds customerId to 1st placeholder to ensure CustomerID is queried
            pstmt.setInt(1, customerId);

            try (ResultSet rs = pstmt.executeQuery()) {
                // boolean value to check if record is found
                boolean recordCheck = false;
                System.out.println("\nPast purchases of Customer ID " + customerId);

                while (rs.next()) {
                    // variables declared and used to store data from database for matching CustomerID
                    int transactionId = rs.getInt("TransactionID");
                    Timestamp date = rs.getTimestamp("Date");
                    int quantity = rs.getInt("QuantityPurchased");
                    String itemName = rs.getString("ItemName");
                    double itemPrice = rs.getDouble("ItemPrice");
                    String fName = rs.getString("FName");
                    String lName = rs.getString("LName");

                    // Customer information is displayed to the terminal 
                    System.out.println("Customer: " + fName + " " + lName +
                            "  Transaction ID: " + transactionId +
                            ", Date: " + date +
                            ", Product: " + itemName +
                            ", Price: $" + itemPrice +
                            ", Quantity: " + quantity);
                    // change recordCheck to true (customer was found)
                    recordCheck = true;
                }
                if (!recordCheck) {
                    System.out.println("No purchase records found for customer ID " + customerId);
                }
            }
        } catch (SQLException e) {
            System.out.println("Error retrieving purchase history: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
