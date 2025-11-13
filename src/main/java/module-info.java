module businessmanager.surface {
    requires javafx.controls;
    requires javafx.fxml;

    opens businessmanager.surface to javafx.fxml;
    exports businessmanager.surface;
}