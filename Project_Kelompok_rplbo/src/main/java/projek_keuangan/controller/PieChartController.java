package projek_keuangan.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import projek_keuangan.data.DataStore;
import projek_keuangan.item.keuanganItem;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Region;
import javafx.application.Platform;

public class PieChartController {

    @FXML private PieChart pieChart;
    @FXML private HBox buttonContainer;
    @FXML private VBox customLegend;
    @FXML private ScrollPane legendScrollPane;
    @FXML private Button backButton;

    private String currentUsername;
    private int currentUserId = -1;
    private List<keuanganItem> items;

    private final String[] CHART_COLORS = {
            "#ef4444", "#f97316", "#eab308", "#22c55e",
            "#06b6d4", "#3b82f6", "#8b5cf6", "#ec4899"
    };

    public void setCurrentUsernameAndId(String username, int userId) {
        this.currentUsername = username;
        this.currentUserId = userId;
        loadItems();
        // Delay untuk memastikan UI sudah siap
        Platform.runLater(() -> showChart("Pengeluaran", "Distribusi Pengeluaran"));
    }

    private void loadItems() {
        if (currentUserId == -1) {
            pieChart.setTitle("Data pengguna tidak valid");
            return;
        }
        items = DataStore.getTodos(currentUserId);
        addChartTypeButtons();
    }

    @FXML
    private void handleShowIncome() {
        Platform.runLater(() -> showChart("Pemasukan", "Distribusi Pemasukan"));
    }

    @FXML
    private void handleShowExpense() {
        Platform.runLater(() -> showChart("Pengeluaran", "Distribusi Pengeluaran"));
    }

    private void showChart(String transactionType, String title) {
        // Clear data terlebih dahulu
        pieChart.getData().clear();
        customLegend.getChildren().clear();

        // Set title
        pieChart.setTitle(title + " - " + currentUsername);

        if (items == null || items.isEmpty()) {
            pieChart.setTitle("Tidak ada data " + title.toLowerCase() + " untuk " + currentUsername);
            return;
        }

        Map<String, Double> kategoriTotals = new HashMap<>();
        double totalAmount = 0;

        // Hitung total per kategori
        for (keuanganItem item : items) {
            if (transactionType.equals(item.getTipeTransaksi())) {
                String kategori = item.getKategori();
                double nominal = item.getNominalDouble();
                kategoriTotals.put(kategori, kategoriTotals.getOrDefault(kategori, 0.0) + nominal);
                totalAmount += nominal;
            }
        }

        if (kategoriTotals.isEmpty()) {
            pieChart.setTitle("Tidak ada data " + title.toLowerCase() + " untuk " + currentUsername);
            return;
        }

        // Buat daftar kategori yang sudah diurutkan
        List<Map.Entry<String, Double>> sortedKategori = kategoriTotals.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .toList();

        // Tambahkan data ke chart dengan urutan yang sama
        for (Map.Entry<String, Double> entry : sortedKategori) {
            String label = String.format("%s", entry.getKey());
            pieChart.getData().add(new PieChart.Data(label, entry.getValue()));
        }

        // Set warna chart dan buat custom legend dengan urutan yang sama
        final double finalTotalAmount = totalAmount;
        Platform.runLater(() -> {
            setChartColors(transactionType);
            createCustomLegend(sortedKategori, finalTotalAmount);
        });
    }

    private void createCustomLegend(List<Map.Entry<String, Double>> sortedKategori, double totalAmount) {
        customLegend.getChildren().clear();

        int colorIndex = 0;
        for (Map.Entry<String, Double> entry : sortedKategori) {
            String kategori = entry.getKey();
            double amount = entry.getValue();
            double percentage = (amount / totalAmount) * 100;
            String color = CHART_COLORS[colorIndex % CHART_COLORS.length];

            // Buat item legend
            HBox legendItem = new HBox();
            legendItem.getStyleClass().add("legend-item");

            HBox content = new HBox();
            content.getStyleClass().add("legend-item-content");

            // Color indicator (kotak kecil berwarna)
            Region colorBox = new Region();
            colorBox.getStyleClass().add("color-indicator");
            colorBox.setStyle("-fx-background-color: " + color + ";");

            // Text container
            VBox textContainer = new VBox();
            textContainer.getStyleClass().add("legend-text-container");

            Label categoryLabel = new Label(kategori);
            categoryLabel.getStyleClass().add("legend-category-text");

            Label amountLabel = new Label(String.format("Rp %,.0f", amount));
            amountLabel.getStyleClass().add("legend-amount-text");

            Label percentageLabel = new Label(String.format("%.1f%%", percentage));
            percentageLabel.getStyleClass().add("legend-percentage-text");

            textContainer.getChildren().addAll(categoryLabel, amountLabel, percentageLabel);
            content.getChildren().addAll(colorBox, textContainer);
            legendItem.getChildren().add(content);

            customLegend.getChildren().add(legendItem);
            colorIndex++;
        }
    }


