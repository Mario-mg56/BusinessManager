package businessmanager.surface;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import businessmanager.database.DataStore;
import businessmanager.management.BusinessManager;
import businessmanager.management.Company;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class AddEditCompanyController implements Initializable { //This class will create or add companies.

    @FXML
    private TextField nifField, nameField, addressField, cityField, provinceField, countryField, emailField, taxAddressField;

    @FXML
    private Label labelAddEditCompany;

    @FXML
    private Button butonAddEditCompany;

    @FXML
    private void buttonReturn() throws IOException {
        System.out.println("Returning to selectCompanyView");
        BusinessManager.getInstance().editing = false;
        App.setRoot("selectCompanyView");
    }

    @FXML
    private void buttonAddCompany() throws IOException {
        //Need to check if the can be nullable or not
        String nifStr = nifField.getText();
        int nif = Integer.parseInt(Character.isLetter(nifStr.charAt(nifStr.length()-1)) ? nifStr.substring(0, nifStr.length()-1) : nifStr);
        String name = nameField.getText();
        String address = addressField.getText();
        String city = cityField.getText();
        String province = provinceField.getText();
        String country = countryField.getText();
        String email = emailField.getText();
        String taxAddress = taxAddressField.getText();

        //Upload into DB
        BusinessManager bm = BusinessManager.getInstance();
        if (bm.editing)
            bm.updateCompany(new Company(nif, name, address, city, province, country, email, taxAddress));
        else
            bm.addCompany(new Company(nif, name, address, city, province, country, email, taxAddress));

        bm.editing = false;
        System.out.println("Company added");
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        String addCompanyText = "Añadir empresa";
        String editCompanyText = "Editar empresa";

        if (DataStore.selectedCompany != null) {
            //Change buttons text
            labelAddEditCompany.setText(editCompanyText);
            butonAddEditCompany.setText(editCompanyText);

            //Change fields text
            nifField.setText(DataStore.selectedCompany.getNif());
            nameField.setText(DataStore.selectedCompany.getName());
            addressField.setText(DataStore.selectedCompany.getAddress());
            cityField.setText(DataStore.selectedCompany.getCity());
            provinceField.setText(DataStore.selectedCompany.getProvince());
            countryField.setText(DataStore.selectedCompany.getCountry());
            emailField.setText(DataStore.selectedCompany.getEmail());
            taxAddressField.setText(DataStore.selectedCompany.getTaxAddress());

        }else {
            //Change buttons text
            labelAddEditCompany.setText(addCompanyText);
            butonAddEditCompany.setText(addCompanyText);

            //Change fields text
            nifField.setText("");
            nameField.setText("");
            addressField.setText("");
            cityField.setText("");
            provinceField.setText("");
            countryField.setText("");
            emailField.setText("");
            taxAddressField.setText("");

        }
    }

    public void clean(ActionEvent actionEvent) {
        nifField.clear();
        nameField.clear();
        addressField.clear();
        cityField.clear();
        provinceField.clear();
        countryField.clear();
        emailField.clear();
        taxAddressField.clear();
    }
}