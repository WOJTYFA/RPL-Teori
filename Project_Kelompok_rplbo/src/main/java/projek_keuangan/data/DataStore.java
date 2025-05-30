package projek_keuangan.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

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
}