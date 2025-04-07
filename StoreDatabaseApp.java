import java.sql.*;
import java.util.Scanner;

public class StoreDatabaseApp {

    // Database connection details
    private static final String DB_URL = "jdbc:mysql://localhost:3306/storedb";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "password";

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        boolean running = true;

        while (running) {
            System.out.println("Welcome to Product Management System");
            System.out.println("1. Manage Products");
            System.out.println("2. Complete Transactions");
            System.out.println("3. View Customer History");
            System.out.println("4. Exit");
            System.out.print("Enter your choice: ");

            int choice = scanner.nextInt();

            switch (choice) {
                case 1 -> manageProducts(scanner);
                case 2 -> completeTransactions(scanner);
                case 3 -> viewCustomerHistory(scanner);
                case 4 -> {
                    System.out.println("Exiting the application. Goodbye!");
                    running = false;
                }
                default -> System.out.println("Invalid choice. Please try again.");
            }
        }

        scanner.close();
    }

    private static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

    private static void manageProducts(Scanner scanner) {
        try (Connection connection = getConnection()) {
            System.out.println("Managing Products...");
            System.out.println("1. View Products");
            System.out.println("2. Add New Product");
            System.out.print("Enter your choice: ");
            int choice = scanner.nextInt();

            if (choice == 1) {
                String query = "SELECT * FROM Products";
                try (PreparedStatement statement = connection.prepareStatement(query);
                     ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        System.out.printf("Product ID: %s, Name: %s, Price: %.2f, Quantity: %d%n",
                                resultSet.getString("ProductID"),
                                resultSet.getString("ItemName"),
                                resultSet.getDouble("ItemPrice"),
                                resultSet.getInt("ItemQuantity"));
                    }
                }
            } else if (choice == 2) {
                System.out.print("Enter Product ID: ");
                String productId = scanner.next();
                System.out.print("Enter Product Name: ");
                scanner.nextLine(); // Consume newline
                String name = scanner.nextLine();
                System.out.print("Enter Product Price: ");
                double price = scanner.nextDouble();
                System.out.print("Enter Product Quantity: ");
                int quantity = scanner.nextInt();

                // Check if Product ID already exists
                String checkQuery = "SELECT COUNT(*) FROM Products WHERE ProductID = ?";
                try (PreparedStatement checkStatement = connection.prepareStatement(checkQuery)) {
                    checkStatement.setString(1, productId);
                    try (ResultSet resultSet = checkStatement.executeQuery()) {
                        if (resultSet.next() && resultSet.getInt(1) > 0) {
                            System.out.println("Error: Product ID already exists!");
                            return; // Exit method if Product ID exists
                        }
                    }
                }

                // Insert new product if Product ID does not exist
                String insertQuery = "INSERT INTO Products (ProductID, ItemName, ItemPrice, ItemQuantity) VALUES (?, ?, ?, ?)";
                try (PreparedStatement insertStatement = connection.prepareStatement(insertQuery)) {
                    insertStatement.setString(1, productId);
                    insertStatement.setString(2, name);
                    insertStatement.setDouble(3, price);
                    insertStatement.setInt(4, quantity);
                    insertStatement.executeUpdate();
                    System.out.println("Product added successfully!");
                }
            } else {
                System.out.println("Invalid choice!");
            }
        } catch (SQLException e) {
            System.err.println("Error managing products: " + e.getMessage());
        }
    }

    private static void completeTransactions(Scanner scanner) {
        try (Connection connection = getConnection()) {
            System.out.println("Completing Transactions...");
            System.out.print("Enter Customer ID: ");
            int customerId = scanner.nextInt();
            System.out.print("Enter Product ID: ");
            String productId = scanner.next();
            System.out.print("Enter Quantity Purchased: ");
            int quantity = scanner.nextInt();

            String query = "INSERT INTO Purchase (PersonID, ProductID, Date, QuantityPurchased) VALUES (?, ?, ?, ?)";
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setInt(1, customerId);
                statement.setString(2, productId);
                statement.setDate(3, new java.sql.Date(System.currentTimeMillis())); // Current date
                statement.setInt(4, quantity);
                statement.executeUpdate();
                System.out.println("Transaction completed successfully!");
            }
        } catch (SQLException e) {
            System.err.println("Error completing transactions: " + e.getMessage());
        }
    }

    private static void viewCustomerHistory(Scanner scanner) {
        try (Connection connection = getConnection()) {
            System.out.println("Viewing Customer History...");
            System.out.print("Enter Customer ID: ");
            int customerId = scanner.nextInt();

            String query = "SELECT * FROM Purchase WHERE PersonID = ?";
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setInt(1, customerId);
                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        System.out.printf("Transaction ID: %d, Product ID: %s, Date: %s, Quantity Purchased: %d%n",
                                resultSet.getInt("TransactionID"),
                                resultSet.getString("ProductID"),
                                resultSet.getDate("Date").toString(),
                                resultSet.getInt("QuantityPurchased"));
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error viewing customer history: " + e.getMessage());
        }
    }
}