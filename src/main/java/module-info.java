module com.getiondereservas.businessmanager {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    opens com.getiondereservas.businessmanager to javafx.fxml;
    exports com.getiondereservas.businessmanager;
    exports com.getiondereservas.businessmanager.surfaces;
    opens com.getiondereservas.businessmanager.surfaces to javafx.fxml;
    exports com.getiondereservas.businessmanager.database;
    opens com.getiondereservas.businessmanager.database to javafx.fxml;
}
