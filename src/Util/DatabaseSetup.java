package src.Util;

import java.io.BufferedReader;
import java.io.Console;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;

// database setup utility
// 
// this class provides functionality to set up the database with all necessary objects.
// it replaces the setup_all.sh script with a java implementation.
public class DatabaseSetup {
    
    private static final String SCHEMA_SCRIPT = "sql/schema/StoreDB.sql";
    private static final String AUTH_SCRIPT = "sql/auth/StoreDB_Auth.sql";
    private static final String SETUP_SCRIPT = "sql/setup/SetupMissingDatabaseObjects.sql";
    
    private String jdbcUrl;
    private String username;
    private String password;
    
// main method to run the database setup
public static void main(String[] args) {
    // Load the JDBC driver
    try {
        // Explicitly load the MySQL JDBC driver
        Class.forName("com.mysql.cj.jdbc.Driver");
        System.out.println("MySQL JDBC Driver loaded successfully");
    } catch (ClassNotFoundException e) {
        System.err.println("Error: MySQL JDBC Driver not found!");
        System.err.println("Make sure mysql-connector-j-9.1.0.jar is in your classpath");
        e.printStackTrace();
        return;
    }
    
    DatabaseSetup setup = new DatabaseSetup();
    setup.run();
}
    
    // run the database setup process
    public void run() {
        displayBanner();
        
        // Get MySQL credentials
        promptForCredentials();
        
        try {
            // Step 1: Create the database schema
            System.out.println("\nStep 1: Creating database schema...");
            executeScript(SCHEMA_SCRIPT, null);
            
            // Step 2: Add authentication support
            System.out.println("\nStep 2: Adding authentication support...");
            executeScript(AUTH_SCRIPT, "storedb");
            
            // Step 3: Create missing database objects
            System.out.println("\nStep 3: Creating missing database objects...");
            executeScript(SETUP_SCRIPT, "storedb");
            
            // Step 4: Verify the setup
            System.out.println("\nStep 4: Verifying the setup...");
            verifySetup();
            
            // Display success message
            displaySuccessMessage();
            
        } catch (Exception e) {
            System.err.println("Error setting up database: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // display the banner
    private void displayBanner() {
        System.out.println("=====================================================");
        System.out.println("  Store Database Complete Setup Script");
        System.out.println("=====================================================");
        System.out.println();
    }
    
    // prompt for mysql credentials
    private void promptForCredentials() {
        Scanner scanner = new Scanner(System.in);
        
        // Get hostname
        System.out.print("Enter MySQL hostname [localhost]: ");
        String hostname = scanner.nextLine().trim();
        if (hostname.isEmpty()) {
            hostname = "localhost";
        }
        
        // Get port
        System.out.print("Enter MySQL port [3306]: ");
        String portStr = scanner.nextLine().trim();
        String port = portStr.isEmpty() ? "3306" : portStr;
        
        // Get username
        System.out.print("Enter MySQL username [root]: ");
        username = scanner.nextLine().trim();
        if (username.isEmpty()) {
            username = "root";
        }
        
        // Get password
        Console console = System.console();
        if (console != null) {
            // Use console for password input (hides input)
            char[] passwordArray = console.readPassword("Enter MySQL password (input will be hidden): ");
            password = new String(passwordArray);
            System.out.println("Password received.");
        } else {
            // Fallback if console is not available
            System.out.print("Enter MySQL password: ");
            password = scanner.nextLine();
        }
        
        // Construct JDBC URL with additional parameters for better compatibility
        jdbcUrl = "jdbc:mysql://" + hostname + ":" + port + 
                  "/?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
        
        System.out.println("Using JDBC URL: " + jdbcUrl);
    }
    
    // execute an sql script
    // 
    // scriptPath: the path to the sql script
    // database: the database name (can be null for scripts that create the database)
    // throws IOException if there's an error reading the script
    // throws SQLException if there's an error executing the script
    private void executeScript(String scriptPath, String database) throws IOException, SQLException {
        System.out.println("Executing " + scriptPath + "...");
        
        try {
            // Parse the JDBC URL to extract hostname and port
            String hostname = "localhost";  // Default hostname
            String port = "3306";           // Default port
            
            // Extract hostname and port from JDBC URL if possible
            if (jdbcUrl.contains("//") && jdbcUrl.contains(":") && jdbcUrl.contains("/")) {
                try {
                    int hostStart = jdbcUrl.indexOf("//") + 2;
                    int hostEnd = jdbcUrl.indexOf(":", hostStart);
                    if (hostEnd > hostStart) {
                        hostname = jdbcUrl.substring(hostStart, hostEnd);
                    }
                    
                    int portStart = hostEnd + 1;
                    int portEnd = jdbcUrl.indexOf("/", portStart);
                    if (portEnd > portStart) {
                        port = jdbcUrl.substring(portStart, portEnd);
                    }
                } catch (Exception e) {
                    System.err.println("Warning: Could not parse JDBC URL. Using default hostname and port.");
                }
            }
            
            // Find MySQL client executable
            String mysqlPath = findMySQLClient();
            if (mysqlPath == null) {
                throw new IOException("MySQL client not found. Please ensure MySQL is installed and in your PATH.");
            }
            
            System.out.println("Found MySQL client at: " + mysqlPath);
            
            // Create the MySQL command
            String command;
            if (database != null) {
                command = String.format("%s -u%s -p%s -h%s --port=%s %s < %s",
                    mysqlPath, username, password, hostname, port, database, scriptPath);
            } else {
                command = String.format("%s -u%s -p%s -h%s --port=%s < %s",
                    mysqlPath, username, password, hostname, port, scriptPath);
            }
            
            System.out.println("Executing MySQL command...");
            
            // Execute the command
            Process process;
            if (System.getProperty("os.name").toLowerCase().contains("windows")) {
                // On Windows, we need to use cmd /c to execute the command
                process = Runtime.getRuntime().exec(new String[]{"cmd", "/c", command});
            } else {
                // On Unix-like systems, we can use /bin/sh -c
                process = Runtime.getRuntime().exec(new String[]{"/bin/sh", "-c", command});
            }
            
            // Read the output
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
            
            // Read the error output
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            StringBuilder errorOutput = new StringBuilder();
            while ((line = errorReader.readLine()) != null) {
                errorOutput.append(line).append("\n");
            }
            
            // Wait for the process to complete
            int exitCode = process.waitFor();
            
            if (exitCode == 0) {
                System.out.println("✓ Successfully executed " + scriptPath);
            } else {
                System.err.println("Error executing script: " + errorOutput.toString());
                throw new SQLException("Error executing script: Exit code " + exitCode);
            }
        } catch (InterruptedException e) {
            System.err.println("Error executing script: " + e.getMessage());
            throw new SQLException("Error executing script: " + e.getMessage());
        }
    }
    
    // find the MySQL client executable
    private String findMySQLClient() {
        // Common locations for MySQL client
        String[] mysqlLocations = {
            "mysql",                                  // If MySQL is in PATH
            "/usr/bin/mysql",                         // Common Linux location
            "/usr/local/bin/mysql",                   // Common macOS location
            "/usr/local/mysql/bin/mysql",             // Official MySQL on macOS
            "/opt/homebrew/bin/mysql",                // Homebrew on Apple Silicon
            "/opt/homebrew/mysql/bin/mysql",          // Homebrew MySQL on macOS
            "/opt/homebrew/Cellar/mysql-client/8.0.33/bin/mysql",  // Homebrew MySQL client
            "/opt/homebrew/Cellar/mysql-client/8.1.0/bin/mysql",   // Newer Homebrew MySQL client
            "/usr/local/mysql-8.0/bin/mysql",         // Official MySQL with version
            "C:/Program Files/MySQL/MySQL Server 8.0/bin/mysql.exe",  // Windows
            "C:/xampp/mysql/bin/mysql.exe"            // XAMPP on Windows
        };
        
        // Try each location
        for (String location : mysqlLocations) {
            try {
                // Check if the file exists and is executable
                File file = new File(location);
                if (file.exists() && file.canExecute()) {
                    return location;
                }
                
                // If the location is just "mysql", try to execute it to see if it's in PATH
                if (location.equals("mysql")) {
                    Process process;
                    if (System.getProperty("os.name").toLowerCase().contains("windows")) {
                        process = Runtime.getRuntime().exec(new String[]{"where", "mysql"});
                    } else {
                        process = Runtime.getRuntime().exec(new String[]{"which", "mysql"});
                    }
                    
                    int exitCode = process.waitFor();
                    if (exitCode == 0) {
                        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                        String path = reader.readLine();
                        if (path != null && !path.isEmpty()) {
                            return path;
                        }
                    }
                }
            } catch (Exception e) {
                // Ignore and try the next location
            }
        }
        
        // MySQL client not found
        return null;
    }
    
    // process script to handle DELIMITER blocks
    private String processScript(String script) {
        System.out.println("Processing script to handle DELIMITER blocks...");
        
        // Replace DELIMITER statements
        script = script.replaceAll("(?i)DELIMITER\\s+//", "");
        script = script.replaceAll("(?i)DELIMITER\\s+;", "");
        
        // Handle CREATE TRIGGER statements
        StringBuilder processedScript = new StringBuilder();
        String[] lines = script.split("\n");
        boolean inTrigger = false;
        StringBuilder triggerStatement = new StringBuilder();
        
        for (String line : lines) {
            line = line.trim();
            
            // Skip empty lines and comments
            if (line.isEmpty() || line.startsWith("--")) {
                continue;
            }
            
            // Check if this is the start of a trigger
            if (line.toUpperCase().contains("CREATE TRIGGER")) {
                inTrigger = true;
                triggerStatement = new StringBuilder();
                triggerStatement.append(line).append("\n");
            } 
            // Check if this is the end of a trigger
            else if (inTrigger && line.toUpperCase().contains("END")) {
                triggerStatement.append(line).append("\n");
                
                // Add the complete trigger statement
                processedScript.append("DELIMITER //\n");
                processedScript.append(triggerStatement.toString());
                processedScript.append("//\n");
                processedScript.append("DELIMITER ;\n");
                
                inTrigger = false;
            }
            // Add line to trigger statement if in trigger
            else if (inTrigger) {
                triggerStatement.append(line).append("\n");
            }
            // Add regular statement
            else {
                processedScript.append(line).append("\n");
            }
        }
        
        System.out.println("Script processing completed.");
        return processedScript.toString();
    }
    
    // preprocess the script to handle delimiter statements
    // 
    // script: the sql script content
    // returns: the preprocessed script
    private String preprocessScript(String script) {
        System.out.println("Preprocessing SQL script...");
        
        // Split the script into individual statements based on DELIMITER blocks
        StringBuilder processedScript = new StringBuilder();
        String[] lines = script.split("\n");
        boolean inDelimiterBlock = false;
        StringBuilder currentBlock = new StringBuilder();
        String currentDelimiter = ";";
        
        for (String line : lines) {
            line = line.trim();
            
            // Skip empty lines and comments
            if (line.isEmpty() || line.startsWith("--")) {
                continue;
            }
            
            // Handle DELIMITER statements
            if (line.toUpperCase().startsWith("DELIMITER")) {
                if (inDelimiterBlock) {
                    // End the previous block
                    String blockContent = currentBlock.toString().trim();
                    if (!blockContent.isEmpty()) {
                        // Add the block with proper termination
                        processedScript.append(blockContent);
                        if (!blockContent.endsWith(";")) {
                            processedScript.append(";");
                        }
                        processedScript.append("\n");
                    }
                    currentBlock = new StringBuilder();
                }
                
                // Extract the new delimiter
                String[] parts = line.split("\\s+");
                if (parts.length > 1) {
                    currentDelimiter = parts[1];
                    inDelimiterBlock = true;
                }
                continue;
            }
            
            // Check if this line ends a delimiter block
            if (inDelimiterBlock && line.equals(currentDelimiter)) {
                // Process the block and add it to the result
                String blockContent = currentBlock.toString().trim();
                if (!blockContent.isEmpty()) {
                    // Make sure BEGIN/END blocks are complete
                    blockContent = ensureCompleteBlocks(blockContent);
                    processedScript.append(blockContent);
                    if (!blockContent.endsWith(";")) {
                        processedScript.append(";");
                    }
                    processedScript.append("\n");
                }
                
                // Reset for the next block
                currentBlock = new StringBuilder();
                inDelimiterBlock = false;
                currentDelimiter = ";";
                continue;
            }
            
            // Add the line to the current block or directly to the result
            if (inDelimiterBlock) {
                currentBlock.append(line).append("\n");
            } else {
                if (!line.endsWith(";")) {
                    line += ";";
                }
                processedScript.append(line).append("\n");
            }
        }
        
        // Add any remaining block
        if (currentBlock.length() > 0) {
            String blockContent = currentBlock.toString().trim();
            if (!blockContent.isEmpty()) {
                // Make sure BEGIN/END blocks are complete
                blockContent = ensureCompleteBlocks(blockContent);
                processedScript.append(blockContent);
                if (!blockContent.endsWith(";")) {
                    processedScript.append(";");
                }
            }
        }
        
        String result = processedScript.toString();
        System.out.println("SQL script preprocessing completed.");
        return result;
    }
    
    // ensure that BEGIN/END blocks are complete
    private String ensureCompleteBlocks(String block) {
        // Make sure IF statements have matching END IF
        int ifCount = countMatches(block.toUpperCase(), "IF ");
        int endIfCount = countMatches(block.toUpperCase(), "END IF");
        
        if (ifCount > endIfCount) {
            // Add missing END IF statements
            for (int i = 0; i < (ifCount - endIfCount); i++) {
                block += " END IF;";
            }
        }
        
        // Make sure BEGIN has matching END
        int beginCount = countMatches(block.toUpperCase(), "BEGIN");
        int endCount = countMatches(block.toUpperCase(), "END;") + countMatches(block.toUpperCase(), "END ");
        
        if (beginCount > endCount) {
            // Add missing END statements
            for (int i = 0; i < (beginCount - endCount); i++) {
                block += " END;";
            }
        }
        
        return block;
    }
    
    // count occurrences of a substring in a string
    private int countMatches(String text, String substring) {
        int count = 0;
        int index = 0;
        while ((index = text.indexOf(substring, index)) != -1) {
            count++;
            index += substring.length();
        }
        return count;
    }
    
    // verify the database setup
    // 
    // throws SQLException if there's an error verifying the setup
    private void verifySetup() throws SQLException {
        String url = jdbcUrl.replace("/?", "/storedb?");
        System.out.println("Verifying setup using JDBC URL: " + url);
        
        try (Connection connection = DriverManager.getConnection(url, username, password);
             Statement statement = connection.createStatement()) {
            
            System.out.println("JDBC connection for verification established successfully");
            
            // Check stored procedures
            System.out.println("Checking stored procedures...");
            try (ResultSet rs = statement.executeQuery("SHOW PROCEDURE STATUS WHERE Db = 'storedb'")) {
                int count = 0;
                while (rs.next()) {
                    System.out.println("  - " + rs.getString("Name"));
                    count++;
                }
                System.out.println("  Total stored procedures: " + count);
            }
            
            // Check views
            System.out.println("\nChecking views...");
            try (ResultSet rs = statement.executeQuery("SHOW FULL TABLES IN storedb WHERE Table_type = 'VIEW'")) {
                int count = 0;
                while (rs.next()) {
                    System.out.println("  - " + rs.getString(1));
                    count++;
                }
                System.out.println("  Total views: " + count);
            }
            
            // Check Persons table structure
            System.out.println("\nChecking Persons table structure...");
            try (ResultSet rs = statement.executeQuery("DESCRIBE Persons")) {
                int count = 0;
                while (rs.next()) {
                    System.out.println("  - " + rs.getString("Field") + ": " + rs.getString("Type"));
                    count++;
                }
                System.out.println("  Total columns: " + count);
            }
            
            System.out.println("\nVerification completed successfully using JDBC");
        } catch (SQLException e) {
            System.err.println("JDBC Error during verification: " + e.getMessage());
            throw e;
        }
    }
    
    // display the success message
    private void displaySuccessMessage() {
        System.out.println();
        System.out.println("=====================================================");
        System.out.println("  Database setup completed successfully!");
        System.out.println("=====================================================");
        System.out.println();
        System.out.println("You can now run the application and log in with:");
        System.out.println("  - Default admin: admin@store.com (set password on first login)");
        System.out.println("  - Or register a new user");
        System.out.println();
        System.out.println("To find your customer ID, use the 'Find My Customer ID'");
        System.out.println("feature in the Transactions menu.");
        System.out.println("=====================================================");
    }
}
