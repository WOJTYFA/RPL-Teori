package projek_keuangan.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.stage.Stage;
import projek_keuangan.data.DataStore;
import projek_keuangan.item.keuanganItem;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PieChartController {

    @FXML private PieChart pieChart;
    private String currentUser;

    // Method pemanggil dari luar (keuanganController)
    public void setCurrentUser(String username) {
        this.currentUser = username;
        loadChartData();
    }

    private void loadChartData() {
        pieChart.getData().clear();

        List<keuanganItem> items = DataStore.getTodos(currentUser);
        Map<String, Double> kategoriTotals = new HashMap<>();

        for (keuanganItem item : items) {
            String kategori = item.getKategori();
            String nominalStr = item.getNominal().replaceAll("[^\\d]", ""); // Hilangkan "Rp." dan non-digit
            double nominal = 0;
            try {
                nominal = Double.parseDouble(nominalStr);
            } catch (NumberFormatException e) {
                // Lewati item jika nominal tidak valid
                continue;
            }

            kategoriTotals.put(kategori,
                    kategoriTotals.getOrDefault(kategori, 0.0) + nominal);
        }

        for (Map.Entry<String, Double> entry : kategoriTotals.entrySet()) {
            pieChart.getData().add(new PieChart.Data(entry.getKey(), entry.getValue()));
        }
    }

    @FXML
    private void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/projek_keuangan/keuangan_view.fxml"));
            Stage stage = (Stage) pieChart.getScene().getWindow();
            Scene scene = new Scene(loader.load());

            keuanganController controller = loader.getController();
            controller.setCurrentUser(currentUser);
            stage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
