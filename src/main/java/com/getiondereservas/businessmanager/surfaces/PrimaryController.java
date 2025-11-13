package com.getiondereservas.businessmanager.surfaces;

import java.io.IOException;

import com.getiondereservas.businessmanager.App;
import javafx.fxml.FXML;

public class PrimaryController {

    @FXML
    private void switchToSecondary() throws IOException {
        App.setRoot("secondary");
    }
}
