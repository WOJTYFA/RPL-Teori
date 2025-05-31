package projek_keuangan.controller;

import java.io.IOException;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.stage.Stage;
import projek_keuangan.data.DataStore;
import projek_keuangan.item.keuanganItem;
import projek_keuangan.manager.SessionManager;

public class keuanganController {
    @FXML private TableView<keuanganItem> todoTable;
    @FXML private TableColumn<keuanganItem, String> tanggalCol, nominalCol, catatanCol, kategoriCol, tipeTransaksiCol;
    @FXML private ComboBox<String> filterCombo;
    @FXML private Label saldoLabel;

    private String currentUsername;
    private int currentUserId = -1;

    public void setCurrentUser(String username) {
        this.currentUsername = username;
        this.currentUserId = DataStore.getUserId(username);

        if (this.currentUserId == -1) {
            System.err.println("Fatal error: Could not retrieve ID for user: " + username);
            showAlert(Alert.AlertType.ERROR, "Error", "User data could not be loaded. Please try logging in again.");
            logout();
            return;
        }
        loadTodos();
        loadCategories();
        updateSaldo();
    }

    private void loadTodos() {
        if (currentUserId == -1) return;
        todoTable.setItems(FXCollections.observableArrayList(DataStore.getTodos(currentUserId)));
        updateSaldo(); // Update saldo after loading todos
    }

    private void loadCategories() {
        if (currentUserId == -1) return;
        filterCombo.getItems().clear();
        filterCombo.getItems().add("All");
        DataStore.getTodos(currentUserId).stream()
                .map(keuanganItem::getKategori)
                .distinct()
                .sorted()
                .forEach(filterCombo.getItems()::add);
        filterCombo.setValue("All");
    }

    public void refreshData() {
        loadCategories();
        filterCombo.setValue("All");
        loadTodos();
        updateSaldo(); // Update saldo after refreshing data
    }

    private void updateSaldo() {
        if (currentUserId == -1) {
            saldoLabel.setText("Saldo: N/A");
            return;
        }
        double totalSaldo = DataStore.getTodos(currentUserId).stream()
                .mapToDouble(item -> {
                    double nominal = item.getNominalDouble();
                    if ("Pemasukan".equals(item.getTipeTransaksi())) {
                        return nominal;
                    } else {
                        return -nominal; // Pengeluaran mengurangi saldo
                    }
                })
                .sum();
        saldoLabel.setText(String.format("Saldo: Rp.%,.0f", totalSaldo));
    }

    @FXML
    private void onBtnClickGrafik() {
        if (currentUserId == -1) {
            showAlert(Alert.AlertType.WARNING, "Warning", "User data not loaded. Cannot show chart.");
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/projek_keuangan/piechart.fxml"));
            Stage stage = new Stage();
            stage.setScene(new Scene(loader.load()));
            stage.setTitle("Grafik Pengeluaran - " + currentUsername);

            PieChartController controller = loader.getController();
            controller.setCurrentUsernameAndId(currentUsername, currentUserId);

            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Could not load pie chart view.");
        }
    }

    @FXML
    private void addTodo() {
        if (currentUserId == -1) {
            showAlert(Alert.AlertType.WARNING, "Warning", "User data not loaded. Cannot add transaction.");
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/projek_keuangan/form.fxml"));
            Stage stage = new Stage();
            stage.setScene(new Scene(loader.load()));
            stage.setTitle("Tambah Transaksi");

            FormController controller = loader.getController();
            controller.initData(currentUserId, currentUsername, null, this::refreshData);

            stage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Could not load form", "Error loading form: " + e.getMessage());
        }
    }

    @FXML
    private void deleteTodo() {
        if (currentUserId == -1) return;
        keuanganItem selected = todoTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            DataStore.removeTodo(currentUserId, selected);
            refreshData();
        } else {
            showAlert(Alert.AlertType.INFORMATION, "Info", "No item selected for deletion.");
        }
    }

    @FXML
    private void editTodo() {
        if (currentUserId == -1) return;
        keuanganItem selected = todoTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/projek_keuangan/form.fxml"));
                Stage stage = new Stage();
                stage.setScene(new Scene(loader.load()));
                stage.setTitle("Edit Transaksi");

                FormController controller = loader.getController();
                controller.initData(currentUserId, currentUsername, selected, this::refreshData);

                stage.showAndWait();
            } catch (Exception e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Error", "Could not load form to edit transaction.");
            }
        } else {
            showAlert(Alert.AlertType.INFORMATION, "Info", "No item selected for editing.");
        }
    }

    @FXML
    private void filterTodos() {
        if (currentUserId == -1) return;
        String filter = filterCombo.getValue();
        if (filter == null || filter.equals("All")) {
            loadTodos();
        } else {
            todoTable.setItems(FXCollections.observableArrayList(
                    DataStore.getTodos(currentUserId).stream().filter(i -> i.getKategori().equals(filter)).toList()
            ));
            updateSaldo(); // Update saldo after filtering
        }
    }

    @FXML
    private void logout() {
        SessionManager.logoutUser();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/projek_keuangan/login-view.fxml"));
            Stage stage = (Stage) todoTable.getScene().getWindow();
            stage.setScene(new Scene(loader.load()));
            stage.setTitle("Login");
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to return to login screen.");
        }
    }

    @FXML
    private void initialize() {
        tanggalCol.setCellValueFactory(data -> data.getValue().tanggalProperty());
        tipeTransaksiCol.setCellValueFactory(data -> data.getValue().tipeTransaksiProperty());
        nominalCol.setCellValueFactory(data -> data.getValue().nominalProperty());
        kategoriCol.setCellValueFactory(data -> data.getValue().kategoriProperty());
        catatanCol.setCellValueFactory(data -> data.getValue().catatanProperty());

        // Apply row style based on tipeTransaksi
        todoTable.setRowFactory(tv -> new TableRow<keuanganItem>() {
            @Override
            protected void updateItem(keuanganItem item, boolean empty) {
                super.updateItem(item, empty);
                getStyleClass().remove("pemasukan-row");
                getStyleClass().remove("pengeluaran-row");
                if (item != null && !empty) {
                    if ("Pemasukan".equals(item.getTipeTransaksi())) {
                        getStyleClass().add("pemasukan-row");
                    } else if ("Pengeluaran".equals(item.getTipeTransaksi())) {
                        getStyleClass().add("pengeluaran-row");
                    }
                }
            }
        });
    }

    @FXML
    private void openTargetWindow() {
        if (currentUserId == -1) {
            showAlert(Alert.AlertType.WARNING, "Warning", "User data not loaded. Cannot open target setting.");
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/projek_keuangan/target.fxml"));
            Stage stage = new Stage();
            stage.setScene(new Scene(loader.load()));
            stage.setTitle("Target Pengeluaran - " + currentUsername);

            TargetController controller = loader.getController();
            controller.initData(currentUserId, currentUsername);

            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Could not load target view.");
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}