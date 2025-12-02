package businessmanager.surface;

import java.io.IOException;

import businessmanager.database.DataStore;
import businessmanager.management.BusinessManager;
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
        BusinessManager.getInstance().editing = true;
        App.setRoot("addEditCompanyView");
    }

    @FXML
    private void checkMore() throws IOException {
        Company selectedCompany = getSelectedCompany();
        if (selectedCompany == null) {
            System.out.println("Select a company!");
            return;
        }
        BusinessManager.getInstance().setCurrentCompany(selectedCompany);
        System.out.println("Going to companyCSPView");
        App.setRoot("companyCSPView");
    }

    private Company getSelectedCompany() {
        //Incluir código que devuelva la empresa seleccionada en la UI, hasta entonces devolverá esta para debuggear
        return new Company(1, "Empresa X", "Direccion 1",
                "Ciudad", "Provincia", "País", "correo@example.com", "Direccion Fiscal");
    }
}
