package businessmanager.surface;

import businessmanager.database.DataStore;
import businessmanager.management.BusinessManager;
import businessmanager.management.Entity;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;

public class AddEditEntityController {

    @FXML private Label labelAddEditEntity;

    @FXML private TextField nifField;
    @FXML private TextField nameField;
    @FXML private ComboBox<Character> typeCombo;
    @FXML private TextField addressField;
    @FXML private TextField cityField;
    @FXML private TextField provinceField;
    @FXML private TextField countryField;
    @FXML private TextField cpField;
    @FXML private TextField phoneField;
    @FXML private TextField emailField;

    @FXML private Button buttonAddEditEntity;

    private Entity editingEntity;

    public void initialize() {
        typeCombo.getItems().addAll('C', 'P');
        if (BusinessManager.getInstance().editing) setEntity(DataStore.selectedEntity);
    }

    public void setEntity(Entity e) {
        if (e == null) return;
        editingEntity = e;
        labelAddEditEntity.setText("Editar Entidad");
        buttonAddEditEntity.setText("Guardar Cambios");
        loadEntityData();
    }

    private void loadEntityData() {
        nifField.setText(String.valueOf(editingEntity.getNif().substring(0, editingEntity.getNif().length() - 1)));
        nameField.setText(editingEntity.getName());
        typeCombo.getSelectionModel().select(editingEntity.getType());
        addressField.setText(editingEntity.getAddress());
        cityField.setText(editingEntity.getCity());
        provinceField.setText(editingEntity.getProvince());
        countryField.setText(editingEntity.getCountry());
        cpField.setText(String.valueOf(editingEntity.getCp()));
        phoneField.setText(String.valueOf(editingEntity.getPhone()));
        emailField.setText(editingEntity.getEmail());
    }

    @FXML
    private void clean() {
        nifField.clear();
        nameField.clear();
        typeCombo.getSelectionModel().clearSelection();
        addressField.clear();
        cityField.clear();
        provinceField.clear();
        countryField.clear();
        cpField.clear();
        phoneField.clear();
        emailField.clear();
    }

    @FXML
    private void buttonReturn() {
        DataStore.setCheckCSPText("Proveedores");
        DataStore.setTypeCSP("Proveedores");
        System.out.println("Going to checkCSPView as Proveedor");
        BusinessManager.getInstance().editing = false;

        try {App.setRoot("checkCSPView");}
        catch (IOException e) {throw new RuntimeException(e);}
    }

    @FXML
    private void buttonAddEditEntity() {
        if (!validate()) return;

        String nif = nifField.getText();
        String name = nameField.getText();
        char type = typeCombo.getSelectionModel().getSelectedItem();
        String address = addressField.getText();
        String city = cityField.getText();
        String province = provinceField.getText();
        String country = countryField.getText();
        int cp = Integer.parseInt(cpField.getText());
        int phone = Integer.parseInt(phoneField.getText());
        String email = emailField.getText();

        BusinessManager bm = BusinessManager.getInstance();

        if (bm.editing) {
            editingEntity.setNif(nif);
            editingEntity.setName(name);
            editingEntity.setType(type);
            editingEntity.setAddress(address);
            editingEntity.setCity(city);
            editingEntity.setProvince(province);
            editingEntity.setCountry(country);
            editingEntity.setCp(cp);
            editingEntity.setPhone(phone);
            editingEntity.setEmail(email);
            bm.updateEntity(editingEntity);
            bm.editing = false;
        }
        else {
            editingEntity = new Entity(nif, name, type, address, city, province, country, email);
            editingEntity.setCp(cp);
            editingEntity.setPhone(phone);
            bm.addEntity(editingEntity);
        }

        DataStore.setCheckCSPText("Proveedores");
        DataStore.setTypeCSP("Proveedores");
        System.out.println("Going to checkCSPView as Proveedor");

        try {App.setRoot("checkCSPView");}
        catch (IOException e) {throw new RuntimeException(e);}
    }

    private boolean validate() {
        if (nifField.getText().isEmpty() ||
                nameField.getText().isEmpty() ||
                typeCombo.getSelectionModel().isEmpty() ||
                addressField.getText().isEmpty() ||
                cityField.getText().isEmpty() ||
                provinceField.getText().isEmpty() ||
                countryField.getText().isEmpty() ||
                cpField.getText().isEmpty() ||
                phoneField.getText().isEmpty() ||
                emailField.getText().isEmpty()) {

            new Alert(Alert.AlertType.WARNING, "Rellena todos los campos.").showAndWait();
            return false;
        }

        try {
            Integer.parseInt(cpField.getText());
            Integer.parseInt(phoneField.getText());
        } catch (NumberFormatException e) {
            new Alert(Alert.AlertType.ERROR, "Campos numéricos inválidos.").showAndWait();
            return false;
        }

        return true;
    }

    public Entity getResultEntity() {
        return editingEntity;
    }
}
