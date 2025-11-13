module businessmanager.surface {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires java.base;

    opens businessmanager.surface to javafx.fxml;
    exports businessmanager.surface;
}
