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

public class LoginController {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label messageLabel;

    @FXML
    private void handleLogin() {
        User user = DataStore.findUser(usernameField.getText(), passwordField.getText());
        if (user != null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/projek_keuangan/keuangan_view.fxml"));
                Stage stage = (Stage) usernameField.getScene().getWindow();
                Scene scene = new Scene(loader.load());
                keuanganController controller = loader.getController();
                controller.setCurrentUser(user.getUsername());
                stage.setScene(scene);
            } catch (Exception e) { e.printStackTrace(); }
        } else {
            messageLabel.setText("Invalid credentials.");
        }
    }

    @FXML
    private void switchToRegister() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/projek_keuangan/register_view.fxml"));
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(new Scene(loader.load()));
        } catch (Exception e) { e.printStackTrace(); }
    }
}
