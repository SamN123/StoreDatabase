package src.Logic;

import src.Security.SecurityUtil;
import java.sql.*;
import java.util.Scanner;

public class ManageProducts {
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

    // main method for managing products
    // @param scanner scanner for user input
    public static void manageProducts(Scanner scanner) {
        // verify admin permissions before allowing access
        if (!SecurityUtil.hasAdminPermission()) {
            System.out.println("Access denied. Admin privileges required.");
            return;
        }
        
        boolean managing = true;

        while (managing) {
            System.out.println("\n--- Manage Products ---");
            System.out.println("1. View Products");
            System.out.println("2. Add New Product");
            System.out.println("3. Modify Existing Product");
            System.out.println("4. Remove Product");
            System.out.println("5. Return to Main Menu");
            System.out.print("Enter your choice: ");
            int choice = scanner.nextInt();

            switch (choice) {
                case 1 -> viewProducts(); // view all products
                case 2 -> addNewProduct(scanner); // add a new product
                case 3 -> modifyProduct(scanner); // modify an existing product
                case 4 -> removeProduct(scanner); // remove a product
                case 5 -> managing = false; // return to main menu
                default -> System.out.println("Invalid choice!");
            }
        }
    }

    // displays all products in the database
    private static void viewProducts() {
        try (Connection connection = getConnection()) {
            // query to select all products
            String query = "SELECT * FROM Products";
            try (PreparedStatement statement = connection.prepareStatement(query);
                 ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    // format and display each product
                    System.out.printf("Product ID: %s, Name: %s, Price: %.2f, Quantity: %d%n",
                            resultSet.getString("ProductID"),
                            resultSet.getString("ItemName"),
                            resultSet.getDouble("ItemPrice"),
                            resultSet.getInt("ItemQuantity"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error viewing products: " + e.getMessage());
        }
    }

    // adds a new product to the database
    // @param scanner scanner for user input
    private static void addNewProduct(Scanner scanner) {
        try (Connection connection = getConnection()) {
            // get product details from user
            System.out.print("Enter Product ID: ");
            String productId = scanner.next();
            System.out.print("Enter Product Name: ");
            scanner.nextLine(); // consume newline character
            String name = scanner.nextLine();
            System.out.print("Enter Product Price: ");
            double price = scanner.nextDouble();
            System.out.print("Enter Product Quantity: ");
            int quantity = scanner.nextInt();

            // check if product ID already exists
            String checkQuery = "SELECT COUNT(*) FROM Products WHERE ProductID = ?";
            try (PreparedStatement checkStatement = connection.prepareStatement(checkQuery)) {
                checkStatement.setString(1, productId);
                try (ResultSet resultSet = checkStatement.executeQuery()) {
                    if (resultSet.next() && resultSet.getInt(1) > 0) {
                        System.out.println("Error: Product ID already exists!");
                        return;
                    }
                }
            }

            // insert the new product
            String insertQuery = "INSERT INTO Products (ProductID, ItemName, ItemPrice, ItemQuantity) VALUES (?, ?, ?, ?)";
            try (PreparedStatement insertStatement = connection.prepareStatement(insertQuery)) {
                // set parameters for the insert query
                insertStatement.setString(1, productId);
                insertStatement.setString(2, name);
                insertStatement.setDouble(3, price);
                insertStatement.setInt(4, quantity);
                insertStatement.executeUpdate();
                System.out.println("Product added successfully!");
            }
        } catch (SQLException e) {
            System.err.println("Error adding product: " + e.getMessage());
        }
    }

    // modifies an existing product
    // @param scanner scanner for user input
    private static void modifyProduct(Scanner scanner) {
        try (Connection connection = getConnection()) {
            // get product ID to modify
            System.out.print("Enter Product ID to modify: ");
            String productId = scanner.next();

            // check if product exists
            String checkQuery = "SELECT COUNT(*) FROM Products WHERE ProductID = ?";
            try (PreparedStatement checkStatement = connection.prepareStatement(checkQuery)) {
                checkStatement.setString(1, productId);
                try (ResultSet resultSet = checkStatement.executeQuery()) {
                    if (!resultSet.next() || resultSet.getInt(1) == 0) {
                        System.out.println("Error: Product ID does not exist!");
                        return;
                    }
                }
            }

            // display modification options
            System.out.println("What would you like to modify?");
            System.out.println("1. Name");
            System.out.println("2. Price");
            System.out.println("3. Quantity");
            System.out.print("Enter your choice: ");
            int modifyChoice = scanner.nextInt();

            String updateQuery;
            switch (modifyChoice) {
                case 1 -> {
                    // modify product name
                    System.out.print("Enter new Product Name: ");
                    scanner.nextLine(); // consume newline character
                    String newName = scanner.nextLine();
                    updateQuery = "UPDATE Products SET ItemName = ? WHERE ProductID = ?";
                    try (PreparedStatement updateStatement = connection.prepareStatement(updateQuery)) {
                        updateStatement.setString(1, newName);
                        updateStatement.setString(2, productId);
                        updateStatement.executeUpdate();
                        System.out.println("Product name updated successfully!");
                    }
                }
                case 2 -> {
                    // modify product price
                    System.out.print("Enter new Product Price: ");
                    double newPrice = scanner.nextDouble();
                    updateQuery = "UPDATE Products SET ItemPrice = ? WHERE ProductID = ?";
                    try (PreparedStatement updateStatement = connection.prepareStatement(updateQuery)) {
                        updateStatement.setDouble(1, newPrice);
                        updateStatement.setString(2, productId);
                        updateStatement.executeUpdate();
                        System.out.println("Product price updated successfully!");
                    }
                }
                case 3 -> {
                    // modify product quantity
                    System.out.print("Enter new Product Quantity: ");
                    int newQuantity = scanner.nextInt();
                    updateQuery = "UPDATE Products SET ItemQuantity = ? WHERE ProductID = ?";
                    try (PreparedStatement updateStatement = connection.prepareStatement(updateQuery)) {
                        updateStatement.setInt(1, newQuantity);
                        updateStatement.setString(2, productId);
                        updateStatement.executeUpdate();
                        System.out.println("Product quantity updated successfully!");
                    }
                }
                default -> System.out.println("Invalid choice!");
            }
        } catch (SQLException e) {
            System.err.println("Error modifying product: " + e.getMessage());
        }
    }

    // removes a product from the database
    // @param scanner scanner for user input
    private static void removeProduct(Scanner scanner) {
        try (Connection connection = getConnection()) {
            // get product ID to remove
            System.out.print("Enter Product ID to remove: ");
            String productId = scanner.next();

            // check if product is used in any transactions
            String checkTransactionQuery = "SELECT COUNT(*) FROM Purchase WHERE ProductID = ?";
            try (PreparedStatement checkStatement = connection.prepareStatement(checkTransactionQuery)) {
                checkStatement.setString(1, productId);
                try (ResultSet resultSet = checkStatement.executeQuery()) {
                    if (resultSet.next() && resultSet.getInt(1) > 0) {
                        System.out.println("Error: Cannot remove product. There are pending transactions!");
                        return;
                    }
                }
            }

            // delete the product
            String deleteQuery = "DELETE FROM Products WHERE ProductID = ?";
            try (PreparedStatement deleteStatement = connection.prepareStatement(deleteQuery)) {
                deleteStatement.setString(1, productId);
                deleteStatement.executeUpdate();
                System.out.println("Product removed successfully!");
            }
        } catch (SQLException e) {
            System.err.println("Error removing product: " + e.getMessage());
        }
    }
}
