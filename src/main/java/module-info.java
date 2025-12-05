module businessmanager.surface {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires java.desktop;

    opens businessmanager.surface to javafx.fxml;
    exports businessmanager.surface;
}
