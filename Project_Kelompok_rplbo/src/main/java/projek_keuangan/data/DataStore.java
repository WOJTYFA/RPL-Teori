package projek_keuangan.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import projek_keuangan.item.User;
import projek_keuangan.item.keuanganItem;
import projek_keuangan.manager.DatabaseManager;

public class DataStore {

    public static boolean addUser(User user) {
        String sql = "INSERT INTO users(username, password) VALUES(?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getPassword());
            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        user.setId(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
            return false;
        } catch (SQLException e) {
            if (e.getErrorCode() == 19 && e.getMessage() != null && e.getMessage().contains("SQLITE_CONSTRAINT_UNIQUE")) {
                System.err.println("Username already exists: " + user.getUsername());
            } else {
                System.err.println("Error adding user: " + e.getMessage());
                e.printStackTrace();
            }
            return false;
        }
    }

    public static User findUser(String username, String password) {
        System.out.println("Attempting login for: " + username);
        String sql = "SELECT id, username, password FROM users WHERE username = ? AND password = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.setString(2, password);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                int id = rs.getInt("id");
                String dbUsername = rs.getString("username");
                System.out.println("User found: " + dbUsername + " with ID: " + id);
                return new User(id, dbUsername, rs.getString("password"));
            } else {
                System.out.println("No user found with that username/password");
            }
        } catch (SQLException e) {
            System.err.println("Error finding user: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public static User getUserByUsername(String username) {
        String sql = "SELECT id, username, password FROM users WHERE username = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new User(rs.getInt("id"), rs.getString("username"), rs.getString("password"));
            }
        } catch (SQLException e) {
            System.err.println("Error finding user by username: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }


    public static int getUserId(String username) {
        String sql = "SELECT id FROM users WHERE username = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("id");
            }
        } catch (SQLException e) {
            System.err.println("Error finding user id for " + username + ": " + e.getMessage());
            // e.printStackTrace(); // Bisa di-uncomment untuk debug lebih lanjut
        }
        return -1; // Indikasi user tidak ditemukan atau error
    }


    // FINANCIAL RECORDS MANAGEMENT
    public static List<keuanganItem> getTodos(int userId) {
        List<keuanganItem> items = new ArrayList<>();
        String sql = "SELECT id, tanggal, nominal, catatan, kategori, tipe_transaksi FROM financial_records WHERE user_id = ? ORDER BY tanggal DESC";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                int id = rs.getInt("id");
                String tanggal = rs.getString("tanggal");
                double nominalValue = rs.getDouble("nominal");
                String catatan = rs.getString("catatan");
                String kategori = rs.getString("kategori");
                String tipeTransaksi = rs.getString("tipe_transaksi");

                String nominalStr = String.format("Rp.%,.0f", nominalValue);
                System.out.println("Ambil data: id=" + id + ", tanggal=" + tanggal + ", nominalDouble=" + nominalValue + ", catatan=" + catatan + ", kategori=" + kategori + ", tipe=" + tipeTransaksi);

                items.add(new keuanganItem(
                        id,
                        tanggal,
                        nominalStr,
                        catatan,
                        kategori,
                        tipeTransaksi,
                        nominalValue
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error getting financial records for user_id " + userId + ": " + e.getMessage());
            e.printStackTrace();
        }
        return items;
    }

    public static void addTodo(int userId, keuanganItem item) {
        String sql = "INSERT INTO financial_records(user_id, tanggal, nominal, catatan, kategori, tipe_transaksi) VALUES(?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, userId);
            pstmt.setString(2, item.getTanggal());
            pstmt.setDouble(3, Double.parseDouble(item.getNominal().replace("Rp.", "").replace(".", "").replace(",", ".")));
            pstmt.setString(4, item.getCatatan());
            pstmt.setString(5, item.getKategori());
            pstmt.setString(6, item.getTipeTransaksi());
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        item.setId(generatedKeys.getInt(1));
                    }
                }
            }
        } catch (SQLException | NumberFormatException e) {
            System.err.println("Error adding financial record: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void removeTodo(int userId, keuanganItem item) {
        String sql = "DELETE FROM financial_records WHERE id = ? AND user_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, item.getId());
            pstmt.setInt(2, userId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error removing financial record: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void editTodo(int userId, keuanganItem oldItem, keuanganItem newItem) {
        String sql = "UPDATE financial_records SET tanggal = ?, nominal = ?, catatan = ?, kategori = ?, tipe_transaksi = ? WHERE id = ? AND user_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            String nominalStr = newItem.getNominal().replace("Rp.", "").replace(".", "").replace(",", ".");
            double nominalValue = Double.parseDouble(nominalStr);

            pstmt.setString(1, newItem.getTanggal());
            pstmt.setDouble(2, nominalValue);
            pstmt.setString(3, newItem.getCatatan());
            pstmt.setString(4, newItem.getKategori());
            pstmt.setString(5, newItem.getTipeTransaksi());
            pstmt.setInt(6, oldItem.getId());
            pstmt.setInt(7, userId);

            pstmt.executeUpdate();
        } catch (SQLException | NumberFormatException e) {
            System.err.println("Error editing financial record: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Tambahkan di class DataStore
    private static final Map<Integer, Double> userTargets = new HashMap<>();
    private static final Map<Integer, Map<YearMonth, Double>> historicalTargets = new HashMap<>();

    public static double getHistoricalTarget(int userId, YearMonth month) {
        if (historicalTargets.containsKey(userId)) {
            return historicalTargets.get(userId).getOrDefault(month, 0.0);
        }
        return 0.0;
    }


    public static void saveTarget(int userId, double amount, String category) {
        // Jika "Semua Kategori", set category menjadi null
        if ("Semua Kategori".equals(category)) {
            category = null;
        }

        String monthYear = YearMonth.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
        String sql = "INSERT INTO expense_targets (user_id, target_amount, category, month_year) " +
                "VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setDouble(2, amount);
            pstmt.setString(3, category);
            pstmt.setString(4, monthYear);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Hapus target saat ini
    public static void deleteCurrentTarget(int userId) {
        String monthYear = YearMonth.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
        String sql = "DELETE FROM expense_targets WHERE user_id = ? AND month_year = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setString(2, monthYear);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Dapatkan target saat ini
    public static double getCurrentTarget(int userId) {
        String monthYear = YearMonth.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
        String sql = "SELECT SUM(target_amount) as total FROM expense_targets " +
                "WHERE user_id = ? AND month_year = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setString(2, monthYear);

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble("total");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    // Dapatkan target historis
    public static Map<YearMonth, Double> getHistoricalTargets(int userId) {
        Map<YearMonth, Double> targets = new HashMap<>();
        String sql = "SELECT month_year, SUM(target_amount) as total_target " +
                "FROM expense_targets " +
                "WHERE user_id = ? " +
                "GROUP BY month_year";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                String monthYear = rs.getString("month_year");
                YearMonth ym = YearMonth.parse(monthYear, DateTimeFormatter.ofPattern("yyyy-MM"));
                double total = rs.getDouble("total_target");
                targets.put(ym, total);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return targets;
    }

    // Dapatkan daftar kategori
    public static List<String> getCategories(int userId) {
        List<String> categories = new ArrayList<>();
        String sql = "SELECT DISTINCT kategori FROM financial_records WHERE user_id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                categories.add(rs.getString("kategori"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return categories;
    }

    // Dapatkan daftar kategori hanya untuk pengeluaran
    public static List<String> getExpenseCategories(int userId) {
        List<String> categories = new ArrayList<>();
        // Hanya ambil kategori dengan tipe transaksi = 'Pengeluaran'
        String sql = "SELECT DISTINCT kategori FROM financial_records " +
                "WHERE user_id = ? AND tipe_transaksi = 'Pengeluaran'";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                categories.add(rs.getString("kategori"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return categories;
    }
}