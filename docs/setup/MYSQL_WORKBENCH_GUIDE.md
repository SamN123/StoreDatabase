# Setting Up the Database with MySQL Workbench

This guide provides step-by-step instructions for setting up the database using MySQL Workbench instead of the command-line client.

## Prerequisites

- MySQL Server installed and running
- MySQL Workbench installed
- Access to the database with appropriate permissions

## Step 1: Connect to Your MySQL Server

1. Open MySQL Workbench
2. Click on your existing connection to your MySQL Server, or create a new one:
   - Click the "+" button next to "MySQL Connections"
   - Enter a connection name (e.g., "Local MySQL")
   - Enter the hostname (usually "localhost" or "127.0.0.1")
   - Enter the port (default is 3306)
   - Enter your username (usually "root")
   - Click "Test Connection" to verify it works
   - Enter your password when prompted
   - Click "OK" to save the connection
3. Click on your connection to open it

## Step 2: Create the Database Schema

1. In MySQL Workbench, click on "File" > "Open SQL Script"
2. Navigate to the project directory and open `sql/schema/StoreDB.sql`
3. Click the lightning bolt icon (⚡) or press Ctrl+Shift+Enter (Cmd+Shift+Enter on Mac) to execute the script
4. Wait for the script to complete execution
5. Verify in the "Schemas" panel that the "storedb" database has been created

## Step 3: Add Authentication Support

1. Click on "File" > "Open SQL Script"
2. Navigate to the project directory and open `sql/auth/StoreDB_Auth.sql`
3. Make sure "storedb" is selected in the schema dropdown above the query editor
4. Click the lightning bolt icon (⚡) to execute the script
5. Wait for the script to complete execution

## Step 4: Create Missing Database Objects

1. Click on "File" > "Open SQL Script"
2. Navigate to the project directory and open `sql/setup/SetupMissingDatabaseObjects.sql`
3. Make sure "storedb" is selected in the schema dropdown above the query editor
4. Click the lightning bolt icon (⚡) to execute the script
5. Wait for the script to complete execution

## Step 5: Verify the Setup

1. To check if all stored procedures exist:
   - In a new query tab, enter: `SHOW PROCEDURE STATUS WHERE Db = 'storedb';`
   - Execute the query
   - You should see all the stored procedures listed

2. To check if all views exist:
   - In a new query tab, enter: `SHOW FULL TABLES IN storedb WHERE Table_type = 'VIEW';`
   - Execute the query
   - You should see all the views listed

3. To check if the Persons table has the authentication columns:
   - In a new query tab, enter: `DESCRIBE storedb.Persons;`
   - Execute the query
   - Verify that the `password`, `salt`, and `role` columns are present

## Troubleshooting

### Error: Table already exists

If you get errors about objects already existing when running the scripts, you can drop the objects first:

1. In a new query tab, enter:
   ```sql
   DROP PROCEDURE IF EXISTS SearchProducts;
   DROP PROCEDURE IF EXISTS GetCustomerPurchaseHistory;
   DROP PROCEDURE IF EXISTS FindCustomerIDByEmail;
   DROP PROCEDURE IF EXISTS ListAllCustomers;
   DROP PROCEDURE IF EXISTS FindMyCustomerID;
   DROP VIEW IF EXISTS CustomerPurchaseSummary;
   ```
2. Execute the query
3. Then run the setup scripts again

### Error: Syntax error

If you encounter syntax errors, make sure:
1. You're using the correct MySQL version (8.0 or higher recommended)
2. You've selected "storedb" as the active schema
3. You're executing the entire script, not just a portion of it

## Next Steps

After setting up the database, you can run the application. The first time you log in:

1. Use the default admin account:
   - Email: admin@store.com
   - Set a password on first login

2. Or register a new user account

3. To find your customer ID, use the "Find My Customer ID" feature in the Transactions menu
