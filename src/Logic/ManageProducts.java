package src.Logic;

import src.Security.SecurityUtil;
import src.Util.ErrorHandler;
import src.Util.Logger;
import src.Util.ValidationException;
import java.sql.*;
import java.util.InputMismatchException;
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
                
                Logger.log(Logger.INFO, "Retrieving all products from database");
                boolean hasProducts = false;
                
                while (resultSet.next()) {
                    hasProducts = true;
                    // format and display each product
                    System.out.printf("Product ID: %s, Name: %s, Price: %.2f, Quantity: %d%n",
                            resultSet.getString("ProductID"),
                            resultSet.getString("ItemName"),
                            resultSet.getDouble("ItemPrice"),
                            resultSet.getInt("ItemQuantity"));
                }
                
                if (!hasProducts) {
                    System.out.println("No products found in the database.");
                    Logger.log(Logger.INFO, "No products found in database");
                }
            }
        } catch (SQLException e) {
            String errorMessage = ErrorHandler.handleSQLException(e, "retrieving products");
            System.err.println(errorMessage);
        } catch (Exception e) {
            String errorMessage = ErrorHandler.handleException(e, "retrieving products");
            System.err.println(errorMessage);
        }
    }

    // adds a new product to the database
    // @param scanner scanner for user input
    private static void addNewProduct(Scanner scanner) {
        try {
            // get product details from user
            System.out.print("Enter Product ID: ");
            String productId = scanner.next();
            
            // validate product ID
            if (productId == null || productId.trim().isEmpty()) {
                throw new ValidationException("Product ID cannot be empty", "Product ID");
            }
            
            System.out.print("Enter Product Name: ");
            scanner.nextLine(); // consume newline character
            String name = scanner.nextLine();
            
            // validate product name
            if (name == null || name.trim().isEmpty()) {
                throw new ValidationException("Product name cannot be empty", "Product Name");
            }
            
            System.out.print("Enter Product Price: ");
            double price = scanner.nextDouble();
            
            // validate price
            if (price <= 0) {
                throw new ValidationException("Price must be greater than zero", "Price");
            }
            
            System.out.print("Enter Product Quantity: ");
            int quantity = scanner.nextInt();
            
            // validate quantity
            if (quantity < 0) {
                throw new ValidationException("Quantity cannot be negative", "Quantity");
            }
            
            try (Connection connection = getConnection()) {
                // check if product ID already exists
                String checkQuery = "SELECT COUNT(*) FROM Products WHERE ProductID = ?";
                try (PreparedStatement checkStatement = connection.prepareStatement(checkQuery)) {
                    checkStatement.setString(1, productId);
                    try (ResultSet resultSet = checkStatement.executeQuery()) {
                        if (resultSet.next() && resultSet.getInt(1) > 0) {
                            Logger.log(Logger.WARNING, "Attempt to add product with existing ID: " + productId);
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
                    
                    Logger.log(Logger.INFO, "New product added: " + productId + " - " + name);
                    System.out.println("Product added successfully!");
                }
            }
        } catch (ValidationException e) {
            String errorMessage = ErrorHandler.handleValidationException(e, e.getField());
            System.err.println(errorMessage);
        } catch (InputMismatchException e) {
            String errorMessage = ErrorHandler.handleException(e, "reading product information");
            System.err.println(errorMessage);
            System.out.println("Please enter valid numeric values for price and quantity.");
            scanner.nextLine(); // consume invalid input
        } catch (SQLException e) {
            String errorMessage = ErrorHandler.handleSQLException(e, "adding product");
            System.err.println(errorMessage);
        } catch (Exception e) {
            String errorMessage = ErrorHandler.handleException(e, "adding product");
            System.err.println(errorMessage);
        }
    }

    // modifies an existing product
    // @param scanner scanner for user input
    private static void modifyProduct(Scanner scanner) {
        try {
            // get product ID to modify
            System.out.print("Enter Product ID to modify: ");
            String productId = scanner.next();
            
            // validate product ID
            if (productId == null || productId.trim().isEmpty()) {
                throw new ValidationException("Product ID cannot be empty", "Product ID");
            }

            try (Connection connection = getConnection()) {
                // check if product exists
                String checkQuery = "SELECT * FROM Products WHERE ProductID = ?";
                try (PreparedStatement checkStatement = connection.prepareStatement(checkQuery)) {
                    checkStatement.setString(1, productId);
                    try (ResultSet resultSet = checkStatement.executeQuery()) {
                        if (!resultSet.next()) {
                            Logger.log(Logger.WARNING, "Attempt to modify non-existent product: " + productId);
                            System.out.println("Error: Product ID does not exist!");
                            return;
                        }
                        
                        // store current values for logging
                        String currentName = resultSet.getString("ItemName");
                        double currentPrice = resultSet.getDouble("ItemPrice");
                        int currentQuantity = resultSet.getInt("ItemQuantity");
                        
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
                                
                                // validate new name
                                if (newName == null || newName.trim().isEmpty()) {
                                    throw new ValidationException("Product name cannot be empty", "Product Name");
                                }
                                
                                updateQuery = "UPDATE Products SET ItemName = ? WHERE ProductID = ?";
                                try (PreparedStatement updateStatement = connection.prepareStatement(updateQuery)) {
                                    updateStatement.setString(1, newName);
                                    updateStatement.setString(2, productId);
                                    updateStatement.executeUpdate();
                                    
                                    Logger.log(Logger.INFO, "Product name updated: " + productId + 
                                              " from '" + currentName + "' to '" + newName + "'");
                                    System.out.println("Product name updated successfully!");
                                }
                            }
                            case 2 -> {
                                // modify product price
                                System.out.print("Enter new Product Price: ");
                                double newPrice = scanner.nextDouble();
                                
                                // validate new price
                                if (newPrice <= 0) {
                                    throw new ValidationException("Price must be greater than zero", "Price");
                                }
                                
                                updateQuery = "UPDATE Products SET ItemPrice = ? WHERE ProductID = ?";
                                try (PreparedStatement updateStatement = connection.prepareStatement(updateQuery)) {
                                    updateStatement.setDouble(1, newPrice);
                                    updateStatement.setString(2, productId);
                                    updateStatement.executeUpdate();
                                    
                                    Logger.log(Logger.INFO, "Product price updated: " + productId + 
                                              " from $" + currentPrice + " to $" + newPrice);
                                    System.out.println("Product price updated successfully!");
                                }
                            }
                            case 3 -> {
                                // modify product quantity
                                System.out.print("Enter new Product Quantity: ");
                                int newQuantity = scanner.nextInt();
                                
                                // validate new quantity
                                if (newQuantity < 0) {
                                    throw new ValidationException("Quantity cannot be negative", "Quantity");
                                }
                                
                                updateQuery = "UPDATE Products SET ItemQuantity = ? WHERE ProductID = ?";
                                try (PreparedStatement updateStatement = connection.prepareStatement(updateQuery)) {
                                    updateStatement.setInt(1, newQuantity);
                                    updateStatement.setString(2, productId);
                                    updateStatement.executeUpdate();
                                    
                                    Logger.log(Logger.INFO, "Product quantity updated: " + productId + 
                                              " from " + currentQuantity + " to " + newQuantity);
                                    System.out.println("Product quantity updated successfully!");
                                }
                            }
                            default -> {
                                Logger.log(Logger.WARNING, "Invalid modification choice: " + modifyChoice);
                                System.out.println("Invalid choice!");
                            }
                        }
                    }
                }
            }
        } catch (ValidationException e) {
            String errorMessage = ErrorHandler.handleValidationException(e, e.getField());
            System.err.println(errorMessage);
        } catch (InputMismatchException e) {
            String errorMessage = ErrorHandler.handleException(e, "reading product information");
            System.err.println(errorMessage);
            System.out.println("Please enter valid numeric values.");
            scanner.nextLine(); // consume invalid input
        } catch (SQLException e) {
            String errorMessage = ErrorHandler.handleSQLException(e, "modifying product");
            System.err.println(errorMessage);
        } catch (Exception e) {
            String errorMessage = ErrorHandler.handleException(e, "modifying product");
            System.err.println(errorMessage);
        }
    }

    // removes a product from the database
    // @param scanner scanner for user input
    private static void removeProduct(Scanner scanner) {
        try {
            // get product ID to remove
            System.out.print("Enter Product ID to remove: ");
            String productId = scanner.next();
            
            // validate product ID
            if (productId == null || productId.trim().isEmpty()) {
                throw new ValidationException("Product ID cannot be empty", "Product ID");
            }

            try (Connection connection = getConnection()) {
                // check if product exists
                String checkExistsQuery = "SELECT COUNT(*) FROM Products WHERE ProductID = ?";
                try (PreparedStatement checkExistsStatement = connection.prepareStatement(checkExistsQuery)) {
                    checkExistsStatement.setString(1, productId);
                    try (ResultSet resultSet = checkExistsStatement.executeQuery()) {
                        if (resultSet.next() && resultSet.getInt(1) == 0) {
                            Logger.log(Logger.WARNING, "Attempt to remove non-existent product: " + productId);
                            System.out.println("Error: Product ID does not exist!");
                            return;
                        }
                    }
                }
                
                // check if product is used in any transactions
                String checkTransactionQuery = "SELECT COUNT(*) FROM Purchase WHERE ProductID = ?";
                try (PreparedStatement checkStatement = connection.prepareStatement(checkTransactionQuery)) {
                    checkStatement.setString(1, productId);
                    try (ResultSet resultSet = checkStatement.executeQuery()) {
                        if (resultSet.next() && resultSet.getInt(1) > 0) {
                            Logger.log(Logger.WARNING, "Attempt to remove product with existing transactions: " + productId);
                            System.out.println("Error: Cannot remove product. There are pending transactions!");
                            return;
                        }
                    }
                }

                // get product name for logging
                String productName = "";
                String getNameQuery = "SELECT ItemName FROM Products WHERE ProductID = ?";
                try (PreparedStatement getNameStatement = connection.prepareStatement(getNameQuery)) {
                    getNameStatement.setString(1, productId);
                    try (ResultSet resultSet = getNameStatement.executeQuery()) {
                        if (resultSet.next()) {
                            productName = resultSet.getString("ItemName");
                        }
                    }
                }
                
                // delete the product
                String deleteQuery = "DELETE FROM Products WHERE ProductID = ?";
                try (PreparedStatement deleteStatement = connection.prepareStatement(deleteQuery)) {
                    deleteStatement.setString(1, productId);
                    int rowsAffected = deleteStatement.executeUpdate();
                    
                    if (rowsAffected > 0) {
                        Logger.log(Logger.INFO, "Product removed: " + productId + " - " + productName);
                        System.out.println("Product removed successfully!");
                    } else {
                        Logger.log(Logger.WARNING, "No product was removed with ID: " + productId);
                        System.out.println("No product was removed. Please check the product ID.");
                    }
                }
            }
        } catch (ValidationException e) {
            String errorMessage = ErrorHandler.handleValidationException(e, e.getField());
            System.err.println(errorMessage);
        } catch (SQLException e) {
            String errorMessage = ErrorHandler.handleSQLException(e, "removing product");
            System.err.println(errorMessage);
        } catch (Exception e) {
            String errorMessage = ErrorHandler.handleException(e, "removing product");
            System.err.println(errorMessage);
        }
    }
}
