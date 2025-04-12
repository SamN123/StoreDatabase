package src.logic
    
import java.util.Scanner;
import java.sql.*;
import java.util.ArrayList;
import java.util.Comparator;
// CustomerHistory class used to handle tasks and operations for Customer History sub menu
public class CustomerHistory {
    private static String dbUrl;
    private static String dbUser;
    private static String dbPassword;
    
    // method to establish connection from main class
    public static void setConnectionInfo(String url, String user, String password) {
        dbUrl = url;
        dbUser = user;
        dbPassword = password;
    }

    private static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(dbUrl, dbUser, dbPassword);
    }
    // customer history sub menu is defined with in the class and called in main
    public static void customerHistoryMenu(Scanner scanner) {
        boolean managing = true;

        while (managing) {
            System.out.println("\n--- Customer History ---");
            System.out.println("1. Search Customer");
            System.out.println("2. View Past Purchases");
            System.out.println("3. Return to Main Menu");
            System.out.print("Enter your choice: ");
            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1 -> {
                    System.out.print("Enter customer email to search: ");
                    String email = scanner.nextLine();
                    searchCustomer(email);
                }
                case 2 -> viewPastPurchases();
                case 3 -> managing = false;
                default -> System.out.println("Invalid choice!");
            }
        }
    }
    // Transactions helper class used to instantiate arrayList that holds all transactions from MySQL database
    private static class Transactions {
        int personID;
        String firstName;
        String lastName;
        int transactionID;
        Timestamp date;
        int quantity;
        String itemName;
        double itemPrice;

        public Transactions(int personID, String firstName, String lastName,
                            int transactionID, Timestamp date, int quantity,
                            String itemName, double itemPrice) {
            this.personID = personID;
            this.firstName = firstName;
            this.lastName = lastName;
            this.transactionID = transactionID;
            this.date = date;
            this.quantity = quantity;
            this.itemName = itemName;
            this.itemPrice = itemPrice;
        }

        @Override
        public String toString() {
            return "Customer: " + firstName + " " + lastName + " ID: " + personID +
                    ", Transaction ID: " + transactionID + ", Date: " + date +
                    ", Product: " + itemName + ", Price: $" + itemPrice +
                    ", Quantity: " + quantity;
        }
    }
    // method to search for a given customer by using their unique email address
    private static void searchCustomer(String email) {
        String sql = "SELECT PersonID, FName, LName, Email, Phone FROM Persons WHERE Email = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, email);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
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
    
    // viewPastPurchases() method is used to display all transactions from the database
    private static void viewPastPurchases() {
        String sql = "SELECT pu.TransactionID, pu.Date, pu.QuantityPurchased, " +
                "pr.ItemName, pr.ItemPrice, p.PersonID, p.FName, p.LName " +
                "FROM Purchase pu " +
                "JOIN Products pr ON pu.ProductID = pr.ProductID " +
                "JOIN Persons p ON pu.PersonID = p.PersonID";
        
        // ArrayList of type Transactions is created to handle the data
        ArrayList<Transactions> purchases = new ArrayList<>();
        // connection created from within try with resources block to auto close on end of execution
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                int transactionId = rs.getInt("TransactionID");
                Timestamp date = rs.getTimestamp("Date");
                int quantity = rs.getInt("QuantityPurchased");
                String itemName = rs.getString("ItemName");
                double itemPrice = rs.getDouble("ItemPrice");
                int personID = rs.getInt("PersonID");
                String firstName = rs.getString("FName");
                String lastName = rs.getString("LName");

                purchases.add(new Transactions(personID, firstName, lastName,
                        transactionId, date, quantity, itemName, itemPrice));
            }
        } catch (SQLException e) {
            System.out.println("Error getting data: " + e.getMessage());
            e.printStackTrace();
        }
        // lambda expression using comparator sub methods to sort the transactions
        // by personID and then sort by date for each person's transactions
        purchases.sort(Comparator.comparing((Transactions t) -> t.personID)
                .thenComparing(t -> t.date));

        System.out.println("\n All past purchases: ");
        if (purchases.isEmpty()) {
            System.out.println("No purchase records found.");
        } else {
            // for loop used to display transactions
            for (Transactions purchase : purchases) {
                System.out.println(purchase);
            }
        }
    }
}
