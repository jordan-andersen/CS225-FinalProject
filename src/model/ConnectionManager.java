package model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Singleton connection manager for an Access database.
 * Manages a single shared JDBC {@link Connection} instance.
 * @author Baheeja M., Jordan A.
 */
public class ConnectionManager {
    /// Filesystem path to the Access database file.
    private static final String DATABASE_PATH = System.getProperty("user.dir") + "/data/database.accdb";

    /// JDBC connection URL for the Access database, using the UCanAccess driver.
    private static final String DATABASE_URL = "jdbc:ucanaccess://" + DATABASE_PATH + ";singleConnection=true";

    /// Singleton instance of the ConnectionManager.
    private static ConnectionManager instance;

    /// The JDBC Connection managed by this manager.
    private final Connection connection;

    /**
     * Private constructor.
     * Loads the UCanAccess JDBC driver and establishes a connection to the database.
     * @throws RuntimeException if the driver cannot be loaded or the connection cannot be established.
     */
    private ConnectionManager() {
        try {
            // Weird way of creating a Driver object and registering it with the DriverManager.
            Class.forName("net.ucanaccess.jdbc.UcanaccessDriver");

            // Contacts DriverManager to get a connection through the registered driver.
            connection = DriverManager.getConnection(DATABASE_URL);
        } catch (ClassNotFoundException | SQLException ex) {
            throw new RuntimeException("Cannot open DB: " + DATABASE_PATH, ex);
        }
    }

    /**
     * Returns the singleton {@link ConnectionManager} instance, creating it if necessary.
     * This method is synchronized to ensure thread-safe lazy initialization.
     *
     * @return the single instance of {@link ConnectionManager}
     */
    public static synchronized ConnectionManager getInstance() {
        return instance == null ? (instance = new ConnectionManager()) : instance;
    }

    /**
     * Returns the shared JDBC {@link Connection}.
     *
     * @return the active JDBC {@link Connection}
     */
    public Connection getConnection() {
        return connection;
    }
}
