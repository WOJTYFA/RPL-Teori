package projek_keuangan.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import projek_keuangan.data.DataStore;
import projek_keuangan.item.keuanganItem;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PieChartController {

    @FXML private PieChart pieChart;
    private String currentUsername;
    private int currentUserId = -1;

    public void setCurrentUsernameAndId(String username, int userId) {
        this.currentUsername = username;
        this.currentUserId = userId;
        loadChartData();
    }

    private void loadChartData() {
        if (currentUserId == -1) {
            pieChart.setTitle("Data pengguna tidak valid");
            return;
        }
        pieChart.getData().clear();
        pieChart.setTitle("Distribusi Pengeluaran per Kategori - " + currentUsername);


        List<keuanganItem> items = DataStore.getTodos(currentUserId);
        Map<String, Double> kategoriTotals = new HashMap<>();

        if (items.isEmpty()) {
            pieChart.setTitle("Tidak ada data pengeluaran untuk " + currentUsername);
            return;
        }

        for (keuanganItem item : items) {
            String kategori = item.getKategori();
            String nominalStrClean = item.getNominal().replaceAll("[^\\d.,]", "").replace(",", ".");
            double nominal = 0;
            try {
                if (!nominalStrClean.isEmpty()) {
                    nominal = Double.parseDouble(nominalStrClean);
                }
            } catch (NumberFormatException e) {
                System.err.println("Invalid nominal format for item: " + item.getNominal() + " - " + e.getMessage());
                continue;
            }

            kategoriTotals.put(kategori,
                    kategoriTotals.getOrDefault(kategori, 0.0) + nominal);
        }

        for (Map.Entry<String, Double> entry : kategoriTotals.entrySet()) {
            String pieSliceLabel = String.format("%s (Rp.%.0f)", entry.getKey(), entry.getValue());
            pieChart.getData().add(new PieChart.Data(pieSliceLabel, entry.getValue()));
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