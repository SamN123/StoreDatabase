# Authentication and Role-Based Access Control Implementation

This document describes the authentication and role-based access control (RBAC) implementation for the store database application.

## Features Implemented

1. **User Authentication**
   - login system requiring users to authenticate before accessing the application
   - password hashing using SHA-256 with unique salt for each user
   - secure storage of user credentials in the database

2. **Role-Based Access Control**
   - two roles: ADMIN and USER
   - admins can:
     - manage products (add, modify, remove)
     - view all customer purchase history
     - perform administrative transaction operations
   - regular users can:
     - view products
     - complete transactions (make purchases)
     - view customer history (with restrictions to their own history)

3. **Security Utilities**
   - permission checking based on user roles
   - session management

## Folder Structure

The authentication and security code is organized into separate folders:

```
src/
├── Authentication/
│   ├── AuthenticationService.java  // handles user authentication and session management
│   └── LoginScreen.java           // manages the login and registration UI
├── Security/
│   └── SecurityUtil.java          // provides utilities for role-based access control
└── Logic/
    ├── StoreDatabaseApp.java      // main application with authentication requirement
    ├── ManageProducts.java        // product management with permission checks
    ├── CompleteTransactions.java  // transaction handling with permission checks
    ├── CustomerHistory.java       // customer history with permission checks
    ├── OptimizedManageProducts.java       // optimized product management with permission checks
    ├── OptimizedCompleteTransactions.java // optimized transaction handling with permission checks
    └── OptimizedCustomerHistory.java      // optimized customer history with permission checks
```

## Database Changes

The following changes were made to the database schema:

```sql
-- add password and role columns to the persons table
ALTER TABLE Persons
ADD COLUMN password VARCHAR(64) NOT NULL DEFAULT '',
ADD COLUMN salt VARCHAR(32) NOT NULL DEFAULT '',
ADD COLUMN role ENUM('USER', 'ADMIN') NOT NULL DEFAULT 'USER';
```

## Setup Instructions

The authentication system is now self-configuring and will automatically update the database schema when needed. no manual setup is required!

When the application starts:
1. it automatically checks if the authentication columns exist in the database
2. if they don't exist, it adds them automatically:
   - adds password, salt, and role columns to the Persons table
   - creates a default admin user
   - updates existing users to have the USER role

### Default Admin Account

After the first run, you can log in with the default admin account:
- email: admin@store.com
- password: (set this on first login)

### Manual Setup (Alternative)

If you prefer to manually set up the database schema:
1. run the `StoreDB_Auth.sql` script to update the database schema:
   ```
   mysql -u root -p < StoreDB_Auth.sql
   ```

## Implementation Details

### Authentication Flow

1. when the application starts, it checks and updates the database schema if needed
2. users are presented with a login screen
3. users can either login with existing credentials or register a new account
4. passwords are hashed using SHA-256 with a unique salt for each user
5. upon successful authentication, the user's information is stored in memory for the session

### Role-Based Access Control

The application implements role-based access control through the `SecurityUtil` class:

- `SecurityUtil.hasPermission(role)`: checks if the current user has the specified role
- `SecurityUtil.hasAdminPermission()`: checks if the current user has admin privileges
- `SecurityUtil.hasUserPermission()`: checks if the user is authenticated

Each functionality in the application checks for the appropriate permissions before allowing access.

## Classes Added/Modified

1. **New Classes:**
   - `src/Authentication/AuthenticationService.java`: handles user authentication, session management, and automatic schema updates
   - `src/Security/SecurityUtil.java`: provides utilities for role-based access control
   - `src/Authentication/LoginScreen.java`: manages the login and registration UI

2. **Modified Classes:**
   - `src/Objects/Person.java`: added role field and related methods
   - `src/Logic/StoreDatabaseApp.java`: added authentication requirement and role-based menu options
   - `src/Logic/ManageProducts.java`: added permission checks for admin-only operations
   - `src/Logic/CompleteTransactions.java`: added permission checks for various operations
   - `src/Logic/CustomerHistory.java`: added permission checks for viewing all purchase history
   - `src/Logic/OptimizedManageProducts.java`: added permission checks for admin-only operations
   - `src/Logic/OptimizedCompleteTransactions.java`: added permission checks for various operations
   - `src/Logic/OptimizedCustomerHistory.java`: added permission checks for viewing customer history with restrictions

## Security Considerations

1. **Password Storage**: passwords are never stored in plain text. they are hashed using SHA-256 with a unique salt for each user.

2. **Access Control**: each operation checks for the appropriate permissions before execution.

3. **Session Management**: the current user's information is stored in memory for the duration of the session.

4. **First-Time Login**: for existing users after the database update, passwords are set on first login.

5. **Automatic Schema Updates**: the application checks and updates the database schema automatically, ensuring all security features are properly set up.
