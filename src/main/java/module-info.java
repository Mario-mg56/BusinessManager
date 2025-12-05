module businessmanager.surface {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires java.base;
    requires java.desktop; 
    requires jasperreports;

    opens businessmanager.surface to javafx.fxml;
    exports businessmanager.surface;
}
