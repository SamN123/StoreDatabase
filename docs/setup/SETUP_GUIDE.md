# Complete Database Setup Guide

This guide provides step-by-step instructions to set up the entire database with all necessary objects.

## Prerequisites

- MySQL installed and running
- Java Runtime Environment (JRE) 8 or higher
- Access to the database with appropriate permissions

## Java-based Setup (Recommended)

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

This Java-based setup uses JDBC (Java Database Connectivity) and the MySQL command-line client to:

1. Create the database schema
   - Connect to MySQL using JDBC driver
   - Create the StoreDB database if it doesn't exist
   - Automatically find the MySQL command-line client in common locations
   - Execute SQL scripts using the MySQL command-line client
   - Create the Persons, Products, and Purchase tables
   - Create triggers for phone validation and inventory updates
   - Create the MakePurchase stored procedure
   - Create the RecentPurchases view
   - Insert sample data for testing

2. Add authentication support
   - Establish secure JDBC connection to the database
   - Execute SQL scripts using the MySQL command-line client
   - Add password, salt, and role columns to the Persons table
   - Create a default admin user
   - Update existing users to have the USER role

3. Create missing database objects
   - Execute SQL scripts using the MySQL command-line client
   - Create SearchProducts stored procedure
   - Create GetCustomerPurchaseHistory stored procedure
   - Create CustomerPurchaseSummary view
   - Create FindCustomerIDByEmail stored procedure
   - Create ListAllCustomers stored procedure
   - Create FindMyCustomerID stored procedure

4. Verify the setup
   - Use JDBC metadata queries to verify all objects were created
   - Display detailed information about created database objects

> **Note**: This setup utility requires the MySQL command-line client to be installed, but it will automatically find it in common locations.

## Step 4: Verify the Setup

To verify that everything is set up correctly, you can:

1. Check if all stored procedures exist:

```sql
SHOW PROCEDURE STATUS WHERE Db = 'storedb';
```

2. Check if all views exist:

```sql
SHOW FULL TABLES IN storedb WHERE Table_type = 'VIEW';
```

3. Check if the Persons table has the authentication columns:

```sql
DESCRIBE storedb.Persons;
```

## Alternative Setup Methods

### Option 1: Using MySQL Workbench or Another GUI Client

1. Open your MySQL GUI client (MySQL Workbench, DBeaver, etc.)
2. Connect to your database
3. Execute the following SQL scripts in order:
   - `sql/schema/StoreDB.sql`
   - `sql/auth/StoreDB_Auth.sql`
   - `sql/setup/SetupMissingDatabaseObjects.sql`

### Option 2: Using the Java Application

The application has been updated to automatically check for and create the necessary database objects when it starts. Simply run the application, and it will set up the required database objects if they don't exist.

## Troubleshooting

If you encounter errors like:

```
PROCEDURE storedb.FindMyCustomerID does not exist
```

It means one or more of the setup scripts hasn't been executed. Follow the steps above to ensure all scripts are run in the correct order.

If you get errors about objects already existing when running the scripts, you can drop the objects first:

```sql
DROP PROCEDURE IF EXISTS SearchProducts;
DROP PROCEDURE IF EXISTS GetCustomerPurchaseHistory;
DROP PROCEDURE IF EXISTS FindCustomerIDByEmail;
DROP PROCEDURE IF EXISTS ListAllCustomers;
DROP PROCEDURE IF EXISTS FindMyCustomerID;
DROP VIEW IF EXISTS CustomerPurchaseSummary;
```

Then run the setup scripts again.

## Using the Application

After setting up the database, you can run the application. The first time you log in:

1. Use the default admin account:
   - Email: admin@store.com
   - Set a password on first login

2. Or register a new user account

3. To find your customer ID, use the "Find My Customer ID" feature in the Transactions menu
