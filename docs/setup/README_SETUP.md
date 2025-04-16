# Store Database Setup Instructions

This document provides instructions for setting up the missing database objects and using the "Find My Customer ID" feature.

## Prerequisites

- MySQL installed and running
- MySQL command-line client (required for the setup script) or a GUI client (like MySQL Workbench, DBeaver, etc.)
- Access to the database with appropriate permissions

> **Note**: The setup script (`scripts/setup_all.sh`) requires the MySQL command-line client to be installed. The script will automatically detect your MySQL installation. If you don't have the MySQL command-line client installed, you can use one of the alternative setup methods described below.

## Setting Up Missing Database Objects

The application requires several stored procedures and views that may not be present in your database. Follow these steps to set them up:

### Option 1: Using the Setup Script (Recommended)

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

### Option 2: Using MySQL Command Line

If you have the MySQL command line client installed, you can run the following command:

```
mysql -u root -p storedb < sql/setup/SetupMissingDatabaseObjects.sql
```

You'll be prompted for your MySQL password. This will create all the necessary database objects.

### Option 2: Using MySQL Workbench or Another GUI Client

1. Open your MySQL GUI client (MySQL Workbench, DBeaver, etc.)
2. Connect to your database
3. Open the `sql/setup/SetupMissingDatabaseObjects.sql` file
4. Execute the script

### Option 3: Using the Java Application

The application has been updated to automatically check for and create the necessary database objects when it starts. Simply run the application, and it will set up the required database objects if they don't exist.

## Finding Your Customer ID

The application now includes a feature to help you find your customer ID by email. Here's how to use it:

1. From the main menu, select "Complete Transactions"
2. In the transactions menu, select "Find My Customer ID"
3. Enter your email address
4. The system will display your customer ID and other information

## SQL Scripts Organization

The SQL scripts are organized in the following directory structure:

```
sql/
├── README.md                       # Overview of SQL scripts
├── schema/
│   └── StoreDB.sql                 # Main database creation script
├── auth/
│   └── StoreDB_Auth.sql            # Authentication schema additions
├── setup/
│   └── SetupMissingDatabaseObjects.sql  # Main setup script
├── procedures/
│   ├── GetCustomerPurchaseHistory.sql   # Customer history procedure
│   ├── FindCustomerID.sql               # Customer lookup procedure
│   └── CreateSearchProducts.sql         # Product search procedure
├── views/
│   └── CreateCustomerPurchaseSummary.sql  # Customer summary view
└── queries/
    └── OptimizedQueries.sql        # Optimized queries
```

## Database Objects Created

The following database objects are created by the setup script:

1. **Stored Procedures:**
   - `GetCustomerPurchaseHistory`: Retrieves paginated purchase history for a customer
   - `SearchProducts`: Searches for products with various criteria
   - `FindCustomerIDByEmail`: Finds a customer by email address
   - `FindMyCustomerID`: User-friendly procedure to find customer ID by email
   - `ListAllCustomers`: Lists all customers (for admin use)

2. **Views:**
   - `CustomerPurchaseSummary`: Provides a summary of each customer's purchase history

## Troubleshooting

If you encounter errors related to missing database objects, such as:

```
PROCEDURE storedb.GetCustomerPurchaseHistory does not exist
```

or

```
PROCEDURE storedb.SearchProducts does not exist
```

Run the setup script as described above to create the missing objects.

## Default Test Users

The database includes several test users. You can use any of their email addresses to find their customer IDs:

- john.doe@email.com
- jane.smith@email.com
- michael.j@email.com
- emily.w@email.com
- david.b@email.com
- t.sanders@email.com
- bill.thomas@email.com
- susie.R@email.com
- Corie.Tanner@email.com
- Will.Karl@email.com

For example, to find John Doe's customer ID, enter "john.doe@email.com" when prompted for your email address.
