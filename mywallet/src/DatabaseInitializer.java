import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseInitializer {

    private static final String DATABASE_URL = "jdbc:sqlite:mywallet.db"; // Path to your SQLite database file

    public static void initializeDatabase() {
        String createUsersTableQuery = """
                CREATE TABLE IF NOT EXISTS users (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    email TEXT UNIQUE NOT NULL,
                    password TEXT NOT NULL
                );
                """;

        try (Connection connection = DriverManager.getConnection(DATABASE_URL);
             Statement statement = connection.createStatement()) {

            // Create the users table if it does not exist
            statement.execute(createUsersTableQuery);
            System.out.println("Database initialized successfully!");

        } catch (SQLException e) {
            System.err.println("Error initializing the database: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
