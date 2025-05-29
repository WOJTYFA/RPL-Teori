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

public class RegisterController {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label messageLabel;

    @FXML
    private void handleRegister() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            messageLabel.setText("Username and password cannot be empty.");
            messageLabel.setStyle("-fx-text-fill: red;");
            return;
        }
        if (username.length() < 3) {
            messageLabel.setText("Username must be at least 3 characters.");
            messageLabel.setStyle("-fx-text-fill: red;");
            return;
        }
        if (password.length() < 6) {
            messageLabel.setText("Password must be at least 6 characters.");
            messageLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        boolean success = DataStore.addUser(new User(username, password));
        if (success) {
            messageLabel.setText("Registration successful. You can now login.");
            messageLabel.setStyle("-fx-text-fill: green;");
            usernameField.clear();
            passwordField.clear();
        } else {
            messageLabel.setText("Username already exists or registration failed.");
            messageLabel.setStyle("-fx-text-fill: red;");
        }
    }

    @FXML
    private void switchToLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/projek_keuangan/login-view.fxml"));
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(new Scene(loader.load()));
            stage.setTitle("Login");
        } catch (Exception e) {
            e.printStackTrace();
            if (messageLabel != null) {
                messageLabel.setText("Error switching to login screen.");
                messageLabel.setStyle("-fx-text-fill: red;");
            }
        }
    }
}