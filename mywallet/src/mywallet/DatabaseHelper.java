package mywallet;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DatabaseHelper {
    // Database URL
    private static final String URL = "jdbc:sqlite:mywallet.db";

    /**
     * Initialize the database by creating necessary tables.
     */
    public static void initializeDatabase() {
        try (Connection conn = DriverManager.getConnection(URL)) {
            if (conn != null) {
                // Create `users` table if it doesn't exist
                String createUsersTable = "CREATE TABLE IF NOT EXISTS users ("
                        + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                        + "email TEXT UNIQUE NOT NULL, "
                        + "password TEXT NOT NULL, "
                        + "balance REAL DEFAULT 0.0)";
                
                // Create `transactions` table if it doesn't exist
                String createTransactionsTable = "CREATE TABLE IF NOT EXISTS transactions ("
                        + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                        + "user_id INTEGER NOT NULL, "
                        + "amount REAL NOT NULL, "
                        + "transaction_id TEXT UNIQUE NOT NULL, "
                        + "FOREIGN KEY(user_id) REFERENCES users(id))";

                // Execute table creation queries
                conn.createStatement().execute(createUsersTable);
                conn.createStatement().execute(createTransactionsTable);
                System.out.println("Database initialized successfully.");
            }
        } catch (SQLException e) {
            System.err.println("Error initializing the database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Get a new database connection.
     *
     * @return A valid connection to the SQLite database.
     */
    public static Connection getConnection() {
        try {
            return DriverManager.getConnection(URL); // Open a new connection
        } catch (SQLException e) {
            System.err.println("Error establishing a database connection: " + e.getMessage());
            e.printStackTrace();
            return null; // Return null if connection fails
        }
    }

    /**
     * Close a database connection safely.
     *
     * @param conn The database connection to close.
     */
    public static void closeConnection(Connection conn) {
        try {
            if (conn != null && !conn.isClosed()) {
                conn.close();
                System.out.println("Database connection closed successfully.");
            }
        } catch (SQLException e) {
            System.err.println("Error closing the database connection: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
