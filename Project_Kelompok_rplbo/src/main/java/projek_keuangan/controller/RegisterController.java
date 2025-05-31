package projek_keuangan.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import projek_keuangan.data.DataStore;
import projek_keuangan.item.User;

import java.net.URL;
import java.util.ResourceBundle;

public class RegisterController implements Initializable {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label messageLabel;
    @FXML private Button registerButton;
    @FXML private Button backButton;

    // Password strength indicators
    @FXML private Region strengthBar1;
    @FXML private Region strengthBar2;
    @FXML private Region strengthBar3;
    @FXML private Region strengthBar4;
    @FXML private Label strengthLabel;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Add listener for password strength checking
        passwordField.textProperty().addListener((observable, oldValue, newValue) -> {
            updatePasswordStrength(newValue);
        });

        // Add listener for real-time validation
        usernameField.textProperty().addListener((observable, oldValue, newValue) -> {
            clearMessage();
        });
    }

    @FXML
    private void handleRegister() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        // Clear previous messages
        clearMessage();

        // Validation
        if (username.isEmpty() || password.isEmpty()) {
            showMessage("Username and password cannot be empty.", "error");
            return;
        }

        if (username.length() < 3) {
            showMessage("Username must be at least 3 characters long.", "error");
            return;
        }

        if (password.length() < 6) {
            showMessage("Password must be at least 6 characters long.", "error");
            return;
        }

        // Check password strength
        int strength = calculatePasswordStrength(password);
        if (strength < 2) {
            showMessage("Please use a stronger password with mixed characters.", "error");
            return;
        }

        // Attempt registration
        try {
            boolean success = DataStore.addUser(new User(username, password));
            if (success) {
                showMessage("Registration successful! You can now login.", "success");

                // Clear fields after successful registration
                usernameField.clear();
                passwordField.clear();
                resetPasswordStrength();

                // Optional: Auto-switch to login after delay
                // switchToLoginAfterDelay();

            } else {
                showMessage("Username already exists. Please choose a different username.", "error");
            }
        } catch (Exception e) {
            showMessage("Registration failed. Please try again.", "error");
            e.printStackTrace();
        }
    }

    @FXML
    private void switchToLogin() {
        try {
            // Debug: Print current working directory
            System.out.println("Current class path: " + getClass().getResource("/"));

            // Try different possible paths
            URL loginFXML = null;
            String[] possiblePaths = {
                    "/projek_keuangan/login-view.fxml",
                    "/login-view.fxml",
                    "/projek_keuangan/login.fxml",
                    "/login.fxml",
                    "login-view.fxml",
                    "login.fxml"
            };

            for (String path : possiblePaths) {
                loginFXML = getClass().getResource(path);
                if (loginFXML != null) {
                    System.out.println("Found FXML at: " + path);
                    break;
                }
            }

            if (loginFXML == null) {
                showMessage("Login FXML file not found. Please check file location.", "error");
                return;
            }

            FXMLLoader loader = new FXMLLoader(loginFXML);
            Stage stage = (Stage) usernameField.getScene().getWindow();
            Scene scene = new Scene(loader.load());

            // Try to apply stylesheet if it exists
            URL cssURL = getClass().getResource("/projek_keuangan/styles/login-styles.css");
            if (cssURL != null) {
                scene.getStylesheets().add(cssURL.toExternalForm());
            } else {
                System.out.println("CSS file not found, continuing without styles");
            }

            stage.setScene(scene);
            stage.setTitle("Login - Financial Management");

        } catch (Exception e) {
            e.printStackTrace();
            showMessage("Error switching to login screen: " + e.getMessage(), "error");
        }
    }

    private void updatePasswordStrength(String password) {
        int strength = calculatePasswordStrength(password);

        // Reset all bars
        resetPasswordStrength();

        // Update bars based on strength
        switch (strength) {
            case 0:
                strengthLabel.setText("Password strength: Very Weak");
                strengthLabel.getStyleClass().removeAll("strength-text-weak", "strength-text-medium", "strength-text-strong");
                strengthLabel.getStyleClass().add("strength-text-weak");
                break;
            case 1:
                strengthBar1.getStyleClass().add("strength-weak");
                strengthLabel.setText("Password strength: Weak");
                strengthLabel.getStyleClass().removeAll("strength-text-weak", "strength-text-medium", "strength-text-strong");
                strengthLabel.getStyleClass().add("strength-text-weak");
                break;
            case 2:
                strengthBar1.getStyleClass().add("strength-medium");
                strengthBar2.getStyleClass().add("strength-medium");
                strengthLabel.setText("Password strength: Fair");
                strengthLabel.getStyleClass().removeAll("strength-text-weak", "strength-text-medium", "strength-text-strong");
                strengthLabel.getStyleClass().add("strength-text-medium");
                break;
            case 3:
                strengthBar1.getStyleClass().add("strength-medium");
                strengthBar2.getStyleClass().add("strength-medium");
                strengthBar3.getStyleClass().add("strength-medium");
                strengthLabel.setText("Password strength: Good");
                strengthLabel.getStyleClass().removeAll("strength-text-weak", "strength-text-medium", "strength-text-strong");
                strengthLabel.getStyleClass().add("strength-text-medium");
                break;
            case 4:
                strengthBar1.getStyleClass().add("strength-strong");
                strengthBar2.getStyleClass().add("strength-strong");
                strengthBar3.getStyleClass().add("strength-strong");
                strengthBar4.getStyleClass().add("strength-strong");
                strengthLabel.setText("Password strength: Strong");
                strengthLabel.getStyleClass().removeAll("strength-text-weak", "strength-text-medium", "strength-text-strong");
                strengthLabel.getStyleClass().add("strength-text-strong");
                break;
        }
    }

    private int calculatePasswordStrength(String password) {
        if (password == null || password.isEmpty()) {
            return 0;
        }

        int score = 0;

        // Length check
        if (password.length() >= 8) score++;
        if (password.length() >= 12) score++;

        // Character variety checks
        if (password.matches(".*[a-z].*")) score++; // lowercase
        if (password.matches(".*[A-Z].*")) score++; // uppercase
        if (password.matches(".*[0-9].*")) score++; // numbers
        if (password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*")) score++; // special chars

        // Return normalized score (0-4)
        return Math.min(4, Math.max(0, score - 1));
    }

    private void resetPasswordStrength() {
        strengthBar1.getStyleClass().removeAll("strength-weak", "strength-medium", "strength-strong");
        strengthBar2.getStyleClass().removeAll("strength-weak", "strength-medium", "strength-strong");
        strengthBar3.getStyleClass().removeAll("strength-weak", "strength-medium", "strength-strong");
        strengthBar4.getStyleClass().removeAll("strength-weak", "strength-medium", "strength-strong");

        strengthLabel.setText("Password strength: Enter password");
        strengthLabel.getStyleClass().removeAll("strength-text-weak", "strength-text-medium", "strength-text-strong");
    }

    private void showMessage(String text, String type) {
        messageLabel.setText(text);
        messageLabel.getStyleClass().removeAll("error-message", "success-message", "loading-message");
        messageLabel.getStyleClass().addAll("message-label", "visible");

        switch (type) {
            case "error":
                messageLabel.getStyleClass().add("error-message");
                break;
            case "success":
                messageLabel.getStyleClass().add("success-message");
                break;
            case "loading":
                messageLabel.getStyleClass().add("loading-message");
                break;
        }
    }

    private void clearMessage() {
        messageLabel.setText("");
        messageLabel.getStyleClass().removeAll("visible", "error-message", "success-message", "loading-message");
    }
}