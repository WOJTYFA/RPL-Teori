package projek_keuangan;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import projek_keuangan.controller.keuanganController;
import projek_keuangan.data.DataStore;
import projek_keuangan.manager.DatabaseManager;
import projek_keuangan.item.User;
import projek_keuangan.manager.SessionManager;

public class MainApp extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        DatabaseManager.initializeDatabase(); // Inisialisasi database
        String loggedInUsername = SessionManager.getLoggedInUser();

        FXMLLoader loader;
        Scene scene;

        if (loggedInUsername != null) {
            User user = DataStore.getUserByUsername(loggedInUsername);
            if (user != null) {
                System.out.println("User " + loggedInUsername + " found in session. Opening main app.");
                loader = new FXMLLoader(getClass().getResource("/projek_keuangan/keuangan_view.fxml"));
                scene = new Scene(loader.load());
                keuanganController controller = loader.getController();
                controller.setCurrentUser(loggedInUsername);
                stage.setTitle("Manajemen Keuangan - " + loggedInUsername);
            } else {
                System.out.println("User " + loggedInUsername + " from session not found in DB. Clearing session.");
                SessionManager.logoutUser();
                loader = new FXMLLoader(getClass().getResource("/projek_keuangan/login-view.fxml"));
                scene = new Scene(loader.load());
                stage.setTitle("Login");
            }
        } else {
            System.out.println("No active session. Opening login page.");
            loader = new FXMLLoader(getClass().getResource("/projek_keuangan/login-view.fxml"));
            scene = new Scene(loader.load());
            stage.setTitle("Login");
        }
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}