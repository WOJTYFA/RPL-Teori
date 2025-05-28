package projek_keuangan.manager;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {
    private static final String DB_URL = "jdbc:sqlite:keuangan_app.db"; // Nama file database

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    public static void initializeDatabase() {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            // Tabel Users
            String createUserTableSQL = "CREATE TABLE IF NOT EXISTS users (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "username TEXT UNIQUE NOT NULL," +
                    "password TEXT NOT NULL)";
            stmt.execute(createUserTableSQL);

            // Tabel Financial Records
            String createFinancialRecordsTableSQL = "CREATE TABLE IF NOT EXISTS financial_records (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "user_id INTEGER NOT NULL," +
                    "tanggal TEXT NOT NULL," +
                    "nominal REAL NOT NULL," +
                    "catatan TEXT NOT NULL," +
                    "kategori TEXT NOT NULL," +
                    "FOREIGN KEY (user_id) REFERENCES users(id))";
            stmt.execute(createFinancialRecordsTableSQL);

            System.out.println("Database initialized successfully.");

        } catch (SQLException e) {
            System.err.println("Error initializing database: " + e.getMessage());
            e.printStackTrace();
        }
    }
}