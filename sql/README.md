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

### Option 2: Manual Setup

To set up all database objects at once, use the main setup script:

```
mysql -u root -p storedb < sql/setup/SetupMissingDatabaseObjects.sql
```

Or you can run individual scripts as needed.

For more detailed setup instructions, see:
- [Setup Guide](../docs/setup/SETUP_GUIDE.md)
- [Setup Instructions](../docs/setup/README_SETUP.md)
