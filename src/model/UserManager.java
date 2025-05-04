package model;

//import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

//import javax.management.RuntimeErrorException;

import org.mindrot.jbcrypt.BCrypt;

//import com.healthmarketscience.jackcess.RuntimeIOException;

// created by Valerie Holland
/**
 * Manages user accounts and authentication in the database.
 * On class load, ensures the Users table exists and a default admin account is created.
 * Provides methods for verifying login credentials, creating new users, and deleting users.
 */
public class UserManager {
    /// Default administrator username created during bootstrap.
    private static final String DEFAULT_USERNAME = "admin";

    /// Default administrator password created during bootstrap.
    private static final String DEFAULT_PASSWORD = "admin1234";

    // Bootstrap (executes on class load): ensure Users table exists and default admin user is present
    static {
        try {
            Connection connection = ConnectionManager.getInstance().getConnection();
            boolean hasTable;
            try (ResultSet resultSet = connection.getMetaData().getTables(null, null, "Users", new String[] { "TABLE" })) {
                hasTable = resultSet.next();
            }
            if (!hasTable) {
                String statementString = """
                        CREATE TABLE Users (
                          id AUTOINCREMENT PRIMARY KEY,
                          username TEXT(255) UNIQUE,
                          password_hash TEXT(255),
                          role TEXT(50))
                        """;
                try (Statement statement = connection.createStatement()) {
                    statement.executeUpdate(statementString);
                }
            }
            boolean exists;
            String statementString = "SELECT 1 FROM Users WHERE username=?";
            try (PreparedStatement ps = connection.prepareStatement(statementString)) {
                ps.setString(1, DEFAULT_USERNAME);
                try (ResultSet rs = ps.executeQuery()) {
                    exists = rs.next();
                }
            }
            if (!exists)
                UserManager.createUser(connection, DEFAULT_USERNAME, DEFAULT_PASSWORD, "admin");
        } catch (SQLException ex) {
            throw new RuntimeException("Auth bootstrap failed", ex);
        }
    }

    /// JDBC connection used for user.
    private final Connection connection;

    public UserManager() {
        connection = ConnectionManager.getInstance().getConnection();
    }

    /**
     * Verifies the given username and password against stored credentials.
     * Returns a User record if authentication succeeds, or null if it fails.
     *
     * @param username the username to authenticate
     * @param password the plaintext password to verify
     * @return a User object on success, or null if authentication fails
     * @throws RuntimeException if a database access error occurs
     */
    public User verifyLogin(String username, String password) {
        /*
         - prepare SQL "SELECT password_hash, role FROM Users WHERE username=?"
         - bind username to parameter 1
         - execute query
         - if a row is returned and BCrypt.checkpw(password, password_hash) is true
            - return new User(username, role)
         - else
            - return null
         */
        String statement = "SELECT password_hash, role FROM Users WHERE username=?";
        try(PreparedStatement ps = connection.prepareStatement(statement)) {
        ps.setString(1, username);
        try(ResultSet rs = ps.executeQuery()){
            if(rs.next()){
                String hashed = rs.getString("password_hash");
                String role = rs.getString("role");

                if(BCrypt.checkpw(password,hashed)){
                    return new User(username,role);
                }
            }
        }
        } catch (SQLException e){
            throw new RuntimeException("Login verification failed. Try again.", e);

        }
        return null;
    }
    /**
     * allows admin to change User role
     * @param username
     * @param newRole
     */
    public void updateUserRole(String username, String newRole) {
        String sql = "UPDATE Users SET role=? WHERE username=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, newRole);
            ps.setString(2, username);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Updating user role failed", e);
        }
    }
    

    /**
     * Creates a new user with the specified credentials and role.
     *
     * @param username the username for the new account
     * @param password the plaintext password for the new account
     * @param role     the assigned role (e.g. "admin", "user")
     * @throws SQLException 
     * @throws RuntimeException if a database access error occurs
     */
    public void createUser(String username, String password, String role) throws SQLException {
        /*
         - call createUser(connection, username, password, role)
         - if SQLException occurs
            - throw runtime exception
         */

         createUser(connection, username, password, role);
    }

    /**
     * Inserts a user record into the database with a hashed password.
     * Used internally for bootstrap and user creation.
     *
     * @param connection the JDBC connection to use
     * @param username   the username to insert
     * @param password   the plaintext password to hash
     * @param role       the assigned role
     * @throws SQLException if a database access error occurs
     */
    public static void createUser(Connection connection, String username, String password, String role) throws SQLException {
        /*
         - prepare SQL "INSERT INTO Users(username,password_hash,role) VALUES(?,?,?)"
         - bind parameters:
            1 → username
            2 → BCrypt.hashpw(password, BCrypt.gensalt())
            3 → role
         - execute update
         - if SQLException occurs
            - propagate exception
         */
        String statement = "INSERT INTO Users(username, password_hash,role) VALUES (?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(statement)){
        ps.setString(1, username);  
        ps.setString(2, BCrypt.hashpw(password, BCrypt.gensalt()));
        ps.setString(3, role);  
        ps.executeUpdate();
        }
    }

    /**
     * Deletes the user with the specified username.
     *
     * @param username the username of the account to remove
     * @throws SQLException 
     * @throws RuntimeException if a database access error occurs
     */
    public void deleteUser(String username) throws SQLException {
        /*
         - prepare SQL "DELETE FROM Users WHERE username=?"
         - bind username to parameter 1
         - execute update
         - if SQLException occurs
            - throw runtime exception
         */
        String statement = "DELETE FROM Users WHERE username=?";
        try(PreparedStatement ps = connection.prepareStatement(statement)){
            ps.setString(1,username);
            ps.executeUpdate();

        }
    }
    /**
     * Checks if a user with the specified username already exists.
     * Returns true if the user exists, false otherwise.
     * @param username
     * @return
     */
    public boolean userExists(String username) {
        String sql = "SELECT 1 FROM Users WHERE username=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Checking user existence failed", e);
        }
    }
    /**
     * Lists all users in the database.
     * Returns a list of User objects representing each user.
     * @throws SQLException
     * @throws RuntimeException if a database access error occurs
     * @return
     */
    public List<User> listUsers() {
    List<User> users = new ArrayList<>();
    String sql = "SELECT username, role FROM Users";
    try (Statement stmt = connection.createStatement();
         ResultSet rs = stmt.executeQuery(sql)) {
        while (rs.next()) {
            users.add(new User(rs.getString("username"), rs.getString("role")));
        }
    } catch (SQLException e) {
        throw new RuntimeException("Listing users failed", e);
    }
    return users;
}
    /**
     * Changes the password for the specified user.
    * Hashes the new password before storing it in the database.
    * @throws SQLException
    * @param username
    * @param newPassword
    */
    public void changePassword(String username, String newPassword) {
        String sql = "UPDATE Users SET password_hash=? WHERE username=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, BCrypt.hashpw(newPassword, BCrypt.gensalt()));
            ps.setString(2, username);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Password change failed", e);
        }
}

    

}
