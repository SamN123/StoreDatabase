package src.Util;

import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;

// utility class for handling errors and exceptions
public class ErrorHandler {
    
    // handle a general exception
    // @param exception the exception to handle
    // @param context the context in which the exception occurred
    // @return a user-friendly error message
    public static String handleException(Exception exception, String context) {
        // log the exception for debugging and auditing
        Logger.logException("Exception in " + context, exception);
        
        // return a user-friendly message that hides technical details
        return "An error occurred while " + context + ". Please try again later.";
    }
    
    // handle a SQL exception
    // @param exception the SQL exception to handle
    // @param context the context in which the exception occurred
    // @return a user-friendly error message
    public static String handleSQLException(SQLException exception, String context) {
        // log the exception with SQL-specific details
        Logger.logException("SQL Exception in " + context, exception);
        
        // determine the type of SQL error and return an appropriate message
        int errorCode = exception.getErrorCode(); // vendor-specific error code
        String sqlState = exception.getSQLState(); // SQL state code
        
        // handle integrity constraint violations (e.g., duplicate keys, foreign key violations)
        if (exception instanceof SQLIntegrityConstraintViolationException) {
            return "The operation could not be completed because it would violate data integrity. " +
                   "This might be due to duplicate data or missing required information.";
        }
        
        // handle connection errors (connection refused, network issues)
        if (sqlState != null && sqlState.startsWith("08")) {
            return "Could not connect to the database. Please check your connection and try again.";
        }
        
        // handle authentication errors (invalid username/password)
        if (sqlState != null && sqlState.startsWith("28")) {
            return "Database authentication failed. Please contact your administrator.";
        }
        
        // default message for other SQL errors
        return "A database error occurred while " + context + ". Please try again later.";
    }
    
    // handle an authentication exception
    // @param exception the exception to handle
    // @return a user-friendly error message
    public static String handleAuthenticationException(Exception exception) {
        // log the authentication error
        Logger.logException("Authentication error", exception);
        
        // return a user-friendly message for authentication failures
        return "Authentication failed. Please check your credentials and try again.";
    }
    
    // handle an authorization exception
    // @param exception the exception to handle
    // @param requiredRole the role required for the operation
    // @return a user-friendly error message
    public static String handleAuthorizationException(Exception exception, String requiredRole) {
        // log the authorization error
        Logger.logException("Authorization error", exception);
        
        // return a user-friendly message that includes the required role if available
        return "You do not have permission to perform this operation. " + 
               (requiredRole != null ? "Required role: " + requiredRole : "");
    }
    
    // handle an input validation exception
    // @param exception the exception to handle
    // @param field the field that failed validation
    // @return a user-friendly error message
    public static String handleValidationException(Exception exception, String field) {
        // log the validation error with the field name
        Logger.logException("Validation error for field: " + field, exception);
        
        // return a user-friendly message that includes the field name and validation error
        return "Invalid input for " + field + ": " + exception.getMessage();
    }
}
