package businessmanager.surface;

import Bills.GeneradorFactura;
import businessmanager.database.ConnectionDAO;
import businessmanager.database.DataStore;
import businessmanager.management.Bill;
import businessmanager.management.BusinessManager;
import businessmanager.management.Company;
import businessmanager.management.Entity;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.io.IOException;
import java.net.URL;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Locale;
import java.util.ResourceBundle;

public class ListBillController implements Initializable {

    // --- Elementos FXML ---
    @FXML private Label labelTitulo, labelSubtitulo, lblTotalFacturado;
    @FXML private TextField searchField;
    @FXML private Button buttonVerFactura, buttonImprimir, buttonNuevaFactura;

    @FXML private TableView<Bill> tablaFacturas;
    @FXML private TableColumn<Bill, String> colNumero;
    @FXML private TableColumn<Bill, LocalDate> colFecha;
    @FXML private TableColumn<Bill, String> colConcepto;
    @FXML private TableColumn<Bill, Double> colBase;
    @FXML private TableColumn<Bill, Double> colIVA;
    @FXML private TableColumn<Bill, Double> colTotal;
    @FXML private TableColumn<Bill, String> colEstado;

    // --- Datos ---
    private ObservableList<Bill> masterData = FXCollections.observableArrayList();
    private FilteredList<Bill> filteredData;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupTable();
        loadData();
    }

    // ==========================================
    //           CONFIGURACIÓN TABLA
    // ==========================================
    private void setupTable() {
        // 1. Vincular columnas con el modelo Bill
        colNumero.setCellValueFactory(cell -> new SimpleStringProperty(String.valueOf(cell.getValue().getNumber())));
        colFecha.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().getIssueDate()));
        colConcepto.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getConcept()));
        colEstado.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getStatus()));

        // Para valores numéricos (convertimos int a double para facilitar formateo si fuera necesario)
        colBase.setCellValueFactory(cell -> new SimpleDoubleProperty(cell.getValue().getBaseImponible()).asObject());
        colIVA.setCellValueFactory(cell -> new SimpleDoubleProperty(cell.getValue().getIva()).asObject());
        colTotal.setCellValueFactory(cell -> new SimpleDoubleProperty(cell.getValue().getTotal()).asObject());

        // 2. Formateadores de celdas

        // Fecha: dd/MM/yyyy
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        colFecha.setCellFactory(column -> new TableCell<Bill, LocalDate>() {
            @Override
            protected void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) setText(null);
                else setText(dateFormatter.format(item));
            }
        });


        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.getDefault());
        colBase.setCellFactory(column -> new TableCell<Bill, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) setText(null);
                else setText(currencyFormat.format(item));
            }
        });
        colIVA.setCellFactory(column -> createCurrencyCell(currencyFormat));
        colTotal.setCellFactory(column -> createCurrencyCell(currencyFormat));
    }

    // Helper para no repetir código de celda de moneda
    private TableCell<Bill, Double> createCurrencyCell(NumberFormat format) {
        return new TableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) setText(null);
                else setText(format.format(item));
            }
        };
    }

    // ==========================================
    //           CARGA DE DATOS
    // ==========================================
    private void loadData() {
        System.out.println("=== INICIANDO CARGA DE FACTURAS ===");

        Company currentCompany = BusinessManager.getInstance().getCurrentCompany();
        Entity filtroCliente = DataStore.selectedEntity; // <--- AQUÍ RECUPERAMOS AL CLIENTE

        // 1. CHEQUEO DEL FILTRO
        if (filtroCliente == null) {
            System.out.println("❌ ALERTA: filtroCliente es NULL. (Se mostrarán todas)");
        } else {
            System.out.println("✅ CLIENTE DETECTADO: " + filtroCliente.getName() + " | ID: " + filtroCliente.getId());
        }

        if (currentCompany == null) {
            showAlert("Error", "No hay ninguna empresa seleccionada.");
            return;
        }

        try {
            // Traemos todas de la BD
            ArrayList<Bill> todasLasFacturas = ConnectionDAO.getFacturas(currentCompany.getNif());
            System.out.println("📊 Facturas totales en BD: " + todasLasFacturas.size());

            ArrayList<Bill> facturasFiltradas = new ArrayList<>();

            // 2. LÓGICA DE FILTRADO
            if (filtroCliente != null) {
                labelSubtitulo.setText("Facturas de: " + filtroCliente.getName());

                System.out.println("--- FILTRANDO ---");
                for (Bill b : todasLasFacturas) {
                    int idTerceroFactura = (b.getThirdParty() != null) ? b.getThirdParty().getId() : -1;

                    System.out.println("   > Revisando Factura " + b.getNumber() + " | Tercero ID: " + idTerceroFactura);

                    if (idTerceroFactura == filtroCliente.getId()) {
                        System.out.println("     -> COINCIDE! Añadida.");
                        facturasFiltradas.add(b);
                    } else {
                        System.out.println("     -> No coincide (Esperado: " + filtroCliente.getId() + ")");
                    }
                }
            } else {
                // Si no hay filtro, metemos todas
                System.out.println("--- SIN FILTRO: Copiando todas ---");
                labelSubtitulo.setText("Empresa: " + currentCompany.getName());
                facturasFiltradas.addAll(todasLasFacturas);
            }

            System.out.println("🏁 Resultado final: " + facturasFiltradas.size() + " facturas para mostrar.");

            // 3. ACTUALIZAR TABLA
            masterData.clear();
            masterData.addAll(facturasFiltradas);

            filteredData = new FilteredList<>(masterData, p -> true);
            SortedList<Bill> sortedData = new SortedList<>(filteredData);
            sortedData.comparatorProperty().bind(tablaFacturas.comparatorProperty());
            tablaFacturas.setItems(sortedData);

            updateTotalLabel();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Error al cargar las facturas de la base de datos.");
        }
    }

    private void updateTotalLabel() {
        double totalSum = 0;

        // 1. Recorremos todas las facturas que hay actualmente en la tabla
        if (tablaFacturas.getItems() != null) {
            for (Bill b : tablaFacturas.getItems()) {
                totalSum += b.getTotal();
            }
        }

        // 2. Formateamos el número a formato moneda (ej: 1.500,00 €)
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance();

        // 3. Actualizamos la etiqueta en la pantalla
        lblTotalFacturado.setText(currencyFormat.format(totalSum));
    }

    // ==========================================
    //           BUSCADOR / FILTRO
    // ==========================================


    // ==========================================
    //           ACCIONES (BOTONES)
    // ==========================================

    @FXML
    public void verFacturaDetalle(ActionEvent actionEvent) throws IOException {
        Bill selected = tablaFacturas.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Selección requerida", "Por favor, selecciona una factura para ver el detalle.");
            return;
        }

        // Guardamos la factura seleccionada para que la siguiente vista la recoja
        DataStore.selectedBill = selected; // Asegúrate de tener este campo en DataStore
        // Opcional: DataStore.isEditingBill = true;

        System.out.println("Navegando a detalle de factura: " + selected.getNumber());
        App.setRoot("detailBillView");
    }

    @FXML
    public void nuevaFactura(ActionEvent actionEvent) throws IOException {
        DataStore.selectedBill = null;
        BusinessManager.getInstance().editing = false;

        System.out.println("Creando nueva factura...");
        App.setRoot("addEditBillView");
    }

    @FXML
    public void imprimirFactura(ActionEvent actionEvent) {
        // 1. Obtener la factura seleccionada
        Bill selected = tablaFacturas.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showAlert("Selección requerida", "Selecciona una factura para imprimir.");
            return;
        }

        // 2. Feedback visual inmediato
        System.out.println("Solicitando impresión para factura ID: " + selected.getId());


            try {
                // Instanciamos tu clase
                GeneradorFactura generador = new GeneradorFactura();

                // Llamamos al método pasando el ID de la factura seleccionada
                generador.generarFactura(selected.getId());

            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Error generando factura.");

            }

    }

    @FXML
    public void goBack(ActionEvent actionEvent) throws IOException {
        // Volver a la vista de Clientes/Proveedores
        App.setRoot("checkCSPView");
    }

    // Helper para alertas
    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    public void editarFactura(ActionEvent actionEvent) throws IOException {
        Bill selected = tablaFacturas.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Selección requerida", "Por favor, selecciona una factura para editar.");
            return;
        }

        // Configurar DataStore para modo edición
        DataStore.selectedBill = selected;
        BusinessManager.getInstance().editing = true; // Importante para saber si es update o insert

        System.out.println("Editando factura: " + selected.getNumber());
        App.setRoot("addEditBillView");
    }

    @FXML
    public void eliminarFactura(ActionEvent actionEvent) {
        Bill selected = tablaFacturas.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Selección requerida", "Por favor, selecciona una factura para eliminar.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmar eliminación");
        confirm.setHeaderText("¿Eliminar Factura " + selected.getNumber() + "?");
        confirm.setContentText("Esta acción borrará la factura y todas sus líneas de producto.\nNo se puede deshacer.");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                boolean success = ConnectionDAO.deleteFactura(selected.getId());
                if (success) {
                    showAlert("Éxito", "Factura eliminada correctamente.");
                    loadData(); // Recargar la tabla
                } else {
                    showAlert("Error", "No se pudo eliminar la factura.");
                }
            }
        });
    }
}