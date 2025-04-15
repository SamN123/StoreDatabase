package src.Logic;

import src.Security.SecurityUtil;
import src.Util.ErrorHandler;
import src.Util.Logger;
import src.Util.ValidationException;
import java.sql.*;
import java.util.InputMismatchException;
import java.util.Scanner;

public class OptimizedManageProducts {
    private static String dbUrl; // database url
    private static String dbUser; // database username
    private static String dbPassword; // database password
    private static final int DEFAULT_PAGE_SIZE = 10; // default number of items per page

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
            System.out.println("1. View Products (Paginated)");
            System.out.println("2. Search Products");
            System.out.println("3. View Product Sales Analysis");
            System.out.println("4. Add New Product");
            System.out.println("5. Modify Existing Product");
            System.out.println("6. Remove Product");
            System.out.println("7. Return to Main Menu");
            System.out.print("Enter your choice: ");
            
            try {
                int choice = scanner.nextInt();
                scanner.nextLine(); // consume newline

                switch (choice) {
                    case 1 -> viewProductsPaginated(scanner); // view products with pagination
                    case 2 -> searchProducts(scanner); // search products with criteria
                    case 3 -> viewProductSalesAnalysis(); // view product sales analysis
                    case 4 -> addNewProduct(scanner); // add a new product
                    case 5 -> modifyProduct(scanner); // modify an existing product
                    case 6 -> removeProduct(scanner); // remove a product
                    case 7 -> managing = false; // return to main menu
                    default -> {
                        Logger.log(Logger.WARNING, "Invalid menu choice: " + choice);
                        System.out.println("Invalid choice!");
                    }
                }
            } catch (InputMismatchException e) {
                String errorMessage = ErrorHandler.handleException(e, "reading menu choice");
                System.out.println(errorMessage);
                System.out.println("Please enter a number corresponding to the menu options.");
                scanner.nextLine(); // consume invalid input
            } catch (Exception e) {
                String errorMessage = ErrorHandler.handleException(e, "processing product management menu");
                System.out.println(errorMessage);
            }
        }
    }

    // displays products with pagination
    // @param scanner scanner for user input
    private static void viewProductsPaginated(Scanner scanner) {
        try {
            int page = 1;
            int pageSize = DEFAULT_PAGE_SIZE;
            String sortColumn = "ProductID";
            String sortDirection = "ASC";
            boolean viewing = true;
            
            while (viewing) {
                try (Connection connection = getConnection();
                     CallableStatement stmt = connection.prepareCall("{CALL GetPaginatedProducts(?, ?, ?, ?)}")) {
                    
                    // set parameters for the stored procedure
                    stmt.setInt(1, page);
                    stmt.setInt(2, pageSize);
                    stmt.setString(3, sortColumn);
                    stmt.setString(4, sortDirection);
                    
                    Logger.log(Logger.INFO, "Retrieving products page " + page + 
                              " (sort: " + sortColumn + " " + sortDirection + ")");
                    
                    boolean hasResults = stmt.execute();
                    int totalProducts = 0;
                    
                    if (hasResults) {
                        // display products
                        try (ResultSet rs = stmt.getResultSet()) {
                            System.out.println("\n--- Products (Page " + page + ") ---");
                            System.out.printf("%-10s %-30s %-10s %-10s%n", 
                                    "ID", "Name", "Price", "Quantity");
                            System.out.println("------------------------------------------------------");
                            
                            boolean hasProducts = false;
                            int count = 0;
                            
                            while (rs.next()) {
                                hasProducts = true;
                                count++;
                                
                                // format and display each product
                                System.out.printf("%-10s %-30s $%-9.2f %-10d%n",
                                        rs.getString("ProductID"),
                                        rs.getString("ItemName"),
                                        rs.getDouble("ItemPrice"),
                                        rs.getInt("ItemQuantity"));
                            }
                            
                            if (!hasProducts) {
                                System.out.println("No products found on this page.");
                            } else {
                                System.out.println("------------------------------------------------------");
                                System.out.println("Showing " + count + " products");
                            }
                        }
                        
                        // get total count for pagination
                        if (stmt.getMoreResults()) {
                            try (ResultSet countRs = stmt.getResultSet()) {
                                if (countRs.next()) {
                                    totalProducts = countRs.getInt("TotalProducts");
                                    int totalPages = (int) Math.ceil((double) totalProducts / pageSize);
                                    System.out.println("Page " + page + " of " + totalPages + 
                                                     " (Total products: " + totalProducts + ")");
                                }
                            }
                        }
                    }
                    
                    // pagination menu
                    System.out.println("\n--- Navigation ---");
                    System.out.println("1. Next Page");
                    System.out.println("2. Previous Page");
                    System.out.println("3. Change Sort Order");
                    System.out.println("4. Return to Product Menu");
                    System.out.print("Enter your choice: ");
                    
                    int navChoice = scanner.nextInt();
                    scanner.nextLine(); // consume newline
                    
                    switch (navChoice) {
                        case 1 -> {
                            // calculate total pages
                            int totalPages = (int) Math.ceil((double) totalProducts / pageSize);
                            if (page < totalPages) {
                                page++;
                            } else {
                                System.out.println("Already on the last page.");
                            }
                        }
                        case 2 -> {
                            if (page > 1) {
                                page--;
                            } else {
                                System.out.println("Already on the first page.");
                            }
                        }
                        case 3 -> {
                            // change sort order
                            System.out.println("\n--- Sort By ---");
                            System.out.println("1. Product ID");
                            System.out.println("2. Product Name");
                            System.out.println("3. Price");
                            System.out.println("4. Quantity");
                            System.out.print("Enter your choice: ");
                            
                            int sortChoice = scanner.nextInt();
                            scanner.nextLine(); // consume newline
                            
                            switch (sortChoice) {
                                case 1 -> sortColumn = "ProductID";
                                case 2 -> sortColumn = "ItemName";
                                case 3 -> sortColumn = "ItemPrice";
                                case 4 -> sortColumn = "ItemQuantity";
                                default -> System.out.println("Invalid choice. Using default sort.");
                            }
                            
                            System.out.println("\n--- Sort Direction ---");
                            System.out.println("1. Ascending");
                            System.out.println("2. Descending");
                            System.out.print("Enter your choice: ");
                            
                            int dirChoice = scanner.nextInt();
                            scanner.nextLine(); // consume newline
                            
                            sortDirection = (dirChoice == 2) ? "DESC" : "ASC";
                        }
                        case 4 -> viewing = false; // return to product menu
                        default -> System.out.println("Invalid choice!");
                    }
                }
            }
        } catch (InputMismatchException e) {
            String errorMessage = ErrorHandler.handleException(e, "reading pagination choice");
            System.err.println(errorMessage);
            System.out.println("Please enter a number corresponding to the menu options.");
            scanner.nextLine(); // consume invalid input
        } catch (SQLException e) {
            String errorMessage = ErrorHandler.handleSQLException(e, "retrieving paginated products");
            System.err.println(errorMessage);
        } catch (Exception e) {
            String errorMessage = ErrorHandler.handleException(e, "viewing paginated products");
            System.err.println(errorMessage);
        }
    }
    
    // search products with multiple criteria
    // @param scanner scanner for user input
    private static void searchProducts(Scanner scanner) {
        try {
            // get search criteria
            System.out.println("\n--- Search Products ---");
            
            System.out.print("Enter product name (or press Enter to skip): ");
            String nameSearch = scanner.nextLine().trim();
            if (nameSearch.isEmpty()) {
                nameSearch = null;
            }
            
            Float minPrice = null;
            Float maxPrice = null;
            Boolean inStockOnly = false;
            
            System.out.print("Enter minimum price (or press Enter to skip): ");
            String minPriceStr = scanner.nextLine().trim();
            if (!minPriceStr.isEmpty()) {
                try {
                    minPrice = Float.parseFloat(minPriceStr);
                    if (minPrice < 0) {
                        throw new ValidationException("Minimum price cannot be negative", "Minimum Price");
                    }
                } catch (NumberFormatException e) {
                    throw new ValidationException("Invalid minimum price format", "Minimum Price");
                }
            }
            
            System.out.print("Enter maximum price (or press Enter to skip): ");
            String maxPriceStr = scanner.nextLine().trim();
            if (!maxPriceStr.isEmpty()) {
                try {
                    maxPrice = Float.parseFloat(maxPriceStr);
                    if (maxPrice < 0) {
                        throw new ValidationException("Maximum price cannot be negative", "Maximum Price");
                    }
                    
                    if (minPrice != null && maxPrice < minPrice) {
                        throw new ValidationException("Maximum price cannot be less than minimum price", "Maximum Price");
                    }
                } catch (NumberFormatException e) {
                    throw new ValidationException("Invalid maximum price format", "Maximum Price");
                }
            }
            
            System.out.print("Show only in-stock items? (y/n): ");
            String inStockStr = scanner.nextLine().trim().toLowerCase();
            inStockOnly = inStockStr.startsWith("y");
            
            // log search criteria
            Logger.log(Logger.INFO, "Searching products with criteria - Name: " + 
                      (nameSearch != null ? nameSearch : "any") + 
                      ", Min Price: " + (minPrice != null ? minPrice : "any") + 
                      ", Max Price: " + (maxPrice != null ? maxPrice : "any") + 
                      ", In Stock Only: " + inStockOnly);
            
            // execute search
            try (Connection connection = getConnection();
                 CallableStatement stmt = connection.prepareCall("{CALL SearchProducts(?, ?, ?, ?)}")) {
                
                // set parameters for the stored procedure
                stmt.setString(1, nameSearch);
                
                if (minPrice != null) {
                    stmt.setFloat(2, minPrice);
                } else {
                    stmt.setNull(2, Types.FLOAT);
                }
                
                if (maxPrice != null) {
                    stmt.setFloat(3, maxPrice);
                } else {
                    stmt.setNull(3, Types.FLOAT);
                }
                
                stmt.setBoolean(4, inStockOnly);
                
                // execute and display results
                try (ResultSet rs = stmt.executeQuery()) {
                    System.out.println("\n--- Search Results ---");
                    System.out.printf("%-10s %-30s %-10s %-10s%n", 
                            "ID", "Name", "Price", "Quantity");
                    System.out.println("------------------------------------------------------");
                    
                    boolean hasResults = false;
                    int count = 0;
                    
                    while (rs.next()) {
                        hasResults = true;
                        count++;
                        
                        // format and display each product
                        System.out.printf("%-10s %-30s $%-9.2f %-10d%n",
                                rs.getString("ProductID"),
                                rs.getString("ItemName"),
                                rs.getDouble("ItemPrice"),
                                rs.getInt("ItemQuantity"));
                    }
                    
                    if (!hasResults) {
                        System.out.println("No products found matching your criteria.");
                    } else {
                        System.out.println("------------------------------------------------------");
                        System.out.println("Found " + count + " products matching your criteria.");
                    }
                }
            }
        } catch (ValidationException e) {
            String errorMessage = ErrorHandler.handleValidationException(e, e.getField());
            System.err.println(errorMessage);
        } catch (SQLException e) {
            String errorMessage = ErrorHandler.handleSQLException(e, "searching products");
            System.err.println(errorMessage);
        } catch (Exception e) {
            String errorMessage = ErrorHandler.handleException(e, "searching products");
            System.err.println(errorMessage);
        }
    }
    
    // view product sales analysis
    private static void viewProductSalesAnalysis() {
        try {
            Logger.log(Logger.INFO, "Viewing product sales analysis");
            
            // query to get product sales analysis
            String query = "SELECT * FROM ProductSalesAnalysis";
            
            try (Connection connection = getConnection();
                 PreparedStatement stmt = connection.prepareStatement(query);
                 ResultSet rs = stmt.executeQuery()) {
                
                System.out.println("\n--- Product Sales Analysis ---");
                System.out.printf("%-10s %-25s %-10s %-10s %-10s %-15s %-15s%n", 
                        "ID", "Name", "Price", "Stock", "Times Sold", "Qty Sold", "Revenue");
                System.out.println("---------------------------------------------------------------------------------");
                
                boolean hasData = false;
                
                while (rs.next()) {
                    hasData = true;
                    
                    // format and display each product with sales data
                    System.out.printf("%-10s %-25s $%-9.2f %-10d %-10d %-15d $%-14.2f%n",
                            rs.getString("ProductID"),
                            rs.getString("ItemName"),
                            rs.getDouble("ItemPrice"),
                            rs.getInt("CurrentStock"),
                            rs.getInt("TimesSold"),
                            rs.getInt("TotalQuantitySold"),
                            rs.getDouble("TotalRevenue"));
                }
                
                if (!hasData) {
                    System.out.println("No sales data available.");
                } else {
                    System.out.println("---------------------------------------------------------------------------------");
                }
            }
        } catch (SQLException e) {
            String errorMessage = ErrorHandler.handleSQLException(e, "retrieving product sales analysis");
            System.err.println(errorMessage);
        } catch (Exception e) {
            String errorMessage = ErrorHandler.handleException(e, "viewing product sales analysis");
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
                // check if product ID already exists - using prepared statement for security
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

                // insert the new product - using prepared statement for security
                String insertQuery = "INSERT INTO Products (ProductID, ItemName, ItemPrice, ItemQuantity) VALUES (?, ?, ?, ?)";
                try (PreparedStatement insertStatement = connection.prepareStatement(insertQuery)) {
                    // set parameters for the insert query
                    insertStatement.setString(1, productId);
                    insertStatement.setString(2, name);
                    insertStatement.setDouble(3, price);
                    insertStatement.setInt(4, quantity);
                    
                    // execute the insert
                    int rowsAffected = insertStatement.executeUpdate();
                    
                    if (rowsAffected > 0) {
                        Logger.log(Logger.INFO, "New product added: " + productId + " - " + name);
                        System.out.println("Product added successfully!");
                    } else {
                        Logger.log(Logger.WARNING, "Failed to add product: " + productId);
                        System.out.println("Failed to add product. Please try again.");
                    }
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
                // check if product exists - using prepared statement for security
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
                        
                        // display current product details
                        System.out.println("\nCurrent Product Details:");
                        System.out.println("ID: " + productId);
                        System.out.println("Name: " + currentName);
                        System.out.println("Price: $" + currentPrice);
                        System.out.println("Quantity: " + currentQuantity);
                        
                        // display modification options
                        System.out.println("\nWhat would you like to modify?");
                        System.out.println("1. Name");
                        System.out.println("2. Price");
                        System.out.println("3. Quantity");
                        System.out.println("4. All Fields");
                        System.out.print("Enter your choice: ");
                        int modifyChoice = scanner.nextInt();

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
                                
                                // update query with prepared statement for security
                                String updateQuery = "UPDATE Products SET ItemName = ? WHERE ProductID = ?";
                                try (PreparedStatement updateStatement = connection.prepareStatement(updateQuery)) {
                                    updateStatement.setString(1, newName);
                                    updateStatement.setString(2, productId);
                                    int rowsAffected = updateStatement.executeUpdate();
                                    
                                    if (rowsAffected > 0) {
                                        Logger.log(Logger.INFO, "Product name updated: " + productId + 
                                                  " from '" + currentName + "' to '" + newName + "'");
                                        System.out.println("Product name updated successfully!");
                                    } else {
                                        Logger.log(Logger.WARNING, "Failed to update product name: " + productId);
                                        System.out.println("Failed to update product name. Please try again.");
                                    }
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
                                
                                // update query with prepared statement for security
                                String updateQuery = "UPDATE Products SET ItemPrice = ? WHERE ProductID = ?";
                                try (PreparedStatement updateStatement = connection.prepareStatement(updateQuery)) {
                                    updateStatement.setDouble(1, newPrice);
                                    updateStatement.setString(2, productId);
                                    int rowsAffected = updateStatement.executeUpdate();
                                    
                                    if (rowsAffected > 0) {
                                        Logger.log(Logger.INFO, "Product price updated: " + productId + 
                                                  " from $" + currentPrice + " to $" + newPrice);
                                        System.out.println("Product price updated successfully!");
                                    } else {
                                        Logger.log(Logger.WARNING, "Failed to update product price: " + productId);
                                        System.out.println("Failed to update product price. Please try again.");
                                    }
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
                                
                                // update query with prepared statement for security
                                String updateQuery = "UPDATE Products SET ItemQuantity = ? WHERE ProductID = ?";
                                try (PreparedStatement updateStatement = connection.prepareStatement(updateQuery)) {
                                    updateStatement.setInt(1, newQuantity);
                                    updateStatement.setString(2, productId);
                                    int rowsAffected = updateStatement.executeUpdate();
                                    
                                    if (rowsAffected > 0) {
                                        Logger.log(Logger.INFO, "Product quantity updated: " + productId + 
                                                  " from " + currentQuantity + " to " + newQuantity);
                                        System.out.println("Product quantity updated successfully!");
                                    } else {
                                        Logger.log(Logger.WARNING, "Failed to update product quantity: " + productId);
                                        System.out.println("Failed to update product quantity. Please try again.");
                                    }
                                }
                            }
                            case 4 -> {
                                // modify all fields
                                System.out.print("Enter new Product Name: ");
                                scanner.nextLine(); // consume newline character
                                String newName = scanner.nextLine();
                                
                                // validate new name
                                if (newName == null || newName.trim().isEmpty()) {
                                    throw new ValidationException("Product name cannot be empty", "Product Name");
                                }
                                
                                System.out.print("Enter new Product Price: ");
                                double newPrice = scanner.nextDouble();
                                
                                // validate new price
                                if (newPrice <= 0) {
                                    throw new ValidationException("Price must be greater than zero", "Price");
                                }
                                
                                System.out.print("Enter new Product Quantity: ");
                                int newQuantity = scanner.nextInt();
                                
                                // validate new quantity
                                if (newQuantity < 0) {
                                    throw new ValidationException("Quantity cannot be negative", "Quantity");
                                }
                                
                                // update query with prepared statement for security
                                String updateQuery = "UPDATE Products SET ItemName = ?, ItemPrice = ?, ItemQuantity = ? WHERE ProductID = ?";
                                try (PreparedStatement updateStatement = connection.prepareStatement(updateQuery)) {
                                    updateStatement.setString(1, newName);
                                    updateStatement.setDouble(2, newPrice);
                                    updateStatement.setInt(3, newQuantity);
                                    updateStatement.setString(4, productId);
                                    int rowsAffected = updateStatement.executeUpdate();
                                    
                                    if (rowsAffected > 0) {
                                        Logger.log(Logger.INFO, "Product updated: " + productId + 
                                                  " - Name: '" + currentName + "' to '" + newName + "'" +
                                                  ", Price: $" + currentPrice + " to $" + newPrice +
                                                  ", Quantity: " + currentQuantity + " to " + newQuantity);
                                        System.out.println("Product updated successfully!");
                                    } else {
                                        Logger.log(Logger.WARNING, "Failed to update product: " + productId);
                                        System.out.println("Failed to update product. Please try again.");
                                    }
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
                // check if product exists - using prepared statement for security
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
                
                // check if product is used in any transactions - using prepared statement for security
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

                // get product name for logging - using prepared statement for security
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
                
                // delete the product - using prepared statement for security
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
