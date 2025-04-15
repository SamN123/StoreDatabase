package src.Authentication;

import src.Objects.Person;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Base64;

public class AuthenticationService {
    private static String dbUrl; // database url
    private static String dbUser; // database username
    private static String dbPassword; // database password
    private static Person currentUser = null; // currently authenticated user

    // sets the database connection information
    // @param url database url
    // @param user database username
    // @param password database password
    public static void setConnectionInfo(String url, String user, String password) {
        dbUrl = url;
        dbUser = user;
        dbPassword = password;
    }

    // gets a database connection
    // @return a connection to the database
    // @throws SQLException if a database error occurs
    private static Connection getConnection() throws SQLException {
        return java.sql.DriverManager.getConnection(dbUrl, dbUser, dbPassword);
    }

    // authenticates a user with the provided email and password
    // @param email user's email
    // @param password user's password
    // @return true if authentication is successful, false otherwise
    public static boolean authenticate(String email, String password) {
        String sql = "SELECT PersonID, FName, LName, Email, Phone, password, salt, role FROM Persons WHERE Email = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, email); // set the email parameter in the query

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String storedPassword = rs.getString("password");
                    String salt = rs.getString("salt");
                    
                    // if this is a first-time login with empty password (after DB migration)
                    if (storedPassword.isEmpty()) {
                        // set the password for the user
                        salt = generateSalt();
                        String hashedPassword = hashPassword(password, salt);
                        updatePassword(email, hashedPassword, salt);
                        storedPassword = hashedPassword;
                    }
                    
                    // verify the password by hashing the input password with the stored salt
                    String hashedInputPassword = hashPassword(password, salt);
                    if (hashedInputPassword.equals(storedPassword)) {
                        // create the user object and store it for the session
                        currentUser = new Person(
                                rs.getInt("PersonID"),
                                rs.getString("FName"),
                                rs.getString("LName"),
                                rs.getString("Phone"),
                                rs.getString("Email"),
                                rs.getString("role")
                        );
                        return true;
                    }
                }
            }
        } catch (SQLException | NoSuchAlgorithmException e) {
            System.err.println("Authentication error: " + e.getMessage());
        }
        return false;
    }

    // registers a new user with the provided information
    // @param firstName first name
    // @param lastName last name
    // @param email email
    // @param phone phone number
    // @param password password
    // @return true if registration is successful, false otherwise
    public static boolean register(String firstName, String lastName, String email, String phone, String password) {
        try {
            // check if email already exists
            if (emailExists(email)) {
                System.out.println("Email already exists.");
                return false;
            }

            // generate salt and hash password for secure storage
            String salt = generateSalt();
            String hashedPassword = hashPassword(password, salt);

            // insert new user with USER role by default
            String sql = "INSERT INTO Persons (FName, LName, Email, Phone, password, salt, role) VALUES (?, ?, ?, ?, ?, ?, 'USER')";
            
            try (Connection conn = getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                
                // set parameters for the insert query
                pstmt.setString(1, firstName);
                pstmt.setString(2, lastName);
                pstmt.setString(3, email);
                pstmt.setString(4, phone);
                pstmt.setString(5, hashedPassword);
                pstmt.setString(6, salt);
                
                int rowsAffected = pstmt.executeUpdate();
                return rowsAffected > 0;
            }
        } catch (SQLException | NoSuchAlgorithmException e) {
            System.err.println("Registration error: " + e.getMessage());
            return false;
        }
    }

    // updates a user's password
    // @param email user's email
    // @param hashedPassword hashed password
    // @param salt salt used for hashing
    // @throws SQLException if a database error occurs
    private static void updatePassword(String email, String hashedPassword, String salt) throws SQLException {
        String sql = "UPDATE Persons SET password = ?, salt = ? WHERE Email = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            // set parameters for the update query
            pstmt.setString(1, hashedPassword);
            pstmt.setString(2, salt);
            pstmt.setString(3, email);
            
            pstmt.executeUpdate();
        }
    }

    // checks if an email already exists in the database
    // @param email email to check
    // @return true if email exists, false otherwise
    // @throws SQLException if a database error occurs
    private static boolean emailExists(String email) throws SQLException {
        String sql = "SELECT COUNT(*) FROM Persons WHERE Email = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, email);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0; // if count > 0, email exists
                }
            }
        }
        return false;
    }

    // generates a random salt for password hashing
    // @return base64 encoded salt
    private static String generateSalt() {
        SecureRandom random = new SecureRandom(); // secure random number generator
        byte[] salt = new byte[16]; // 16 bytes (128 bits) of salt
        random.nextBytes(salt); // fill the salt array with random bytes
        return Base64.getEncoder().encodeToString(salt); // encode as base64 string
    }

    // hashes a password with SHA-256 and the provided salt
    // @param password password to hash
    // @param salt salt to use for hashing
    // @return hashed password
    // @throws NoSuchAlgorithmException if SHA-256 is not available
    private static String hashPassword(String password, String salt) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256"); // use SHA-256 algorithm
        md.update(Base64.getDecoder().decode(salt)); // add the salt to the digest
        byte[] hashedPassword = md.digest(password.getBytes(StandardCharsets.UTF_8)); // hash the password
        return Base64.getEncoder().encodeToString(hashedPassword); // encode as base64 string
    }

    // gets the currently authenticated user
    // @return the current user, or null if no user is authenticated
    public static Person getCurrentUser() {
        return currentUser;
    }

    // logs out the current user
    public static void logout() {
        currentUser = null; // clear the current user
    }

    // checks if the current user has admin role
    // @return true if the current user is an admin, false otherwise
    public static boolean isAdmin() {
        return currentUser != null && "ADMIN".equals(currentUser.getRole());
    }

    // checks if a user is currently authenticated
    // @return true if a user is authenticated, false otherwise
    public static boolean isAuthenticated() {
        return currentUser != null;
    }
}
