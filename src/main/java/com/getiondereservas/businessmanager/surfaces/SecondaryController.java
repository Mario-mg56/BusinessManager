package com.getiondereservas.businessmanager.surfaces;

import java.io.IOException;

import com.getiondereservas.businessmanager.App;
import javafx.fxml.FXML;

public class SecondaryController {

    @FXML
    private void switchToPrimary() throws IOException {
        App.setRoot("primary");
    }
}