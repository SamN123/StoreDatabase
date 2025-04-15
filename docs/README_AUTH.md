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
    └── CustomerHistory.java       // customer history with permission checks
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

1. run the `StoreDB_Auth.sql` script to update the database schema:
   ```
   mysql -u root -p < StoreDB_Auth.sql
   ```

2. the first time you run the application after the database update, you'll need to set up an admin account:
   - register a new user
   - connect to the database and update the user's role to 'ADMIN':
     ```sql
     UPDATE Persons SET role = 'ADMIN' WHERE Email = 'admin@example.com';
     ```

3. alternatively, you can use the default admin account:
   - email: admin@store.com
   - set a password on first login

## Implementation Details

### Authentication Flow

1. when the application starts, users are presented with a login screen
2. users can either login with existing credentials or register a new account
3. passwords are hashed using SHA-256 with a unique salt for each user
4. upon successful authentication, the user's information is stored in memory for the session

### Role-Based Access Control

The application implements role-based access control through the `SecurityUtil` class:

- `SecurityUtil.hasPermission(role)`: checks if the current user has the specified role
- `SecurityUtil.hasAdminPermission()`: checks if the current user has admin privileges
- `SecurityUtil.hasUserPermission()`: checks if the user is authenticated

each functionality in the application checks for the appropriate permissions before allowing access.

## Classes Added/Modified

1. **New Classes:**
   - `src/Authentication/AuthenticationService.java`: handles user authentication and session management
   - `src/Security/SecurityUtil.java`: provides utilities for role-based access control
   - `src/Authentication/LoginScreen.java`: manages the login and registration UI

2. **Modified Classes:**
   - `src/Objects/Person.java`: added role field and related methods
   - `src/Logic/StoreDatabaseApp.java`: added authentication requirement and role-based menu options
   - `src/Logic/ManageProducts.java`: added permission checks for admin-only operations
   - `src/Logic/CompleteTransactions.java`: added permission checks for various operations
   - `src/Logic/CustomerHistory.java`: added permission checks for viewing all purchase history

## Security Considerations

1. **Password Storage**: passwords are never stored in plain text. they are hashed using SHA-256 with a unique salt for each user.

2. **Access Control**: each operation checks for the appropriate permissions before execution.

3. **Session Management**: the current user's information is stored in memory for the duration of the session.

4. **First-Time Login**: for existing users after the database update, passwords are set on first login.
