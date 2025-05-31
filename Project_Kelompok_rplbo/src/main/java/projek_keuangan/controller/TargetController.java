package projek_keuangan.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import projek_keuangan.data.DataStore;
import projek_keuangan.item.TargetItem;
import projek_keuangan.item.keuanganItem;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class TargetController {

    @FXML private Label currentTargetLabel;
    @FXML private Label currentExpenseLabel;
    @FXML private Label statusLabel;
    @FXML private ProgressBar progressBar;
    @FXML private Label progressLabel;
    @FXML private TextField targetField;
    @FXML private ComboBox<String> categoryCombo;
    @FXML private TableView<TargetItem> targetHistoryTable;
    @FXML private TableColumn<TargetItem, String> monthCol;
    @FXML private TableColumn<TargetItem, String> targetAmountCol;
    @FXML private TableColumn<TargetItem, String> actualExpenseCol;
    @FXML private TableColumn<TargetItem, String> statusCol;



    @FXML
    public void initialize() {
        // Atur lebar kolom secara proporsional
        monthCol.prefWidthProperty().bind(targetHistoryTable.widthProperty().multiply(0.27));
        targetAmountCol.prefWidthProperty().bind(targetHistoryTable.widthProperty().multiply(0.23));
        actualExpenseCol.prefWidthProperty().bind(targetHistoryTable.widthProperty().multiply(0.27));
        statusCol.prefWidthProperty().bind(targetHistoryTable.widthProperty().multiply(0.23));
    }

    private int currentUserId;
    private String currentUsername;
    private final DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("MMMM yyyy", new Locale("id", "ID"));

    public void initData(int userId, String username) {
        this.currentUserId = userId;
        this.currentUsername = username;

        // Load categories
        loadCategories();

        // Load current target and expenses
        refreshCurrentTargetStatus();
        loadTargetHistory();
    }

    private void loadCategories() {
        categoryCombo.getItems().clear();
        categoryCombo.getItems().add("Semua Kategori");

        // Hanya ambil kategori untuk pengeluaran
        categoryCombo.getItems().addAll(DataStore.getExpenseCategories(currentUserId));

        categoryCombo.getSelectionModel().selectFirst();
    }

    private void refreshCurrentTargetStatus() {
        // Dapatkan target saat ini dari database
        double currentTarget = DataStore.getCurrentTarget(currentUserId);
        currentTargetLabel.setText(currentTarget > 0 ?
                String.format("Rp.%,.0f", currentTarget) : "Belum diset");

        // Hitung pengeluaran bulan ini
        double currentExpense = calculateCurrentMonthExpense();
        currentExpenseLabel.setText(String.format("Rp.%,.0f", currentExpense));

        // Update progress
        if (currentTarget > 0) {
            double progress = Math.min(currentExpense / currentTarget, 1.0);
            progressBar.setProgress(progress);
            progressLabel.setText(String.format("%.1f%% dari target tercapai", progress * 100));

            // Update status
            if (progress >= 1.0) {
                statusLabel.setText("Melebihi Target");
                statusLabel.setStyle("-fx-text-fill: #dc3545; -fx-font-weight: bold;");
            } else if (progress >= 0.8) {
                statusLabel.setText("Mendekati Target");
                statusLabel.setStyle("-fx-text-fill: #ffc107; -fx-font-weight: bold;");
            } else {
                statusLabel.setText("Normal");
                statusLabel.setStyle("-fx-text-fill: #28a745; -fx-font-weight: bold;");
            }
        } else {
            progressBar.setProgress(0);
            progressLabel.setText("0% dari target tercapai");
            statusLabel.setText("Target belum diset");
            statusLabel.setStyle("-fx-text-fill: #6c757d; -fx-font-weight: bold;");
        }
    }

    private double calculateCurrentMonthExpense() {
        YearMonth currentYearMonth = YearMonth.now();
        return DataStore.getTodos(currentUserId).stream()
                .filter(item ->
                        "Pengeluaran".equals(item.getTipeTransaksi()) &&
                                isSameMonth(item.getTanggal(), currentYearMonth))
                .mapToDouble(keuanganItem::getNominalDouble)
                .sum();
    }

    private boolean isSameMonth(String dateString, YearMonth yearMonth) {
        try {
            // Asumsikan format tanggal: yyyy-MM-dd
            YearMonth itemMonth = YearMonth.parse(dateString.substring(0, 7), DateTimeFormatter.ofPattern("yyyy-MM"));
            return itemMonth.equals(yearMonth);
        } catch (Exception e) {
            return false;
        }
    }

    private void loadTargetHistory() {
        ObservableList<TargetItem> historyData = FXCollections.observableArrayList();

        // Dapatkan target historis dari database
        Map<YearMonth, Double> historicalTargets = DataStore.getHistoricalTargets(currentUserId);

        // Dapatkan 6 bulan terakhir
        YearMonth current = YearMonth.now();
        for (int i = 5; i >= 0; i--) {
            YearMonth month = current.minusMonths(i);
            double target = historicalTargets.getOrDefault(month, 0.0);
            double expense = calculateMonthExpense(month);

            String status;
            if (target <= 0) {
                status = "Tidak ada target";
            } else if (expense > target) {
                status = "Melebihi";
            } else {
                status = "Tercapai";
            }

            historyData.add(new TargetItem(
                    month.format(monthFormatter),
                    target > 0 ? String.format("Rp.%,.0f", target) : "-",
                    String.format("Rp.%,.0f", expense),
                    status
            ));
        }

        targetHistoryTable.setItems(historyData);

        // Set cell value factories
        monthCol.setCellValueFactory(cellData -> cellData.getValue().monthProperty());
        targetAmountCol.setCellValueFactory(cellData -> cellData.getValue().targetAmountProperty());
        actualExpenseCol.setCellValueFactory(cellData -> cellData.getValue().actualExpenseProperty());
        statusCol.setCellValueFactory(cellData -> cellData.getValue().statusProperty());
    }

    private double calculateMonthExpense(YearMonth month) {
        return DataStore.getTodos(currentUserId).stream()
                .filter(item ->
                        "Pengeluaran".equals(item.getTipeTransaksi()) &&
                                isSameMonth(item.getTanggal(), month))
                .mapToDouble(keuanganItem::getNominalDouble)
                .sum();
    }

    @FXML
    private void saveTarget() {
        try {
            double targetAmount = Double.parseDouble(targetField.getText());
            String category = categoryCombo.getValue();

            // Simpan target ke database
            DataStore.saveTarget(currentUserId, targetAmount, category);

            // Refresh UI
            refreshCurrentTargetStatus();
            loadTargetHistory();

            showAlert("Sukses", "Target berhasil disimpan!", Alert.AlertType.INFORMATION);
        } catch (NumberFormatException e) {
            showAlert("Error", "Format nominal tidak valid", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void deleteTarget() {
        // Hapus target dari database
        DataStore.deleteCurrentTarget(currentUserId);
        refreshCurrentTargetStatus();
        loadTargetHistory();
        showAlert("Sukses", "Target berhasil dihapus", Alert.AlertType.INFORMATION);
    }

    @FXML
    private void closeWindow() {
        Stage stage = (Stage) targetField.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}