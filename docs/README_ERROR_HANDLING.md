# Error Handling and Logging Implementation

This document describes the error handling and logging implementation for the Store Database application.

## Features Implemented

1. **Error Handling**
   - comprehensive exception handling throughout the application
   - user-friendly error messages
   - specific handling for different types of exceptions (SQL, validation, etc.)
   - centralized error handling through the ErrorHandler utility class

2. **Logging**
   - application-wide logging using Java's logging API
   - different log levels (INFO, WARNING, ERROR, DEBUG)
   - logging of critical application actions (login attempts, product modifications, purchases)
   - user action logging with timestamps and user IDs

## Implementation Details

### Error Handling

The application implements a centralized error handling approach through the `ErrorHandler` utility class. This class provides methods for handling different types of exceptions and returning user-friendly error messages.

Key features of the error handling implementation:

1. **Exception Types**
   - `SQLException`: database-related errors
   - `ValidationException`: input validation errors
   - `InputMismatchException`: user input errors
   - General exceptions: all other unexpected errors

2. **User-Friendly Messages**
   - technical error details are logged but not shown to users
   - users receive clear, actionable error messages
   - different error types have different message formats

3. **Context-Aware Error Handling**
   - error messages include the context in which the error occurred
   - SQL errors are categorized by error code for more specific messages

### Logging

The application uses Java's logging API through the `Logger` utility class to log application events and errors.

Key features of the logging implementation:

1. **Log Levels**
   - `INFO`: normal application events
   - `WARNING`: potential issues or security concerns
   - `ERROR`: serious errors that affect functionality
   - `DEBUG`: detailed information for debugging

2. **Critical Actions Logged**
   - user authentication (login attempts, successful/failed logins)
   - product management (adding, modifying, removing products)
   - transactions (purchases, customer history access)
   - security events (unauthorized access attempts)

3. **Log Format**
   - timestamp
   - log level
   - message
   - additional context (user ID, action details)

4. **Log Storage**
   - logs are stored in the `logs/storedb.log` file
   - log file is created automatically if it doesn't exist

## Classes Added

1. **Utility Classes**
   - `src/Util/Logger.java`: handles logging throughout the application
   - `src/Util/ErrorHandler.java`: centralizes error handling
   - `src/Util/ValidationException.java`: custom exception for validation errors

## Classes Modified

1. **Main Application**
   - `src/Logic/StoreDatabaseApp.java`: added error handling and logging to the main application flow

2. **Business Logic**
   - `src/Logic/ManageProducts.java`: added error handling and logging for product management
   - `src/Logic/CompleteTransactions.java`: added error handling and logging for transactions
   - `src/Logic/CustomerHistory.java`: added error handling and logging for customer history

## Usage Examples

### Error Handling

```java
try {
    // validate input
    if (productId == null || productId.trim().isEmpty()) {
        throw new ValidationException("Product ID cannot be empty", "Product ID");
    }
    
    // database operations
    // ...
} catch (ValidationException e) {
    String errorMessage = ErrorHandler.handleValidationException(e, e.getField());
    System.err.println(errorMessage);
} catch (SQLException e) {
    String errorMessage = ErrorHandler.handleSQLException(e, "adding product");
    System.err.println(errorMessage);
} catch (Exception e) {
    String errorMessage = ErrorHandler.handleException(e, "adding product");
    System.err.println(errorMessage);
}
```

### Logging

```java
// log an informational message
Logger.log(Logger.INFO, "New product added: " + productId + " - " + name);

// log a warning
Logger.log(Logger.WARNING, "Attempt to add product with existing ID: " + productId);

// log an error
Logger.logException("Error adding product", exception);

// log a user action
Logger.logUserAction(currentUser.getPersonID(), "Purchase", 
                   "Processed purchase of " + quantity + " " + productName + 
                   " for customer " + customerId);
```

## Benefits

1. **Improved User Experience**
   - clear error messages help users understand and resolve issues
   - consistent error handling across the application

2. **Better Debugging and Maintenance**
   - detailed logs make it easier to identify and fix issues
   - centralized error handling simplifies code maintenance

3. **Enhanced Security**
   - logging of critical actions helps detect security issues
   - unauthorized access attempts are logged for security auditing

4. **Reliability**
   - proper exception handling prevents application crashes
   - graceful error recovery improves application stability
