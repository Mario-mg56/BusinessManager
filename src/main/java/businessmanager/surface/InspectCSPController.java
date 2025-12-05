package businessmanager.surface;

import businessmanager.database.ConnectionDAO;
import businessmanager.database.DataStore;
import businessmanager.management.Entity;
import businessmanager.management.Product;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class InspectCSPController implements Initializable {

    @FXML
    private VBox entitySection, productSection;

    @FXML
    private TextField nameField, addressField, cityField, typeField, provinceField,
            countryField, emailField, phoneField, cpField, codeField,
            quantityField, purchasePriceField, sellingPriceField, ivaField;

    @FXML
    private TextArea descriptionField;

    @FXML
    private Label lblTitulo, lblSubtipo, lblEstado;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle){
        String typeCSP = DataStore.getTypeCSP();

        // Configurar visibilidad según el tipo
        switch (typeCSP) {
            case "Clientes":
            case "Proveedores":
                entitySection.setVisible(true);
                entitySection.setManaged(true);
                productSection.setVisible(false);
                productSection.setManaged(false);
                loadEntityData();
                break;

            case "Productos":
                entitySection.setVisible(false);
                entitySection.setManaged(false);
                productSection.setVisible(true);
                productSection.setManaged(true);
                loadProductData();
                break;

            default:
                entitySection.setVisible(false);
                productSection.setVisible(false);
        }

        setFieldsEditable(false);
    }

    public void goBack(ActionEvent actionEvent) throws IOException {
        System.out.println("Returning to checkCSPView");
        App.setRoot("checkCSPView");
    }

    public void editItem(ActionEvent actionEvent) throws IOException {
        String typeCSP = DataStore.getTypeCSP();

        switch (typeCSP) {
            case "Clientes":
            case "Proveedores":
                // Aquí irías a la vista de edición de entidades
                showAlert("Editar", "Funcionalidad de editar " + typeCSP.toLowerCase() + " no implementada aún.");
                break;
            case "Productos":
                // Aquí irías a la vista de edición de productos
                showAlert("Editar", "Funcionalidad de editar productos no implementada aún.");
                break;
        }
    }

    public void deleteItem(ActionEvent actionEvent) {
        String typeCSP = DataStore.getTypeCSP();
        Alert confirm = new Alert(AlertType.CONFIRMATION);
        confirm.setTitle("Confirmar eliminación");
        confirm.setHeaderText("¿Eliminar elemento?");

        String itemName = "";
        if (typeCSP.equals("Clientes") || typeCSP.equals("Proveedores")) {
            itemName = nameField.getText();
        } else if (typeCSP.equals("Productos")) {
            itemName = descriptionField.getText();
        }

        confirm.setContentText("¿Estás seguro de que quieres eliminar: " + itemName + "?\nEsta acción no se puede deshacer.");

        confirm.showAndWait().ifPresent(response -> {
            if (response == javafx.scene.control.ButtonType.OK) {
                try {
                    boolean success = false;

                    if (typeCSP.equals("Clientes") || typeCSP.equals("Proveedores")) {
                        Entity entity = DataStore.selectedEntity;
                        if (entity != null) {
                            success = ConnectionDAO.deleteEntity(String.valueOf(Integer.parseInt(entity.getNif().replaceAll("[^0-9]", ""))));
                        }
                    } else if (typeCSP.equals("Productos")) {
                        Product product = DataStore.selectedProduct;
                        if (product != null) {
                            success = ConnectionDAO.deleteProduct(product.getId());
                        }
                    }

                    if (success) {
                        showAlert("Éxito", "Elemento eliminado correctamente.");
                        goBack(null);
                    } else {
                        showAlert("Error", "No se pudo eliminar el elemento.");
                    }
                } catch (Exception e) {
                    showAlert("Error", "Error al eliminar: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        });
    }

    public void printItem(ActionEvent actionEvent) {
        showAlert("Imprimir", "Funcionalidad de impresión no implementada aún.");
    }

    private void setFieldsEditable(boolean editable) {
        TextField[] fields = {nameField, addressField, cityField, typeField, provinceField,
                countryField, emailField, phoneField, cpField, codeField,
                quantityField, purchasePriceField, sellingPriceField, ivaField};

        for (TextField field : fields) {
            if (field != null) {
                field.setEditable(editable);
            }
        }

        if (descriptionField != null) {
            descriptionField.setEditable(editable);
        }
    }

    private void loadEntityData() {
        Entity entity = DataStore.selectedEntity;
        if (entity != null) {
            lblTitulo.setText("Detalles de " + entity.getName());
            lblSubtipo.setText(DataStore.getTypeCSP() + " | NIF: " + entity.getNif());
            lblEstado.setText("Activo");

            nameField.setText(entity.getName());
            addressField.setText(entity.getAddress() != null ? entity.getAddress() : "");
            cityField.setText(entity.getCity() != null ? entity.getCity() : "");
            typeField.setText(String.valueOf(entity.getType()));
            provinceField.setText(entity.getProvince() != null ? entity.getProvince() : "");
            countryField.setText(entity.getCountry() != null ? entity.getCountry() : "");
            emailField.setText(entity.getEmail() != null ? entity.getEmail() : "");
            phoneField.setText(entity.getPhone() > 0 ? String.valueOf(entity.getPhone()) : "");
            cpField.setText(entity.getCp() > 0 ? String.valueOf(entity.getCp()) : "");
        } else {
            showAlert("Error", "No hay datos de entidad para mostrar");
            lblTitulo.setText("Sin datos");
        }
    }

    private void loadProductData() {
        Product product = DataStore.selectedProduct;
        if (product != null) {
            lblTitulo.setText("Detalles de Producto");
            lblSubtipo.setText("Código: " + product.getCode() + " | Stock: " + product.getQuantity());
            lblEstado.setText(product.getQuantity() > 0 ? "En stock" : "Sin stock");

            codeField.setText(String.valueOf(product.getCode()));
            quantityField.setText(String.valueOf(product.getQuantity()));
            purchasePriceField.setText(String.valueOf(product.getPurchasePrize()));
            sellingPriceField.setText(String.valueOf(product.getSellingPrice()));
            ivaField.setText(String.valueOf(product.getIva()));
            descriptionField.setText(product.getDescription() != null ? product.getDescription() : "");

            // Si tiene proveedor, mostrar información
            if (product.getSupplier() != null) {
                // Podrías mostrar información del proveedor en campos adicionales
            }
        } else {
            showAlert("Error", "No hay datos de producto para mostrar");
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