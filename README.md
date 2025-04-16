# Store Database Management System

A Java application for managing a store database with features for product management, transactions, and customer history.

## Features

- **User Authentication**: Secure login system with role-based access control
- **Product Management**: Add, update, and remove products (admin only)
- **Transaction Processing**: Make purchases and manage customer transactions
- **Customer History**: View purchase history and customer summaries
- **Optimized Queries**: Efficient database operations with pagination
- **Multi-threading**: Background processing for improved responsiveness
- **Error Handling**: Comprehensive error handling and logging

## Project Structure

- **src/**: Java source code
  - **Logic/**: Business logic classes
  - **Objects/**: Data model classes
  - **Authentication/**: Authentication-related classes
  - **Security/**: Security utility classes
  - **Util/**: Utility classes

- **sql/**: SQL scripts for database setup
  - **schema/**: Database schema definition
  - **auth/**: Authentication-related scripts
  - **setup/**: Database setup scripts
  - **procedures/**: Stored procedure definitions
  - **views/**: View definitions
  - **queries/**: Optimized query definitions

- **docs/**: Documentation
  - **README_AUTH.md**: Authentication system documentation
  - **README_ERROR_HANDLING.md**: Error handling documentation
  - **README_MULTITHREADING.md**: Multi-threading documentation
  - **README_OPTIMIZED_QUERIES.md**: Query optimization documentation
  - **setup/**: Setup-related documentation
    - **README_SETUP.md**: Detailed setup instructions
    - **SETUP_GUIDE.md**: Comprehensive setup guide

- **scripts/**: Utility scripts
  - **setup_all.sh**: Database setup script

## Setup Instructions

For a complete setup of the database and application:

1. **Quick Setup**: Use the provided setup script:
   
   ```bash
   ./scripts/setup_all.sh
   ```
   
   This script will:
   - Automatically detect your MySQL installation
   - Guide you through setting up the entire database with all necessary objects
   - Work on any platform (Windows, macOS, Linux)

2. **Detailed Setup**: See the following documentation:
   - [Setup Guide](docs/setup/SETUP_GUIDE.md) - Comprehensive step-by-step setup guide
   - [Setup Instructions](docs/setup/README_SETUP.md) - Detailed setup instructions and troubleshooting

## Database Schema

The application uses a MySQL database with the following main tables:
- **Persons**: Customer and user information
- **Products**: Product catalog
- **Purchase**: Transaction records

## Getting Started

1. Ensure you have Java and MySQL installed
2. Set up the database using the SQL scripts in the `sql/setup` directory
3. Run the application using your IDE or command line
4. Log in with the default admin account or register a new user
5. Use the menu options to navigate the application

## Documentation

- [Authentication System](docs/README_AUTH.md)
- [Error Handling](docs/README_ERROR_HANDLING.md)
- [Multi-threading](docs/README_MULTITHREADING.md)
- [Optimized Queries](docs/README_OPTIMIZED_QUERIES.md)
- [Setup Instructions](docs/setup/README_SETUP.md)
- [Setup Guide](docs/setup/SETUP_GUIDE.md)
- [SQL Scripts](sql/README.md)
