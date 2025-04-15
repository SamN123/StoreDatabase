package src.Logic;
    
import src.Objects.*;
import src.Security.SecurityUtil;

import java.util.Scanner;
import java.sql.*;
import java.util.ArrayList;
import java.util.Comparator;

public class CustomerHistory {
    private static String dbUrl; // database url
    private static String dbUser; // database username
    private static String dbPassword; // database password
    
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
        // verify user is authenticated before allowing access
        if (!SecurityUtil.hasUserPermission()) {
            System.out.println("Authentication required to access customer history.");
            return;
        }
        
        boolean managing = true;

        while (managing) {
            System.out.println("\n--- Customer History ---");
            System.out.println("1. Search Customer");
            // show admin-only label for viewing all purchases if user is not admin
            System.out.println("2. View Past Purchases" + (SecurityUtil.hasAdminPermission() ? "" : " (Admin Only)"));
            System.out.println("3. Return to Main Menu");
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
                    // only admins can view all past purchases
                    if (SecurityUtil.hasAdminPermission()) {
                        viewPastPurchases();
                    } else {
                        System.out.println("Access denied. Admin privileges required.");
                    }
                }
                case 3 -> managing = false; // return to main menu
                default -> System.out.println("Invalid choice!");
            }
        }
    }
    
    // helper class to store transaction information
    private static class Transactions {
        int personID; // customer ID
        String firstName; // customer first name
        String lastName; // customer last name
        int transactionID; // transaction ID
        Timestamp date; // transaction date
        int quantity; // quantity purchased
        String itemName; // product name
        double itemPrice; // product price

        // constructor to initialize transaction object
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

        // string representation of transaction
        @Override
        public String toString() {
            return "Customer: " + firstName + " " + lastName + " ID: " + personID +
                    ", Transaction ID: " + transactionID + ", Date: " + date +
                    ", Product: " + itemName + ", Price: $" + itemPrice +
                    ", Quantity: " + quantity;
        }
    }
    
    // searches for a customer by email
    // @param email email to search for
    private static void searchCustomer(String email) {
        // query to find customer by email
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
    
    // displays all transactions from the database
    private static void viewPastPurchases() {
        // query to get all transactions with customer and product information
        String sql = "SELECT pu.TransactionID, pu.Date, pu.QuantityPurchased, " +
                "pr.ItemName, pr.ItemPrice, p.PersonID, p.FName, p.LName " +
                "FROM Purchase pu " +
                "JOIN Products pr ON pu.ProductID = pr.ProductID " +
                "JOIN Persons p ON pu.PersonID = p.PersonID";
        
        // arraylist to store all transactions
        ArrayList<Transactions> purchases = new ArrayList<>();
        
        // execute query and process results
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                // get data from result set
                int transactionId = rs.getInt("TransactionID");
                Timestamp date = rs.getTimestamp("Date");
                int quantity = rs.getInt("QuantityPurchased");
                String itemName = rs.getString("ItemName");
                double itemPrice = rs.getDouble("ItemPrice");
                int personID = rs.getInt("PersonID");
                String firstName = rs.getString("FName");
                String lastName = rs.getString("LName");

                // add transaction to list
                purchases.add(new Transactions(personID, firstName, lastName,
                        transactionId, date, quantity, itemName, itemPrice));
            }
        } catch (SQLException e) {
            System.out.println("Error getting data: " + e.getMessage());
            e.printStackTrace();
        }
        
        // sort transactions by person ID and then by date
        purchases.sort(Comparator.comparing((Transactions t) -> t.personID)
                .thenComparing(t -> t.date));

        // display all transactions
        System.out.println("\n All past purchases: ");
        if (purchases.isEmpty()) {
            System.out.println("No purchase records found.");
        } else {
            // display each transaction
            for (Transactions purchase : purchases) {
                System.out.println(purchase);
            }
        }
    }
}
