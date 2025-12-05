package businessmanager.surface;

import businessmanager.database.ConnectionDAO;
import businessmanager.database.DataStore;
import businessmanager.management.BusinessManager;
import businessmanager.management.Company;
import businessmanager.management.Entity;
import businessmanager.management.Product;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class CompanyCSPController implements Initializable {

    @FXML
    private Label labelCompanyName, lblResumen;

    @FXML
    public void goToClients(ActionEvent actionEvent) throws IOException {
        DataStore.setCheckCSPText("Clientes");
        DataStore.setTypeCSP("Clientes");
        System.out.println("Going to checkCSPView as Cliente");
        App.setRoot("checkCSPView");
    }

    @FXML
    public void goToSupplier(ActionEvent actionEvent) throws IOException {
        DataStore.setCheckCSPText("Proveedores");
        DataStore.setTypeCSP("Proveedores");
        System.out.println("Going to checkCSPView as Proveedor");
        App.setRoot("checkCSPView");
    }

    @FXML
    public void goToProducts(ActionEvent actionEvent) throws IOException {
        DataStore.setCheckCSPText("Productos");
        DataStore.setTypeCSP("Productos");
        System.out.println("Going to checkCSPView as Producto");
        App.setRoot("checkCSPView");
    }

    @FXML
    public void goToBills(ActionEvent actionEvent) throws IOException {
        DataStore.setCheckCSPText("Facturas");
        DataStore.setTypeCSP("Facturas");
        System.out.println("Going to checkCSPView as Factura");
        App.setRoot("checkCSPView");
    }

    @FXML
    public void goBack(ActionEvent actionEvent) throws IOException {
        System.out.println("Returning to SelectCompanyView");
        BusinessManager.getInstance().setCurrentCompany(null);
        DataStore.clear();
        App.setRoot("selectCompanyView");
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Company company = BusinessManager.getInstance().getCurrentCompany();
        if (company != null) {
            setCompany(company);
            loadSummary();
        } else {
            labelCompanyName.setText("No hay empresa seleccionada");
            lblResumen.setText("📊 Resumen: Sin datos");
            System.err.println("Error: No hay empresa actual seleccionada");
        }
    }

    private void setCompany(Company company) {
        labelCompanyName.setText(company.getName() + " - " + company.getNif());
    }

    private void loadSummary() {
        try {
            Company company = BusinessManager.getInstance().getCurrentCompany();
            if (company == null) return;

            // Obtener conteos reales de la base de datos
            ArrayList<Entity> clientes = ConnectionDAO.getClientes();
            ArrayList<Entity> proveedores = ConnectionDAO.getProveedores();
            ArrayList<Product> productos = ConnectionDAO.getProductosPorEmpresa(company.getNif());

            int totalClientes = clientes != null ? clientes.size() : 0;
            int totalProveedores = proveedores != null ? proveedores.size() : 0;
            int totalProductos = productos != null ? productos.size() : 0;

            lblResumen.setText(String.format("📊 Resumen: %d Clientes | %d Proveedores | %d Productos",
                    totalClientes, totalProveedores, totalProductos));

        } catch (Exception e) {
            lblResumen.setText("📊 Resumen: Error cargando datos");
            e.printStackTrace();
        }
    }
}