import java.sql.*;
import java.util.Scanner;

public class Products {
    private final String dbUrl;
    private final String dbUser;
    private final String dbPassword;

    public Products(String dbUrl, String dbUser, String dbPassword) {
        this.dbUrl = dbUrl;
        this.dbUser = dbUser;
        this.dbPassword = dbPassword;
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(dbUrl, dbUser, dbPassword);
    }

    public void manageProducts(Scanner scanner) {
        boolean managing = true;

        while (managing) {
            System.out.println("Managing Products...");
            System.out.println("1. View Products");
            System.out.println("2. Add New Product");
            System.out.println("3. Modify Existing Product");
            System.out.println("4. Remove Product");
            System.out.println("5. Return to Main Menu");
            System.out.print("Enter your choice: ");
            int choice = scanner.nextInt();

            switch (choice) {
                case 1 -> viewProducts();
                case 2 -> addNewProduct(scanner);
                case 3 -> modifyProduct(scanner);
                case 4 -> removeProduct(scanner);
                case 5 -> managing = false;
                default -> System.out.println("Invalid choice!");
            }
        }
    }

    private void viewProducts() {
        try (Connection connection = getConnection()) {
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
        } catch (SQLException e) {
            System.err.println("Error viewing products: " + e.getMessage());
        }
    }

    private void addNewProduct(Scanner scanner) {
        try (Connection connection = getConnection()) {
            System.out.print("Enter Product ID: ");
            String productId = scanner.next();
            System.out.print("Enter Product Name: ");
            scanner.nextLine(); // Consume newline
            String name = scanner.nextLine();
            System.out.print("Enter Product Price: ");
            double price = scanner.nextDouble();
            System.out.print("Enter Product Quantity: ");
            int quantity = scanner.nextInt();

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

            String insertQuery = "INSERT INTO Products (ProductID, ItemName, ItemPrice, ItemQuantity) VALUES (?, ?, ?, ?)";
            try (PreparedStatement insertStatement = connection.prepareStatement(insertQuery)) {
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

    private void modifyProduct(Scanner scanner) {
        try (Connection connection = getConnection()) {
            System.out.print("Enter Product ID to modify: ");
            String productId = scanner.next();

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

            System.out.println("What would you like to modify?");
            System.out.println("1. Name");
            System.out.println("2. Price");
            System.out.println("3. Quantity");
            System.out.print("Enter your choice: ");
            int modifyChoice = scanner.nextInt();

            String updateQuery;
            switch (modifyChoice) {
                case 1 -> {
                    System.out.print("Enter new Product Name: ");
                    scanner.nextLine(); // Consume newline
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

    private void removeProduct(Scanner scanner) {
        try (Connection connection = getConnection()) {
            System.out.print("Enter Product ID to remove: ");
            String productId = scanner.next();

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
