module projek_keuangan {
    requires javafx.controls;
    requires javafx.fxml;

    exports projek_keuangan;
    opens projek_keuangan to javafx.fxml;

    exports projek_keuangan.item;
    opens projek_keuangan.item to javafx.fxml;

    exports projek_keuangan.data;
    opens projek_keuangan.data to javafx.fxml;

    exports projek_keuangan.controller;
    opens projek_keuangan.controller to javafx.fxml;
}
