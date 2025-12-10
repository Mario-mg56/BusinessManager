package businessmanager.surface;

import businessmanager.database.ConnectionDAO;
import businessmanager.database.DataStore;
import businessmanager.management.BusinessManager;
import businessmanager.management.Entity;
import businessmanager.management.Product;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;

public class AddEditProductController {

    @FXML private Label labelAddEditProduct;

    @FXML private TextField codeField;
    @FXML private TextField quantityField;
    @FXML private ComboBox<Entity> supplierCombo;
    @FXML private TextField purchasePriceField;
    @FXML private TextField sellingPriceField;
    @FXML private TextField ivaField;
    @FXML private TextArea descriptionField;

    @FXML private Button buttonAddEditProduct;
    @FXML private Button buttonClean;

    private Product editingProduct;

    @FXML
    public void initialize() {
        setSuppliers(ConnectionDAO.getEntidades());
        if (BusinessManager.getInstance().editing) setProduct(DataStore.selectedProduct);
    }

    public void setProduct(Product p) {
        if (p == null) return;
        this.editingProduct = p;
        labelAddEditProduct.setText("Editar Producto");
        buttonAddEditProduct.setText("Guardar Cambios");
        loadProductData();
    }

    public void setSuppliers(ArrayList<Entity> suppliers) {
        supplierCombo.getItems().setAll(suppliers);
    }

    private void loadProductData() {
        codeField.setText(String.valueOf(editingProduct.getCode()));
        quantityField.setText(String.valueOf(editingProduct.getQuantity()));
        supplierCombo.getSelectionModel().select(editingProduct.getSupplier());
        purchasePriceField.setText(String.valueOf(editingProduct.getPurchasePrize()));
        sellingPriceField.setText(String.valueOf(editingProduct.getSellingPrice()));
        ivaField.setText(String.valueOf(editingProduct.getIva()));
        descriptionField.setText(editingProduct.getDescription());
    }

    @FXML
    private void clean() {
        codeField.clear();
        quantityField.clear();
        supplierCombo.getSelectionModel().clearSelection();
        purchasePriceField.clear();
        sellingPriceField.clear();
        ivaField.clear();
        descriptionField.clear();
    }

    @FXML
    private void buttonReturn() {
        DataStore.setCheckCSPText("Productos");
        DataStore.setTypeCSP("Productos");
        System.out.println("Going to checkCSPView as Producto");
        BusinessManager.getInstance().editing = false;

        try {App.setRoot("checkCSPView");}
        catch (IOException e) {throw new RuntimeException(e);}
    }

    @FXML
    private void buttonAddEditProduct() {
        if (!validate()) return;

        int code = Integer.parseInt(codeField.getText());
        int quantity = Integer.parseInt(quantityField.getText());
        int purchasePrice = Integer.parseInt(purchasePriceField.getText());
        int sellingPrice = Integer.parseInt(sellingPriceField.getText());
        int iva = Integer.parseInt(ivaField.getText());
        String description = descriptionField.getText();
        Entity supplier = supplierCombo.getSelectionModel().getSelectedItem();

        BusinessManager bm = BusinessManager.getInstance();

        if (bm.editing) {
            editingProduct.setCode(code);
            editingProduct.setQuantity(quantity);
            editingProduct.setSupplier(supplier);
            editingProduct.setPurchasePrize(purchasePrice);
            editingProduct.setSellingPrice(sellingPrice);
            editingProduct.setIva(iva);
            editingProduct.setDescription(description);
            bm.getCurrentCompany().updateProduct(editingProduct);
            bm.editing = false;
        }
        else {
            editingProduct = new Product(0, code, quantity, supplier, purchasePrice, sellingPrice, iva, description);
            bm.getCurrentCompany().addProduct(editingProduct);
        }

        DataStore.setCheckCSPText("Productos");
        DataStore.setTypeCSP("Productos");
        System.out.println("Going to checkCSPView as Producto");

        try {App.setRoot("checkCSPView");}
        catch (IOException e) {throw new RuntimeException(e);}
    }

    private boolean validate() {
        if (codeField.getText().isEmpty() ||
                quantityField.getText().isEmpty() ||
                supplierCombo.getSelectionModel().getSelectedItem() == null ||
                purchasePriceField.getText().isEmpty() ||
                sellingPriceField.getText().isEmpty() ||
                ivaField.getText().isEmpty()) {

            System.out.println(codeField.getText() + " " +
                    quantityField.getText() + " " +
                    supplierCombo.getSelectionModel() + " " +
                    purchasePriceField.getText() + " " +
                    sellingPriceField.getText() + " " +
                    ivaField.getText());

            new Alert(Alert.AlertType.WARNING, "Rellena todos los campos obligatorios.").showAndWait();
            return false;
        }

        try {
            Integer.parseInt(codeField.getText());
            Integer.parseInt(quantityField.getText());
            Integer.parseInt(purchasePriceField.getText());
            Integer.parseInt(sellingPriceField.getText());
            Integer.parseInt(ivaField.getText());
        } catch (NumberFormatException e) {
            new Alert(Alert.AlertType.ERROR, "Campos numéricos inválidos.").showAndWait();
            return false;
        }

        return true;
    }

    public Product getResultProduct() {
        return editingProduct;
    }
}
