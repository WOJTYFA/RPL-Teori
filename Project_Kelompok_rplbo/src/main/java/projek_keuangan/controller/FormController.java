package projek_keuangan.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import projek_keuangan.data.DataStore;
import projek_keuangan.item.keuanganItem;

import java.time.LocalDate;

public class FormController {
    @FXML private DatePicker datePicker;
    @FXML private TextField nominalField;
    @FXML private TextArea catatanArea;
    @FXML private ComboBox<String> kategoriCombo;
    @FXML private TextField kategoriBaruField;

    private String currentUser;
    private keuanganItem editingItem;
    private Runnable onSaveCallback;

    @FXML
    private void initialize() {
        // No initialization needed for nominal field
    }

    public void initData(String user, keuanganItem item, Runnable onSave) {
        this.currentUser = user;
        this.editingItem = item;
        this.onSaveCallback = onSave;

        // Load categories into combo box
        kategoriCombo.getItems().addAll(
                DataStore.getTodos(user).stream()
                        .map(keuanganItem::getKategori)
                        .distinct()
                        .toList()
        );

        if (item != null) {
            datePicker.setValue(LocalDate.parse(item.getTanggal()));
            nominalField.setText(item.getNominal());
            catatanArea.setText(item.getCatatan());
            kategoriCombo.setValue(item.getKategori());
        }
    }

    @FXML
    private void handleAddCategory() {
        String newCategory = kategoriBaruField.getText().trim();
        if (!newCategory.isEmpty() && !kategoriCombo.getItems().contains(newCategory)) {
            kategoriCombo.getItems().add(newCategory);
            kategoriCombo.setValue(newCategory);
        }
    }

    @FXML
    private void handleSaveTask() {
        String tanggal = datePicker.getValue() != null ? datePicker.getValue().toString() : "";
        String nominal = nominalField.getText().trim();
        String catatan = catatanArea.getText().trim();
        String kategori = kategoriCombo.getValue();

        if (tanggal.isEmpty() || nominal.isEmpty() || catatan.isEmpty() || kategori == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Semua field harus diisi!");
            alert.showAndWait();
            return;
        }

        keuanganItem newItem = new keuanganItem(tanggal, nominal, catatan, kategori);

        if (editingItem == null) {
            DataStore.addTodo(currentUser, newItem);
        } else {
            DataStore.editTodo(currentUser, editingItem, newItem);
        }

        if (onSaveCallback != null) onSaveCallback.run();

        ((Stage) datePicker.getScene().getWindow()).close();
    }
}
