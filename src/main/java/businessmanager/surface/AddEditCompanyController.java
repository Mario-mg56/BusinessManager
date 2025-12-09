package businessmanager.surface;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import businessmanager.database.ConnectionDAO;
import businessmanager.database.DataStore;
import businessmanager.management.BusinessManager;
import businessmanager.management.Company;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class AddEditCompanyController implements Initializable {
    ConnectionDAO dao = new ConnectionDAO();

    public Button buttonClean;
    @FXML
    private TextField nifField, nameField, addressField, cityField, provinceField,
            countryField, emailField, taxAddressField, phoneField, cpField;

    @FXML
    private Label labelAddEditCompany;

    @FXML
    private Button buttonAddEditCompany;

    @FXML
    private void buttonReturn() throws IOException {
        System.out.println("Returning to selectCompanyView");
        BusinessManager.getInstance().editing = false;
        App.setRoot("selectCompanyView");
    }

    @FXML
    private void buttonAddEditCompany() {
        if (!validateFields()) {
            return;
        }

        try {

            String nifNum = nifField.getText().trim().toUpperCase();

            // Obtener otros campos
            String name = nameField.getText().trim();
            String address = addressField.getText().trim();
            String city = cityField.getText().trim();
            String province = provinceField.getText().trim();
            String country = countryField.getText().trim();
            String email = emailField.getText().trim();
            String taxAddress = taxAddressField.getText().trim();

            // Parsear CP y teléfono (opcionales)
            int cp = 0;
            int phone = 0;

            if (cpField != null && !cpField.getText().trim().isEmpty()) {
                try {
                    cp = Integer.parseInt(cpField.getText().trim());
                } catch (NumberFormatException e) {
                    showAlert("Advertencia", "El código postal debe ser numérico. Se usará 0.");
                }
            }

            if (phoneField != null && !phoneField.getText().trim().isEmpty()) {
                try {
                    phone = Integer.parseInt(phoneField.getText().trim());
                } catch (NumberFormatException e) {
                    showAlert("Advertencia", "El teléfono debe ser numérico. Se usará 0.");
                }
            }

            // Crear objeto Company
            Company company = new Company(nifNum, name, address, city, province, country, email, taxAddress);
            company.setCp(cp);
            company.setPhone(phone);

            // Operar con la base de datos
            BusinessManager bm = BusinessManager.getInstance();
            boolean success;

            if (bm.editing) {
                // Actualizar empresa existente
                success = ConnectionDAO.updateEmpresa(company);
                if (success) {
                    showAlert("Éxito", "Empresa actualizada correctamente.");
                } else {
                    showAlert("Error", "No se pudo actualizar la empresa.");
                    return;
                }
            } else {
                // Insertar nueva empresa
                success = ConnectionDAO.insertEmpresa(company);
                if (success) {
                    showAlert("Éxito", "Empresa creada correctamente.");
                } else {
                    showAlert("Error", "No se pudo crear la empresa. Verifica que el NIF no exista.");
                    return;
                }
            }

            bm.editing = false;
            System.out.println("Company " + (bm.editing ? "updated" : "added"));

            // Regresar a la vista principal
            App.setRoot("selectCompanyView");

        } catch (NumberFormatException e) {
            showAlert("Error de formato", "Por favor, verifica que los campos numéricos contengan valores válidos.");
            e.printStackTrace();
        } catch (Exception e) {
            showAlert("Error", "Ocurrió un error inesperado: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private boolean validateFields() {
        StringBuilder errors = new StringBuilder();

        if (nifField.getText().trim().isEmpty()) {
            errors.append("• El NIF es obligatorio.\n");
        }
        if (nameField.getText().trim().isEmpty()) {
            errors.append("• El nombre es obligatorio.\n");
        }
        if (addressField.getText().trim().isEmpty()) {
            errors.append("• La dirección es obligatoria.\n");
        }
        if (cityField.getText().trim().isEmpty()) {
            errors.append("• La ciudad es obligatoria.\n");
        }

        // Validar formato de email si se proporciona
        String email = emailField.getText().trim();
        if (!email.isEmpty() && !email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            errors.append("• El formato del email no es válido.\n");
        }

        // Validar NIF si se proporciona
        String nifStr = nifField.getText().trim();
        if (!nifStr.isEmpty() && nifStr.matches(".*[a-zA-Z].*")) {
            // Verificar letra de control para NIF español
            if (nifStr.length() == 9) {
                String numbers = nifStr.substring(0, 8);
                String letter = nifStr.substring(8).toUpperCase();
                String validLetters = "TRWAGMYFPDXBNJZSQVHLCKE";

                try {
                    int nifNum = Integer.parseInt(numbers);
                    char expectedLetter = validLetters.charAt(nifNum % 23);
                    if (letter.charAt(0) != expectedLetter) {
                        errors.append("• La letra del NIF no es válida.\n");
                    }
                } catch (NumberFormatException e) {
                    errors.append("• Los primeros 8 dígitos del NIF deben ser números.\n");
                }
            }
        }

        if (errors.length() > 0) {
            showAlert("Errores de validación", errors.toString());
            return false;
        }

        return true;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        String addCompanyText = "Añadir empresa";
        String editCompanyText = "Editar empresa";

        if (DataStore.selectedCompany != null && BusinessManager.getInstance().editing) {
            // Modo edición
            labelAddEditCompany.setText(editCompanyText);
            buttonAddEditCompany.setText(editCompanyText);

            // Rellenar campos con datos de la empresa
            nifField.setText(DataStore.selectedCompany.getNif());
            nameField.setText(DataStore.selectedCompany.getName());
            addressField.setText(DataStore.selectedCompany.getAddress());
            cityField.setText(DataStore.selectedCompany.getCity());
            provinceField.setText(DataStore.selectedCompany.getProvince());
            countryField.setText(DataStore.selectedCompany.getCountry());
            emailField.setText(DataStore.selectedCompany.getEmail());
            taxAddressField.setText(DataStore.selectedCompany.getTaxAddress());

            // Campos opcionales
            if (cpField != null) {
                cpField.setText(String.valueOf(DataStore.selectedCompany.getCp()));
            }
            if (phoneField != null) {
                phoneField.setText(String.valueOf(DataStore.selectedCompany.getPhone()));
            }

        } else {
            // Modo creación
            labelAddEditCompany.setText(addCompanyText);
            buttonAddEditCompany.setText(addCompanyText);

            // Limpiar campos
            cleanFields();
        }
    }

    @FXML
    public void clean(ActionEvent actionEvent) {
        cleanFields();
    }

    private void cleanFields() {
        nameField.clear();
        addressField.clear();
        cityField.clear();
        provinceField.clear();
        countryField.clear();
        emailField.clear();
        taxAddressField.clear();

        if (cpField != null) {
            cpField.clear();
        }
        if (phoneField != null) {
            phoneField.clear();
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}