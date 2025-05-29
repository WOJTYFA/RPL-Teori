package projek_keuangan.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import projek_keuangan.data.DataStore;
import projek_keuangan.item.User;
import projek_keuangan.manager.SessionManager; // Import SessionManager

public class LoginController {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label messageLabel;

    @FXML
    private void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            messageLabel.setText("Username and password cannot be empty.");
            return;
        }

        User user = DataStore.findUser(username, password);
        if (user != null) {
            SessionManager.loginUser(user.getUsername()); // Simpan sesi pengguna
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/projek_keuangan/keuangan_view.fxml"));
                Stage stage = (Stage) usernameField.getScene().getWindow();
                Scene scene = new Scene(loader.load());
                keuanganController controller = loader.getController();
                controller.setCurrentUser(user.getUsername()); // Kirim username ke keuanganController
                stage.setScene(scene);
                stage.setTitle("Manajemen Keuangan - " + user.getUsername()); // Set title di sini juga
            } catch (Exception e) {
                e.printStackTrace();
                messageLabel.setText("Failed to load main application window.");
            }
        } else {
            messageLabel.setText("Invalid username or password.");
        }
    }

    @FXML
    private void switchToRegister() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/projek_keuangan/register_view.fxml"));
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(new Scene(loader.load()));
            stage.setTitle("Register New User");
        } catch (Exception e) {
            e.printStackTrace();
            messageLabel.setText("Failed to load registration window.");
        }
    }
}