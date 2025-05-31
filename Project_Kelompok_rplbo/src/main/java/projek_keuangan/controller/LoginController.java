package projek_keuangan.controller;

import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.util.Duration;
import projek_keuangan.data.DataStore;
import projek_keuangan.item.User;
import projek_keuangan.manager.SessionManager;

public class LoginController {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label messageLabel;

    @FXML
    private void initialize() {
        // Clear message when user starts typing
        usernameField.textProperty().addListener((obs, oldText, newText) -> clearMessage());
        passwordField.textProperty().addListener((obs, oldText, newText) -> clearMessage());

        // Add enter key support
        usernameField.setOnAction(e -> passwordField.requestFocus());
        passwordField.setOnAction(e -> handleLogin());
    }

    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        // Validation
        if (username.isEmpty() || password.isEmpty()) {
            showMessage("Username dan password tidak boleh kosong!", "error");
            return;
        }

        // Disable fields during login attempt
        setFieldsDisabled(true);
        showMessage("Sedang memverifikasi...", "loading");

        // Simulate network delay for better UX (optional)
        Platform.runLater(() -> {
            try {
                User user = DataStore.findUser(username, password);
                if (user != null) {
                    // Login successful
                    SessionManager.loginUser(user.getUsername());
                    showMessage("Login berhasil! Selamat datang, " + user.getUsername(), "success");

                    // Delay for user to see success message
                    Platform.runLater(() -> {
                        try {
                            FXMLLoader loader = new FXMLLoader(getClass().getResource("/projek_keuangan/keuangan-view.fxml"));
                            Stage stage = (Stage) usernameField.getScene().getWindow();
                            Scene scene = new Scene(loader.load());
                            keuanganController controller = loader.getController();
                            controller.setCurrentUser(user.getUsername());
                            stage.setScene(scene);
                            stage.setTitle("MoneyTracker - " + user.getUsername());
                        } catch (Exception e) {
                            e.printStackTrace();
                            showMessage("Gagal memuat aplikasi utama. Silakan coba lagi.", "error");
                            setFieldsDisabled(false);
                        }
                    });
                } else {
                    // Login failed
                    showMessage("Username atau password salah!", "error");
                    setFieldsDisabled(false);

                    // Add shake animation to form
                    addShakeAnimation();

                    // Clear password field
                    passwordField.clear();
                    passwordField.requestFocus();
                }
            } catch (Exception e) {
                e.printStackTrace();
                showMessage("Terjadi kesalahan sistem. Silakan coba lagi.", "error");
                setFieldsDisabled(false);
            }
        });
    }

    @FXML
    private void switchToRegister() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/projek_keuangan/register_view.fxml"));
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(new Scene(loader.load()));
            stage.setTitle("MoneyTracker - Daftar Akun Baru");
        } catch (Exception e) {
            e.printStackTrace();
            showMessage("Gagal memuat halaman registrasi.", "error");
        }
    }

    private void showMessage(String message, String type) {
        messageLabel.setText(message);
        messageLabel.setVisible(true);

        // Remove existing style classes
        messageLabel.getStyleClass().removeAll("error-message", "success-message", "loading-message");

        // Add appropriate style class
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

        // Fade in animation
        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), messageLabel);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);
        fadeIn.play();
    }

    private void clearMessage() {
        if (messageLabel.isVisible()) {
            FadeTransition fadeOut = new FadeTransition(Duration.millis(200), messageLabel);
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.0);
            fadeOut.setOnFinished(e -> {
                messageLabel.setVisible(false);
                messageLabel.setText("");
            });
            fadeOut.play();
        }
    }

    private void setFieldsDisabled(boolean disabled) {
        usernameField.setDisable(disabled);
        passwordField.setDisable(disabled);
    }

    private void addShakeAnimation() {
        // Shake animation for wrong credentials
        ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(100), usernameField.getParent());
        scaleTransition.setFromX(1.0);
        scaleTransition.setToX(0.95);
        scaleTransition.setAutoReverse(true);
        scaleTransition.setCycleCount(4);

        scaleTransition.play();
    }
}