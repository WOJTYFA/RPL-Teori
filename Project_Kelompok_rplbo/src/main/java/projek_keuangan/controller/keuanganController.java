package projek_keuangan.controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;
import projek_keuangan.data.DataStore;
import projek_keuangan.item.keuanganItem;

public class keuanganController {
    @FXML private TableView<keuanganItem> todoTable;
    @FXML private TableColumn<keuanganItem, String> tanggalCol, nominalCol, catatanCol, kategoriCol;
    @FXML private ComboBox<String> filterCombo;
    private String currentUser;

    public void setCurrentUser(String username) {
        this.currentUser = username;
        loadTodos();
        loadCategories();
    }

    private void loadTodos() {
        todoTable.setItems(FXCollections.observableArrayList(DataStore.getTodos(currentUser)));
    }

    private void loadCategories() {
        filterCombo.getItems().clear();
        filterCombo.getItems().add("All");
        DataStore.getTodos(currentUser).stream().map(keuanganItem::getKategori).distinct().forEach(filterCombo.getItems()::add);
    }

    // ✅ Tambahkan method refreshData
    public void refreshData() {
        loadTodos();
        loadCategories();
    }

//    @FXML
//    private void openPieChart() {
//        try {
//            FXMLLoader loader = new FXMLLoader(getClass().getResource("/projek_keuangan/piechart.fxml"));
//            Stage stage = new Stage();
//            stage.setScene(new Scene(loader.load()));
//            stage.setTitle("Grafik Pengeluaran");
//
//            PieChartController controller = loader.getController();
//            controller.setCurrentUser(currentUser);
//
//            stage.show();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

    @FXML
    private void onBtnClickGrafik() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/projek_keuangan/piechart.fxml"));
            Stage stage = new Stage();
            stage.setScene(new Scene(loader.load()));
            stage.setTitle("Grafik Pengeluaran");

            PieChartController controller = loader.getController();
            controller.setCurrentUser(currentUser);

            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @FXML
    private void addTodo() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/projek_keuangan/form.fxml"));
            Stage stage = new Stage();
            stage.setScene(new Scene(loader.load()));
            stage.setTitle("Tambah Transaksi");

            FormController controller = loader.getController();
            controller.initData(currentUser, null, this::refreshData);

            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void deleteTodo() {
        keuanganItem selected = todoTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            DataStore.removeTodo(currentUser, selected);
            loadTodos();
            loadCategories();
        }
    }

    @FXML
    private void editTodo() {
        keuanganItem selected = todoTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/projek_keuangan/form.fxml"));
                Stage stage = new Stage();
                stage.setScene(new Scene(loader.load()));
                stage.setTitle("Edit Transaksi");

                FormController controller = loader.getController();
                controller.initData(currentUser, selected, this::refreshData);

                stage.show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void filterTodos() {
        String filter = filterCombo.getValue();
        if (filter == null || filter.equals("All")) {
            loadTodos();
        } else {
            todoTable.setItems(FXCollections.observableArrayList(
                    DataStore.getTodos(currentUser).stream().filter(i -> i.getKategori().equals(filter)).toList()
            ));
        }
    }

    @FXML
    private void logout() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/projek_keuangan/login-view.fxml"));
            Stage stage = (Stage) todoTable.getScene().getWindow();
            stage.setScene(new Scene(loader.load()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void initialize() {
        tanggalCol.setCellValueFactory(data -> data.getValue().tanggalProperty());
        nominalCol.setCellValueFactory(data -> data.getValue().nominalProperty());
        catatanCol.setCellValueFactory(data -> data.getValue().catatanProperty());
        kategoriCol.setCellValueFactory(data -> data.getValue().kategoriProperty());
    }
}