    @FXML
    public void handleBack() {
        try {
            // Replace with your actual main menu/dashboard FXML file
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/projek_keuangan/keuangan-view.fxml"));
            // Alternative common paths:
            // FXMLLoader loader = new FXMLLoader(getClass().getResource("/MainMenu.fxml"));
            // FXMLLoader loader = new FXMLLoader(getClass().getResource("/Dashboard.fxml"));
            // FXMLLoader loader = new FXMLLoader(getClass().getResource("/projek_keuangan/view/MainMenu.fxml"));

            Parent root = loader.load();

            // Pass user data ke controller yang baru
            keuanganController controller = loader.getController();
            if (currentUsername != null) {
                controller.setCurrentUser(currentUsername);
            }
            // Get current stage and switch scene
            Stage stage = (Stage) backButton.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();


        } catch (IOException e) {
            System.err.println("Error loading main menu: " + e.getMessage());
            e.printStackTrace();

            // Optional: Show error dialog to user
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Navigation Error");
            alert.setHeaderText("Cannot navigate back");
            alert.setContentText("Unable to load the main menu. Please check the FXML files.");
            alert.showAndWait();
        }
    }

    // Method untuk navigasi ke form (baru)
    @FXML
    private void handleGoToForm() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/projek_keuangan/form.fxml"));
            Parent root = loader.load();

            FormController controller = loader.getController();
            if (currentUsername != null && currentUserId != -1) {
                // Initialize form dengan data user saat ini
                controller.initData(currentUserId, currentUsername, null, () -> {
                    // Callback ketika form disimpan - refresh chart
                    loadItems();
                    // Refresh chart dengan tipe yang sedang aktif
                    if (pieChart.getTitle().contains("Pemasukan")) {
                        Platform.runLater(() -> showChart("Pemasukan", "Distribusi Pemasukan"));
                    } else {
                        Platform.runLater(() -> showChart("Pengeluaran", "Distribusi Pengeluaran"));
                    }
                });
            }

            Stage stage = (Stage) pieChart.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Form Transaksi - " + currentUsername);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Navigation Error");
            alert.setHeaderText("Kesalahan Navigasi");
            alert.setContentText("Tidak dapat membuka form. Error: " + e.getMessage());
            alert.showAndWait();
        }
    }

    private void addChartTypeButtons() {
        Button incomeButton = new Button("💰 Tampilkan Pemasukan");
        incomeButton.setOnAction(e -> handleShowIncome());
        incomeButton.getStyleClass().add("income-button");

        Button expenseButton = new Button("💸 Tampilkan Pengeluaran");
        expenseButton.setOnAction(e -> handleShowExpense());
        expenseButton.getStyleClass().add("expense-button");

        // Tombol untuk ke form (baru)
        Button formButton = new Button("📝 Tambah Transaksi");
        formButton.setOnAction(e -> handleGoToForm());
        formButton.getStyleClass().add("form-button");

        // Clear existing buttons first
        buttonContainer.getChildren().clear();
        buttonContainer.getChildren().addAll(incomeButton, expenseButton, formButton);
    }

    private void setChartColors(String transactionType) {
        // Reset semua styling
        pieChart.getStyleClass().removeAll("income-chart", "expense-chart");

        // Tambahkan class styling berdasarkan tipe
        if ("Pemasukan".equals(transactionType)) {
            pieChart.getStyleClass().add("income-chart");
        } else if ("Pengeluaran".equals(transactionType)) {
            pieChart.getStyleClass().add("expense-chart");
        }
    }
}