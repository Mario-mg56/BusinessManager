module com.getiondereservas.businessmanager {
    requires javafx.controls;
    requires javafx.fxml;

    opens com.getiondereservas.businessmanager to javafx.fxml;
    exports com.getiondereservas.businessmanager;
}
