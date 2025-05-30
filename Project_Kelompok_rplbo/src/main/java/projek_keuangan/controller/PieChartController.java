package projek_keuangan.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.geometry.Insets;
import javafx.stage.Stage;
import projek_keuangan.data.DataStore;
import projek_keuangan.item.keuanganItem;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PieChartController {

    @FXML private PieChart pieChart;
    @FXML private HBox buttonContainer; // Tambahkan referensi ke HBox

    private String currentUsername;
    private int currentUserId = -1;
    private List<keuanganItem> items; // Tambahkan variabel untuk menyimpan data

    public void setCurrentUsernameAndId(String username, int userId) {
        this.currentUsername = username;
        this.currentUserId = userId;
        loadItems(); // Panggil metode untuk memuat data
        showChart("Pengeluaran", "Distribusi Pengeluaran"); // Tampilkan pengeluaran secara default
    }

    // Metode baru untuk memuat data
    private void loadItems() {
        if (currentUserId == -1) {
            pieChart.setTitle("Data pengguna tidak valid");
            return;
        }
        items = DataStore.getTodos(currentUserId);

        // Tambahkan tombol untuk pemasukan/pengeluaran
        addChartTypeButtons();
    }

    // Tambahkan tombol untuk beralih antara pemasukan dan pengeluaran
    private void addChartTypeButtons() {
        Button incomeButton = new Button("Tampilkan Pemasukan");
        incomeButton.setOnAction(e -> handleShowIncome());

        Button expenseButton = new Button("Tampilkan Pengeluaran");
        expenseButton.setOnAction(e -> handleShowExpense());

        buttonContainer.getChildren().addAll(incomeButton, expenseButton);
    }

    @FXML
    private void handleShowIncome() {
        showChart("Pemasukan", "Distribusi Pemasukan");
    }

    @FXML
    private void handleShowExpense() {
        showChart("Pengeluaran", "Distribusi Pengeluaran");
    }

    private void showChart(String transactionType, String title) {
        pieChart.getData().clear();
        pieChart.setTitle(title + " - " + currentUsername);

        if (items == null || items.isEmpty()) {
            pieChart.setTitle("Tidak ada data " + title.toLowerCase() + " untuk " + currentUsername);
            return;
        }

        Map<String, Double> kategoriTotals = new HashMap<>();

        for (keuanganItem item : items) {
            if (transactionType.equals(item.getTipeTransaksi())) {
                String kategori = item.getKategori();
                double nominal = item.getNominalDouble();
                kategoriTotals.put(kategori,
                        kategoriTotals.getOrDefault(kategori, 0.0) + nominal);
            }
        }

        if (kategoriTotals.isEmpty()) {
            pieChart.setTitle("Tidak ada data " + title.toLowerCase() + " untuk " + currentUsername);
            return;
        }

        // Tambahkan data ke chart
        for (Map.Entry<String, Double> entry : kategoriTotals.entrySet()) {
            String label = String.format("%s (Rp%,.0f)", entry.getKey(), entry.getValue());
            pieChart.getData().add(new PieChart.Data(label, entry.getValue()));
        }
    }

    @FXML
    private void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/projek_keuangan/keuangan_view.fxml"));
            Stage stage = (Stage) pieChart.getScene().getWindow();
            Scene scene = new Scene(loader.load());

            keuanganController controller = loader.getController();
            if (currentUsername != null) {
                controller.setCurrentUser(currentUsername);
            } else {
                System.err.println("Cannot go back, currentUsername is null in PieChartController");

            }
            stage.setScene(scene);
            stage.setTitle("Manajemen Keuangan - " + currentUsername);
        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Navigation Error");
            alert.setHeaderText(null);
            alert.setContentText("Could not return to the main view.");
            alert.showAndWait();
        }
    }

}