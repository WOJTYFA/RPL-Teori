package projek_keuangan.controller;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.stage.Stage;
import projek_keuangan.data.DataStore;
import projek_keuangan.item.keuanganItem;

public class FormController {
    @FXML private DatePicker datePicker;
    @FXML private TextField nominalField;
    @FXML private TextArea catatanArea;
    @FXML private ComboBox<String> kategoriCombo;
    @FXML private TextField kategoriBaruField;
    @FXML private RadioButton radioPemasukan;
    @FXML private RadioButton radioPengeluaran;

    // Kita akan membuat ToggleGroup secara manual di initialize
    private ToggleGroup tipeTransaksiGroupManual;

    private int currentUserId;
    private String currentUsername;
    private keuanganItem editingItem;
    private Runnable onSaveCallback;

    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE;

    @FXML
    private void initialize() {
        // Initialize ToggleGroup manually
        tipeTransaksiGroupManual = new ToggleGroup();
        radioPemasukan.setToggleGroup(tipeTransaksiGroupManual);
        radioPengeluaran.setToggleGroup(tipeTransaksiGroupManual);

        nominalField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("Rp\\.\\d*")) {
                String plainNumber = newValue.replaceAll("[^\\d]", "");
                if (!plainNumber.isEmpty()) {
                    long number = Long.parseLong(plainNumber);
                    String formatted = String.format("Rp.%,d", number);
                    nominalField.setText(formatted);
                } else {
                    nominalField.setText("Rp.");
                }
            }
        });
        datePicker.setValue(LocalDate.now());
        
        // Set default tipe transaksi
        radioPemasukan.setSelected(true);
    }

    public void initData(int userId, String username, keuanganItem item, Runnable onSave) {
        this.currentUserId = userId;
        this.currentUsername = username;
        this.editingItem = item;
        this.onSaveCallback = onSave;

        kategoriCombo.getItems().clear();
        kategoriCombo.getItems().addAll(
                DataStore.getTodos(this.currentUserId).stream()
                        .map(keuanganItem::getKategori)
                        .distinct()
                        .sorted()
                        .toList()
        );

        if (item != null) {
            try {
                if (item.getTanggal() != null && !item.getTanggal().isEmpty()) {
                    datePicker.setValue(LocalDate.parse(item.getTanggal(), dateFormatter));
                }
            } catch (DateTimeParseException e) {
                System.err.println("Failed to parse date from item: " + item.getTanggal() + " - " + e.getMessage());
                datePicker.setValue(LocalDate.now());
            }
            nominalField.setText(item.getNominal());
            catatanArea.setText(item.getCatatan());
            kategoriCombo.setValue(item.getKategori());
            
            // Set tipe transaksi
            if ("Pemasukan".equals(item.getTipeTransaksi())) {
                radioPemasukan.setSelected(true);
            } else {
                radioPengeluaran.setSelected(true);
            }
        } else {
            datePicker.setValue(LocalDate.now());
            nominalField.setText("Rp.");
            catatanArea.clear();
            kategoriCombo.setValue(null);
            radioPemasukan.setSelected(true);
        }
    }

    @FXML
    private void handleAddCategory() {
        String newCategory = kategoriBaruField.getText().trim();
        if (!newCategory.isEmpty() && !kategoriCombo.getItems().contains(newCategory)) {
            kategoriCombo.getItems().add(newCategory);
            kategoriCombo.setValue(newCategory);
            kategoriBaruField.clear();
        } else if (newCategory.isEmpty()){
            showAlert(Alert.AlertType.WARNING, "Input Error", "Category name cannot be empty.");
        } else {
            showAlert(Alert.AlertType.INFORMATION, "Info", "Category already exists or is empty.");
        }
    }

    @FXML
    private void handleSaveTask() {
        String tanggal = (datePicker.getValue() != null) ? datePicker.getValue().format(dateFormatter) : "";
        String nominalInput = nominalField.getText().trim();
        String catatan = catatanArea.getText().trim();
        String kategori = kategoriCombo.getValue();
        // Gunakan selected radio button dari ToggleGroup manual
        String tipeTransaksi = ((RadioButton) tipeTransaksiGroupManual.getSelectedToggle()).getText();

        if (tanggal.isEmpty() || nominalInput.equals("Rp.") || nominalInput.isEmpty() || catatan.isEmpty() || kategori == null || kategori.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Semua field harus diisi!");
            return;
        }

        String nominalAngka = nominalInput.replace("Rp.", "").replace(".", "");
        if (!nominalAngka.matches("\\d+")) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Nominal harus berupa angka setelah 'Rp.'.");
            return;
        }

        try {
            double nominalValue = Double.parseDouble(nominalAngka);
            if (nominalValue <= 0) {
                showAlert(Alert.AlertType.WARNING, "Validation Error", "Nominal harus lebih besar dari 0.");
                return;
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Format nominal tidak valid.");
            return;
        }

        keuanganItem newItem = new keuanganItem(tanggal, nominalInput, catatan, kategori, tipeTransaksi);

        if (editingItem == null) {
            DataStore.addTodo(currentUserId, newItem);
        } else {
            newItem.setId(editingItem.getId());
            DataStore.editTodo(currentUserId, editingItem, newItem);
        }

        if (onSaveCallback != null) {
            onSaveCallback.run();
        }

        ((Stage) datePicker.getScene().getWindow()).close();
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}