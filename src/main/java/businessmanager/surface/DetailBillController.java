package businessmanager.surface;

import Bills.GeneradorFactura;
import businessmanager.database.DataStore;
import businessmanager.management.Bill;
import businessmanager.management.BusinessManager;
import businessmanager.management.Company;
import businessmanager.management.Product;
import javafx.application.Platform;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.io.IOException;
import java.net.URL;
import java.text.NumberFormat;
import java.util.ResourceBundle;

public class DetailBillController implements Initializable {

    // --- Etiquetas de la Empresa (Izquierda) ---
    @FXML private Label lblNombreEmpresa;
    @FXML private Label lblDireccionEmpresa;
    @FXML private Label lblCifEmpresa;
    @FXML private Label lblEmailEmpresa;

    // --- Etiquetas de la Factura (Derecha) ---
    @FXML private Label lblNumeroFactura;
    @FXML private Label lblFechaFactura;
    @FXML private Label lblEstadoFactura;

    // --- Etiquetas del Cliente (Abajo) ---
    @FXML private Label lblNombreCliente;
    @FXML private Label lblDireccionCliente;
    @FXML private Label lblNifCliente;

    // --- Tabla y Columnas ---
    @FXML private TableView<Product> tablaLineas;
    @FXML private TableColumn<Product, String> colRef;
    @FXML private TableColumn<Product, String> colDescripcion;
    @FXML private TableColumn<Product, Double> colCantidad;
    @FXML private TableColumn<Product, Double> colPrecio;
    @FXML private TableColumn<Product, String> colDesc; // Descuento
    @FXML private TableColumn<Product, Double> colIvaLinea;
    @FXML private TableColumn<Product, Double> colTotalLinea;

    // --- Totales y Observaciones ---
    @FXML private TextArea txtObservaciones;
    @FXML private Label lblBaseImponible;
    @FXML private Label lblTotalIVA;
    @FXML private Label lblGranTotal;

    // Variable para guardar la factura actual
    private Bill currentBill;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // 1. Recuperamos la factura seleccionada de la memoria global
        currentBill = DataStore.selectedBill;

        if (currentBill != null) {
            setupTable(); // Configurar columnas
            fillData();   // Rellenar textos
        } else {
            System.err.println("Error: No se ha seleccionado ninguna factura para ver.");
        }
    }

    private void setupTable() {
        NumberFormat currency = NumberFormat.getCurrencyInstance();

        // Vinculación de columnas con el objeto Product (que actúa como línea de factura)
        colRef.setCellValueFactory(cell -> new SimpleStringProperty(String.valueOf(cell.getValue().getCode())));
        colDescripcion.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getDescription()));
        colCantidad.setCellValueFactory(cell -> new SimpleDoubleProperty(cell.getValue().getQuantity()).asObject());

        // Precio Unitario
        colPrecio.setCellValueFactory(cell -> new SimpleDoubleProperty(cell.getValue().getSellingPrice()).asObject());
        colPrecio.setCellFactory(column -> new TableCell<>() {
            @Override protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText((empty || item == null) ? null : currency.format(item));
            }
        });

        // Descuento (Si tu modelo no tiene descuento, ponemos "0%" o "-")
        colDesc.setCellValueFactory(cell -> new SimpleStringProperty("0%"));

        // IVA %
        colIvaLinea.setCellValueFactory(cell -> new SimpleDoubleProperty(cell.getValue().getIva()).asObject()); // Asumiendo que getIva() devuelve el %
        colIvaLinea.setCellFactory(column -> new TableCell<>() {
            @Override protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText((empty || item == null) ? null : String.format("%.0f%%", item));
            }
        });

        // Total Línea (Calculado: Cantidad * Precio)
        colTotalLinea.setCellValueFactory(cell -> {
            Product p = cell.getValue();
            double total = p.getQuantity() * p.getSellingPrice();
            return new SimpleDoubleProperty(total).asObject();
        });
        colTotalLinea.setCellFactory(column -> new TableCell<>() {
            @Override protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText((empty || item == null) ? null : currency.format(item));
            }
        });
    }

    private void fillData() {
        // --- 1. Datos de la Empresa (Emisor) ---
        Company comp = BusinessManager.getInstance().getCurrentCompany();
        if (comp != null) {
            lblNombreEmpresa.setText(comp.getName());
            lblDireccionEmpresa.setText(comp.getAddress() + " (" + comp.getCity() + ")");
            lblCifEmpresa.setText("NIF: " + comp.getNif());
            lblEmailEmpresa.setText(comp.getEmail());
        }

        // --- 2. Datos de la Factura (Metadatos) ---
        lblNumeroFactura.setText(String.valueOf(currentBill.getNumber()));
        lblFechaFactura.setText(currentBill.getIssueDate().toString());
        lblEstadoFactura.setText(currentBill.getStatus());

        // Colores para el estado
        if (currentBill.getStatus().equalsIgnoreCase("Pagada")) {
            lblEstadoFactura.setStyle("-fx-text-fill: #28a745; -fx-font-weight: bold;");
        } else {
            lblEstadoFactura.setStyle("-fx-text-fill: #dc3545; -fx-font-weight: bold;");
        }

        // --- 3. Datos del Cliente (Receptor) ---
        if (currentBill.getThirdParty() != null) {
            lblNombreCliente.setText(currentBill.getThirdParty().getName());
            lblDireccionCliente.setText(currentBill.getThirdParty().getAddress());
            lblNifCliente.setText("NIF/CIF: " + currentBill.getThirdParty().getNif());
        } else {
            lblNombreCliente.setText("Cliente Genérico");
            lblDireccionCliente.setText("-");
            lblNifCliente.setText("-");
        }

        // --- 4. Totales y Observaciones ---
        NumberFormat currency = NumberFormat.getCurrencyInstance();

        lblBaseImponible.setText(currency.format(currentBill.getBaseImponible()));
        lblTotalIVA.setText(currency.format(currentBill.getIva())); // Asumiendo que getIva devuelve el total monetario del IVA en la cabecera
        lblGranTotal.setText(currency.format(currentBill.getTotal()));

        txtObservaciones.setText(currentBill.getObservations());

        // --- 5. Rellenar Tabla ---
        if (currentBill.getProducts() != null) {
            tablaLineas.setItems(FXCollections.observableArrayList(currentBill.getProducts()));
        }
    }

    @FXML
    public void goBack(ActionEvent actionEvent) throws IOException {
        System.out.println("Volviendo a la lista de facturas...");
        App.setRoot("listBillView");
    }

    @FXML
    public void imprimirActual(ActionEvent actionEvent) {
        if (currentBill == null) return;

        System.out.println("Imprimiendo factura actual: " + currentBill.getNumber());

        // Usamos un hilo para no congelar la pantalla
        new Thread(() -> {
            try {
                GeneradorFactura generador = new GeneradorFactura();
                generador.generarFactura(currentBill.getId());
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error de impresión");
                    alert.setContentText("No se pudo generar el PDF.");
                    alert.show();
                });
            }
        }).start();
    }
}
