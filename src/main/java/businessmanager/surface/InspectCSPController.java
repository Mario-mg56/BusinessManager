package businessmanager.surface;

import businessmanager.database.ConnectionDAO;
import businessmanager.database.DataStore;
import businessmanager.management.BusinessManager;
import businessmanager.management.Entity;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class InspectCSPController implements Initializable {

    @FXML private TextField nameField, addressField, cityField, typeField, provinceField,
            countryField, emailField, phoneField, cpField;

    @FXML private Label lblTitulo, lblSubtipo, lblEstado;
    @FXML private Button btnGuardar, btnVolver;

    private boolean isEditing = false;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle){
        // 1. Detectamos si venimos a editar o solo a ver
        isEditing = BusinessManager.getInstance().editing;

        loadEntityData();
        setupMode();
    }

    private void setupMode() {
        if (isEditing) {
            // MODO EDICIÓN: Campos editables y botón Guardar visible
            setFieldsEditable(true);
            btnGuardar.setVisible(true);
            btnGuardar.setManaged(true);

            // Si es nuevo (selectedEntity es null), limpiamos campos
            if (DataStore.selectedEntity == null) {
                lblTitulo.setText("Nueva Entidad");
                lblSubtipo.setText("Rellena los datos");
                lblEstado.setVisible(false);
                clearFields();
            } else {
                lblTitulo.setText("Editar: " + DataStore.selectedEntity.getName());
                // NIF no debe ser editable al editar (Primary Key o clave única)
                typeField.setEditable(false);
                typeField.setStyle("-fx-background-color: #e9ecef; -fx-border-color: #dee2e6;");
            }
        } else {
            // MODO LECTURA (Inspección): Campos bloqueados, solo botón volver
            setFieldsEditable(false);
            btnGuardar.setVisible(false);
            btnGuardar.setManaged(false);
        }
    }

    private void setFieldsEditable(boolean editable) {
        nameField.setEditable(editable);
        typeField.setEditable(editable); // NIF
        emailField.setEditable(editable);
        phoneField.setEditable(editable);
        addressField.setEditable(editable);
        cityField.setEditable(editable);
        provinceField.setEditable(editable);
        countryField.setEditable(editable);
        cpField.setEditable(editable);

        // Cambio visual para que el usuario sepa que puede escribir
        String style = editable ? "-fx-background-color: white; -fx-border-color: #2c3e50;"
                : "-fx-background-color: #f8f9fa; -fx-border-color: #dee2e6;";

        nameField.setStyle(style);
        // ... aplicar estilo a los demás si quieres
    }

    private void clearFields() {
        nameField.clear(); typeField.clear(); emailField.clear(); phoneField.clear();
        addressField.clear(); cityField.clear(); provinceField.clear();
        countryField.clear(); cpField.clear();
    }

    public void guardarCambios(ActionEvent actionEvent) {
        // 1. Validar campos mínimos
        if (nameField.getText().isEmpty() || typeField.getText().isEmpty()) {
            showAlert("Error", "Nombre y NIF son obligatorios.");
            return;
        }

        try {
            // 2. Preparar objeto
            int id = (DataStore.selectedEntity != null) ? DataStore.selectedEntity.getId() : 0;
            String nif = typeField.getText();
            String name = nameField.getText();
            char tipoChar = DataStore.getTypeCSP().equals("Clientes") ? 'C' : 'P';

            // Crear entidad con los datos del formulario
            Entity entityToSave = new Entity(
                    id, nif, name, tipoChar,
                    addressField.getText(),
                    cityField.getText(),
                    provinceField.getText(),
                    countryField.getText(),
                    emailField.getText()
            );

            // Parsear números opcionales
            try { entityToSave.setPhone(Integer.parseInt(phoneField.getText())); } catch(Exception e){}
            try { entityToSave.setCp(Integer.parseInt(cpField.getText())); } catch(Exception e){}

            // 3. Llamar al DAO
            boolean success;
            if (id == 0) {
                // INSERTAR NUEVO
                String nifEmpresa = BusinessManager.getInstance().getCurrentCompany().getNif();
                success = ConnectionDAO.insertEntity(entityToSave, nifEmpresa);
            } else {
                // ACTUALIZAR EXISTENTE
                success = ConnectionDAO.updateEntity(entityToSave);
            }

            if (success) {
                showAlert("Éxito", "Datos guardados correctamente.");
                goBack(null);
            } else {
                showAlert("Error", "No se pudo guardar en la base de datos.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Ocurrió un error inesperado: " + e.getMessage());
        }
    }

    public void goBack(ActionEvent actionEvent) throws IOException {
        App.setRoot("checkCSPView");
    }

    private void loadEntityData() {
        Entity entity = DataStore.selectedEntity;

        if (entity != null) {
            lblTitulo.setText(entity.getName());
            lblSubtipo.setText(DataStore.getTypeCSP() + " | NIF: " + entity.getNif());
            lblEstado.setText("Activo");

            nameField.setText(entity.getName());
            typeField.setText(entity.getNif());
            emailField.setText(entity.getEmail() != null ? entity.getEmail() : "");
            phoneField.setText(entity.getPhone() > 0 ? String.valueOf(entity.getPhone()) : "");
            addressField.setText(entity.getAddress() != null ? entity.getAddress() : "");
            cityField.setText(entity.getCity() != null ? entity.getCity() : "");
            provinceField.setText(entity.getProvince() != null ? entity.getProvince() : "");
            countryField.setText(entity.getCountry() != null ? entity.getCountry() : "");
            cpField.setText(entity.getCp() > 0 ? String.valueOf(entity.getCp()) : "");
        } else if (!isEditing) {
            // Solo mostramos error si estamos en modo lectura y no hay datos
            showAlert("Error", "No hay datos de entidad para mostrar");
            lblTitulo.setText("Sin datos");
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