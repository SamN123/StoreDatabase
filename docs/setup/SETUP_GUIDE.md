# Complete Database Setup Guide

This guide provides step-by-step instructions to set up the entire database with all necessary objects.

## Prerequisites

- MySQL installed and running
- MySQL command-line client (required for the setup script) or a GUI client (like MySQL Workbench, DBeaver, etc.)
- Access to the database with appropriate permissions

> **Note**: The setup script (`scripts/setup_all.sh`) requires the MySQL command-line client to be installed. The script will automatically detect your MySQL installation. If you don't have the MySQL command-line client installed, you can use one of the alternative setup methods described below.

## Step 1: Create the Database Schema

First, create the basic database schema with tables:

```bash
mysql -u root -p < sql/schema/StoreDB.sql
```

This will:
- Create the StoreDB database if it doesn't exist
- Create the Persons, Products, and Purchase tables
- Create triggers for phone validation and inventory updates
- Create the MakePurchase stored procedure
- Create the RecentPurchases view
- Insert sample data for testing

## Step 2: Add Authentication Support

Next, add authentication support to the database:

```bash
mysql -u root -p storedb < sql/auth/StoreDB_Auth.sql
```

This will:
- Add password, salt, and role columns to the Persons table
- Create a default admin user
- Update existing users to have the USER role

## Step 3: Create Missing Database Objects

Finally, create all the additional stored procedures and views:

```bash
mysql -u root -p storedb < sql/setup/SetupMissingDatabaseObjects.sql
```

This will create:
- SearchProducts stored procedure
- GetCustomerPurchaseHistory stored procedure
- CustomerPurchaseSummary view
- FindCustomerIDByEmail stored procedure
- ListAllCustomers stored procedure
- FindMyCustomerID stored procedure

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

## All-in-One Setup (Alternative)

### Option 1: Using the Setup Script

The easiest way to set up everything is to use the provided setup script:

```bash
./scripts/setup_all.sh
```

This script will:
- Automatically detect your MySQL installation on any platform (Windows, macOS, Linux)
- Guide you through the setup process with clear prompts
- Ask for your MySQL username and password (password input will be hidden for security)
- Execute all SQL scripts in the correct order
- Verify that all database objects were created successfully
- Provide feedback on each step of the process

> **Note**: When entering your MySQL password, the input will be hidden (no characters will appear as you type) for security reasons. This is normal behavior.

### Option 2: Manual All-in-One Command

If you prefer to manually execute all scripts in one go, you can use this command:

```bash
cat sql/schema/StoreDB.sql sql/auth/StoreDB_Auth.sql sql/setup/SetupMissingDatabaseObjects.sql | mysql -u root -p
```

This will execute all three scripts in sequence.

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
