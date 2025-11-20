package businessmanager.surface;

import businessmanager.database.DataStore;
import businessmanager.management.Company;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class CompanyCSPController implements Initializable {

    @FXML
    private Label labelCompanyName;

    @FXML
    public void goToClients(ActionEvent actionEvent) throws IOException {
        DataStore.setCheckCSPText("Clientes");
        DataStore.setTypeCSP("Clientes");
        System.out.println("Going to checkCSPView as Cliente");
        App.setRoot("checkCSPView");
    }

    @FXML
    public void goToSupplier(ActionEvent actionEvent) throws IOException {
        DataStore.setCheckCSPText("Proveedores");
        DataStore.setTypeCSP("Proveedores");
        System.out.println("Going to checkCSPView as Proveedor");
        App.setRoot("checkCSPView");
    }

    @FXML
    public void goToProducts(ActionEvent actionEvent) throws IOException {
        DataStore.setCheckCSPText("Productos");
        DataStore.setTypeCSP("Productos");
        System.out.println("Going to checkCSPView as Producto");
        App.setRoot("checkCSPView");
    }

    @FXML
    public void goBack(ActionEvent actionEvent) throws IOException {
        System.out.println("Returning to SelectCompanyView");
        App.setRoot("selectCompanyView");
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        if (DataStore.selectedCompany != null) {
            setCompany(DataStore.selectedCompany);
        }
    }

    private void setCompany(Company company) {
        labelCompanyName.setText(company.getName());
    }
}