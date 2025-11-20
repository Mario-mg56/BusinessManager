package businessmanager.surface;

import businessmanager.database.DataStore;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class InspectCSPController implements Initializable {

    private List<Label> entityLabels;
    private List<TextField> entityFields;

    private List<Label> productLabels;
    private List<TextField> productFields;

    @FXML
    public TextField nameField;

    @FXML
    public TextField addressField;

    @FXML
    public TextField cityField;

    @FXML
    public TextField typeField;

    @FXML
    public TextField provinceField;

    @FXML
    public TextField countryField;

    @FXML
    public TextField emailField;

    @FXML
    public TextField codeField;

    @FXML
    public TextField quantityField;

    @FXML
    public TextField purchasePriceField;

    @FXML
    public TextField sellingPriceField;

    @FXML
    public TextField ivaField;

    @FXML
    public TextField descriptionField;

    @FXML
    public Label descriptionLabel;

    @FXML
    public Label ivaLabel;

    @FXML
    public Label sellingPriceLabel;

    @FXML
    public Label purchasePriceLabel;

    @FXML
    public Label quantityLabel;

    @FXML
    public Label codeLabel;

    @FXML
    public Label emailLabel;

    @FXML
    public Label countryLabel;

    @FXML
    public Label provinceLabel;

    @FXML
    public Label cityLabel;

    @FXML
    public Label addressLabel;

    @FXML
    public Label typeLabel;

    @FXML
    public Label nameLabel;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle){
        String typeCSP = DataStore.getTypeCSP();
        setupFieldGroups();

        switch (typeCSP) {
            case "Clientes":
            case "Proveedores":
                setVisible(entityLabels, entityFields, true);
                setVisible(productLabels, productFields, false);
                break;

            case "Productos":
                setVisible(entityLabels, entityFields, false);
                setVisible(productLabels, productFields, true);
                break;

            default:
                setVisible(entityLabels, entityFields, false);
                setVisible(productLabels, productFields, false);
        }
    }

    private void setVisible(List<Label> labels, List<TextField> fields, boolean visible) {
        labels.forEach(label -> label.setVisible(visible));
        fields.forEach(field -> field.setVisible(visible));
    }


    public void goBack(ActionEvent actionEvent) throws IOException {
        System.out.println("Returning to checkCSPView");
        App.setRoot("checkCSPView");
    }

    private void setupFieldGroups() {
        entityLabels = List.of(nameLabel, addressLabel, cityLabel, typeLabel, provinceLabel, countryLabel, emailLabel);
        entityFields = List.of(nameField, addressField, cityField, typeField, provinceField, countryField, emailField);

        productLabels = List.of(codeLabel, quantityLabel, purchasePriceLabel, sellingPriceLabel, ivaLabel, descriptionLabel);
        productFields = List.of(codeField, quantityField, purchasePriceField, sellingPriceField, ivaField, descriptionField);
    }

    public void clean(ActionEvent actionEvent) {
        nameField.clear();
        typeField.clear();
        addressField.clear();
        cityField.clear();
        provinceField.clear();
        countryField.clear();
        emailField.clear();
        codeField.clear();
        quantityField.clear();
        purchasePriceField.clear();
        sellingPriceField.clear();
        ivaField.clear();
        descriptionField.clear();
    }
}
