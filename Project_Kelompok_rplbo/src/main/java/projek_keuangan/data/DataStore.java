package projek_keuangan.data;

import projek_keuangan.manager.DatabaseManager;
import projek_keuangan.item.keuanganItem;
import projek_keuangan.item.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

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
        String sql = "SELECT id, username, password FROM users WHERE username = ? AND password = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new User(rs.getInt("id"), rs.getString("username"), rs.getString("password"));
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
        // Ambil juga ID dari financial_records jika Anda ingin menambahkannya ke keuanganItem
        // String sql = "SELECT id, tanggal, nominal, catatan, kategori FROM financial_records WHERE user_id = ?";
        String sql = "SELECT tanggal, nominal, catatan, kategori FROM financial_records WHERE user_id = ? ORDER BY tanggal DESC"; // Tambahkan ORDER BY
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                //  Jika Anda tidak menyimpan "Rp." di database:
                String nominalStr = String.format("Rp.%.0f", rs.getDouble("nominal"));
                //  Jika Anda ingin menyimpan ID item (misal, `keuanganItem` punya field `id`):
                //  int itemId = rs.getInt("id");
                items.add(new keuanganItem(
                        // itemId, // jika ada
                        rs.getString("tanggal"),
                        nominalStr,
                        rs.getString("catatan"),
                        rs.getString("kategori")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error getting financial records for user_id " + userId + ": " + e.getMessage());
            e.printStackTrace();
        }
        return items;
    }

    public static void addTodo(int userId, keuanganItem item) {
        String sql = "INSERT INTO financial_records(user_id, tanggal, nominal, catatan, kategori) VALUES(?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setString(2, item.getTanggal());
            double nominalValue = Double.parseDouble(item.getNominal().replaceAll("[^\\d.,]", "").replace(",", "."));
            pstmt.setDouble(3, nominalValue);
            pstmt.setString(4, item.getCatatan());
            pstmt.setString(5, item.getKategori());
            pstmt.executeUpdate();
        } catch (SQLException | NumberFormatException e) {
            System.err.println("Error adding financial record: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Idealnya, item punya ID unik untuk remove dan edit.
    // Menggunakan kombinasi field bisa berisiko jika ada duplikat data (kecuali tanggal, nominal, catatan, kategori dianggap unik per user).
    // Jika keuanganItem dimodifikasi untuk memiliki ID dari database, gunakan ID itu.
    public static void removeTodo(int userId, keuanganItem item) {
        // Asumsi sementara: menghapus berdasarkan konten jika tidak ada ID unik pada item
        // Jika item memiliki ID unik (misalnya setelah diambil dari DB):
        // String sql = "DELETE FROM financial_records WHERE id = ? AND user_id = ?";
        // pstmt.setInt(1, item.getId());
        // pstmt.setInt(2, userId);
        String sql = "DELETE FROM financial_records WHERE user_id = ? AND tanggal = ? AND nominal = ? AND catatan = ? AND kategori = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setString(2, item.getTanggal());
            double nominalValue = Double.parseDouble(item.getNominal().replaceAll("[^\\d.,]", "").replace(",", "."));
            pstmt.setDouble(3, nominalValue);
            pstmt.setString(4, item.getCatatan());
            pstmt.setString(5, item.getKategori());
            pstmt.executeUpdate();
        } catch (SQLException | NumberFormatException e) {
            System.err.println("Error removing financial record: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void editTodo(int userId, keuanganItem oldItem, keuanganItem newItem) {
        // Sama seperti removeTodo, idealnya berdasarkan ID unik item yang akan di-edit.
        // Jika oldItem memiliki ID unik:
        // String sql = "UPDATE financial_records SET tanggal = ?, nominal = ?, catatan = ?, kategori = ? WHERE id = ? AND user_id = ?";
        // pstmt.setInt(5, oldItem.getId());
        // pstmt.setInt(6, userId);
        String sql = "UPDATE financial_records SET tanggal = ?, nominal = ?, catatan = ?, kategori = ? " +
                "WHERE user_id = ? AND tanggal = ? AND nominal = ? AND catatan = ? AND kategori = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            double newNominalValue = Double.parseDouble(newItem.getNominal().replaceAll("[^\\d.,]", "").replace(",", "."));
            double oldNominalValue = Double.parseDouble(oldItem.getNominal().replaceAll("[^\\d.,]", "").replace(",", "."));

            pstmt.setString(1, newItem.getTanggal());
            pstmt.setDouble(2, newNominalValue);
            pstmt.setString(3, newItem.getCatatan());
            pstmt.setString(4, newItem.getKategori());

            // Kondisi WHERE untuk menemukan oldItem
            pstmt.setInt(5, userId);
            pstmt.setString(6, oldItem.getTanggal());
            pstmt.setDouble(7, oldNominalValue);
            pstmt.setString(8, oldItem.getCatatan());
            pstmt.setString(9, oldItem.getKategori());

            pstmt.executeUpdate();
        } catch (SQLException | NumberFormatException e) {
            System.err.println("Error editing financial record: " + e.getMessage());
            e.printStackTrace();
        }
    }
}