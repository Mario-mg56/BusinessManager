package businessmanager.surface;

import java.io.IOException;

import businessmanager.database.DataStore;
import businessmanager.management.Company;
import javafx.fxml.FXML;

public class SelectCompanyController {

    @FXML
    private void addEnterprice() throws IOException {
        DataStore.selectedCompany = null;
        System.out.println("Going to addEditCompanyView");
        App.setRoot("addEditCompanyView");
    }

    @FXML
    private void editEnterprice() throws IOException {
        DataStore.selectedCompany  = new Company(1, "Empresa X", "Direccion 1", "Ciudad", "Provincia", "País", "correo@example.com", "Direccion Fiscal");
        System.out.println("Going to addEditCompanyView");
        App.setRoot("addEditCompanyView");
    }

    @FXML
    private void checkMore() throws IOException {
        DataStore.selectedCompany  = new Company(1, "Empresa X", "Direccion 1", "Ciudad", "Provincia", "País", "correo@example.com", "Direccion Fiscal");
        System.out.println("Going to companyCSPView");
        App.setRoot("companyCSPView");
    }
}
