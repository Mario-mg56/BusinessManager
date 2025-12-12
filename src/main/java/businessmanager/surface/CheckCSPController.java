package businessmanager.surface;

import businessmanager.database.ConnectionDAO;
import businessmanager.database.DataStore;
import businessmanager.management.BusinessManager;
import businessmanager.management.Company;
import businessmanager.management.Entity;
import businessmanager.management.Product;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class CheckCSPController implements Initializable {

    @FXML
    public Label labelCSP, lblSubtitle, lblTotal, lblSelected;

    @FXML
    public Button buttonGoBack, buttonInspect, buttonAdd, buttonEdit, buttonDelete, buttonBill, buttonRefresh;

    @FXML
    private TableView<Object> tableView;

    @FXML
    private TableColumn<Object, String> colCodigo, colNombre, colNif, colEmail,
            colTelefono, colDireccion, colCiudad, colEstado;

    @FXML
    private TextField searchField;

    private ObservableList<Object> items;
    private ArrayList<Object> allItems = new ArrayList<>();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        String type = DataStore.getTypeCSP();
        String labelText = DataStore.getCheckCSPText();

        labelCSP.setText(labelText);
        lblSubtitle.setText("Gestión de " + labelText.toLowerCase());

        setupTableColumns();
        loadData();
        updateCounters();
    }

    private void setupTableColumns() {
        colCodigo.setCellValueFactory(cellData -> {
            Object item = cellData.getValue();
            if (item instanceof Entity) {
                Entity e = (Entity) item;
                return new SimpleStringProperty("ENT-" + e.getNif().hashCode() % 10000);
            } else if (item instanceof Product) {
                Product p = (Product) item;
                return new SimpleStringProperty(String.valueOf(p.getCode()));
            }
            return new SimpleStringProperty("");
        });

        colNombre.setCellValueFactory(cellData -> {
            Object item = cellData.getValue();
            if (item instanceof Entity) {
                return new SimpleStringProperty(((Entity) item).getName());
            } else if (item instanceof Product) {
                return new SimpleStringProperty(((Product) item).getDescription());
            }
            return new SimpleStringProperty("");
        });

        colNif.setCellValueFactory(cellData -> {
            Object item = cellData.getValue();
            if (item instanceof Entity) {
                return new SimpleStringProperty(((Entity) item).getNif());
            }
            return new SimpleStringProperty("");
        });

        colEmail.setCellValueFactory(cellData -> {
            Object item = cellData.getValue();
            if (item instanceof Entity) {
                String email = ((Entity) item).getEmail();
                return new SimpleStringProperty(email != null ? email : "");
            }
            return new SimpleStringProperty("");
        });

        colTelefono.setCellValueFactory(cellData -> {
            Object item = cellData.getValue();
            if (item instanceof Entity) {
                return new SimpleStringProperty(String.valueOf(((Entity) item).getPhone()));
            }
            return new SimpleStringProperty("");
        });

        colDireccion.setCellValueFactory(cellData -> {
            Object item = cellData.getValue();
            if (item instanceof Entity) {
                String address = ((Entity) item).getAddress();
                return new SimpleStringProperty(address != null ? address : "");
            }
            return new SimpleStringProperty("");
        });

        colCiudad.setCellValueFactory(cellData -> {
            Object item = cellData.getValue();
            if (item instanceof Entity) {
                String city = ((Entity) item).getCity();
                return new SimpleStringProperty(city != null ? city : "");
            }
            return new SimpleStringProperty("");
        });

        colEstado.setCellValueFactory(cellData -> {
            Object item = cellData.getValue();
            if (item instanceof Entity) {
                return new SimpleStringProperty("Activo");
            } else if (item instanceof Product) {
                Product p = (Product) item;
                return new SimpleStringProperty(p.getQuantity() > 0 ? "En stock" : "Sin stock");
            }
            return new SimpleStringProperty("");
        });

        items = FXCollections.observableArrayList();
        tableView.setItems(items);

        // Escuchar selecciones
        tableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            updateCounters();
        });
    }

    private void loadData() {
        items.clear();
        allItems.clear();
        String type = DataStore.getTypeCSP();
        Company currentCompany = BusinessManager.getInstance().getCurrentCompany();

        if (currentCompany == null) {
            showAlert("Error", "No hay empresa seleccionada.");
            return;
        }

        try {
            switch (type) {
                case "Clientes":
                    ArrayList<Entity> clientes = ConnectionDAO.getClientesPorEmpresa(currentCompany.getNif());
                    allItems.addAll(clientes);
                    items.addAll(clientes);
                    break;

                case "Proveedores":
                    ArrayList<Entity> proveedores = ConnectionDAO.getProveedoresPorEmpresa(currentCompany.getNif());
                    allItems.addAll(proveedores);
                    items.addAll(proveedores);
                    break;

                case "Productos":
                    ArrayList<Product> productos = ConnectionDAO.getProductosPorEmpresa(currentCompany.getNif());
                    allItems.addAll(productos);
                    items.addAll(productos);
                    break;

                default:
                    showAlert("Tipo no soportado", "El tipo '" + type + "' no está soportado.");
                    return;
            }

            updateCounters();

        } catch (Exception e) {
            showAlert("Error al cargar datos", "No se pudieron cargar los datos: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void goBack(ActionEvent actionEvent) throws IOException {
        System.out.println("Returning to companyCSPView");
        App.setRoot("companyCSPView");
    }

    @FXML
    public void inspectCSP(ActionEvent actionEvent) throws IOException {
        Object selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Selección requerida", "Por favor, selecciona un elemento de la tabla.");
            return;
        }

        // Guardar el elemento seleccionado en DataStore
        if (selected instanceof Entity) {
            DataStore.selectedEntity = (Entity) selected;
        } else if (selected instanceof Product) {
            DataStore.selectedProduct = (Product) selected;
        }

        System.out.println("Going to inspectCSPView");
        App.setRoot("inspectCSPView");
    }

    @FXML
    public void addCSP(ActionEvent actionEvent) throws IOException {
        DataStore.selectedEntity = null; // Limpiamos para que sepa que es nuevo

        // ACTIVAMOS MODO EDICIÓN (Creación es un tipo de edición)

        App.setRoot("addEditProductView");
    }

    @FXML
    public void editCSP(ActionEvent actionEvent) throws IOException {
        Object selected = tableView.getSelectionModel().getSelectedItem();
        if (selected != null && selected instanceof Entity) {
            DataStore.selectedEntity = (Entity) selected;

            // ACTIVAMOS MODO EDICIÓN
            BusinessManager.getInstance().editing = true;

            App.setRoot("inspectCSPView"); // Reutilizamos la misma vista
        }
    }

    @FXML
    public void deleteCSP(ActionEvent actionEvent) {
        Object selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Selección requerida", "Por favor, selecciona un elemento de la tabla para eliminar.");
            return;
        }

        Alert confirm = new Alert(AlertType.CONFIRMATION);
        confirm.setTitle("Confirmar eliminación");
        confirm.setHeaderText("¿Eliminar elemento?");

        String itemName = "";
        if (selected instanceof Entity) {
            itemName = ((Entity) selected).getName();
        } else if (selected instanceof Product) {
            itemName = ((Product) selected).getDescription();
        }

        confirm.setContentText("¿Estás seguro de que quieres eliminar: " + itemName + "?\nEsta acción no se puede deshacer.");

        confirm.showAndWait().ifPresent(response -> {
            if (response == javafx.scene.control.ButtonType.OK) {
                boolean success = false;
                String type = DataStore.getTypeCSP();

                try {
                    if (selected instanceof Entity) {
                        Entity entity = (Entity) selected;
                        success = ConnectionDAO.deleteEntity(entity.getNif());
                    } else if (selected instanceof Product) {
                        Product product = (Product) selected;
                        success = ConnectionDAO.deleteProduct(product.getId());
                    }

                    if (success) {
                        showAlert("Éxito", "Elemento eliminado correctamente.");
                        loadData();
                    } else {
                        showAlert("Error", "No se pudo eliminar el elemento. Verifica que no tenga datos asociados.");
                    }
                } catch (Exception e) {
                    showAlert("Error", "Ocurrió un error al eliminar: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        });
    }

    @FXML
    public void searchItems() {
        String searchText = searchField.getText().toLowerCase().trim();
        if (searchText.isEmpty()) {
            items.clear();
            items.addAll(allItems);
            updateCounters();
            return;
        }

        ArrayList<Object> filtered = new ArrayList<>();

        for (Object item : allItems) {
            if (item instanceof Entity) {
                Entity e = (Entity) item;
                if (e.getName().toLowerCase().contains(searchText) ||
                        e.getNif().toLowerCase().contains(searchText) ||
                        (e.getEmail() != null && e.getEmail().toLowerCase().contains(searchText)) ||
                        (e.getCity() != null && e.getCity().toLowerCase().contains(searchText))) {
                    filtered.add(item);
                }
            } else if (item instanceof Product) {
                Product p = (Product) item;
                if (p.getDescription().toLowerCase().contains(searchText) ||
                        String.valueOf(p.getCode()).contains(searchText)) {
                    filtered.add(item);
                }
            }
        }

        items.clear();
        items.addAll(filtered);
        updateCounters();
    }

    @FXML
    public void refreshData() {
        loadData();
        searchField.clear();
        showAlert("Actualizado", "Datos actualizados correctamente.");
    }

    @FXML
    public void goToBills(ActionEvent actionEvent) throws IOException {
        System.out.println("--- 1. BOTÓN PULSADO ---"); // ¿Sale esto en consola?

        Object selected = tableView.getSelectionModel().getSelectedItem();

        if (selected == null) {
            System.out.println("--- ERROR: Nada seleccionado ---");
            showAlert("Selección requerida", "Selecciona un cliente.");
            return;
        }

        System.out.println("--- 2. SELECCIONADO: " + selected.getClass().getSimpleName() + " ---");

        if (selected instanceof Entity) {
            DataStore.selectedEntity = (Entity) selected;

            // VERIFICACIÓN CLAVE
            System.out.println("--- 3. GUARDADO EN DATASTORE: " + DataStore.selectedEntity.getName() + " ---");

            App.setRoot("listBillView");
        } else {
            System.out.println("--- ERROR: No es una Entidad ---");
            showAlert("Error", "Selecciona un Cliente/Proveedor.");
        }
    }

    private void updateCounters() {
        int selectedCount = tableView.getSelectionModel().getSelectedItems().size();
        lblSelected.setText("Seleccionados: " + selectedCount);
        lblTotal.setText("Total: " + items.size());
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}