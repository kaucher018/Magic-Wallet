package mywallet;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class CreateDatabase {
    public static void main(String[] args) {
        String url = "jdbc:sqlite:mywallet.db"; // Path to your database file

        try (Connection conn = DriverManager.getConnection(url);
             Statement stmt = conn.createStatement()) {

            // Create a users table
            String createUsersTable = "CREATE TABLE IF NOT EXISTS users (" +
                                       "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                                       "email TEXT NOT NULL UNIQUE, " +
                                       "password TEXT NOT NULL" +
                                       ")";
            stmt.execute(createUsersTable);

            System.out.println("Database and table created successfully!");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
