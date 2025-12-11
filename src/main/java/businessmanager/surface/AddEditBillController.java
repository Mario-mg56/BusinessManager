package businessmanager.surface;

import businessmanager.database.ConnectionDAO;
import businessmanager.database.DataStore;
import businessmanager.management.Bill;
import businessmanager.management.BusinessManager;
import businessmanager.management.Company;
import businessmanager.management.Entity;
import businessmanager.management.Product;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.util.StringConverter;

import java.io.IOException;
import java.net.URL;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class AddEditBillController implements Initializable {

    // --- Elementos FXML ---
    @FXML private Label lblTituloVista;

    // Formulario Cabecera
    @FXML private TextField txtNumero;
    @FXML private DatePicker dateFecha;
    @FXML private TextField txtConcepto;
    @FXML private ComboBox<String> comboEstado;
    @FXML private TextField txtCliente;

    // Zona de Líneas (Productos)
    @FXML private ComboBox<Product> comboProductos;
    @FXML private TextField txtCantidad;

    // Tabla
    @FXML private TableView<Product> tablaLineas;
    @FXML private TableColumn<Product, String> colCodigo;
    @FXML private TableColumn<Product, String> colDescripcion;
    @FXML private TableColumn<Product, Double> colPrecio;
    @FXML private TableColumn<Product, Double> colCantidad;
    @FXML private TableColumn<Product, Double> colTotalLinea;

    // Footer
    @FXML private TextArea txtObservaciones;
    @FXML private Label lblBaseImponible;
    @FXML private Label lblTotalIVA;
    @FXML private Label lblGranTotal;

    // --- Variables de Control ---
    private ObservableList<Product> listaLineas = FXCollections.observableArrayList();
    private ObservableList<Product> catalogoProductos = FXCollections.observableArrayList();
    private Entity clienteActual;
    private boolean isEditing;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // 1. Configurar Controles
        comboEstado.setItems(FXCollections.observableArrayList("Pendiente", "Pagada", "Cancelada"));
        setupTable();
        loadCatalog();

        // 2. Obtener Cliente (Obligatorio)
        clienteActual = DataStore.selectedEntity;
        if (clienteActual != null) {
            txtCliente.setText(clienteActual.getName() + " (NIF: " + clienteActual.getNif() + ")");
        }

        // 3. Detectar Modo (Nuevo o Edición)
        isEditing = BusinessManager.getInstance().editing;
        Bill billToEdit = DataStore.selectedBill;

        if (isEditing && billToEdit != null) {
            // MODO EDICIÓN
            lblTituloVista.setText("Editar Factura");
            cargarDatosFactura(billToEdit);
            // txtNumero.setDisable(true); // Opcional: Descomentar si no quieres que cambien el número
        } else {
            // MODO NUEVA
            lblTituloVista.setText("Nueva Factura");
            dateFecha.setValue(LocalDate.now());
            comboEstado.getSelectionModel().select("Pendiente");
            // Generador simple de String para el número
            txtNumero.setText("FAC-" + LocalDate.now().getYear() + "-" + (System.currentTimeMillis() % 10000));
        }

        // 4. Listeners para recálculo automático
        listaLineas.addListener((javafx.collections.ListChangeListener<Product>) c -> calcularTotales());
    }

    // ... (setupTable y loadCatalog se mantienen igual que antes) ...
    private void setupTable() {
        NumberFormat currency = NumberFormat.getCurrencyInstance();

        colCodigo.setCellValueFactory(cell -> new SimpleStringProperty(String.valueOf(cell.getValue().getCode())));
        colDescripcion.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getDescription()));

        colPrecio.setCellValueFactory(cell -> new SimpleDoubleProperty(cell.getValue().getSellingPrice()).asObject());
        colPrecio.setCellFactory(c -> new TableCell<>() {
            @Override protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText((empty || item == null) ? null : currency.format(item));
            }
        });

        colCantidad.setCellValueFactory(cell -> new SimpleDoubleProperty(cell.getValue().getQuantity()).asObject());

        colTotalLinea.setCellValueFactory(cell -> {
            Product p = cell.getValue();
            return new SimpleDoubleProperty(p.getSellingPrice() * p.getQuantity()).asObject();
        });
        colTotalLinea.setCellFactory(c -> new TableCell<>() {
            @Override protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText((empty || item == null) ? null : currency.format(item));
            }
        });

        tablaLineas.setItems(listaLineas);
    }

    private void loadCatalog() {
        Company comp = BusinessManager.getInstance().getCurrentCompany();
        if (comp != null) {
            ArrayList<Product> prods = ConnectionDAO.getProductosPorEmpresa(comp.getNif());
            catalogoProductos.addAll(prods);
            comboProductos.setItems(catalogoProductos);

            comboProductos.setConverter(new StringConverter<Product>() {
                @Override
                public String toString(Product p) {
                    return p == null ? null : p.getDescription() + " (" + p.getSellingPrice() + "€)";
                }
                @Override
                public Product fromString(String s) { return null; }
            });
        }
    }

    private void cargarDatosFactura(Bill b) {
        // CAMBIO IMPORTANTE: Asignamos el número directamente como String
        txtNumero.setText(b.getNumber());

        dateFecha.setValue(b.getIssueDate());
        txtConcepto.setText(b.getConcept());
        comboEstado.getSelectionModel().select(b.getStatus());
        txtObservaciones.setText(b.getObservations());

        if (b.getProducts() != null) {
            listaLineas.addAll(b.getProducts());
        }
        calcularTotales();
    }

    @FXML
    public void agregarLinea(ActionEvent event) {
        Product selectedProd = comboProductos.getSelectionModel().getSelectedItem();
        String qtyText = txtCantidad.getText();

        if (selectedProd == null || qtyText.isEmpty()) {
            showAlert("Error", "Selecciona un producto y una cantidad.");
            return;
        }

        try {
            int cantidad = Integer.parseInt(qtyText);
            if (cantidad <= 0) throw new NumberFormatException();

            // CLONAMOS el producto
            Product linea = new Product(
                    selectedProd.getId(),
                    selectedProd.getCode(),
                    cantidad,
                    selectedProd.getSupplier(),
                    selectedProd.getPurchasePrize(),
                    selectedProd.getSellingPrice(),
                    selectedProd.getIva(),
                    selectedProd.getDescription()
            );

            listaLineas.add(linea);
            comboProductos.getSelectionModel().clearSelection();
            txtCantidad.clear();

        } catch (NumberFormatException e) {
            showAlert("Error", "La cantidad debe ser un número entero positivo.");
        }
    }

    @FXML
    public void eliminarLinea(ActionEvent event) {
        Product selected = tablaLineas.getSelectionModel().getSelectedItem();
        if (selected != null) {
            listaLineas.remove(selected);
        } else {
            showAlert("Aviso", "Selecciona una línea de la tabla para quitarla.");
        }
    }

    private void calcularTotales() {
        double base = 0;
        double totalIva = 0;

        for (Product p : listaLineas) {
            double importeLinea = p.getSellingPrice() * p.getQuantity();
            base += importeLinea;
            // Cálculo de IVA (Asumiendo 21% por defecto si el producto no tiene)
            double porcentaje = (p.getIva() > 0) ? p.getIva() : 21.0;
            totalIva += importeLinea * (porcentaje / 100);
        }

        double granTotal = base + totalIva;

        NumberFormat currency = NumberFormat.getCurrencyInstance();
        lblBaseImponible.setText(currency.format(base));
        lblTotalIVA.setText(currency.format(totalIva));
        lblGranTotal.setText(currency.format(granTotal));
    }

    @FXML
    public void guardarFactura(ActionEvent event) {
        // 1. Validaciones
        if (txtNumero.getText().isEmpty() || dateFecha.getValue() == null) {
            showAlert("Error", "El número y la fecha son obligatorios.");
            return;
        }
        if (listaLineas.isEmpty()) {
            showAlert("Error", "La factura debe tener al menos una línea.");
            return;
        }

        // 2. Preparar Datos
        int idFactura = isEditing && DataStore.selectedBill != null ? DataStore.selectedBill.getId() : 0;

        // CAMBIO IMPORTANTE: Obtenemos el String directamente
        String numeroFactura = txtNumero.getText().trim();

        // Recalcular totales numéricos para el objeto Bill
        double base = 0;
        double iva = 0;
        for (Product p : listaLineas) {
            double importe = p.getSellingPrice() * p.getQuantity();
            base += importe;
            double porcentaje = (p.getIva() > 0) ? p.getIva() : 21.0;
            iva += importe * (porcentaje / 100);
        }

        // 3. Crear Objeto Bill (Asegúrate que tu constructor Bill acepta String en el 2º parámetro)
        Bill nuevaFactura = new Bill(
                idFactura,
                numeroFactura, // <-- STRING
                new ArrayList<>(listaLineas),
                clienteActual,
                (int)base,
                comboEstado.getValue(),
                txtConcepto.getText(),
                txtObservaciones.getText(),
                dateFecha.getValue(),
                'V'
        );

        nuevaFactura.setIva((int)iva);

        // Si tu objeto Bill guarda el total, cálculalo aquí (base + iva)
        // nuevaFactura.setTotal((int)(base + iva));

        Company currentCompany = BusinessManager.getInstance().getCurrentCompany();
        boolean success;

        // 4. Llamada al DAO
        if (isEditing) {
            // USAMOS EL NUEVO MÉTODO DAO QUE CREAMOS ARRIBA
            success = ConnectionDAO.updateFacturaCompleta(nuevaFactura);
        } else {
            // INSERTAR NUEVA
            success = ConnectionDAO.insertFactura(nuevaFactura, currentCompany.getNif());
        }

        if (success) {
            showAlert("Éxito", "Factura guardada correctamente.");
            try {
                App.setRoot("listBillView");
            } catch (IOException e) { e.printStackTrace(); }
        } else {
            showAlert("Error", "No se pudo guardar en la base de datos.");
        }
    }

    @FXML
    public void cancelar(ActionEvent event) throws IOException {
        App.setRoot("listBillView");
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}