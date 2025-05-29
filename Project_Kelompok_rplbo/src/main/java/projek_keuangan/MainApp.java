package projek_keuangan;

import java.io.IOException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import projek_keuangan.controller.keuanganController;
import projek_keuangan.manager.DatabaseManager;
import projek_keuangan.manager.SessionManager;

public class MainApp extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        DatabaseManager.initializeDatabase(); // Inisialisasi database
        String currentUser = SessionManager.getLoggedInUser();
        
        if (currentUser != null) {
            System.out.println("User " + currentUser + " found in session. Opening main app.");
            FXMLLoader loader = new FXMLLoader(MainApp.class.getResource("/projek_keuangan/keuangan-view.fxml"));
            Scene scene = new Scene(loader.load());
            stage.setTitle("Aplikasi Keuangan - " + currentUser);
            stage.setScene(scene);
            stage.show();
            
            keuanganController controller = loader.getController();
            controller.setCurrentUser(currentUser);
        } else {
            System.out.println("No user in session. Opening login screen.");
            FXMLLoader loader = new FXMLLoader(MainApp.class.getResource("/projek_keuangan/login-view.fxml"));
            Scene scene = new Scene(loader.load());
            stage.setTitle("Login");
            stage.setScene(scene);
            stage.show();
        }
    }

    public static void main(String[] args) {
        launch();
    }
}