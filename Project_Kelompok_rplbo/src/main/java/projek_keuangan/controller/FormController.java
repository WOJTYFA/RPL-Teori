package projek_keuangan.controller;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
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
    @FXML private Button saveButton;
    @FXML private Button clearButton;
    @FXML private Button addCategoryButton;

    // ToggleGroup untuk RadioButton
    private ToggleGroup tipeTransaksiGroup;

    private int currentUserId;
    private String currentUsername;
    private keuanganItem editingItem;
    private Runnable onSaveCallback;

    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE;

    @FXML
    private void initialize() {
        // Initialize ToggleGroup manually
        tipeTransaksiGroup = new ToggleGroup();
        radioPemasukan.setToggleGroup(tipeTransaksiGroup);
        radioPengeluaran.setToggleGroup(tipeTransaksiGroup);

        // Format nominal field dengan Rupiah
        nominalField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("Rp\\.\\s?\\d{1,3}(,\\d{3})*")) {
                String plainNumber = newValue.replaceAll("[^\\d]", "");
                if (!plainNumber.isEmpty()) {
                    try {
                        long number = Long.parseLong(plainNumber);
                        String formatted = String.format("Rp. %,d", number).replace(",", ".");
                        nominalField.setText(formatted);
                    } catch (NumberFormatException e) {
                        nominalField.setText("Rp. ");
                    }
                } else {
                    nominalField.setText("Rp. ");
                }
            }
        });

        // Set default values
        datePicker.setValue(LocalDate.now());
        radioPemasukan.setSelected(true);
        nominalField.setText("Rp. ");

        // Setup clear button action
        clearButton.setOnAction(e -> clearForm());
    }

    public void initData(int userId, String username, keuanganItem item, Runnable onSave) {
        this.currentUserId = userId;
        this.currentUsername = username;
        this.editingItem = item;
        this.onSaveCallback = onSave;

        // Load existing categories
        loadCategories();

        if (item != null) {
            // Edit mode - populate fields with existing data
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

            // Set transaction type
            if ("Pemasukan".equals(item.getTipeTransaksi())) {
                radioPemasukan.setSelected(true);
            } else {
                radioPengeluaran.setSelected(true);
            }
        } else {
            // New entry mode - set defaults
            clearForm();
        }
    }

    private void loadCategories() {
        if (kategoriCombo != null) {
            kategoriCombo.getItems().clear();
            kategoriCombo.getItems().addAll(
                    DataStore.getTodos(this.currentUserId).stream()
                            .map(keuanganItem::getKategori)
                            .distinct()
                            .filter(kategori -> kategori != null && !kategori.trim().isEmpty())
                            .sorted()
                            .toList()
            );
        }
    }

    @FXML
    private void handleAddCategory() {
        String newCategory = kategoriBaruField.getText().trim();
        if (!newCategory.isEmpty() && !kategoriCombo.getItems().contains(newCategory)) {
            kategoriCombo.getItems().add(newCategory);
            kategoriCombo.setValue(newCategory);
            kategoriBaruField.clear();
            showAlert(Alert.AlertType.INFORMATION, "Success", "Kategori '" + newCategory + "' berhasil ditambahkan!");
        } else if (newCategory.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Input Error", "Nama kategori tidak boleh kosong.");
        } else {
            showAlert(Alert.AlertType.INFORMATION, "Info", "Kategori sudah ada.");
        }
    }

    @FXML
    private void handleSaveTask() {
        // Validate inputs
        if (!validateInputs()) {
            return;
        }

        // Get form data
        String tanggal = datePicker.getValue().format(dateFormatter);
        String nominal = nominalField.getText().trim();
        String catatan = catatanArea.getText().trim();
        String kategori = kategoriCombo.getValue();
        String tipeTransaksi = ((RadioButton) tipeTransaksiGroup.getSelectedToggle()).getText();

        // Create new item
        keuanganItem newItem = new keuanganItem(tanggal, nominal, catatan, kategori, tipeTransaksi);

        try {
            if (editingItem == null) {
                // Add new item
                DataStore.addTodo(currentUserId, newItem);
                showAlert(Alert.AlertType.INFORMATION, "Success", "Transaksi berhasil disimpan!");
            } else {
                // Edit existing item
                newItem.setId(editingItem.getId());
                DataStore.editTodo(currentUserId, editingItem, newItem);
                showAlert(Alert.AlertType.INFORMATION, "Success", "Transaksi berhasil diperbarui!");
            }

            // Execute callback and close window
            if (onSaveCallback != null) {
                onSaveCallback.run();
            }

            closeWindow();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Terjadi kesalahan saat menyimpan data: " + e.getMessage());
        }
    }

    private boolean validateInputs() {
        // Check date
        if (datePicker.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Tanggal harus diisi!");
            return false;
        }

        // Check nominal
        String nominalInput = nominalField.getText().trim();
        if (nominalInput.equals("Rp. ") || nominalInput.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Nominal harus diisi!");
            return false;
        }

        // Validate nominal format and value
        String nominalAngka = nominalInput.replace("Rp. ", "").replace(".", "");
        if (!nominalAngka.matches("\\d+")) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Format nominal tidak valid.");
            return false;
        }

        try {
            double nominalValue = Double.parseDouble(nominalAngka);
            if (nominalValue <= 0) {
                showAlert(Alert.AlertType.WARNING, "Validation Error", "Nominal harus lebih besar dari 0.");
                return false;
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Format nominal tidak valid.");
            return false;
        }

        // Check description
        if (catatanArea.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Catatan/deskripsi harus diisi!");
            return false;
        }

        // Check category
        String kategori = kategoriCombo.getValue();
        if (kategori == null || kategori.trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Kategori harus dipilih!");
            return false;
        }

        // Check transaction type
        if (tipeTransaksiGroup.getSelectedToggle() == null) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Tipe transaksi harus dipilih!");
            return false;
        }

        return true;
    }

    private void clearForm() {
        datePicker.setValue(LocalDate.now());
        nominalField.setText("Rp. ");
        catatanArea.clear();
        kategoriCombo.setValue(null);
        kategoriBaruField.clear();
        radioPemasukan.setSelected(true);
    }

    private void closeWindow() {
        Stage stage = (Stage) datePicker.getScene().getWindow();
        stage.close();
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}