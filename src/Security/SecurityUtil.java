package src.Security;

import src.Authentication.AuthenticationService;

public class SecurityUtil {
    
    // checks if the current user has permission to perform an operation
    // @param requiredRole the role required to perform the operation
    // @return true if the user has permission, false otherwise
    public static boolean hasPermission(String requiredRole) {
        // if no user is authenticated, deny access
        if (!AuthenticationService.isAuthenticated()) {
            return false;
        }
        
        // if admin role is required, check if user is admin
        if ("ADMIN".equals(requiredRole)) {
            return AuthenticationService.isAdmin();
        }
        
        // for USER role, any authenticated user has access
        return true;
    }
    
    // checks if the current user has admin permissions
    // @return true if the user has admin permissions, false otherwise
    public static boolean hasAdminPermission() {
        // calls the hasPermission method with ADMIN role
        return hasPermission("ADMIN");
    }
    
    // checks if the current user has user permissions
    // @return true if the user has user permissions, false otherwise
    public static boolean hasUserPermission() {
        // any authenticated user has user permissions
        return AuthenticationService.isAuthenticated();
    }
}
