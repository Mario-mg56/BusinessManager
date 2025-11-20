package businessmanager.surface;

import businessmanager.database.DataStore;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class CheckCSPController implements Initializable {

    @FXML
    public Button buttonGoBack;

    @FXML
    public Label labelCSP;

    @FXML
    public Button buttonInspect;

    @FXML
    public void goBack(ActionEvent actionEvent) throws IOException {
        System.out.println("Returning to companyCSPView");
        App.setRoot("companyCSPView");
    }

    @FXML
    public void inspectCSP(ActionEvent actionEvent) throws IOException {
        System.out.println("Going to inspectCSPView");
        App.setRoot("inspectCSPView");
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        String labelText = DataStore.getCheckCSPText();
        labelCSP.setText(labelText);
    }
}