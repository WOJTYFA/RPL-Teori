package projek_keuangan.manager;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {
    // Use absolute path to keuangan_app.db in the root project directory
    // Ensure the path uses forward slashes and the correct drive letter format for the URL
    private static final String DB_URL = "jdbc:sqlite:keuangan_app.db";

    public static Connection getConnection() throws SQLException {
        System.out.println("Attempting to connect to database: " + DB_URL);
        Connection conn = DriverManager.getConnection(DB_URL);
        System.out.println("Database connection successful.");
        return conn;
    }

    public static void initializeDatabase() {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            System.out.println("Initializing tables...");
            // Tabel Users
            String createUserTableSQL = "CREATE TABLE IF NOT EXISTS users (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "username TEXT UNIQUE NOT NULL," +
                    "password TEXT NOT NULL)";
            stmt.execute(createUserTableSQL);
            System.out.println("Table 'users' checked/created.");

            // Tabel Financial Records
            String createFinancialRecordsTableSQL = "CREATE TABLE IF NOT EXISTS financial_records (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "user_id INTEGER NOT NULL," +
                    "tanggal TEXT NOT NULL," +
                    "nominal REAL NOT NULL," +
                    "catatan TEXT NOT NULL," +
                    "kategori TEXT NOT NULL," +
                    "tipe_transaksi TEXT NOT NULL," +
                    "FOREIGN KEY (user_id) REFERENCES users(id))";
            stmt.execute(createFinancialRecordsTableSQL);
            System.out.println("Table 'financial_records' checked/created.");

            // Check and add tipe_transaksi column if it doesn't exist
            if (!columnExists(conn, "financial_records", "tipe_transaksi")) {
                String addColumnSQL = "ALTER TABLE financial_records ADD COLUMN tipe_transaksi TEXT NOT NULL DEFAULT 'Pengeluaran'";
                stmt.execute(addColumnSQL);
                System.out.println("Added 'tipe_transaksi' column to 'financial_records' table.");
            }

            System.out.println("Database initialization and schema check completed.");

        } catch (SQLException e) {
            System.err.println("Error initializing or checking database schema: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static boolean columnExists(Connection conn, String tableName, String columnName) throws SQLException {
        DatabaseMetaData md = conn.getMetaData();
        try (ResultSet rs = md.getColumns(null, null, tableName, columnName)) {
            return rs.next();
        }
    }



}