# SQL Scripts for Store Database

This directory contains SQL scripts for setting up and maintaining the Store Database.

## Directory Structure

- **schema/**: Contains database schema definition
  - `StoreDB.sql`: Main database creation script with table definitions

- **auth/**: Contains authentication-related scripts
  - `StoreDB_Auth.sql`: Authentication schema additions

- **setup/**: Contains scripts for setting up the database
  - `SetupMissingDatabaseObjects.sql`: Main setup script that creates all necessary database objects

- **procedures/**: Contains stored procedure definitions
  - `GetCustomerPurchaseHistory.sql`: Retrieves paginated purchase history for a customer
  - `FindCustomerID.sql`: Finds a customer by email address
  - `CreateSearchProducts.sql`: Searches for products with various criteria
  - `GetPaginatedProducts.sql`: Retrieves paginated list of products with sorting options

- **views/**: Contains view definitions
  - `CreateCustomerPurchaseSummary.sql`: Provides a summary of each customer's purchase history
  - `ProductSalesAnalysis.sql`: Provides sales analysis data for products

- **queries/**: Contains optimized query definitions
  - `OptimizedQueries.sql`: Optimized queries for improved performance

## Usage

### Option 1: Using the Java Setup Utility (Recommended)

The easiest way to set up everything is to use the provided Java setup utility:

```
java -cp ".;lib/mysql-connector-j-9.1.0.jar;setup" setup_database
```

Or on Unix/macOS:
```
java -cp ".:lib/mysql-connector-j-9.1.0.jar:setup" setup_database
```

Alternatively, you can run the DatabaseSetup class directly:

```
java -cp .;lib/mysql-connector-j-9.1.0.jar src.Util.DatabaseSetup
```

Or on Unix/macOS:
```
java -cp .:lib/mysql-connector-j-9.1.0.jar src.Util.DatabaseSetup
```

This Java-based setup will:
- Connect to your MySQL database using JDBC (Java Database Connectivity)
- Automatically load the MySQL JDBC driver
- Automatically find the MySQL command-line client in common locations
- Execute SQL scripts using the MySQL command-line client
- Guide you through the setup process with clear prompts
- Ask for your MySQL connection details and credentials
- Execute all SQL scripts in the correct order
- Verify that all database objects were created successfully using JDBC metadata queries
- Provide detailed feedback on each step of the process

> **Note**: This setup utility requires the MySQL command-line client to be installed, but it will automatically find it in common locations.

> **Note**: When entering your MySQL password, the input will be hidden (no characters will appear as you type) for security reasons. This is normal behavior.

### Option 2: Using MySQL Workbench or Another GUI Client

1. Open your MySQL GUI client (MySQL Workbench, DBeaver, etc.)
2. Connect to your database
3. Execute the following SQL scripts in order:
   - `sql/schema/StoreDB.sql`
   - `sql/auth/StoreDB_Auth.sql`
   - `sql/setup/SetupMissingDatabaseObjects.sql`

For more detailed setup instructions, see:
- [Setup Guide](../docs/setup/SETUP_GUIDE.md)
- [Setup Instructions](../docs/setup/README_SETUP.md)
